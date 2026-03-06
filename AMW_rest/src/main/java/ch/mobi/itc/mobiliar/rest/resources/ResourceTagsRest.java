/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2026 by Puzzle ITC
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

import ch.mobi.itc.mobiliar.rest.dtos.ResourceTagDTO;
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.configurationtag.control.TagConfigurationService;
import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources", description = "Resource Tags")
public class ResourceTagsRest {

    @Inject
    private TagConfigurationService tagConfigurationService;

    @Inject
    private ResourceLocator resourceLocator;

    @Inject
    private PermissionService permissionService;

    @GET
    @Path("/{resourceId}/tags")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all tags for a resource")
    public Response getTagsForResource(@Parameter(description = "Resource ID") @PathParam("resourceId") Integer resourceId) {
        ResourceEntity resource = resourceLocator.getResourceById(resourceId);
        if (resource == null) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto("Resource not found")).build();
        }

        List<ResourceTagEntity> tags = tagConfigurationService.loadTagLabelsForResource(resource);
        List<ResourceTagDTO> tagDTOs = tags.stream()
                .map(ResourceTagDTO::new)
                .collect(Collectors.toList());

        return Response.ok(tagDTOs).build();
    }

    @POST
    @Path("/{resourceId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new tag for a resource")
    public Response createTag(
            @Parameter(description = "Resource ID") @PathParam("resourceId") Integer resourceId,
            @Parameter(description = "Tag data") ResourceTagDTO tagDTO) {

        if (tagDTO.getLabel() == null || tagDTO.getLabel().trim().isEmpty()) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Tag label must not be empty")).build();
        }

        if (tagDTO.getTagDate() == null) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Tag date must not be empty")).build();
        }

        ResourceEntity resource = resourceLocator.getResourceById(resourceId);
        if (resource == null) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto("Resource not found")).build();
        }

        // Check if tag label already exists for this resource
        List<ResourceTagEntity> existingTags = tagConfigurationService.loadTagLabelsForResource(resource);
        boolean labelExists = existingTags.stream()
                .anyMatch(tag -> tag.getLabel().trim().equals(tagDTO.getLabel().trim()));

        if (labelExists) {
            return Response.status(BAD_REQUEST)
                    .entity(new ExceptionDto("A label with the value '" + tagDTO.getLabel() + "' already exists for this resource."))
                    .build();
        }

        try {
            ResourceTagEntity createdTag = tagConfigurationService.tagConfiguration(
                    resourceId,
                    tagDTO.getLabel(),
                    tagDTO.getTagDate()
            );
            return Response.ok(new ResourceTagDTO(createdTag)).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ExceptionDto("Not authorized to create tags for this resource"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ExceptionDto("Failed to create tag: " + e.getMessage()))
                    .build();
        }
    }

}
