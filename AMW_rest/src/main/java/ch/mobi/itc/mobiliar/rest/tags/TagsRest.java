package ch.mobi.itc.mobiliar.rest.tags;

import ch.mobi.itc.mobiliar.rest.dtos.TagDTO;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

import static javax.ws.rs.core.Response.Status.*;

@Stateless
@Path("/settings/tags")
@Api(value = "/settings/tags")
public class TagsRest {

    @Inject
    PropertyTagEditingService propertyTagEditingService;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets all tags")
    public Response getAllTags() {
        return Response.status(OK).entity(propertyTagEditingService.loadAllGlobalPropertyTagEntities(false)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Adds one tag")
    public Response addOneTag(TagDTO tagDTO) {
        String tagName = tagDTO.getName();
        PropertyTagEntity newTag = new PropertyTagEntity();
        newTag.setName(tagName);
        newTag.setTagType(PropertyTagType.GLOBAL);
        propertyTagEditingService.addPropertyTag(newTag);
        return Response.status(CREATED).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes one tag")
    public Response deleteOneTag(TagDTO tagDTO) {
        String tagName = tagDTO.getName();
        List<PropertyTagEntity> propertyTags=  propertyTagEditingService.loadAllGlobalPropertyTagEntities(false);
        for (PropertyTagEntity tag : propertyTags) {
            if (tag.getName().equals(tagName)) {
                propertyTagEditingService.deletePropertyTagById(tag.getId());
                return Response.status(OK).build();
            }
        }
        return Response.status(NOT_FOUND).entity("Tag not found").build();
    }

}
