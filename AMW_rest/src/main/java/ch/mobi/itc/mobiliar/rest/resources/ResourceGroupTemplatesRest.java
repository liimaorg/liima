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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import ch.mobi.itc.mobiliar.rest.dtos.TemplateDTO;
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.ResourceRelationLocator;
import ch.puzzle.itc.mobiliar.business.template.boundary.TemplateEditor;
import ch.puzzle.itc.mobiliar.business.template.control.TemplatesScreenDomainService;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Rest boundary for Resource-Templates
 */
@RequestScoped
@Path("/resources/resourceGroups/{resourceGroupId}/releases/{releaseId}/templates")
@Api(value = "/resources/resourceGroups/{resourceGroupId}/releases/{releaseId}/templates", description = "Resource templates")
public class ResourceGroupTemplatesRest {

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    ResourceRelationLocator resourceRelationLocator;

    @Inject
    TemplateEditor templateEditor;

    @Inject
    TemplatesScreenDomainService templateService;

    @Inject
    ResourceDependencyResolverService resourceDependencyResolverService;

    @GET
    @ApiOperation(value = "Get all templates for a resource in a specific release")
    public Response getResourceTemplates(@PathParam("resourceGroupId") Integer resourceGroupId,
                                         @PathParam("releaseId") Integer releaseId)
                                         throws ValidationException {
        List<TemplateDTO> result = new ArrayList<>();
        ResourceEntity resource = resourceDependencyResolverService.getResourceEntityForRelease(resourceGroupId, releaseId);
        if (resource == null) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto("Resource not found")).build();
        }

        List<TemplateDescriptorEntity> templates = templateService.getGlobalTemplateDescriptorsForResource(resource, false);
        for (TemplateDescriptorEntity template : templates) {
            result.add(new TemplateDTO(template));
        }

        return Response.ok(result).build();
    }

}
