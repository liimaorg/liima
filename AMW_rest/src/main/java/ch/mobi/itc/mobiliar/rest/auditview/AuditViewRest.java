package ch.mobi.itc.mobiliar.rest.auditview;

import ch.mobi.itc.mobiliar.rest.dtos.AuditViewEntryDTO;
import ch.puzzle.itc.mobiliar.business.auditview.boundary.AuditViewBoundary;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.utils.AuditViewEntry;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Path("/auditview")
@Api(value = "/auditview", description = "Managing auditview")
public class AuditViewRest {

    @Inject
    AuditViewBoundary auditViewBoundary;

    @Inject
    PropertyEditor propertyEditor;

    @GET
    @Path("/resource/{id : \\d+}")
    @ApiOperation(value = "Get detail information of a Deployment - used by Angular")
    public Response getAuditLog(@ApiParam("resource ID") @PathParam("id") Integer resourceId,
                                @QueryParam("contextId") Integer contextId) {
        List<AuditViewEntry> auditlogForResource = new ArrayList<>();
        List<ResourceEditProperty> propertiesForResource = propertyEditor.getPropertiesForResource(resourceId, contextId);
        auditlogForResource.addAll(auditViewBoundary.getAuditlogForResource(propertiesForResource));
        List<AuditViewEntryDTO> dtos = createDtos(auditlogForResource);
        return Response.status(Response.Status.OK).entity(dtos).build();
    }

    private List<AuditViewEntryDTO> createDtos(List<AuditViewEntry> auditlogForResource) {
        List<AuditViewEntryDTO> dtos = new ArrayList<>(auditlogForResource.size());
        for (AuditViewEntry auditViewEntry : auditlogForResource) {
            dtos.add(new AuditViewEntryDTO(auditViewEntry));
        }
        return dtos;
    }


}
