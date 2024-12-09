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

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceTypeDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ResourceTypeRequestDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceTypeLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeDomainService;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import ch.puzzle.itc.mobiliar.common.util.NameChecker;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Path("/resources")
@Api(value = "/resources", description = "ResourceTypes")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class ResourceTypesRest {

    @Inject
    private ResourceTypeLocator resourceTypeLocator;

    @Inject
    private ResourceTypeDomainService domainService;

    @Path("/resourceTypes")
    @GET
    @ApiOperation(value = "Get all resource types")
    public List<ResourceTypeDTO> getAllResourceTypes() {
        return resourceTypeLocator.getAllResourceTypes().stream()
                .map(ResourceTypeDTO::new)
                .collect(Collectors.toList());
    }

    @Path("/predefinedResourceTypes")
    @GET
    @ApiOperation(value = "Get predefined resource types")
    public List<ResourceTypeDTO> getPredefinedResourceTypes() {
        return resourceTypeLocator.getPredefinedResourceTypes().stream()
                .map(ResourceTypeDTO::new)
                .collect(Collectors.toList());
    }

    @Path("/rootResourceTypes")
    @GET
    @ApiOperation(value = "Get root resource types")
    public List<ResourceTypeDTO> getRootResourceTypes() {
        return resourceTypeLocator.getRootResourceTypes().stream()
                .map(ResourceTypeDTO::new)
                .collect(Collectors.toList());
    }

    @Path("/resourceTypes")
    @POST
    @ApiOperation(value = "Add a new resource type")
    @Consumes("application/json")
    public Response addNewResourceType(ResourceTypeRequestDTO request)
            throws ElementAlreadyExistsException, ValidationException {

        if (StringUtils.isEmpty(request.getName()) || StringUtils.isEmpty(request.getName().trim())) {
            throw new ValidationException("Resource type name must not be null or blank");
        }

        if (!NameChecker.isValidAlphanumericWithUnderscoreHyphenName(request.getName())) {
            throw new ValidationException(NameChecker.getErrorTextForResourceType(request.getName()));
        }

        try {
            domainService.addResourceType(request.getName(), request.getParentId());
        } catch (ResourceTypeNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
        return Response.status(Response.Status.CREATED).build();
    }

}
