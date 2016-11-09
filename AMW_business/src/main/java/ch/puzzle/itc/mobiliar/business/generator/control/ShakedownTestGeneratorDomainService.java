/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.generator.control;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.xml.bind.JAXB;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.globalfunction.control.GlobalFunctionService;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.control.ShakedownTestExecutionResultHandlerService;
import ch.puzzle.itc.mobiliar.business.shakedown.control.ShakedownTestGenerationResult;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.STS;
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.ShakedownTestEnvironmentGenerationResult;
import ch.puzzle.itc.mobiliar.common.exception.GeneratorException;
import ch.puzzle.itc.mobiliar.common.exception.GeneratorException.MISSING;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

@Stateless
public class ShakedownTestGeneratorDomainService {

	public static final String STS_NAME = "STS";

	@Inject
	private ShakedownTestExecutionResultHandlerService shakedownTestExecutionResultHandlerService;

	@Inject
	private ContextDomainService contextDomainService;
	
	@Inject 
    private GlobalFunctionService globalFunctionService;

	@Inject
	GeneratorDomainServiceWithAppServerRelations generatorDomainService;

	@Inject
	private ResourceDependencyResolverService resourceDependencyResolver;

	@Inject
	private EntityManager entityManager;

	@Inject
	Logger log;

	/**
	 * @param shakedownTest
	 * @return a ShakedownTestGenerationResult irrespective of the success of the test template generation.
	 *         In case of failure, the result object contains the updated shakedowntest entity holding the
	 *         information about the actual failure.
	 */
	public ShakedownTestGenerationResult generateConfigurationForShakedownTest(ShakedownTestEntity shakedownTest) {

		log.info("Start schreiben der Konfiguration für alle Nodes des Shakedowntests " + shakedownTest.getId());
		ShakedownTestGenerationResult result = null;
		try {
			result = generateTestingTemplatesForAllNodes(shakedownTest);
			if (result != null && result.hasErrors()) {
				shakedownTestExecutionResultHandlerService.handleUnsuccessfulShakedownTest(result.getErrorMessage(), shakedownTest.getId());
			}
		} catch (final GeneratorException e) {
			shakedownTestExecutionResultHandlerService.handleUnsuccessfulShakedownTest(e, shakedownTest.getId());
		} catch (final IOException e) {
			shakedownTestExecutionResultHandlerService.handleUnsuccessfulShakedownTest(e, shakedownTest.getId());
		}
		return result;
	}

	/**
	 * There are two types of templates in AMW: For configuration and for system testing (health check of
	 * environments, etc.). This method generates the templates for test cases and wraps them into test
	 * suites (STS) ready to be sent out to the server instances for execution.<br>
	 * There will be used the actual (not the audited) templates for testing.
	 * 
	 * @param shakedownTest
	 *             - the identifier of the test case the templates should be generated for
	 * @return
	 * @throws GeneratorException
	 */
	private ShakedownTestGenerationResult generateTestingTemplatesForAllNodes(ShakedownTestEntity shakedownTest) throws GeneratorException, IOException {
		// A test should always use current (not the audited) testing templates (at the risk that some
		// properties are different from the deployed version but this is ok).
		// We therefore are not resolving the resources through the audited tables but directly on the live tables...

		Integer contextId = shakedownTest.getContext().getId();
		// If the context is not defined, we choose the global context
		if (contextId == null) {
			contextId = contextDomainService.getGlobalResourceContextEntity().getId();
		}
		// Get the application server group from the database (ensure attached state)
		final ResourceGroupEntity applicationServerGroup = entityManager.find(ResourceGroupEntity.class, shakedownTest.getApplicationServer().getResourceGroup().getId());

		final ResourceEntity applicationServer = resourceDependencyResolver.getResourceEntityForRelease(applicationServerGroup, shakedownTest.getRelease());

		final ContextEntity context = entityManager.find(ContextEntity.class, contextId);

		if (applicationServer == null) {
			final String message = "ApplicationServer " + shakedownTest.getResourceGroup().getName() + " does not exist in release "
					+ shakedownTest.getApplicationServer().getRelease().getName();
			log.info(message);
			throw new GeneratorException(message, MISSING.APPSERVER, shakedownTest.getId());
		}
		if (context == null) {
			final String message = "Keine Umgebung vorhanden";
			log.log(Level.WARNING, message);
			throw new GeneratorException(message, MISSING.CONTEXT, shakedownTest.getId());
		}

		final ResourceEntity runtime = resourceDependencyResolver.getResourceEntityForRelease(applicationServer.getRuntime(), shakedownTest.getRelease());

		// find the environments we want to generate the test configuration
		// for
		// and actually generate them...
		log.info("Start schreiben der Test-Konfiguration für alle Nodes");
		ShakedownTestGenerationResult result = new ShakedownTestGenerationResult();
		result.setShakedownTestEntity(shakedownTest);
		
		Date stateDate = new Date();
		List<GlobalFunctionEntity> globalFunctions = globalFunctionService.getAllGlobalFunctionsAtDate(stateDate);
		
		for (final ContextEntity c : generatorDomainService.getAllEnvironments(context)) {

			// FIXME: Use Shakedowntest Entity in Generation Context
			DeploymentEntity fakeDeplyoment = generatorDomainService.createFakeDeployment(shakedownTest.getId(), shakedownTest.getRelease(),
					runtime, stateDate);
			GenerationContext generationContext = new GenerationContext(c, applicationServer, fakeDeplyoment, new Date(), GenerationModus.DEPLOY, resourceDependencyResolver);
			generationContext.setTesting(true);
			generationContext.setGlobalFunctions(globalFunctions);

			ShakedownTestEnvironmentGenerationResult envResult = generateTestingTemplatesForContext(generationContext);
			result.addEnvironmentGenerationResult(envResult);
			result.addAllTestingTemplates(envResult.getTestingTemplates());
		}
		return result;
	}

