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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import ch.mobi.itc.mobiliar.rest.dtos.AppWithVersionDTO;
import ch.mobi.itc.mobiliar.rest.dtos.DependencyDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ReleaseDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ResourceDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ResourceGroupDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ResourceRelationDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ResourceReleaseCopyDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ResourceReleaseDTO;
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyResource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import ch.puzzle.itc.mobiliar.common.util.NameChecker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources", description = "ResourceGroups")
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
    private ResourceTemplatesRest resourceTemplatesRest;

    @Inject
    private ResourceRelationService resourceRelationService;

    @Inject
    private ch.puzzle.itc.mobiliar.business.releasing.boundary.Releasing releasing;

    @GET
    @Path("/{id : \\d+}")
    @Operation(summary = "Get a resource by id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@Parameter(description = "Resource ID") @PathParam("id") Integer id) throws NotFoundException {
        return Response.ok(new ResourceDTO(resourceBoundary.getResource(id))).build();
    }

    @DELETE
    @Path("/{id : \\d+}")
    @Operation(summary = "Delete a resource by id")
    @Produces(APPLICATION_JSON)
    public Response deleteResourceById(@Parameter(description = "Resource ID") @PathParam("id") Integer id) throws NotFoundException, ElementAlreadyExistsException {
        ResourceEntity resource = resourceLocator.getResourceById(id);
        if (resource == null) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto("Resource not found")).build();
        }
        resourceBoundary.removeResource(id);
        return Response.ok().build();
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get resource groups", description = "Returns the available resource groups")
    public List<ResourceGroupDTO> getResources(
            @Parameter(description = "a resource type name, the list should be filtered by") @QueryParam("type") String type,
            @Parameter(description = "a resource type id, the list should be filtered by") @QueryParam("typeId") Integer typeId) {
        if (type != null && typeId != null) {
            throw new BadRequestException("You cannot filter by both type and typeId at the same");
        };
        if (typeId != null) {
            return getResourceGroupsByResourceTypeId(typeId);
        }
        return getResources(type);
    }

    private List<ResourceGroupDTO> getResources(String type) {
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

    private List<ResourceGroupDTO> getResourceGroupsByResourceTypeId(Integer typeId) {
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

            Map<Integer, ResourceEntity> releaseToResourceMap = new HashMap<>();
            for (ResourceEntity res : resourceGroupEntity.getResources()) {
                releaseToResourceMap.put(res.getRelease().getId(), res);
            }

            var defaultResource = releaseToResourceMap.get(mostRelevantRelease.getId());

            return new ResourceGroupDTO(resourceGroupEntity, mostRelevantRelease, releases, defaultResource);

        }).collect(Collectors.toList());
    }

    @Path("/{resourceGroupName}")
    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get a resource group")
    public ResourceGroupDTO getResourceGroup(@PathParam("resourceGroupName") String resourceGroupName) {
        ResourceGroupEntity resourceGroupByName = resourceGroupLocator.getResourceGroupByName(resourceGroupName);
        List<ReleaseEntity> releases = releaseLocator.getReleasesForResourceGroup(resourceGroupByName);
        return new ResourceGroupDTO(resourceGroupByName, releases);
    }

    @Path("/{resourceGroupName}/{releaseName}")
    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get resource in specific release")
    public ResourceDTO getResource(@PathParam("resourceGroupName") String resourceGroupName,
                                   @PathParam("releaseName") String releaseName,
                                   @QueryParam("env") @DefaultValue("Global") String environment,
                                   @QueryParam("type") String resourceType) throws ValidationException, ResourceNotFoundException {
        ResourceEntity resourceByRelease = resourceLocator.getResourceByGroupNameAndRelease(resourceGroupName, releaseName);
        return new ResourceDTO(resourceByRelease, resourceRelations.getResourceRelations(resourceGroupName,
                releaseName, resourceType), resourceProperties.getPropertiesByResourceGroupNameAndReleaseName(resourceGroupName, releaseName,
                environment), resourceTemplatesRest.getResourceTemplates(resourceGroupName, releaseName));
    }

    @Path("/{resourceGroupName}/lte/{releaseName}")
    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get exact or closest past resource release")
    public ResourceDTO getExactOrClosestPastRelease(@PathParam("resourceGroupName") String resourceGroupName,
                                                    @PathParam("releaseName") String releaseName,
                                                    @QueryParam("env") @DefaultValue("Global") String environment,
                                                    @QueryParam("type") String resourceType) throws ValidationException, NotFoundException {
        ReleaseEntity release = resourceLocator.getExactOrClosestPastReleaseByGroupNameAndRelease(resourceGroupName, releaseName);
        if (release == null) {
            throw new NotFoundException("Release or resource not found");
        }
        return new ResourceDTO(release, resourceRelations.getResourceRelations(resourceGroupName,
                release.getName(), resourceType), resourceProperties.getPropertiesByResourceGroupNameAndReleaseName(resourceGroupName, release.getName(),
                environment), resourceTemplatesRest.getResourceTemplates(resourceGroupName, release.getName()));
    }


    /**
     * Creates a new resource and returns its location.
     *
     * @param request containing a ResourceReleaseDTO
     * @throws AMWException
     */
    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Add a Resource")
    public Response addResource(@Parameter(description = "Add a Resource") ResourceReleaseDTO request) throws AMWException {
        if (StringUtils.isEmpty(request.getName()) || StringUtils.isEmpty(request.getName().trim()))
            throw new ValidationException("Resource name must not be null or blank");

        if (StringUtils.isEmpty(request.getReleaseName()) || StringUtils.isEmpty(request.getReleaseName().trim()))
            throw new ValidationException("Release name must not be null or blank");

        if (!NameChecker.isValidAlphanumericWithUnderscoreHyphenName(request.getName()))
            throw new ValidationException(NameChecker.getErrorTextForInvalidResourceName(
                    (request.getType() != null) ? request.getType() : null, request.getName()));

        try {
            resourceBoundary.createNewResourceByName(request.getName(),
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
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Create a new Resource Release")
    public Response addNewResourceRelease(@Parameter(description = "Create a Resource Release") ResourceReleaseCopyDTO request,
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
                    request.getSourceReleaseName());
        } catch (AMWException e) {
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
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Copy the properties of a Resource into another")
    public Response copyFromResource(@Parameter(description = "The target ResourceGroup (to)") @PathParam("resourceGroupName") String targetResourceGroupName,
                                     @Parameter(description = "The target ReleaseName (to)") @PathParam("releaseName") String targetReleaseName,
                                     @Parameter(description = "The origin ResourceGroup (from)") @QueryParam("originResourceGroupName") String originResourceGroupName,
                                     @Parameter(description = "The origin ReleaseName (from)") @QueryParam("originReleaseName") String originReleaseName) throws ValidationException {

        try {
            CopyResourceResult copyResourceResult = copyResource.doCopyResource(targetResourceGroupName, targetReleaseName, originResourceGroupName, originReleaseName);
            if (!copyResourceResult.isSuccess()) {
                return Response.status(BAD_REQUEST).entity(new ExceptionDto("Copy from Origin failed")).build();
            }
            return Response.ok().build();
        } catch (AMWException e) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto(e.getMessage())).build();
        }
    }

    @Path("/{resourceGroupName}/{releaseName}/dependencies/")
    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get dependencies of a resource in a specific release")
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
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get all available ResourceGroups without releases - used by Angular")
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
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get resource in specific release - used by Angular")
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
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Delete a specific resource release")
    public Response deleteResourceRelease(@PathParam("resourceGroupId") Integer resourceGroupId,
                                          @PathParam("releaseId") Integer releaseId) throws NotFoundException, ElementAlreadyExistsException {
        ResourceEntity resource = resourceLocator.getResourceByGroupIdAndRelease(resourceGroupId, releaseId);
        if (resource == null) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto("Resource not found")).build();
        }
        resourceBoundary.removeResource(resource.getId());
        return Response.ok().build();
    }

    @Path("/resourceGroups/{resourceGroupId}/releases/{releaseId}/appWithVersions/")
    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get application with version for a specific resourceGroup, release and context(s) - used by Angular")
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
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get deployable releases for a specific resourceGroup - used by Angular")
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
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get most relevant release for a specific resourceGroup - used by Angular")
    public Response getMostRelevantReleaseForResourceGroup(@PathParam("resourceGroupId") Integer resourceGroupId) {
        ResourceGroupEntity group = resourceGroupLocator.getResourceGroupById(resourceGroupId);
        if (group == null) {
            return Response.status(NOT_FOUND).build();
        }
        SortedSet<ReleaseEntity> deployableReleases = new TreeSet(releaseMgmtService.getDeployableReleasesForResourceGroup(group));
        ResourceDTO mostRelevant = new ResourceDTO(resourceDependencyResolverService.findMostRelevantRelease(deployableReleases, null));
        return Response.ok(mostRelevant).build();
    }

    @Path("/resourceGroups/releases/{resourceId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all releases related to a specific resource")
    public Response getReleasesForResource(@PathParam("resourceId") Integer resourceId) {
        ResourceEntity resource = resourceLocator.getResourceById(resourceId);
        if (resource == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        ResourceGroupEntity groupEntity = resourceGroupLocator.getResourceGroupById(resource.getResourceGroup().getId());
        if (groupEntity == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ExceptionDto("Resource group not found")).build();
        }
        ResourceGroup resourceGroup = ResourceGroup.createByResource(groupEntity);
        LinkedHashMap<String, Integer> releaseMap = resourceGroup.getReleaseToResourceMap();
        List<ReleaseDTO> releases = releaseMap.entrySet().stream()
                .map(entry -> new ReleaseDTO(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
        return Response.ok(releases).build();
    }

    @Path("/{resourceId}/availableReleases")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get available releases for creating a new resource release - used by Angular")
    public Response getAvailableReleasesForResource(@PathParam("resourceId") Integer resourceId) {
        ResourceEntity resource = resourceLocator.getResourceById(resourceId);
        if (resource == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<ReleaseEntity> availableReleases = releasing.getNotDefinedReleasesForResource(resource);
        List<ReleaseDTO> releaseDTOs = availableReleases.stream()
                .map(release -> new ReleaseDTO(release.getId(), release.getName()))
                .collect(Collectors.toList());
        return Response.ok(releaseDTOs).build();
    }

    @Path("/{resourceId}/release/{releaseId}")
    @PUT
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Change the release of a resource - used by Angular")
    public Response changeResourceRelease(@PathParam("resourceId") Integer resourceId,
                                         @PathParam("releaseId") Integer releaseId) throws NotFoundException {
        releaseMgmtService.changeReleaseOfResource(
            resourceLocator.getResourceById(resourceId),
            releaseLocator.getReleaseById(releaseId)
        );
        return Response.ok().build();
    }
}
