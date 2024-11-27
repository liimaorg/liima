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

package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.*;
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.*;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.common.exception.*;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@RequestScoped
@Path("/resources")
@Api(value = "/resources", description = "ResourceGroups")
public class ResourceGroupsRest {

    @Inject
    private ReleaseLocator releaseLocator;

    @Inject
    private ReleaseMgmtService releaseMgmtService;

    @Inject
    private DeploymentBoundary deploymentBoundary;

    @Inject
    private ResourceGroupLocator resourceGroupLocator;

    @Inject
    private ResourceBoundary resourceBoundary;

    @Inject
    private ResourceLocator resourceLocator;

    @Inject
    private CopyResource copyResource;

    @Inject
    private ResourceRelationsRest resourceRelations;

    @Inject
    private ResourcePropertiesRest resourceProperties;

    @Inject
    private ResourceDependencyResolverService resourceDependencyResolverService;

    @Inject
    private PermissionBoundary permissionBoundary;

    @Inject
    private ResourceTemplatesRest resourceTemplatesRest;

    @Inject
    private ResourceRelationService resourceRelationService;

    @Inject
    PropertyEditor propertyEditor;

    @GET
    @ApiOperation(value = "Get resource groups", notes = "Returns the available resource groups")
    public List<ResourceGroupDTO> getResources(
            @ApiParam(value = "a resource type, the list should be filtered by") @QueryParam("type") String type) {
        List<ResourceGroupDTO> result = new ArrayList<>();
        List<ResourceGroupEntity> resourceGroups;
        if (type != null) {
            resourceGroups = resourceGroupLocator.getGroupsForType(type, true, true);
        } else {
            resourceGroups = resourceGroupLocator.getResourceGroups();
        }

        for (ResourceGroupEntity resourceGroup : resourceGroups) {
            List<ReleaseEntity> releases = new ArrayList<>();
            for (ResourceEntity resource : resourceGroup.getResources()) {
                releases.add(resource.getRelease());
            }
            result.add(new ResourceGroupDTO(resourceGroup, releases));
        }
        return result;
    }

    @GET
    @ApiOperation(value = "Get resource groups by resource type id")
    public List<ResourceGroupDTO> getResourceGroupsByResourceTypeId(
            @ApiParam(value = "a resource type id, the list should be filtered by") @QueryParam("typeId") Integer typeId) {
        List<ResourceGroupEntity> resourceGroups;
        if (typeId != null) {
            resourceGroups = resourceGroupLocator.getGroupsForType(typeId, true, true);
        } else {
            resourceGroups = resourceGroupLocator.getResourceGroups();
        }

        return resourceGroups.stream().map(resourceGroupEntity -> {
            List<ReleaseEntity> releases = resourceGroupEntity.getResources().stream().map(ResourceEntity::getRelease).collect(Collectors.toList());
            SortedSet<ReleaseEntity> sortedReleases = releases.stream()
                    .sorted(Comparator.comparing(ReleaseEntity::getInstallationInProductionAt))
                    .collect(Collectors.toCollection(TreeSet::new));
            ReleaseEntity mostRelevantRelease = this.resourceDependencyResolverService.findMostRelevantRelease(sortedReleases, new Date());
            return new ResourceGroupDTO(resourceGroupEntity, mostRelevantRelease, releases);
        }).collect(Collectors.toList());
    }


    @Path("/{resourceGroupName}")
    @GET
    @ApiOperation(value = "Get a resource group")
    public ResourceGroupDTO getResourceGroup(@PathParam("resourceGroupName") String resourceGroupName) {
        ResourceGroupEntity resourceGroupByName = resourceGroupLocator.getResourceGroupByName(resourceGroupName);
        List<ReleaseEntity> releases = releaseLocator.getReleasesForResourceGroup(resourceGroupByName);
        return new ResourceGroupDTO(resourceGroupByName, releases);
    }

