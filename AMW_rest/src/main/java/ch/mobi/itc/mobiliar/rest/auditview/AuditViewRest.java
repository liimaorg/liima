package ch.mobi.itc.mobiliar.rest.auditview;

import ch.puzzle.itc.mobiliar.business.auditview.boundary.AuditViewBoundary;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Stateless
@Path("/auditview")
@Api(value = "/auditview", description = "Managing auditview")
public class AuditViewRest {

    @Inject
    AuditViewBoundary auditViewBoundary;

    @GET
    @Path("/resource/{id : \\d+}/detail")
    @ApiOperation(value = "Get detail information of a Deployment - used by Angular")
    public Response getAuditLog(@ApiParam("resource ID") @PathParam("id") Integer resourceId) {
//        List<AuditViewEntry> auditlogForResource = auditViewBoundary.getAuditlogForResource(resourceId);
        String auditlogForResource = "Some content";
        return Response.status(Response.Status.PRECONDITION_FAILED).entity(auditlogForResource).build();
    }


}
