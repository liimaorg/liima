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

import ch.mobi.itc.mobiliar.rest.dtos.AddResourceRelationRequestDTO;
import ch.mobi.itc.mobiliar.rest.dtos.GroupedResourceRelationsDTO;
import ch.mobi.itc.mobiliar.rest.dtos.PropertyBulkUpdateDTO;
import ch.mobi.itc.mobiliar.rest.dtos.PropertyDTO;
import ch.mobi.itc.mobiliar.rest.dtos.PropertyExtendedDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ResourceRelationDTO;
import ch.mobi.itc.mobiliar.rest.dtos.UnresolvedRelationDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.boundary.GetRelationPropertiesUseCase;
import ch.puzzle.itc.mobiliar.business.property.boundary.UpdateRelationPropertiesUseCase;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.AddResourceRelationCommand;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.AddResourceRelationUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.GetResourceRelationsUseCase;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.GroupedRelations;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RemoveResourceRelationCommand;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RemoveResourceRelationUseCase;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@RequestScoped
@Path("/resources")
@Tag(name = "/resources/relations", description = "Resource relations by ID")
public class ResourceRelationsByIdRest {

    @Inject
    GetResourceRelationsUseCase getResourceRelationsUseCase;

    @Inject
    GetRelationPropertiesUseCase getRelationPropertiesUseCase;

    @Inject
    UpdateRelationPropertiesUseCase updateRelationPropertiesUseCase;

    @Inject
    AddResourceRelationUseCase addResourceRelationUseCase;

    @Inject
    RemoveResourceRelationUseCase removeResourceRelationUseCase;

    @Inject
    ContextLocator contextLocator;

    @GET
    @Path("/{id : \\d+}/relations")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get grouped relations (runtime, consumed, provided, unresolved) of a resource by ID")
    public Response getResourceRelationsById(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId)
            throws ResourceNotFoundException {

        GroupedRelations grouped = getResourceRelationsUseCase.getGroupedRelations(resourceId);

        GroupedResourceRelationsDTO response = new GroupedResourceRelationsDTO();
        response.setRuntime(toRelationDtos(grouped.getRuntime()));
        response.setConsumed(toRelationDtos(grouped.getConsumed()));
        response.setProvided(toRelationDtos(grouped.getProvided()));
        response.setUnresolved(toUnresolvedDtos(grouped.getUnresolved()));

        return Response.ok(response).build();
    }

    private List<ResourceRelationDTO> toRelationDtos(List<GroupedRelations.RelationGroup> groups) {
        List<ResourceRelationDTO> dtos = new ArrayList<>();
        for (GroupedRelations.RelationGroup group : groups) {
            ResourceRelationDTO dto = new ResourceRelationDTO(group.getBest());
            for (ResourceEditRelation rel : group.getAvailableReleases()) {
                dto.getAvailableReleases().add(
                        new ResourceRelationDTO.RelationReleaseDTO(rel.getResRelId(), rel.getSlaveId(), rel.getSlaveReleaseName()));
            }
            dtos.add(dto);
        }
        dtos.sort(Comparator.comparing(ResourceRelationDTO::getType, String.CASE_INSENSITIVE_ORDER));
        return dtos;
    }

    private List<UnresolvedRelationDTO> toUnresolvedDtos(List<ResourceEditRelation> relations) {
        List<UnresolvedRelationDTO> dtos = new ArrayList<>();
        for (ResourceEditRelation rel : relations) {
            dtos.add(new UnresolvedRelationDTO(rel.getResRelTypeId(), rel.getSlaveTypeName(), rel.getDisplayName(), rel.getTypeIdentifier()));
        }
        dtos.sort(Comparator.comparing(UnresolvedRelationDTO::getName, String.CASE_INSENSITIVE_ORDER));
        return dtos;
    }

    @GET
    @Path("/{id : \\d+}/relations/{relationId : \\d+}/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get properties for a specific resource relation by resource ID and relation ID")
    public Response getRelationProperties(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @Parameter(description = "Relation ID") @PathParam("relationId") Integer relationId,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId)
            throws ResourceNotFoundException, NotFoundException {

        ContextEntity context = contextLocator.getById(contextId);

        List<ResourceEditProperty> properties =
                getRelationPropertiesUseCase.getPropertiesForRelation(resourceId, relationId, contextId);

        List<PropertyExtendedDTO> dtos = new ArrayList<>();
        for (ResourceEditProperty p : properties) {
            dtos.add(new PropertyExtendedDTO(p, context.getName(), contextId, getOverwriteInfos(relationId, contextId, p)));
        }

        return Response.ok(dtos).build();
    }

    private List<PropertyEditingService.DifferingProperty> getOverwriteInfos(Integer relationId, Integer contextId, ResourceEditProperty property) throws ResourceNotFoundException {
        List<ContextEntity> contexts = contextLocator.getChildren(contextId);
        return getRelationPropertiesUseCase.getPropertyOverviewForRelation(relationId, property, contexts);
    }

    @PUT
    @Path("/{id : \\d+}/relations/{relationId : \\d+}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Bulk update/reset property values on a resource relation")
    public Response bulkUpdateRelationProperties(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @Parameter(description = "Relation ID") @PathParam("relationId") Integer relationId,
            PropertyBulkUpdateDTO bulkRequest,
            @Parameter(description = "Context ID") @DefaultValue("1") @QueryParam("contextId") Integer contextId)
            throws ResourceNotFoundException, NotFoundException, ValidationException {

        if (bulkRequest == null || isRequestEmpty(bulkRequest)) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        if (bulkRequest.getUpdates() != null) {
            for (PropertyDTO property : bulkRequest.getUpdates()) {
                if ("relationName".equals(property.getName())) {
                    updateRelationPropertiesUseCase.updateResourceRelationIdentifier(relationId, property.getValue());
                } else {
                    updateRelationPropertiesUseCase.setPropertyOnResourceRelation(
                            relationId, contextId, property.getName(), property.getValue());
                }
            }
        }

        if (bulkRequest.getResets() != null) {
            for (PropertyDTO property : bulkRequest.getResets()) {
                updateRelationPropertiesUseCase.resetPropertyOnResourceRelation(
                        relationId, contextId, property.getName());
            }
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private boolean isRequestEmpty(PropertyBulkUpdateDTO request) {
        boolean updatesEmpty = request.getUpdates() == null || request.getUpdates().isEmpty();
        boolean resetsEmpty = request.getResets() == null || request.getResets().isEmpty();
        return updatesEmpty && resetsEmpty;
    }

    @POST
    @Path("/{id : \\d+}/relations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add a relation to a resource")
    public Response addResourceRelation(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            AddResourceRelationRequestDTO request)
            throws ResourceNotFoundException, ElementAlreadyExistsException {

        AddResourceRelationCommand command = new AddResourceRelationCommand(
                resourceId,
                request.getSlaveResourceGroupId(),
                request.getProvided(),
                request.getRelationName()
        );

        addResourceRelationUseCase.addRelation(command);

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{id : \\d+}/relations/{relationId : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remove a relation from a resource")
    public Response removeResourceRelation(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @Parameter(description = "Relation ID") @PathParam("relationId") Integer relationId)
            throws ResourceNotFoundException {

        RemoveResourceRelationCommand command = new RemoveResourceRelationCommand(relationId);

        removeResourceRelationUseCase.removeRelation(command);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
