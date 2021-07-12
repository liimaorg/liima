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

import ch.mobi.itc.mobiliar.rest.dtos.TemplateDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.template.control.TemplatesScreenDomainService;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Rest boundary for Resource-Templates
 */
@RequestScoped
@Path("/resources/{resourceGroupName}/{releaseName}/templates")
@Api(value = "/resources/{resourceGroupName}/{releaseName}/templates", description = "Resource templates")
public class ResourceTemplatesRest {

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    TemplatesScreenDomainService templateService;

    @GET
    @ApiOperation(value = "Get all templates for a resource in a specific release")
    public List<TemplateDTO> getResourceTemplates(@PathParam("resourceGroupName") String resourceGroupName,
                                                @PathParam("releaseName") String releaseName) throws ValidationException {
        return getResourceTemplate(resourceGroupName, releaseName, "");
    }

    //TODO: should only return one object and not a list
    @GET
    @Path("/{templateName}")
    @ApiOperation(value = "Get a template for a resource in a specific release")
    public List<TemplateDTO> getResourceTemplate(@PathParam("resourceGroupName") String resourceGroupName,
                                                 @PathParam("releaseName") String releaseName,
                                                 @PathParam("templateName") String templateName) throws ValidationException {

        ResourceEntity resource = resourceLocator.getResourceByNameAndReleaseWithConsumedRelations(resourceGroupName,
                releaseName);

        List<TemplateDescriptorEntity> templates = templateService.getGlobalTemplateDescriptorsForResource(resource,
                false);

        return getTemplateDTOs(templateName, templates);
    }

    public List<TemplateDTO> getTemplateDTOs(String templateName, List<TemplateDescriptorEntity> templates) {
        List<TemplateDTO> result = new ArrayList<>();
        if ("".equals(templateName)) {
            // all templates, but without file content
            for (TemplateDescriptorEntity template : templates) {
                TemplateDTO temp = new TemplateDTO(template);
                temp.setFileContent("");
                result.add(temp);
            }
        } else {
            for (TemplateDescriptorEntity template : templates) {
                if (templateName.equals(template.getName())) {
                    TemplateDTO temp = new TemplateDTO(template);
                    result.add(temp);
                }
            }
        }
        return result;
    }
}
