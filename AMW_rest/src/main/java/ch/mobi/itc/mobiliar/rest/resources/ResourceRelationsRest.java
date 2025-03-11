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
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RelationEditor;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.ResourceRelationLocator;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.ws.rs.core.Response.Status.*;

@RequestScoped
@Path("/resources/relations")
@Api(value = "/resources/{resourceGroupName}/{releaseName}/relations", description = "Resource relations")
public class ResourceRelationsRest {

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    ResourceGroupLocator resourceGroupLocator;

    @Inject
    ReleaseLocator releaseLocator;

    @Inject
    ResourceRelationLocator resourceRelationLocator;

    @Inject
    RelationEditor relationEditor;

    @Inject
    ResourceRelationTemplatesRest resourceRelationTemplatesRest;

    @GET
    @Path("/{resourceGroupName}/{releaseName}")
    @ApiOperation(value = "Get all relations of a resource in a specific release, optionally filtered by a slave resource type")
    public List<ResourceRelationDTO> getResourceRelations(@PathParam("resourceGroupName") String resourceGroupName,
                                                          @PathParam("releaseName") String releaseName,
                                                          @ApiParam(value = "A String representing the type of the slave Resource")
                                                          @QueryParam("type") String resourceType) throws ValidationException, ResourceNotFoundException {

        return getResourceRelationsByRelease(resourceGroupName, releaseName, resourceType);
    }

    List<ResourceRelationDTO> getResourceRelationsByRelease(String resourceGroupName, String releaseName, String resourceType) throws ValidationException, ResourceNotFoundException {
        ResourceEntity resource = resourceLocator.getResourceByNameAndReleaseWithAllRelations(resourceGroupName, releaseName);
        List<ResourceRelationDTO> resourceRelations = new ArrayList<>();
        for (ConsumedResourceRelationEntity relation : resource.getConsumedMasterRelations()) {
            ResourceRelationDTO resRel = createResourceRelationDTO(resourceGroupName, releaseName, resourceType, relation);
            if (resRel != null) {
                resourceRelations.add(resRel);
            }
        }
        for (ProvidedResourceRelationEntity relation : resource.getProvidedMasterRelations()) {
            ResourceRelationDTO resRel = createResourceRelationDTO(resourceGroupName, releaseName, resourceType, relation);
            if (resRel != null) {
                resourceRelations.add(resRel);
            }
        }
        return resourceRelations;
    }

    @GET
    @Path("/{resourceGroupId}")
    public List<ResourceRelationDTO> getLatestReleaseResourceRelations(@PathParam("resourceGroupId") Integer resourceGroupId, @QueryParam("resourceType") String resourceType) throws ValidationException, ResourceNotFoundException {

        ResourceGroupEntity resourceGroup = resourceGroupLocator.getResourceGroupById(resourceGroupId);
        ReleaseEntity release = releaseLocator.getLatestReleaseForResourceGroup(resourceGroup);

        return getResourceRelationsByRelease(resourceGroup.getName(), release.getName(), resourceType);
    }

    private ResourceRelationDTO createResourceRelationDTO(String resourceGroupName, String releaseName, String resourceType, AbstractResourceRelationEntity relation) throws ValidationException, ResourceNotFoundException {
        if (resourceType != null && !relation.getResourceRelationType().getResourceTypeB().getName().equals(resourceType)) {
            return null;
        }
        ResourceRelationDTO resRel = new ResourceRelationDTO(relation);
        List<TemplateDTO> templates = resourceRelationTemplatesRest.getResourceRelationTemplates(resourceGroupName, releaseName,
                relation.getSlaveResource().getName(), relation.getSlaveResource().getRelease().getName());
        addTemplates(resRel, templates);
        return resRel;
    }