    @Path("/{resourceGroupName}/{releaseName}")
    @GET
    @ApiOperation(value = "Get resource in specific release")
    public ResourceDTO getResource(@PathParam("resourceGroupName") String resourceGroupName,
                                   @PathParam("releaseName") String releaseName,
                                   @QueryParam("env") @DefaultValue("Global") String environment,
                                   @QueryParam("type") String resourceType) throws ValidationException, ResourceNotFoundException {
        ResourceEntity resourceByRelease = resourceLocator.getResourceByGroupNameAndRelease(resourceGroupName, releaseName);
        return new ResourceDTO(resourceByRelease, resourceRelations.getResourceRelations(resourceGroupName,
                releaseName, resourceType), resourceProperties.getResourceProperties(resourceGroupName, releaseName,
                environment), resourceTemplatesRest.getResourceTemplates(resourceGroupName, releaseName));
    }

    @Path("/{resourceGroupName}/lte/{releaseName}")
    @GET
    @ApiOperation(value = "Get exact or closest past resource release")
    public ResourceDTO getExactOrClosestPastRelease(@PathParam("resourceGroupName") String resourceGroupName,
                                                    @PathParam("releaseName") String releaseName,
                                                    @QueryParam("env") @DefaultValue("Global") String environment,
                                                    @QueryParam("type") String resourceType) throws ValidationException, ResourceNotFoundException {
        ReleaseEntity release = resourceLocator.getExactOrClosestPastReleaseByGroupNameAndRelease(resourceGroupName, releaseName);
        return new ResourceDTO(release, resourceRelations.getResourceRelations(resourceGroupName,
                release.getName(), resourceType), resourceProperties.getResourceProperties(resourceGroupName, release.getName(),
                environment), resourceTemplatesRest.getResourceTemplates(resourceGroupName, release.getName()));
    }


