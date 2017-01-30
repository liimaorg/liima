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

package ch.mobi.itc.mobiliar.rest.deployments;

import ch.mobi.itc.mobiliar.rest.dtos.AppWithVersionDTO;
import ch.mobi.itc.mobiliar.rest.dtos.DeploymentDTO;
import ch.mobi.itc.mobiliar.rest.dtos.DeploymentParameterDTO;
import ch.mobi.itc.mobiliar.rest.dtos.DeploymentRequestDTO;
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentService;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentService.DeploymentFilterTypes;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.deploy.entity.NodeJobEntity.NodeJobStatus;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.control.KeyRepository;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.DeploymentParameter;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.Key;
import ch.puzzle.itc.mobiliar.business.environment.control.EnvironmentsScreenDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorDomainServiceWithAppServerRelations;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupPersistenceService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.common.exception.DeploymentStateException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundExcption;
import ch.puzzle.itc.mobiliar.common.util.CustomFilter;
import ch.puzzle.itc.mobiliar.common.util.CustomFilter.ComperatorFilterOption;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.common.util.Tuple;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.ValidationException;
import java.util.*;

//for transactions
@Stateless
@Path("/deployments")
@Api(value = "/deployments", description = "Managing deployment resources")
public class DeploymentsRest {

    @Inject
    private DeploymentService deploymentService;
    @Inject
    private EnvironmentsScreenDomainService environmentsService;
    @Inject
    private ReleaseMgmtService releaseService;
    @Inject
    private ResourceDependencyResolverService dependencyResolverService;
    @Inject
    private ResourceGroupPersistenceService resourceGroupService;
    @Inject
    private ResourceTypeProvider resourceTypeProvider;
    @Inject
    GeneratorDomainServiceWithAppServerRelations generatorDomainServiceWithAppServerRelations;
    @Inject
    private KeyRepository keyRepository;

    /**
     * Query for deployments. All parameters are optional.
     * Date format: epoch timestamp (number of milliseconds since January 1st, 1970, UTC)
     *
     * @return the deployments. The header X-Total-Count contains the total result count.
     **/
    @GET
    @ApiOperation(value = "returns all Deplyoments matching the optional filter Query Params")
    public Response getDeployments(
            @ApiParam("Tracking ID") @QueryParam("trackingId") Integer trackingId,
            @ApiParam("Deplyoment State") @QueryParam("deploymentState") DeploymentState deploymentState,
            @ApiParam("Application Server Name") @QueryParam("appServerName") List<String> appServerNames,
            @ApiParam("Application Name") @QueryParam("appName") List<String> appNames,
            @ApiParam("Runtime Name") @QueryParam("runtimeName") List<String> runtimeNames,
            @ApiParam("Max Results") @QueryParam("maxResults") Integer maxResults,
            @ApiParam("Offset") @DefaultValue("0") @QueryParam("offset") Integer offset,
            @ApiParam("From Date Filter") @QueryParam("fromDate") Long fromDate,
            @ApiParam("To Date Filter") @QueryParam("toDate") Long toDate,
            @ApiParam("Environment Filter") @QueryParam("environmentName") List<String> environmentNames,
            @ApiParam("Deployment Parameter Filter") @QueryParam("deploymentParameter") List<String> deploymentParameters,
            @ApiParam("Deployment Parameter Value Filter") @QueryParam("deploymentParameterValue") List<String> deploymentParameterValues,
            @ApiParam("only Latest Filter") @DefaultValue("false") @QueryParam("onlyLatest") boolean onlyLatest) {

        LinkedList<CustomFilter> filters = new LinkedList<>();

        if (trackingId != null) {
            filters.add(createFilter(DeploymentFilterTypes.TRACKING_ID, trackingId.toString(), ComperatorFilterOption.equals));
        }
        if (deploymentState != null) {
            filters.add(createFilter(DeploymentFilterTypes.DEPLOYMENT_STATE, deploymentState, ComperatorFilterOption.equals));
        }
        if (appServerNames != null) {
            for (String asName : appServerNames) {
                filters.add(createFilter(DeploymentFilterTypes.APPSERVER_NAME, asName, ComperatorFilterOption.equals));
            }
        }
        if (appNames != null) {
            for (String appName : appNames) {
                filters.add(createFilter(DeploymentFilterTypes.APPLICATION_NAME, appName, ComperatorFilterOption.equals));
            }
        }
        if (runtimeNames != null) {
            for (String runtimeName : runtimeNames) {
                filters.add(createFilter(DeploymentFilterTypes.TARGETPLATFORM, runtimeName, ComperatorFilterOption.equals));
            }
        }
        if (fromDate != null) {
            CustomFilter filter = createFilter(DeploymentFilterTypes.DEPLOYMENT_DATE, ComperatorFilterOption.greaterequals);
            filter.setDateValue(new Date(fromDate));
            filters.add(filter);
        }
        if (toDate != null) {
            CustomFilter filter = createFilter(DeploymentFilterTypes.DEPLOYMENT_DATE, ComperatorFilterOption.smallerequals);
            filter.setDateValue(new Date(toDate));
            filters.add(filter);
        }
        if (environmentNames != null) {
            for (String envName : environmentNames) {
                filters.add(createFilter(DeploymentFilterTypes.ENVIRONMENT_NAME, envName, ComperatorFilterOption.equals));
            }
        }
        if (deploymentParameters != null) {
            for (String deploymentParameter : deploymentParameters) {
                CustomFilter filter = createFilter(DeploymentFilterTypes.DEPLOYMENT_PARAMETER, deploymentParameter, ComperatorFilterOption.equals);
                filter.setJoiningTableQuery("join d.deploymentParameters p");
                filters.add(filter);
            }
        }
        if (deploymentParameterValues != null) {
            for (String deploymentParameterValue : deploymentParameterValues) {
                CustomFilter filter = createFilter(DeploymentFilterTypes.DEPLOYMENT_PARAMETER_VALUE, deploymentParameterValue, ComperatorFilterOption.equals);
                filter.setJoiningTableQuery("join d.deploymentParameters p");
                filters.add(filter);
            }
        }
        if (onlyLatest) {
            filters.add(createFilter(DeploymentFilterTypes.LASTDEPLOYJOBFORASENV, null));
        }

        Tuple<Set<DeploymentEntity>, Integer> result = deploymentService.getFilteredDeployments(true, offset, maxResults, filters, null, null, null);

        List<DeploymentDTO> deploymentDtos = new ArrayList<>();

        for (DeploymentEntity entity : result.getA()) {
            deploymentDtos.add(new DeploymentDTO(entity));
        }

        return Response.status(Status.OK).header("X-Total-Count", result.getB()).entity(deploymentDtos).build();
    }

