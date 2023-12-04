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

import java.net.URI;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;

@Stateless
@Path("/settings/tags")
@Api(value = "/settings/tags")
public class TagsRest {

    @Inject
    PropertyTagEditingService propertyTagEditingService;

    @GET
    @Produces({APPLICATION_JSON})
    @ApiOperation(value = "Gets all tags")
    public Response getAllTags() {
        return Response.status(OK).entity(propertyTagEditingService.loadAllGlobalPropertyTagEntities(false)).build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Adds one tag")
    public Response addOneTag(TagDTO tagDTO) {
        if (tagDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("TagDTO must not be null").build();
        }
        if (tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Tag name must not be null or empty").build();
        }
        PropertyTagEntity newTag = propertyTagEditingService.addPropertyTag(propertyTagEditingService.createPropertyTagEntity(
                tagDTO.getName(),
                PropertyTagType.GLOBAL));
        return Response.status(CREATED).entity(newTag).location(URI.create("/settings/tags/" + newTag.getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(APPLICATION_JSON)
    @ApiOperation(value = "Deletes one tag")
    public Response deleteOneTag(@PathParam("id") int id) {
        propertyTagEditingService.deletePropertyTagById(id);
        return Response.status(OK).build();
    }
}

