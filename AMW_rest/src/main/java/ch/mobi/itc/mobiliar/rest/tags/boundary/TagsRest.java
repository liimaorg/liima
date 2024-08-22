package ch.mobi.itc.mobiliar.rest.tags.boundary;

import ch.mobi.itc.mobiliar.rest.dtos.TagDTO;
import ch.puzzle.itc.mobiliar.business.property.boundary.AddTagUseCase;
import ch.puzzle.itc.mobiliar.business.property.boundary.ListTagsUseCase;
import ch.puzzle.itc.mobiliar.business.property.boundary.RemoveTagUseCase;
import ch.puzzle.itc.mobiliar.business.property.boundary.TagCommand;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;

@Stateless
@Path("/settings/tags")
@Api(value = "/settings/tags")
@Consumes(APPLICATION_JSON)
@Produces({APPLICATION_JSON})
public class TagsRest {

    @Inject
    private ListTagsUseCase listTagsUseCase;

    @Inject
    private AddTagUseCase addTagUseCase;

    @Inject
    private RemoveTagUseCase removeTagUseCase;

    @GET
    @ApiOperation(value = "Gets all tags")
    public Response getAllTags() {
        return Response.status(OK).entity(listTagsUseCase.get()).build();
    }

    @POST
    @ApiOperation(value = "Adds one tag")
    public Response addOneTag(
            @NotNull(message = "Tag must not be null.") TagDTO tagDTO) throws ValidationException {

        TagCommand tagCommand = new TagCommand(tagDTO.getName());
        PropertyTagEntity newTag = addTagUseCase.addTag(tagCommand);
        return Response
                .status(CREATED)
                .entity(newTag)
                .location(URI.create("/settings/tags/" + newTag.getId()))
                .build();
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Deletes one tag")
    public Response deleteOneTag(@PathParam("id") int id) {
        removeTagUseCase.removeTag(id);
        return Response.status(OK).build();
    }
}

