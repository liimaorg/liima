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

import ch.puzzle.itc.mobiliar.business.database.control.AmwAuditReader;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFailureReason;
import ch.puzzle.itc.mobiliar.business.deploy.entity.NodeJobEntity;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.*;
import ch.puzzle.itc.mobiliar.business.globalfunction.control.GlobalFunctionService;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyMaskingContext;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtPersistenceService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.GeneratorException;
import ch.puzzle.itc.mobiliar.common.exception.GeneratorException.MISSING;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class GeneratorDomainServiceWithAppServerRelations {

    @Inject
    Logger log;

    @Inject
    protected GeneratorFileWriter writer;

    @Inject
    private DeploymentBoundary deploymentBoundary;

    @Inject
    private AmwAuditReader amwAuditReader;

    @Inject
    private ContextDomainService contextDomainService;

    @Inject
    private GenerationUnitFactory generationUnitFactory;

    @Inject
    private GlobalFunctionService globalFunctionService;

    @Inject
    private ResourceDependencyResolverService resourceDependencyResolver;

    @Inject
    private ReleaseMgmtPersistenceService releaseMgmtPersistenceService;

    @Inject
    EntityManager entityManager;

    @Inject
    PermissionService permissionService;

    @Inject
    private GeneratorUtils generatorUtils;

    @Inject
    PropertyMaskingContext propertyMaskingContext;

    /**
     * @return a NodeGenerationResult
     */
    public NodeGenerationResult generateApplicationServerConfigPerNode(GenerationContext generationContext)
            throws IOException, GeneratorException {
        // TODO Find a nicer solution for the properties ExceptionHandling
        AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
        GenerationPackage work = initialize(generationContext, templateExceptionHandler);

        // generate
        NodeGenerationResult result = generateApplicationServerConfig(work, generationContext);

        // add validation Errors
        if (!templateExceptionHandler.isSuccess()) {
            result.addAllPropertyValidationExceptions(templateExceptionHandler.getErrorMessages());
        }

        // prepare Result
        result.setFolderToExecute(writer.getGenerationSubFolderForContext(generationContext));
        return result;
    }

    private GenerationPackage initialize(GenerationContext context, AMWTemplateExceptionHandler templateExceptionHandler) {
        GenerationOptions options = new GenerationOptions(context);
        return generationUnitFactory.createWorkForAppServer(options, context.getApplicationServer(), templateExceptionHandler);
    }

    private List<GenerationUnitGenerationResult> generateGeneralAppServerConfig(GenerationPackage work,
                                                                                AppServerRelationsTemplateProcessor appServerRelationsTemplateProcessor,
                                                                                String basePath,
                                                                                GenerationContext context) throws IOException {
        // get appserver and this node
        //Set<GenerationUnit> unitOfWork = GenerationUnit.forAppServer(work.getAsSet(), context.getNode());
        Set<GenerationUnit> unitOfWork = work.getAsWithGivenNode(context.getNode());
        List<GenerationUnitGenerationResult> generationResult = appServerRelationsTemplateProcessor
                .generateAppServer(unitOfWork, context.getNode());
        writer.generateFileStructure(generationResult, basePath, null);
        return generationResult;
    }

    private ApplicationGenerationResult generateApplicationConfig(GenerationPackage work,
                                                                  AppServerRelationsTemplateProcessor appServerRelationsTemplateProcessor,
                                                                  String basePath,
                                                                  ResourceEntity application) throws IOException {

        Map<ResourceEntity, Set<GenerationUnit>> appBatches = work.getAppGenerationBatches();
        Set<GenerationUnit> unitsOfWork = GenerationUnit.batchFor(appBatches, application);
        List<GenerationUnitGenerationResult> generationResults = appServerRelationsTemplateProcessor.generateApp(unitsOfWork, application);
        writer.generateFileStructure(generationResults, basePath, application.getName());

        ApplicationGenerationResult appResult = new ApplicationGenerationResult();
        appResult.setApplication(application);
        appResult.setGenerationResults(generationResults);

        return appResult;
    }

    /*************** OLD GENERATOR Logic ****************/

    /**
     * Get a list of all environments (the leafs of the hierarchical tree) from the given context
     * <pre>
     *     GLOBAL
     *     /    \
     *    DEV   PROD
     *    / \    / \
     *   A   B  X   Y
     * </pre>
     * getAllEnvironments(DEV) -> {A, B} getAllEnvironments(PROD) -> {X, Y} getAllEnvironments(GLOBAL) -> {A,
     * B, X, Y}
     *
     * @param e - the context for which we want to get all sub-contexts and itself
     * @return - a flat list of all context entities
     */
    public List<ContextEntity> getAllEnvironments(final ContextEntity e) {
        if (e.getChildren() == null || e.getChildren().isEmpty()) {
            return Collections.singletonList(e);
        } else {
            final List<ContextEntity> result = new ArrayList<>();
            for (final ContextEntity c : e.getChildren()) {
                result.addAll(getAllEnvironments(c));
            }
            return result;
        }
    }

    /**
     * This method generates the application server configuration for a deployment and also prepares the
     * command for the actual execution of the deployment returned as a {@link GenerationResult}
     *
     * @param deployment      the deployment to be generated
     * @param generationModus - flag to toggle between simulation and production mode. The value of this flag is
     *                        provided in the template. It is the duty of the deployment-script to handle the flag
     *                        appropriately (e.g. by having if clauses which ensure that the actual deployment command
     *                        is only executed if this flag is set to true).
     * @return - a GenerationResult ready to be invoked for the actual deployment, null if the lock on this
     * deployment was not obtained (e.g. another server has picked it up already)
     */
    public GenerationResult generateConfigurationForDeployment(DeploymentEntity deployment, GenerationModus generationModus) {

        log.info("Start writing configuration for all nodes of deployment " + deployment.getId());
        GenerationResult generationResult = null;
        try {
            generationResult = generateConfigurationAndGetFoldersToExecute(deployment, generationModus);

            if (generationResult.hasErrors()) {
                handleExceptions(generationModus, generationResult, deployment);
            }
        } catch (final GeneratorException | IOException e) {
            handleException(generationModus, e, deployment);
        }
        return generationResult;
    }

    /**
     * Generates the Configuration for the given deployment and returns the generated folders to execute them
     * within the Deployment Script
     *
     * @return GenerationResult
     */
    public GenerationResult generateConfigurationAndGetFoldersToExecute(DeploymentEntity deployment, GenerationModus generationModus)
            throws GeneratorException, IOException {
        // look up resource entity for the chosen release
        final ResourceEntity applicationServer = resourceDependencyResolver.getResourceEntityForRelease(deployment.getResourceGroup(),
                deployment.getRelease());

        if (applicationServer == null) {
            final String message = "ApplicationServer " + deployment.getResourceGroup().getName()
                    + " does not exist in release "
                    + deployment.getRelease().getName();
            log.info(message);
            throw new GeneratorException(message, MISSING.APPSERVER);
        }
        deployment.setResource(applicationServer);

        Date stateDate = deployment.getDeploymentStateDate();

        // Get the current application server information at the given
        // stateDate
        final ResourceEntity applicationServerFromHistory = amwAuditReader
                .getByDate(ResourceEntity.class, stateDate, applicationServer.getId());

        // Context
        Integer contextId;
        if (deployment.getContext() != null && deployment.getContext().getId() != null) {
            contextId = deployment.getContext().getId();
        } else {
            contextId = contextDomainService.getGlobalResourceContextEntity().getId();
        }

        // Always read the context with the current data state (we do not
        // want to deploy on out-dated environments)
        final ContextEntity context = entityManager.find(ContextEntity.class, contextId);

        if (context == null) {
            final String message = "Keine Umgebung vorhanden";
            log.log(Level.WARNING, message);
            throw new GeneratorException(message, MISSING.CONTEXT);
        }

        List<GlobalFunctionEntity> globalFunctions = globalFunctionService.getAllGlobalFunctionsAtDate(stateDate);

        // do the deployment right now
        Date deploymentDate = new Date();

        // We generate the configuration for the application server
        // in all environments
        GenerationResult result = new GenerationResult();

        for (final ContextEntity c : getAllEnvironments(context)) {
            GenerationContext generationContext = new GenerationContext(c, applicationServerFromHistory, deployment,
                    deploymentDate, generationModus, resourceDependencyResolver);

            generationContext.setGlobalFunctions(globalFunctions);
            result.addEnvironmentGenerationResult(generateApplicationServerConfigurationForEnvironment(generationContext));
        }
        result.setDeployment(deployment);
        // Now all configurations are written...
        log.info("Finished writing configuration for all nodes");
        return result;
    }

    /**
     * @return an EnvironmentGenerationResult
     */
    public EnvironmentGenerationResult generateApplicationServerForTest(Integer contextId, Integer appServerId, Integer releaseId, Date stateDate)
            throws IOException, AMWException {
        ContextEntity context;
        try {
            context = contextDomainService.getContextEntityById(contextId);
        } catch (ResourceNotFoundException e) {
            return null;
        }

        ResourceEntity appServer = getAppServer(appServerId, stateDate);

        ReleaseEntity release;
        if (releaseId == null) {
            release = appServer.getRelease();
        } else {
            release = releaseMgmtPersistenceService.getById(releaseId);
        }

        if (stateDate == null) {
            stateDate = new Date();
        }

        appServer = resourceDependencyResolver.getResourceEntityForRelease(appServer.getResourceGroup(), release);

        if (appServer.getRuntime() == null) {
            throw new AMWException("No runtime found for the given applicationserver!");
        }
        ResourceEntity runtime = resourceDependencyResolver.getResourceEntityForRelease(appServer.getRuntime(), release);

        DeploymentEntity fakeDeplyoment = createFakeDeployment(-1, release, runtime, stateDate);

        List<GlobalFunctionEntity> globalFunctions = globalFunctionService.getAllGlobalFunctionsAtDate(stateDate);

        GenerationContext generationContext = new GenerationContext(context, appServer, fakeDeplyoment,
                stateDate, GenerationModus.TEST, resourceDependencyResolver);
        generationContext.setGlobalFunctions(globalFunctions);

        // Determine decrypt permission once
        boolean hasDecryptPermission = permissionService.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, context,
                Action.ALL, appServer.getResourceGroup(), null) ||
                permissionService.hasPermission(Permission.RESOURCETYPE_PROPERTY_DECRYPT, context,
                        Action.ALL, null, appServer.getResourceType());

        // Enable masking for this request if user lacks decrypt permission
        if (!hasDecryptPermission) {
            propertyMaskingContext.enableMasking();
        }

        EnvironmentGenerationResult result;
        try {
            result = generateApplicationServerConfigurationForEnvironment(generationContext);
            //If we're in test mode and the user doesn't have the permission to see this template, we omit the content of the template to prevent the giveaway of sensitive information.
            // TODO review: is appServer the correct ResourceEntity?
            omitTemplateForLackingPermissions(context, appServer, result);
        } catch (GeneratorException e) {
            result = createFailureEnvironmentGenerationResult(e);
        }

        return result;
    }

    private ResourceEntity getAppServer(Integer appServerId, Date stateDate) throws AMWException {
        ResourceEntity appServer;
        if (stateDate == null) {
            //If the state date is not set, we load the appserver from the live database (which is a lot faster than from the history)
            appServer = entityManager.find(ResourceEntity.class, appServerId);
        } else {
            //Otherwise, we have to look up the audited version of the appserver
            appServer = amwAuditReader.getByDate(ResourceEntity.class, stateDate, appServerId);
            if (appServer == null) {
                throw new AMWException("No resource found for the given date!");
            }
        }
        return appServer;
    }

    void omitTemplateForLackingPermissions(ContextEntity context, ResourceEntity resource, EnvironmentGenerationResult result) {
        boolean omitTemplateContent = !permissionService.hasPermission(Permission.RESOURCE_TEST_GENERATION_RESULT, context,
                Action.READ, resource.getResourceGroup(), null);
        if (omitTemplateContent) {
            result.omitAllTemplates();
        }
    }

    /**
     * @return a faked deploymentEntity for testing
     */
    public DeploymentEntity createFakeDeployment(Integer id, ReleaseEntity release, ResourceEntity runtime, Date stateToDeploy) {
        DeploymentEntity fakeDeplyoment = new DeploymentEntity();
        fakeDeplyoment
                .setApplicationsWithVersion(new ArrayList<DeploymentEntity.ApplicationWithVersion>());
        fakeDeplyoment.setRelease(release);
        fakeDeplyoment.setRuntime(runtime);
        fakeDeplyoment.setId(id);
        fakeDeplyoment.setStateToDeploy(stateToDeploy);

        return fakeDeplyoment;
    }

    private EnvironmentGenerationResult createFailureEnvironmentGenerationResult(GeneratorException e) {
        EnvironmentGenerationResult result = new EnvironmentGenerationResult();
        result.setEnvironmentException(e);
        return result;
    }

    /**
     * Handles the applicationserver configuration generation for a specific environment.
     *
     * @return EnvironmentGenerationResult
     */
    public EnvironmentGenerationResult generateApplicationServerConfigurationForEnvironment(
            GenerationContext generationContext) throws GeneratorException,
            IOException {

        final List<ResourceEntity> allNodes = generationContext.getApplicationServer()
                .getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.NODE);
        List<ResourceEntity> nodes = new ArrayList<>();
        // filter out all Nodes which are not existing in the Release (or in an earlier) of the current Deployment
        if (allNodes != null) {
            final ReleaseEntity targetRelease = generationContext.getTargetRelease();
            for (ResourceEntity node : allNodes) {
                ResourceEntity resourceEntityForRelease = resourceDependencyResolver.getResourceEntityForRelease(node.getResourceGroup(), targetRelease);
                if (resourceEntityForRelease != null && !nodes.contains(resourceEntityForRelease)) {
                    nodes.add(resourceEntityForRelease);
                }
            }
        }

        if (nodes.isEmpty()) {
            final String message = "No usable nodes enabled";
            log.info(message);
            throw new GeneratorException(message, MISSING.NODE);
        }

        EnvironmentGenerationResult environmentResult = new EnvironmentGenerationResult();
        environmentResult.setGenerationContext(generationContext);

        for (final ResourceEntity node : nodes) {
            generationContext.setNode(node);
            environmentResult.addNodeGenerationResult(generateApplicationServerConfigPerNode(generationContext));
        }
        return environmentResult;
    }

    /**
     * Handle exceptions in the preparation of the deployment.
     *
     * @param generationModus - if the deployment has been executed in simulation or realistic mode
     * @param e               - the causing exception
     * @param deployment      - the deployment order which has failed
     */
    private void handleException(GenerationModus generationModus, final Exception e,
                                 final DeploymentEntity deployment) {
        log.log(Level.WARNING, String.format("Deployment %d failed ",deployment.getId()), e);
        String message = generationModus.getAction() + " failure at " + new Date() + ". Reason: " + e.getMessage() + "\n";
        DeploymentFailureReason reason = (e instanceof GeneratorException && ((GeneratorException) e).getMissingObject().equals(MISSING.NODE)) ?
                DeploymentFailureReason.NODE_MISSING : null;
        deploymentBoundary.updateDeploymentInfoAndSendNotification(generationModus, deployment.getId(), message,
                deployment.getResource() != null ? deployment.getResource().getId() : null, null, reason);
    }

    /**
     * Handle exceptions in the preparation of the deployment.
     */
    private void handleExceptions(GenerationModus generationModus, GenerationResult generationResult, DeploymentEntity deployment) {

        StringBuilder message = new StringBuilder(generationModus.getAction() + " failure at " + new Date() + ". Reasons: ");
        if (generationResult.hasErrors()) {
            message.append(generationResult.getErrorMessage());
        }

        DeploymentFailureReason reason = null;
        for (EnvironmentGenerationResult environmentGenerationResult : generationResult.getEnvironmentGenerationResults()) {
            if (environmentGenerationResult.allNodesDisabled()) {
                reason = DeploymentFailureReason.NODE_MISSING;
                break;
            }
        }

        log.log(Level.WARNING, String.format("Deployment %d failed: %s", deployment.getId(),  message));
        deploymentBoundary.updateDeploymentInfoAndSendNotification(generationModus, deployment.getId(), message.toString(),
                deployment.getResource() != null ? deployment.getResource().getId() : null, generationResult, reason);
    }

    /**
     * Generates the configuration for a whole application server on a specific node - this method more or
     * less defines the whole configuration process
     *
     * @return NodeGenerationResult
     */
    private NodeGenerationResult generateApplicationServerConfig(GenerationPackage work,
                                                                 GenerationContext context) throws IOException, GeneratorException {
        log.info("Start generating configuration for AppServer: " + context.getContext().getName());

        // Find the applications of the application server
        final Set<ResourceEntity> applications = resourceDependencyResolver.getConsumedRelatedResourcesByResourceType(
                        context.getApplicationServer(), DefaultResourceTypeDefinition.APPLICATION, context.getTargetRelease());

        if (applications == null || applications.isEmpty()) {
            final String msg = "Applicationserver contains no Application";
            log.info(msg);
            throw new GeneratorException(msg, MISSING.APPLICATION);
        }

        final String folderForConfiguration = writer.getGenerationFolderForContext(context);
        // First, we remove the folder if it already exists.
        FileUtils.deleteDirectory(new File(folderForConfiguration));

        context.setGenerationDir(folderForConfiguration);

        AppServerRelationsTemplateProcessor appServerRelationsTemplateProcessor = new AppServerRelationsTemplateProcessor(log, context);
        appServerRelationsTemplateProcessor.setGlobals(work);
        appServerRelationsTemplateProcessor.setNodeProperties(work.getAsSet(), context.getNode());

        NodeGenerationResult result = new NodeGenerationResult();

        // set node active and test hostname when Test generation Modus
        if (GenerationModus.TEST.equals(context.getGenerationModus())) {
            if (!appServerRelationsTemplateProcessor.isNodeEnabled()) {
                appServerRelationsTemplateProcessor.setNodeEnabledForTestGeneration();
                result.setNodeEnabledForTestGeneration(true);
            }
            if (StringUtils.isNotEmpty(appServerRelationsTemplateProcessor.getHostname())) {
                appServerRelationsTemplateProcessor.setTestNodeHostname();
            }
        }

        // add Result
        result.setNode(context.getNode());
        result.setDeploymentLogfilePath(context.getDeploymentProperties().getAmwLogFilePath());
        result.setHostname(appServerRelationsTemplateProcessor.getHostname());

        // checks if the node is enabled
        if (appServerRelationsTemplateProcessor.isNodeEnabled()) {

            createNodeJob(context);

            result.setNodeEnabled(true);
            List<ApplicationGenerationResult> appResults = new ArrayList<>();

            // We first iterate through all applications
            for (final ResourceEntity app : applications) {
                // Now we generate the configuration for the application.
                appResults.add(generateApplicationConfig(work, appServerRelationsTemplateProcessor,
                        folderForConfiguration, app));
            }
            // finally, we generate the configurations for the
            // application-independent server (e.g. server.xml)
            List<GenerationUnitGenerationResult> asResults = generateGeneralAppServerConfig(work,
                    appServerRelationsTemplateProcessor, folderForConfiguration, context);

            result.setApplicationResults(appResults);
            result.setApplicationServerResults(asResults);
        } else {
            result.setNodeEnabled(false);
        }

        log.info("Finished generating configuration");

        return result;
    }

    private void createNodeJob(GenerationContext context) {
        NodeJobEntity nodeJobEntity;
        if (GenerationModus.DEPLOY.equals(context.getGenerationModus())
                || GenerationModus.PREDEPLOY.equals(context.getGenerationModus())
                || GenerationModus.SIMULATE.equals(context.getGenerationModus())) {
            // create NodeJob for this Node and Add to Deployment in Deploy and Predeploy mode
            nodeJobEntity = deploymentBoundary.createAndPersistNodeJobEntity(context.getDeployment());

        } else {
            // create Test NodeJob Entity
            nodeJobEntity = new NodeJobEntity();
            nodeJobEntity.setId(1);
        }
        context.setNodeJobEntity(nodeJobEntity);
    }


    /**
     * returns true if a Node for the application server is active at the given moment
     */
    public boolean hasActiveNodeToDeployOnAtDate(ResourceEntity appServer, ContextEntity environment, Date stateToDeployDate) {
        ContextEntity context = entityManager.find(ContextEntity.class, environment.getId());
        // if not set take actual version
        if (stateToDeployDate == null) {
            stateToDeployDate = new Date();
        }
        ResourceEntity appServerAtDate = amwAuditReader.getByDate(ResourceEntity.class, stateToDeployDate, appServer.getId());
        for (ConsumedResourceRelationEntity resourceRelation : appServerAtDate.getConsumedMasterRelations()) {
            if (resourceRelation.getResourceRelationType().getResourceTypeB().isNodeResourceType()) {
                FreeMarkerProperty property = generatorUtils.findPropertyValueByName(context, resourceRelation, AppServerRelationsTemplateProcessor.NODE_ACTIVE);
                if (property != null
                        && StringUtils.isNotBlank(property.getCurrentValue())
                        && Boolean.parseBoolean((property.getCurrentValue()))) {
                    return true;
                }
            }
        }
        return false;
    }
}
