package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.CopyFromCandidateDTO;
import ch.mobi.itc.mobiliar.rest.dtos.CopyFromReleaseDTO;
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyResource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources", description = "Copy from Resource")
public class CopyFromResourcesRest {

    @Inject
    private ResourceLocator resourceLocator;

    @Inject
    private PermissionBoundary permissionBoundary;

    @Inject
    private CopyResource copyResource;

    @Path("/{resourceId}/copyFromCandidates")
    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get resource groups of the same type as candidates for copy-from - used by Angular")
    public Response getCopyFromCandidates(@PathParam("resourceId") Integer resourceId) {
        ResourceEntity resource = resourceLocator.getResourceById(resourceId);
        if (resource == null) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto("Resource not found")).build();
        }
        if (!permissionBoundary.canCopyFromResource(resource)) {
            return Response.status(Response.Status.FORBIDDEN).entity(new ExceptionDto("Permission denied")).build();
        }
        List<ResourceGroup> groups = copyResource.loadResourceGroupsForType(
                resource.getResourceType().getId(), resource);

        List<CopyFromCandidateDTO> candidates = new ArrayList<>();
        for (ResourceGroup group : groups) {
            LinkedHashMap<String, Integer> releaseMap = group.getReleaseToResourceMap();
            if (releaseMap.isEmpty()) {
                continue;
            }
            // exclude the target resource's own group if only its own release remains
            if (group.getId().equals(resource.getResourceGroup().getId()) && releaseMap.size() == 0) {
                continue;
            }
            List<CopyFromReleaseDTO> releases = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : releaseMap.entrySet()) {
                releases.add(new CopyFromReleaseDTO(null, entry.getKey(), entry.getValue()));
            }
            candidates.add(new CopyFromCandidateDTO(group.getId(), group.getName(), releases));
        }
        return Response.ok(candidates).build();
    }

    @Path("/{targetResourceId}/copyFrom/{originResourceId}")
    @POST
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Copy properties, templates, and relations from one resource to another - used by Angular")
    public Response copyFromResourceById(
            @Parameter(description = "Target resource ID (to)") @PathParam("targetResourceId") Integer targetResourceId,
            @Parameter(description = "Origin resource ID (from)") @PathParam("originResourceId") Integer originResourceId) {
        try {
            CopyResourceResult copyResourceResult = copyResource.doCopyResource(targetResourceId, originResourceId);
            if (!copyResourceResult.isSuccess()) {
                return Response.status(BAD_REQUEST).entity(new ExceptionDto("Copy from resource failed")).build();
            }
            return Response.ok().build();
        } catch (AMWException e) {
            return Response.status(BAD_REQUEST).entity(new ExceptionDto(e.getMessage())).build();
        }
    }
}
