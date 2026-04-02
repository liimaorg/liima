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

import ch.mobi.itc.mobiliar.rest.dtos.ResourceRelationDTO;
import ch.mobi.itc.mobiliar.rest.dtos.TemplateDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources/relations", description = "Resource relations by ID")
public class ResourceRelationsByIdRest {

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    ResourceRelationTemplatesRest resourceRelationTemplatesRest;

    @GET
    @Path("/{id : \\d+}/relations")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all relations of a resource by ID")
    public Response getResourceRelationsById(
            @Parameter(description = "Resource ID") @PathParam("id") Integer resourceId,
            @Parameter(description = "Optional filter by slave resource type") @QueryParam("type") String resourceType)
            throws ValidationException, ResourceNotFoundException {

        ResourceEntity resource = resourceLocator.getResourceById(resourceId);
        if (resource == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<ResourceRelationDTO> resourceRelations = new ArrayList<>();

        for (ConsumedResourceRelationEntity relation : resource.getConsumedMasterRelations()) {
            ResourceRelationDTO resRel = createResourceRelationDTO(resource, resourceType, relation);
            if (resRel != null) {
                resourceRelations.add(resRel);
            }
        }

        for (ProvidedResourceRelationEntity relation : resource.getProvidedMasterRelations()) {
            ResourceRelationDTO resRel = createResourceRelationDTO(resource, resourceType, relation);
            if (resRel != null) {
                resourceRelations.add(resRel);
            }
        }

        return Response.ok(resourceRelations).build();
    }

    private ResourceRelationDTO createResourceRelationDTO(ResourceEntity resource, String resourceType,
                                                          ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity relation)
            throws ValidationException, ResourceNotFoundException {
        if (resourceType != null && !relation.getResourceRelationType().getResourceTypeB().getName().equals(resourceType)) {
            return null;
        }
        ResourceRelationDTO resRel = new ResourceRelationDTO(relation);
        List<TemplateDTO> templates = resourceRelationTemplatesRest.getResourceRelationTemplates(
                resource.getName(),
                resource.getRelease().getName(),
                relation.getSlaveResource().getName(),
                relation.getSlaveResource().getRelease().getName());
        addTemplates(resRel, templates);
        return resRel;
    }

    private void addTemplates(ResourceRelationDTO resRel, List<TemplateDTO> templates) {
        List<TemplateDTO> templatesToAdd = new ArrayList<>();
        for (TemplateDTO temp : templates) {
            String tempName;
            if (temp.getRelatedResourceIdentifier() == null) {
                tempName = resRel.getRelatedResourceName();
            } else {
                tempName = resRel.getRelatedResourceName() + "_" + temp.getRelatedResourceIdentifier();
            }
            if (tempName.equals(resRel.getRelationName())) {
                templatesToAdd.add(temp);
            }
        }
        resRel.setTemplates(new ArrayList<TemplateDTO>());
        resRel.getTemplates().addAll(templatesToAdd);
    }
}