    /**
     * Get information to a deployment Id.
     * In the future, this can be also used to update Deployments.
     *
     * @param id
     * @return the deployment or 404 if no deployment exists.
     * @throws Exception
     **/
    @GET
    @Path("/{id : \\d+}")
    // support digit only
    @ApiOperation(value = "get Deployment by id")
    public Response getDeployment(@ApiParam("Deployment ID") @PathParam("id") Integer id) {
        DeploymentEntity result;

        try {
            result = deploymentService.getDeploymentById(id);
        } catch (RuntimeException e) {
            return catchNoResultException(e, "Deployment with id " + id + " not found.");
        }

        return Response.status(Status.OK).entity(new DeploymentDTO(result)).build();
    }

    @GET
    @Path("/deploymentParameterKeys/")
    @ApiOperation(value = "returns the keys of all available DeploymentParameter")
    public Response getAllDeploymentParameterKeys() {
        List<DeploymentParameterDTO> deploymentParameters = new ArrayList<>();
        for (Key key : keyRepository.findAllKeys()) {
            deploymentParameters.add(new DeploymentParameterDTO(key.getName(), null));
        }
        return Response.status(Status.OK).entity(deploymentParameters).build();
    }

    /**
     * Creates a new deployment and returns the newly created deployment. Only creates one deployment per request.
     *
     * @param request containing a DeploymentRequestDTO
     * @return the new DeploymentDTO
     **/
    @POST
    @ApiOperation(value = "adds a DeplyomentRequest")
    public Response addDeployment(@ApiParam("Deployment Request") DeploymentRequestDTO request) {
        Integer trackingId;
        ResourceEntity appServer;
        Set<ResourceEntity> apps;
        ContextEntity environement;
        List<ApplicationWithVersion> applicationsWithVersion;
        LinkedList<CustomFilter> filters = new LinkedList<>();
        ReleaseEntity release;
        ResourceGroupEntity group;
        LinkedList<Integer> contexts = new LinkedList<>();
        ArrayList<DeploymentParameter> deployParams;

        // use default releaseId if no release in request
        if (request.getReleaseName() == null) {
            List<ReleaseEntity> releases = releaseService.loadAllReleases(false);
            release = dependencyResolverService.findMostRelevantRelease(new TreeSet<>(releases), new Date());
        } else {
            // try to fetch release by name
            release = releaseService.findByName(request.getReleaseName());
            if (release == null) {
                return Response.status(Status.BAD_REQUEST).entity(new ExceptionDto("Release " + request
                        .getReleaseName() + " not found."))
                        .build();
            }
        }

        // get the id of the ApplicationServer

        group = resourceGroupService.loadUniqueGroupByNameAndType(request.getAppServerName(), resourceTypeProvider
                .getOrCreateDefaultResourceType(DefaultResourceTypeDefinition.APPLICATIONSERVER)
                .getId());
        if (group == null) {
            return Response.status(Status.BAD_REQUEST).entity(new ExceptionDto("ApplicationServer with name " + request.getAppServerName() + " not found."))
                    .build();
        }
        appServer = dependencyResolverService.getResourceEntityForRelease(group, release);
        if (appServer == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ExceptionDto("ApplicationServer " + request.getAppServerName() + " does not exist in release " + release.getName()))
                    .build();
        }