    @GET
    @Path("/{resourceGroupName}/{releaseName}/{relatedResourceGroupName}")
    @ApiOperation(value = "Get all related releases of the given resource")
    public List<ResourceRelationDTO> getRelatedResourcesForGroup(@PathParam("resourceGroupName") String resourceGroupName,
                                                                 @PathParam("releaseName") String releaseName, @PathParam("relatedResourceGroupName") String relatedResourceGroupName) throws ValidationException, ResourceNotFoundException {
        List<ConsumedResourceRelationEntity> relations =
                resourceRelationLocator.getRelatedResourcesForGroup(resourceGroupName, releaseName, relatedResourceGroupName);
        List<ResourceRelationDTO> resourceRelations = new ArrayList<>();
        for (ConsumedResourceRelationEntity relation : relations) {
            ResourceRelationDTO resRel = new ResourceRelationDTO(relation);
            List<TemplateDTO> templates = resourceRelationTemplatesRest.getResourceRelationTemplates(resourceGroupName, releaseName,
                    relatedResourceGroupName, relation.getSlaveResource().getRelease().getName());
            addTemplates(resRel, templates);
            resourceRelations.add(resRel);
        }
        return resourceRelations;
    }

    // List of ResourceRelationDTO
    @GET
    @Path("/{resourceGroupName}/{releaseName}/{relatedResourceGroupName}/{relatedReleaseName}")
    @ApiOperation(value = "Get the list of relations between the two resource releases")
    public List<ResourceRelationDTO> getResourceRelation(@PathParam("resourceGroupName") String resourceGroupName,
                                                         @PathParam("releaseName") String releaseName,
                                                         @PathParam("relatedResourceGroupName") String relatedResourceGroupName,
                                                         @PathParam("relatedReleaseName") String relatedReleaseName) throws ValidationException, ResourceNotFoundException {
        List<ResourceRelationDTO> list = new ArrayList<>();
        for (ConsumedResourceRelationEntity dto : resourceRelationLocator.getResourceRelationList(resourceGroupName, releaseName,
                relatedResourceGroupName, relatedReleaseName)) {
            ResourceRelationDTO resRel = new ResourceRelationDTO(dto);
            List<TemplateDTO> templates = resourceRelationTemplatesRest.getResourceRelationTemplates(resourceGroupName, releaseName,
                    relatedResourceGroupName, relatedReleaseName);
            addTemplates(resRel, templates);
            list.add(resRel);
        }
        return list;
    }

    @GET
    @Path("/resource/{resourceId}")
    @ApiOperation(value = "Get resource relations by resource id")
    public List<ResourceRelationDTO> getRelationsByResourceId(@PathParam("resourceId") Integer resourceId) throws ResourceNotFoundException {
        ResourceEntity resource = resourceLocator.getResourceById(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException("Resource not found.");
        }

        List<ResourceRelationDTO> resourceRelations = new ArrayList<>();
        for (ConsumedResourceRelationEntity relation : resource.getConsumedMasterRelations()) {
            ResourceRelationDTO dto = new ResourceRelationDTO(relation);
            resourceRelations.add(dto);
        }
        for (ProvidedResourceRelationEntity relation : resource.getProvidedMasterRelations()) {
            ResourceRelationDTO dto = new ResourceRelationDTO(relation);
            resourceRelations.add(dto);
        }
        return resourceRelations;
    }

    /**
     * Creates a new ResourceRelation
     *
     * @param slaveGroupName A String representing the name of an existing ResourceGroup which will be added as slave resource of the new ResourceRelation
     * @param relationType   A String representing the type of the Relation to be added
     */

