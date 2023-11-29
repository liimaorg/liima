package ch.mobi.itc.mobiliar.rest.tags;

import ch.mobi.itc.mobiliar.rest.dtos.TagDTO;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;

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
        //todo:validate request (tagDTO null and tagName null)
        propertyTagEditingService.addPropertyTag(propertyTagEditingService.createPropertyTagEntity(tagDTO.getName(), PropertyTagType.GLOBAL));
        return Response.status(CREATED).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes one tag")
    public Response deleteOneTag(@PathParam("id") int id) {
        propertyTagEditingService.deletePropertyTagById(id);
        return Response.status(OK).build();
    }
}