    /**
     * Creates a new resource and returns its location.
     *
     * @param request containing a ResourceReleaseDTO
     */
    @POST
    @ApiOperation(value = "Add a Resource")
    public Response addResource(@ApiParam("Add a Resource") ResourceReleaseDTO request) throws ValidationException, NotFoundException, ElementAlreadyExistsException {
        if(StringUtils.isEmpty(request.getName()) || StringUtils.isEmpty(request.getName().trim()))
            throw new ValidationException("Resource name must not be null or blank");

        if(StringUtils.isEmpty(request.getReleaseName()) || StringUtils.isEmpty(request.getReleaseName().trim()))
            throw new ValidationException("Release name must not be null or blank");

        try {
            resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), request.getName(),
                    request.getType(), request.getReleaseName());
        } catch (ResourceTypeNotFoundException e) {
            throw new NotFoundException("Resource type: " + request.getType() + " not found");
        } catch (ResourceNotFoundException e) {
            throw new NotFoundException("Release : " + request.getReleaseName() + " not found");
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Creates a new resource release of an existing resource and returns its location.
     *
     * @param request containing a ResourceReleaseCopyDTO
     */
    @POST
    @Path("/{resourceGroupName}")
    @ApiOperation(value = "Create a new Resource Release")
    public Response addNewResourceRelease(@ApiParam("Create a Resource Release") ResourceReleaseCopyDTO request,
                                          @PathParam("resourceGroupName") String resourceGroupName) {
        CopyResourceResult copyResourceResult;
        if (StringUtils.isEmpty(request.getReleaseName())) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Release name must not be empty")).build();
        }
        if (StringUtils.isEmpty(request.getSourceReleaseName())) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Source release name must not be empty")).build();
        }
        try {
            copyResourceResult = copyResource.doCreateResourceRelease(resourceGroupName, request.getReleaseName(),
                    request.getSourceReleaseName(), ForeignableOwner.getSystemOwner());
        } catch (ForeignableOwnerViolationException | AMWException e) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto(e.getMessage())).build();
        }
        if (!copyResourceResult.isSuccess()) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Release creation failed")).build();
        }
        return Response.status(CREATED).header("Location", "/resources/" + copyResourceResult.getTargetResourceName() + "/" + request.getReleaseName()).build();
    }

    /**
     * Copies the properties of a Resource into another
     */
    @Path("/{resourceGroupName}/{releaseName}/copyFrom")
    @PUT
    @ApiOperation(value = "Copy the properties of a Resource into another")
    public Response copyFromResource(@ApiParam(value = "The target ResourceGroup (to)") @PathParam("resourceGroupName") String targetResourceGroupName,
                                     @ApiParam(value = "The target ReleaseName (to)") @PathParam("releaseName") String targetReleaseName,
                                     @ApiParam(value = "The origin ResourceGroup (from)") @QueryParam("originResourceGroupName") String originResourceGroupName,
                                     @ApiParam(value = "The origin ReleaseName (from)") @QueryParam("originReleaseName") String originReleaseName) throws ValidationException {

        try {
            CopyResourceResult copyResourceResult = copyResource.doCopyResource(targetResourceGroupName, targetReleaseName, originResourceGroupName, originReleaseName);
            if (!copyResourceResult.isSuccess()) {
                return Response.status(BAD_REQUEST).entity(new ExceptionDto("Copy from Origin failed")).build();
            }
            return Response.ok().build();
        } catch (ForeignableOwnerViolationException | AMWException e) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto(e.getMessage())).build();
        }
    }

    @Path("/{resourceGroupName}/{releaseName}/dependencies/")
    @GET
    @ApiOperation(value = "Get dependencies of a resource in a specific release")
    public Response getResourceDependencies(@PathParam("resourceGroupName") String resourceGroupName,
                                            @PathParam("releaseName") String releaseName) throws ValidationException {
        ResourceEntity resource = resourceLocator.getResourceByGroupNameAndRelease(resourceGroupName, releaseName);
        if (resource == null) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto("Resource not found")).build();
        }
        List<ConsumedResourceRelationEntity> consumedSlaveRelations = resourceRelationService.getConsumedSlaveRelations(resource);
        List<ProvidedResourceRelationEntity> providedSlaveRelations = resourceRelationService.getProvidedSlaveRelations(resource);
        List<DependencyDTO> resourceRelationDTOs = new ArrayList<>(consumedSlaveRelations.size() + providedSlaveRelations.size());
        for (ConsumedResourceRelationEntity consumedResourceRelationEntity : consumedSlaveRelations) {
            resourceRelationDTOs.add(new DependencyDTO(consumedResourceRelationEntity));
        }
        for (ProvidedResourceRelationEntity providedResourceRelationEntity : providedSlaveRelations) {
            resourceRelationDTOs.add(new DependencyDTO(providedResourceRelationEntity));
        }
        return Response.status(Response.Status.OK).entity(resourceRelationDTOs).build();
    }

    @Path("/resourceGroups")
    @GET
    @ApiOperation(value = "Get all available ResourceGroups without releases - used by Angular")
    public List<ResourceGroupDTO> getAllResourceGroups(@QueryParam("includeAppServerContainer") boolean includeAppServerContainer) {
        List<ResourceGroupEntity> resourceGroups = resourceGroupLocator.getAllResourceGroupsByName();
        List<ResourceGroupDTO> resourceGroupDTOs = new ArrayList<>();
        if (includeAppServerContainer) {
            for (ResourceGroupEntity resourceGroup : resourceGroups) {
                resourceGroupDTOs.add(new ResourceGroupDTO(resourceGroup, null));
            }
        } else {
            for (ResourceGroupEntity resourceGroup : resourceGroups) {
                if (!resourceGroup.isAppServerContainer()) {
                    resourceGroupDTOs.add(new ResourceGroupDTO(resourceGroup, null));
                }
            }
        }
        return resourceGroupDTOs;
    }

    @Path("/resourceGroups/{resourceGroupId}/releases/{releaseId}")
    @GET
    @ApiOperation(value = "Get resource in specific release - used by Angular")
    public ResourceDTO getResourceRelationListForRelease(@PathParam("resourceGroupId") Integer resourceGroupId,
                                                         @PathParam("releaseId") Integer releaseId) throws NotFoundException {

        ResourceEntity resource = resourceDependencyResolverService.getResourceEntityForRelease(resourceGroupId, releaseId);
        if (resource == null) {
            return null;
        }
        List<ResourceRelationDTO> resourceRelationDTOs = new ArrayList<>();
        for (ConsumedResourceRelationEntity consumedResourceRelationEntity : resource.getConsumedMasterRelations()) {
            resourceRelationDTOs.add(new ResourceRelationDTO(consumedResourceRelationEntity));
        }
        return new ResourceDTO(resource, resourceRelationDTOs);
    }

    @Path("/resourceGroups/{resourceGroupId}/releases/{releaseId}")
    @DELETE
    @ApiOperation(value = "Delete a specific resource release")
    public Response deleteResourceRelease(@PathParam("resourceGroupId") Integer resourceGroupId,
                                                            @PathParam("releaseId") Integer releaseId) throws NotFoundException, ElementAlreadyExistsException, ForeignableOwnerViolationException {
        ResourceEntity resource = resourceLocator.getResourceByGroupIdAndRelease(resourceGroupId, releaseId);
        if (resource == null) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto("Resource not found")).build();
        }
        resourceBoundary.removeResource(ForeignableOwner.getSystemOwner(), resource.getId());
        return Response.ok().build();
    }

    @Path("/resourceGroups/{resourceGroupId}/releases/{releaseId}/appWithVersions/")
    @GET
    @ApiOperation(value = "Get application with version for a specific resourceGroup, release and context(s) - used by Angular")
    public Response getApplicationsWithVersionForRelease(@PathParam("resourceGroupId") Integer resourceGroupId,
                                                         @PathParam("releaseId") Integer releaseId,
                                                         @QueryParam("context") List<Integer> contextIds) throws NotFoundException {

        ResourceEntity appServer = resourceLocator.getExactOrClosestPastReleaseByGroupIdAndReleaseId(resourceGroupId, releaseId);
        if (appServer == null) {
            return Response.status(NOT_FOUND).build();
        }
        ReleaseEntity release = releaseLocator.getReleaseById(releaseId);
        List<AppWithVersionDTO> apps = new ArrayList<>();
        List<DeploymentEntity.ApplicationWithVersion> appVersions = deploymentBoundary.getVersions(appServer, contextIds, release);
        for (DeploymentEntity.ApplicationWithVersion appVersion : appVersions) {
            apps.add(new AppWithVersionDTO(appVersion.getApplicationName(), appVersion.getApplicationId(), appVersion.getVersion()));
        }
        return Response.ok(apps).build();

    }

    @Path("/resourceGroups/{resourceGroupId}/releases/")
    @GET
    @ApiOperation(value = "Get deployable releases for a specific resourceGroup - used by Angular")
    public Response getDeployableReleasesForResourceGroup(@PathParam("resourceGroupId") Integer resourceGroupId) {

        ResourceGroupEntity group = resourceGroupLocator.getResourceGroupById(resourceGroupId);
        if (group == null) {
            return Response.status(NOT_FOUND).build();
        }
        List<ResourceDTO> releases = new ArrayList<>();
        List<ReleaseEntity> deployableReleases = releaseMgmtService.getDeployableReleasesForResourceGroup(group);
        for (ReleaseEntity deployableRelease : deployableReleases) {
            releases.add(new ResourceDTO(deployableRelease));
        }
        return Response.ok(releases).build();

    }

    @Path("/resourceGroups/{resourceGroupId}/releases/mostRelevant/")
    @GET
    @ApiOperation(value = "Get most relevant release for a specific resourceGroup - used by Angular")
    public Response getMostRelevantReleaseForResourceGroup(@PathParam("resourceGroupId") Integer resourceGroupId) {
        ResourceGroupEntity group = resourceGroupLocator.getResourceGroupById(resourceGroupId);
        if (group == null) {
            return Response.status(NOT_FOUND).build();
        }
        SortedSet<ReleaseEntity> deployableReleases = new TreeSet(releaseMgmtService.getDeployableReleasesForResourceGroup(group));
        ResourceDTO mostRelevant = new ResourceDTO(resourceDependencyResolverService.findMostRelevantRelease(deployableReleases, null));
        return Response.ok(mostRelevant).build();
    }

}