	/**
	 * Generates the testing templates for the given environment .
	 * 
	 * @param context
	 * @return
	 * @throws GeneratorException
	 */
	private ShakedownTestEnvironmentGenerationResult generateTestingTemplatesForContext(GenerationContext context) throws GeneratorException, IOException {
		// Get all nodes of this application server
		final List<ResourceEntity> nodes = context.getApplicationServer().getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.NODE);
		if (nodes == null || nodes.isEmpty()) {
			final String message = "No nodes available";
			log.info(message);
			throw new GeneratorException(message, MISSING.NODE, context.getDeploymentId());
		}

		ShakedownTestEnvironmentGenerationResult result = new ShakedownTestEnvironmentGenerationResult();
		boolean hasActiveNodes = false;
	   	for (final ResourceEntity node : nodes) {
			context.setNode(node);
			NodeGenerationResult nodeGenerationResult = generatorDomainService.generateApplicationServerConfigPerNode(context);
			if(nodeGenerationResult.isNodeEnabled()) {
			    result.addNodeGenerationResult(nodeGenerationResult);
			    result.addTestingTemplate(
					    extractAndConvertSTSTemplate(context.getDeploymentId(), nodeGenerationResult));
			    hasActiveNodes = true;
			}
		}
	     if(!hasActiveNodes){
		    final String message = "No active nodes available - please define the hostname on the nodes";
		    log.info(message);
		    throw new GeneratorException(message, MISSING.NODE, context.getDeploymentId());
		}
		return result;
	}

	/**
	 * Extracts the STS template out of the given already processed templates and transforms it to a STS
	 * object.
	 * 
	 * @param testId
	 *             - the identifier of the current test (for creating more meaningful exception messages)
	 * @param generationResult
	 *             - holds the already processed templates for the given node
	 * @return the sts template as a {@link STS} object
	 * @throws GeneratorException
	 */
	private STS extractAndConvertSTSTemplate(final Integer testId, NodeGenerationResult generationResult) throws GeneratorException {

		List<GeneratedTemplate> generatedTemplates = generationResult.getGeneratedTemplates();
		if (generatedTemplates != null) {
			for (GeneratedTemplate generatedTemplate : generatedTemplates) {
				// only consider Templates with the name STS
				if (STS_NAME.equals(generatedTemplate.getName())) {
					final String stsContent = generatedTemplate.getContent();
					if (stsContent != null && !stsContent.trim().isEmpty()) {
						try {
							return JAXB.unmarshal(new StringReader(stsContent), STS.class);
						} catch (final Exception e) {
							throw new GeneratorException("STS-generation failed!", null, testId, e);
						}
					} else {
						throw new GeneratorException("Invalid STS-template!", null, testId);
					}
				}
			}
		}
		throw new GeneratorException("No STS-template available!", MISSING.STS_TEMPLATE, testId);
	}

}