    @POST
    @Path("/{resourceGroupName}/{releaseName}/{slaveResourceGroupName}")
    @ApiOperation(value = "Add a consumed or provided Relation")
    public Response addRelation(@PathParam("resourceGroupName") String resourceGroupName,
                                @PathParam("releaseName") String releaseName,
                                @PathParam("slaveResourceGroupName") String slaveGroupName,
                                @ApiParam(value = "A String representing the type of the Relation", required = true)
                                @QueryParam("type") String relationType) {
        if (StringUtils.isEmpty(slaveGroupName)) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Slave resource group name must not be empty")).build();
        } else if (!relationEditor.isValidResourceRelationType(relationType)) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Type must either be 'consumed' or 'provided'")).build();
        }
        boolean isProvidedRelation = RelationEditor.ResourceRelationType.valueOf(relationType.toUpperCase()).equals(RelationEditor.ResourceRelationType.PROVIDED);
        try {
            relationEditor.addResourceRelationForSpecificRelease(resourceGroupName, slaveGroupName,
                    isProvidedRelation, null, relationType, releaseName, ForeignableOwner.getSystemOwner());
        } catch (ResourceNotFoundException | ElementAlreadyExistsException | ValidationException e) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto(e.getMessage())).build();
        }
        return Response.status(CREATED).build();
    }

    /**
     * Removes a new ResourceRelation
     *
     * @param relationName A String representing the identifier of a Relation or the name of the related Resource to be removed
     * @param relationType A String representing the type of the Relation to be removed
     */
    @DELETE
    @Path("/{resourceGroupName}/{releaseName}/{relationName}")
    @ApiOperation(value = "Remove a consumed or provided Relation from a specific Release", notes = "RelationName may be the identifier of a Relation or the name of the related Resource")
    public Response removeRelation(@PathParam("resourceGroupName") String resourceGroupName,
                                   @PathParam("releaseName") String releaseName,
                                   @PathParam("relationName") String relationName,
                                   @ApiParam(value = "A String representing the type of the Relation", required = true)
                                   @QueryParam("type") String relationType) throws ValidationException {
        if (StringUtils.isEmpty(relationName)) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Relation name must not be empty")).build();
        } else if (!relationEditor.isValidResourceRelationType(relationType)) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto("Type must either be 'consumed' or 'provided'")).build();
        }
        RelationEditor.ResourceRelationType resourceRelationType = RelationEditor.ResourceRelationType.valueOf(relationType.toUpperCase());

        Set<? extends AbstractResourceRelationEntity> relations = getRelations(resourceGroupName, releaseName, resourceRelationType);

        try {
            if (relationEditor.removeMatchingRelation(relations, relationName)) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(NOT_FOUND).entity(new ExceptionDto("No matching relation found")).build();
            }

        } catch (ForeignableOwnerViolationException fe) {
            return Response.status(UNAUTHORIZED).entity(new ExceptionDto(fe.getMessage())).build();
        } catch (ResourceNotFoundException | ElementAlreadyExistsException e) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto(e.getMessage())).build();
        }
    }

    private Set<? extends AbstractResourceRelationEntity> getRelations(String resourceGroupName, String releaseName, RelationEditor.ResourceRelationType resourceRelationType) throws ValidationException {
        Set<? extends AbstractResourceRelationEntity> relations = new HashSet<>();
        if (resourceRelationType.equals(RelationEditor.ResourceRelationType.CONSUMED)) {
            ResourceEntity resource = resourceLocator.getResourceByNameAndReleaseWithConsumedRelations(resourceGroupName, releaseName);
            relations = resource.getConsumedMasterRelations();
        } else if (resourceRelationType.equals(RelationEditor.ResourceRelationType.PROVIDED)) {
            ResourceEntity resource = resourceLocator.getResourceByNameAndReleaseWithProvidedRelations(resourceGroupName, releaseName);
            relations = resource.getProvidedMasterRelations();
        }
        return relations;
    }

    private void addTemplates(ResourceRelationDTO resRel, List<TemplateDTO> templates) {
        List<TemplateDTO> templatesToAdd = new ArrayList<>();
        for (TemplateDTO temp : templates) {
            String tempName;
            if (temp.getRelatedResourceIdentifier() == null) {
                //eg standardJob
                tempName = resRel.getRelatedResourceName();
            } else {
                //eg standardJob_1
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
