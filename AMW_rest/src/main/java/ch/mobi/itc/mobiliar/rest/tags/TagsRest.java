package ch.mobi.itc.mobiliar.rest.tags;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
}