        // get the id of the Environment
        try {
            environement = environmentsService.getContextByName(request.getEnvironmentName());
        } catch (RuntimeException e) {
            return catchNoResultException(e, "Environement " + request.getEnvironmentName() + " not found.");
        }

        // get the apps of the appServer
        apps = dependencyResolverService.getConsumedRelatedResourcesByResourceType(appServer, DefaultResourceTypeDefinition.APPLICATION, release);

        // convert the appsWithVersion
        try {
            if (apps == null) {
                apps = new HashSet<>();
            }
            applicationsWithVersion = convertToApplicationWithVersion(request.getAppsWithVersion(), apps);

        } catch (ValidationException e) {
            return Response.status(Status.BAD_REQUEST).entity(
                    new ExceptionDto("ValidationException", e.getMessage())).build();
        }

        try {
            deployParams = convertToDeploymentParameter(request.getDeploymentParameters());
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).entity(new ExceptionDto("Could not create deployment", e.getMessage())).build();
        }

        // check whether the AS has at least one node with hostname to deploy to
        boolean hasNode = generatorDomainServiceWithAppServerRelations.hasActiveNodeToDeployOnAtDate(appServer, environement, request.getStateToDeploy());
        if (!hasNode) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ExceptionDto("No active Node found on Environement " + request.getEnvironmentName()))
                    .build();
        }


        contexts.add(environement.getId());
        trackingId = deploymentService.createDeploymentReturnTrackingId(group.getId(), release.getId(), request.getDeploymentDate(),
                request.getStateToDeploy(), contexts,
                applicationsWithVersion, deployParams, request.getSendEmail(), request.getRequestOnly(), request.getSimulate(), request.getExecuteShakedownTest(),
                request.getNeighbourhoodTest());

        // get the deployment from the tracking id
        filters.add(createFilter(DeploymentFilterTypes.TRACKING_ID, trackingId.toString(), ComperatorFilterOption.equals));
        Tuple<Set<DeploymentEntity>, Integer> result = deploymentService.getFilteredDeployments(true, 0, 1, filters, null, null, null);

        DeploymentDTO deploymentDto = new DeploymentDTO(result.getA().iterator().next());

        return Response.status(Status.CREATED).header("Location", "/deployments/" + deploymentDto.getId()).entity(deploymentDto).build();
    }

    private ArrayList<DeploymentParameter> convertToDeploymentParameter(List<DeploymentParameterDTO> deploymentParameters) {
        ArrayList<DeploymentParameter> parameters = new ArrayList<>();

        if (deploymentParameters != null) {
            for (DeploymentParameterDTO deploymentParameterDto : deploymentParameters) {
                String key = deploymentParameterDto.getKey().trim();
            	parameters.add(new DeploymentParameter(key, deploymentParameterDto.getValue()));
            }
        }

        return parameters;
    }

    @PUT
    @Path("/{id : \\d+}/state")
    @Consumes("text/plain")
    @ApiOperation(value = "Update the state of a deployment")
    public Response patchDeployment(@ApiParam("deployment Id") @PathParam("id") Integer deploymentId, @ApiParam("New status") String stateStr) {
        DeploymentState newState = DeploymentState.getByString(stateStr);

        if (newState == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ExceptionDto("Invalid status",
                    "Allowed values are " + Arrays.asList(DeploymentState.values()))).build();
        }

        try {
            deploymentService.updateDeploymentState(deploymentId, newState);
        } catch (RuntimeException e) {
            return catchDeploymentStateException(e);
        }

        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{id: \\d+}/jobs/{nodeJobId: \\d+}")
    @Consumes("text/plain")
    @ApiOperation(value = "Set the nodeJobResult for the given Deployment (id) and the nodeJob.")
    public Response updateNodeJobResult(
            @ApiParam("deployment Id") @PathParam("id") Integer deploymentId,
            @ApiParam("nodeJob Id") @PathParam("nodeJobId") Integer nodeJobId,
            @ApiParam("Status result") String statusStr) {

        NodeJobStatus status = NodeJobStatus.getNodeJobStatusByString(statusStr);
        if (status == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ExceptionDto("Invalid status",
                    "Allowed values are " + Arrays.asList(NodeJobStatus.values()))).build();
        }

        try {
            deploymentService.updateNodeJobStatus(deploymentId, nodeJobId, status);
        } catch (NotFoundExcption e) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ExceptionDto(e)).build();
        } catch (RuntimeException e) {
            return catchDeploymentStateException(e);
        }
        return Response.status(Response.Status.OK).build();

    }

    /**
     * Convert AppWithVersion to ApplicationWithVersion. Checks if
     * AppWithVersion contains all the Applications and adds the Application id.
     **/
    private List<ApplicationWithVersion> convertToApplicationWithVersion(List<AppWithVersionDTO> requestedApps, Set<ResourceEntity> apps)
            throws ValidationException {
        LinkedList<ApplicationWithVersion> result = new LinkedList<>();
        List<AppWithVersionDTO> requestedAppsCopy = new LinkedList<>(requestedApps);

        // copy the collections as we don't want to modify the originals
        Set<ResourceEntity> appsCopy = new HashSet<>(apps);
        StringBuilder sb = new StringBuilder();

        for (AppWithVersionDTO requestedApp : requestedApps) {
            // find the corresponding app
            for (ResourceEntity app : apps) {
                // if the name matches, convert the app
                if (requestedApp.getApplicationName().equals(app.getName())) {
                    //for backwards compatibility: use MavenVersion as Version
                    String appVersion = (requestedApp.getVersion() != null && !requestedApp.getVersion().isEmpty())
                            ? requestedApp.getVersion() : requestedApp.getVersion();
                    //convert
                    result.add(
                            new ApplicationWithVersion(
                                    requestedApp.getApplicationName(), app.getId(), appVersion));
                    //scratch off
                    requestedAppsCopy.remove(requestedApp);
                    appsCopy.remove(app);
                    break;
                }
            }
        }

        // check if something is left in the collections
        if (requestedAppsCopy.size() != 0) {
            sb.append("Applications not found:");

            for (AppWithVersionDTO app : requestedAppsCopy) {
                sb.append(' ').append(app.getApplicationName());
            }
        }
        if (appsCopy.size() != 0) {
            if (sb.length() != 0) {
                sb.append(" / ");
            }
            sb.append("Applications missing in request:");

            for (ResourceEntity app : appsCopy) {
                sb.append(' ').append(app.getName());
            }
        }

        if (sb.length() != 0) {
            throw new ValidationException(sb.toString());
        }

        return result;
    }

    private CustomFilter createFilter(DeploymentFilterTypes filterType, ComperatorFilterOption comperator) {
        CustomFilter filter = new CustomFilter(filterType.getFilterDisplayName(), filterType.getFilterTabColumnName(), filterType.getFilterType());
        filter.setComperatorSelection(comperator);

        return filter;
    }

    CustomFilter createFilter(DeploymentFilterTypes filterType, String value, ComperatorFilterOption comperator) {
        CustomFilter filter = createFilter(filterType, comperator);
        filter.setValue(value);

        return filter;
    }

    private CustomFilter createFilter(DeploymentFilterTypes filterType, DeploymentState value, ComperatorFilterOption comperator) {
        CustomFilter filter = createFilter(filterType, comperator);
        filter.setEnumType(DeploymentState.class);
        filter.setValue(value.name());

        return filter;
    }

    private Response catchNoResultException(RuntimeException e, String message) {
        if (e instanceof NoResultException || e.getCause() instanceof NoResultException) {
            return Response.status(Status.NOT_FOUND).entity(new ExceptionDto(message)).build();
        } else {
            throw e;
        }
    }

    private Response catchDeploymentStateException(RuntimeException e) {
        if (e instanceof DeploymentStateException || e.getCause() instanceof DeploymentStateException) {
            return Response.status(Response.Status.CONFLICT).entity(new ExceptionDto(e)).build();
        } else {
            throw e;
        }
    }
}
