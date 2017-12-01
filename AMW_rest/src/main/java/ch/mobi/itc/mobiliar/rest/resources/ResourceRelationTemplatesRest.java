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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import ch.mobi.itc.mobiliar.rest.dtos.TemplateDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.ResourceRelationLocator;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.template.boundary.TemplateEditor;
import ch.puzzle.itc.mobiliar.business.template.control.TemplatesScreenDomainService;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import ch.puzzle.itc.mobiliar.common.exception.GeneralDBException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

/**
 * Rest boundary for Resource-Relation-Templates
 */
@RequestScoped
@Path("/resources/{resourceGroupName}/{releaseName}/relations/{relatedResourceGroupName}/{relatedReleaseName}/templates")
@Api(value = "/resources/{resourceGroupName}/{releaseName}/relations/{relatedResourceGroupName}/{relatedReleaseName}/templates", description = "Resource relation templates")
public class ResourceRelationTemplatesRest {

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    ResourceRelationLocator resourceRelationLocator;

    @Inject
    TemplateEditor templateEditor;

    @Inject
    TemplatesScreenDomainService templateService;

    @Inject
    ResourceTemplatesRest resourceTemplatesRest;

    @GET
    @ApiOperation(value = "Get one or all templates for a resource in a specific release")
    public List<TemplateDTO> getResourceRelationTemplates( //
                                                           @PathParam("resourceGroupName") String resourceGroupName,
                                                           @PathParam("releaseName") String releaseName,
                                                           @PathParam("relatedResourceGroupName") String relatedResourceGroupName,
                                                           @PathParam("relatedReleaseName") String relatedReleaseName) throws ValidationException {

        return getResourceRelationTemplates(resourceGroupName, releaseName, relatedResourceGroupName, relatedReleaseName, "");
    }

    @GET
    @Path("/{templateName}")
    @ApiOperation(value = "Get one or all templates for a resource in a specific release")
    public List<TemplateDTO> getResourceRelationTemplates( //
                                                           @PathParam("resourceGroupName") String resourceGroupName,
                                                           @PathParam("releaseName") String releaseName,
                                                           @PathParam("relatedResourceGroupName") String relatedResourceGroupName,
                                                           @PathParam("relatedReleaseName") String relatedReleaseName,
                                                           @PathParam("templateName") String templateName) throws ValidationException {

        List<ConsumedResourceRelationEntity> resRelList = resourceRelationLocator
                .getResourceRelationList(resourceGroupName, releaseName, relatedResourceGroupName, relatedReleaseName);

        List<TemplateDescriptorEntity> templates = new ArrayList<>();
        for (ConsumedResourceRelationEntity resRel : resRelList) {
            try {
                List<TemplateDescriptorEntity> temp = templateService.getTemplatesForResourceRelation(resRel, false);
                for (TemplateDescriptorEntity t : temp) {
                    t.setRelatedResourceIdentifier(resRel.getIdentifier());
                }
                templates.addAll(temp);
            } catch (ResourceNotFoundException | GeneralDBException e) {
                // ignore
            }
        }

        return resourceTemplatesRest.getTemplateDTOs(templateName, templates);
    }

}
