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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.database.control.AmwAuditReader;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.function.control.FunctionService;
import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorUtils;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.AppServerRelationProperties;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyMaskingContext;
import ch.puzzle.itc.mobiliar.business.resourceactivation.entity.ResourceActivationEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.*;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.logging.Logger;

/**
 * Populate the set of {@link GenerationUnit} used when generating templates.
 */
public class GenerationUnitFactory {

	private static final String VERSION = "Version";
	private static final String MAVEN_VERSION = "MavenVersion";
	@Inject
	private GeneratorUtils utils;

	@Inject
	private Logger log;

	@Inject
	ResourceDependencyResolverService dependencyResolver;

	@Inject
	PermissionService permissionService;
	
	@Inject
	EntityManager entityManager;
	
	@Inject
	FunctionService functionService;

    @Inject
    AmwAuditReader amwAuditReader;

	@Inject
	PropertyMaskingContext propertyMaskingContext;

	/**
	 * Collects templates and properties as well as their properties.
	 * 
	 * @param options
	 * @param applicationServer
	 * @param templateExceptionHandler
	 * @return the generation Package
	 */
	public GenerationPackage createWorkForAppServer(GenerationOptions options,
			ResourceEntity applicationServer, AMWTemplateExceptionHandler templateExceptionHandler) {

		//find excluded application
	     List<Integer> excludedApplicationGroupIds = getExcludedApplicationGroups(options.getContext());
		GenerationPackage mainWorkSet = new GenerationPackage();
		mainWorkSet.setGenerationOptions(options);

		GenerationSubPackage generationUnitForResource = getGenerationUnitForResource(mainWorkSet, options,
				templateExceptionHandler, applicationServer, excludedApplicationGroupIds, null, 0, true, null);
	     if(generationUnitForResource!=null) {
		   mainWorkSet.addGenerationSubPackage(generationUnitForResource);
	    	}
		return mainWorkSet;
	}


    private List<Integer> getExcludedApplicationGroups(GenerationContext generationContext) {
	   ConsumedResourceRelationEntity appserver2node = generationContext.getApplicationServer()
			   .getConsumedRelation(generationContext.getNode());
	   ContextEntity ctx = generationContext.getContext();
	   List<Integer> activeResourceGroupIds = new ArrayList<>();
	   List<Integer> inactiveResourceGroupIds = new ArrayList<>();

	   while (ctx != null) {
		  ResourceRelationContextEntity appserver2nodeCtx = appserver2node.getResourceRelationContext(ctx);
		  if (appserver2nodeCtx != null) {
			 for (ResourceActivationEntity resourceActivationEntity : appserver2nodeCtx
					 .getResourceActivationEntities()) {
				//Only handle the resource group, if it hasn't been processed before...
				Integer resourceGroupId = resourceActivationEntity.getResourceGroup().getId();
				if (!activeResourceGroupIds.contains(resourceGroupId) && !inactiveResourceGroupIds
						.contains(resourceGroupId)) {
				    if (resourceActivationEntity.isActive()) {
					   activeResourceGroupIds.add(resourceGroupId);
				    }
				    else {
					   inactiveResourceGroupIds.add(resourceGroupId);
				    }
				}
			 }
		  }
		  ctx = ctx.getParent();
	   }
	   return inactiveResourceGroupIds;
    }

	/**
	 * recursive method to generate a Generation Sub Package for a Resource.
	 * 
	 * @param mainWorkSet
	 * @param options
	 * @param templateExceptionHandler
	 * @param resource
	 * @param walkingPathIndex
	 *             - the current walking path when traversing the tree
	 * @return the generation sub package for this resource or null if the given resource is part of the excluded resource groups defined in the given GenerationOptions
	 */
	private GenerationSubPackage getGenerationUnitForResource(GenerationPackage mainWorkSet,
			GenerationOptions options, AMWTemplateExceptionHandler templateExceptionHandler,
			ResourceEntity resource,  List<Integer> excludedApplicationGroupIds,
			Set<TemplateDescriptorEntity> currentResourceTemplates, int walkingPathIndex, boolean generateTemplates,
			GenerationSubPackage parentResourceWorkSet) {
		log.info(resource.getName());

	     boolean isExcluded = excludedApplicationGroupIds!=null && excludedApplicationGroupIds.contains(
				resource.getResourceGroup().getId());

	     if(isExcluded){
		    return null;
         }


		AppServerRelationProperties properties = propertiesFor(options, resource, templateExceptionHandler);
		merge(options, mainWorkSet, properties);

		GenerationSubPackage resourceWorkSet = new GenerationSubPackage();

		// generate consumedResources, add relationTemplates
		Set<TemplateDescriptorEntity> resourceRelationTemplates = handleConsumedRelations(mainWorkSet, options, templateExceptionHandler, resource, excludedApplicationGroupIds, walkingPathIndex, properties, resourceWorkSet, generateTemplates, parentResourceWorkSet);
		properties.setResourceRelationTemplates(resourceRelationTemplates);

		// process provided resources as they are returned from model
		handleProvidedRelations(mainWorkSet, options, resource, properties, resourceWorkSet);

		if (currentResourceTemplates == null) {
			currentResourceTemplates = templatesForResource(options, resource);
		}
		properties.setResourceTemplates(currentResourceTemplates);
		properties.setFunctions(functionService.getAllFunctionsForResource(resource));
		
		GenerationUnit generationUnit = new GenerationUnit(resource, null, currentResourceTemplates, properties);
		generationUnit.setPackageGenerationUnit(true);
		generationUnit.setGlobalFunctionTemplates(options.getContext().getGlobalFunctions());
		generationUnit.setParentGenerationSubPackage(parentResourceWorkSet);
		resourceWorkSet.setPackageGenerationUnit(generationUnit);

		return resourceWorkSet;
	}

