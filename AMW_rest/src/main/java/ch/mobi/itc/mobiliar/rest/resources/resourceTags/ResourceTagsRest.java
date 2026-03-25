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

package ch.mobi.itc.mobiliar.rest.resources.resourceTags;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceTagDTO;
import ch.puzzle.itc.mobiliar.business.configurationtag.control.TagConfigurationService;
import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources", description = "Resource Tags")
public class ResourceTagsRest {

    @Inject
    private TagConfigurationService tagConfigurationService;

    @Inject
    private ResourceLocator resourceLocator;

    @POST
    @Path("/{resourceId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new tag for a resource")
    public Response createTag(
            @Parameter(description = "Resource ID") @PathParam("resourceId") Integer resourceId,
            @Parameter(description = "Tag data") ResourceTagDTO tagDTO) throws NotFoundException {

        CreateResourceTagCommand command = new CreateResourceTagCommand(resourceId, tagDTO);
        ResourceEntity resource = resourceLocator.getResourceById(command.getResourceId());
        if (resource == null)
            throw new NotFoundException("Resource not found for resource id " + command.getResourceId());

        checkIfLabelExists(resource, command);

        return Response.ok(new ResourceTagDTO(tagConfigurationService.tagConfiguration(
                command.getResourceId(),
                command.getResourceTag().getLabel(),
                command.getResourceTag().getTagDate()
        ))).build();
    }

    private void checkIfLabelExists(ResourceEntity resource, CreateResourceTagCommand command) {
        List<ResourceTagEntity> existingTags = tagConfigurationService.loadTagLabelsForResource(resource);
        boolean labelExists = existingTags.stream()
                .anyMatch(tag -> tag.getLabel().trim().equalsIgnoreCase(command.getResourceTag().getLabel().trim()));

        if (labelExists) {
            throw new IllegalArgumentException("Tag '" + command.getResourceTag().getLabel() + "' already exists for resource id " +
                    command.getResourceId());
        }
    }
}
