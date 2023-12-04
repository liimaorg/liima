package ch.mobi.itc.mobiliar.rest.tags;

import ch.mobi.itc.mobiliar.rest.dtos.TagDTO;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
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
    private ListTagsUseCase listTagsUseCase;

    @Inject
    private AddTagUseCase addTagUseCase;

    @Inject
    private RemoveTagUseCase removeTagUseCase;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets all tags")
    public Response getAllTags() {
        return Response
                .status(OK)
                .entity(listTagsUseCase.get())
                .build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Adds one tag")
    public Response addOneTag(TagDTO tagDTO) throws ValidationException {
        TagCommand tagCommand = new TagCommand(tagDTO);
        PropertyTagEntity newTag = addTagUseCase.addTag(tagCommand);
        return Response
                .status(CREATED)
                .entity(newTag)
                .location(URI.create("/settings/tags/" + newTag.getId()))
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(APPLICATION_JSON)
    @ApiOperation(value = "Deletes one tag")
    public Response deleteOneTag(@PathParam("id") int id) {
        removeTagUseCase.removeTag(id);
        return Response.status(OK).build();
    }
}