	private Set<TemplateDescriptorEntity> handleConsumedRelations(GenerationPackage mainWorkSet, GenerationOptions options, AMWTemplateExceptionHandler templateExceptionHandler, ResourceEntity resource, List<Integer> excludedApplicationGroupIds, int walkingPathIndex, AppServerRelationProperties properties, GenerationSubPackage resourceWorkSet, boolean parentGenerateTemplates, GenerationSubPackage parentResourceWorkSet) {
		Set<ConsumedResourceRelationEntity> consumedMasterRelations = dependencyResolver.getConsumedMasterRelationsForRelease(resource, options.getContext().getTargetRelease());
		List<ConsumedResourceRelationEntity> consumedMasterRelationsSorted = new ArrayList<>(
				consumedMasterRelations);
		Collections.sort(consumedMasterRelationsSorted,
				AbstractResourceRelationEntity.COMPARE_BY_SLAVE_NAME);
		Set<TemplateDescriptorEntity> relationTemplates = new LinkedHashSet<>();

		for (ConsumedResourceRelationEntity resourceRelation : consumedMasterRelationsSorted) {

		    ResourceEntity slave = resourceRelation.getSlaveResource();
		    ResourceEntity currentNode = options.getContext().getNode();

            boolean generateTemplates = parentGenerateTemplates;

		    // filter all other nodes, do not generate the Nodetemplates of the oder node, to avoid that the templates get overwritten.
		    if(slave!=null && currentNode!=null && slave.getResourceType().isNodeResourceType() && !slave.getId().equals(currentNode.getId())){
                generateTemplates = false;
		    }

			relationTemplates.addAll(createGenerationUnitForConsumedResource(mainWorkSet, options, templateExceptionHandler,
					resource, walkingPathIndex, properties, resourceWorkSet, resourceRelation, excludedApplicationGroupIds,  slave, generateTemplates, parentResourceWorkSet));
		}
		return relationTemplates;
	}

	private void handleProvidedRelations(GenerationPackage mainWorkSet, GenerationOptions options, ResourceEntity resource, AppServerRelationProperties properties, GenerationSubPackage resourceWorkSet) {
		Set<ProvidedResourceRelationEntity> providedMasterRelations = dependencyResolver.getProvidedMasterRelationsForRelease(resource, options.getContext().getTargetRelease());
		List<ProvidedResourceRelationEntity> providedMasterRelationsSorted = new ArrayList<>(
				providedMasterRelations);
		Collections.sort(providedMasterRelationsSorted,
				AbstractResourceRelationEntity.COMPARE_BY_SLAVE_NAME);

		for (ProvidedResourceRelationEntity relation : providedMasterRelationsSorted) {

			ResourceEntity slave = relation.getSlaveResource();
			log.info(" (provides): " + slave.getName());
			String identifier = relation.getIdentifier();
			AppServerRelationProperties slaveProperties = properties.addProvidedRelation(identifier,
					slave, relation);

			Set<TemplateDescriptorEntity> resourceTemplates = templatesForResource(options, slave);
			Set<TemplateDescriptorEntity> relationTemplates = templatesForRelation(options, relation);

			merge(options, mainWorkSet, slaveProperties);

			slaveProperties.setFunctions(functionService.getAllFunctionsForResource(slave));

			GenerationUnit generationUnit = new GenerationUnit(slave, resource, slaveProperties,
					resourceTemplates, relationTemplates);
			generationUnit.setGlobalFunctionTemplates(options.getContext().getGlobalFunctions());
			addUnit(resourceWorkSet, generationUnit);
		}
	}


	private Set<TemplateDescriptorEntity> createGenerationUnitForConsumedResource(GenerationPackage mainWorkSet,
			GenerationOptions options, AMWTemplateExceptionHandler templateExceptionHandler,
			ResourceEntity resource, int walkingPathIndex, AppServerRelationProperties properties,
			GenerationSubPackage resourceWorkSet, ConsumedResourceRelationEntity resourceRelation, List<Integer> excludedApplicationGroupIds,
					ResourceEntity slave, boolean generateTemplates, GenerationSubPackage parentResourceWorkSet) {
		
		Set<TemplateDescriptorEntity> resourceTemplates = templatesForResource(options, slave);
		// recursive call getGenerationUnitForResource does traverse the tree
		GenerationSubPackage generationUnitForResource = getGenerationUnitForResource(mainWorkSet, options,
				templateExceptionHandler, slave, excludedApplicationGroupIds, resourceTemplates, ++walkingPathIndex, generateTemplates, resourceWorkSet);
	     //There is no generation sub package - this means, that we ignore this consumed resource and therefore don't need to continue.
	    if(generationUnitForResource==null){
		    return Collections.EMPTY_SET;
	    }

		// do not add if the slave resource has no relations
		if (slave.getConsumedMasterRelations() != null && !slave.getConsumedMasterRelations().isEmpty()
				|| slave.getProvidedMasterRelations() != null
				&& !slave.getProvidedMasterRelations().isEmpty() 
				|| slave.getVirtualConsumedResources()!=null && !slave.getVirtualConsumedResources().isEmpty()
				) {
			mainWorkSet.addGenerationSubPackage(generationUnitForResource);
		}

		log.info(" (consumes) level("+walkingPathIndex+"): " + slave.getName());

		String identifier = resourceRelation.buildIdentifer();
		AppServerRelationProperties slaveProperties = properties.addConsumedRelation(identifier, slave,
				resourceRelation);

		Set<TemplateDescriptorEntity> relationTemplates = templatesForRelation(options, resourceRelation);

		merge(options, mainWorkSet, slaveProperties);

		slaveProperties.setFunctions(functionService.getAllFunctionsForResource(slave));
		slaveProperties.setResourceTemplates(resourceTemplates);
		
		GenerationUnit generationUnit = new GenerationUnit(slave, resource, slaveProperties,
				resourceTemplates, relationTemplates);
		generationUnit.setGlobalFunctionTemplates(options.getContext().getGlobalFunctions());
        if(!generateTemplates) {
            generationUnit.setTemplateGenerationDisabled(true);
        }

        addUnit(resourceWorkSet, generationUnit);

        return relationTemplates;
	}

	private void addUnit(GenerationSubPackage workSet, GenerationUnit generationUnit) {
		log.info("adding workset for " + generationUnit.getSlaveResource().getName());
		generationUnit.setParentGenerationSubPackage(workSet);
		workSet.addGenerationUnit(generationUnit);
	}

	private void merge(GenerationOptions options, GenerationPackage workSet,
			AppServerRelationProperties slaveProperties) {
		decorateVersion(options.getVersions(), slaveProperties);
		for (GenerationUnit unit : workSet.getAsSet()) {
			if (unit.getSlaveResource().equals(slaveProperties.getOwner())) {
				unit.getAppServerRelationProperties().merge(slaveProperties);
			}
		}
	}

	private void decorateVersion(Map<Integer, ApplicationWithVersion> appsWithVersion,
			AppServerRelationProperties properties) {

		if(properties.getProperties().containsKey(VERSION)) {
			//if MavenVersion property exists and has a value do not overwrite with Version
			if (!properties.getProperties().containsKey(MAVEN_VERSION) || StringUtils.isBlank(properties.getProperties().get(MAVEN_VERSION).getCurrentValue())) {
				properties.getProperties().put(MAVEN_VERSION, properties.getProperties().get(VERSION));
			}
		}

		if (appsWithVersion.containsKey(properties.getOwner().getId())) {
			ApplicationWithVersion applicationWithVersion = appsWithVersion.get(properties.getOwner()
					.getId());
			if (StringUtils.isNotBlank(applicationWithVersion.getVersion())) {
				properties.getProperties().put(VERSION,
						new FreeMarkerProperty(applicationWithVersion.getVersion(), VERSION));
				properties.getProperties().put(MAVEN_VERSION,
						new FreeMarkerProperty(applicationWithVersion.getVersion(), MAVEN_VERSION));
			}
		}
	}

	protected Set<TemplateDescriptorEntity> templatesForResource(GenerationOptions options,
			ResourceEntity resource) {
		Set<TemplateDescriptorEntity> templates = new LinkedHashSet<>();
		utils.getTemplates(resource, options.getContext().getContext(), templates, options.getContext()
				.getTargetPlatformId());
		return templates;
	}

	protected Set<TemplateDescriptorEntity> templatesForRelation(GenerationOptions options,
			AbstractResourceRelationEntity resourceRelation) {
		Set<TemplateDescriptorEntity> templates = new LinkedHashSet<>();
		if (resourceRelation != null) {
			// relation is null when handed a ASR base on TYPES
			utils.getTemplates(options.getContext().getContext(), resourceRelation, templates, options
					.getContext().getTargetPlatformId());
		}
		return templates;
	}

	// dont use this to load properties for related resources, use properties.addConsumedRelation
	private AppServerRelationProperties propertiesFor(GenerationOptions options, ResourceEntity resource,
			AMWTemplateExceptionHandler templateExceptionHandler) {
		return new AppServerRelationProperties(options.getContext().getContext(), resource,
				templateExceptionHandler, propertyMaskingContext);
	}

}
