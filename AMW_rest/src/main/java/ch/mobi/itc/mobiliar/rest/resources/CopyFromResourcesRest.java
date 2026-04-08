package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.CopyFromCandidateDTO;
import ch.mobi.itc.mobiliar.rest.dtos.CopyFromResourceRequestDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.*;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Path("/resources")
@Tag(name = "/resources", description = "Copy from Resource")
public class CopyFromResourcesRest {

    @Inject
    private GetResourceUseCase getResourceUseCase;

    @Inject
    private GetCandidatesToCopyFromResourceUseCase getCandidatesToCopyFromResourceUseCase;

    @Inject
    private CopyFromResourceUseCase copyFromResourceUseCase;

    @Inject
    private PermissionBoundary permissionBoundary;

    @Path("/{resourceId}/copyFromCandidates")
    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get resource groups of the same type as candidates for copy-from - used by Angular")
    public Response getCopyFromCandidates(@PathParam("resourceId") Integer resourceId) throws ResourceNotFoundException {
        ResourceIdCommand resourceIdCommand = new ResourceIdCommand(resourceId);
        ResourceEntity resource = getResourceUseCase.getResourceById(resourceIdCommand);
        permissionBoundary.assertPermission(() -> permissionBoundary.canCopyFromResource(resource));

        List<ResourceGroup> groups = getCandidatesToCopyFromResourceUseCase.getCandidates(resource);

        return Response.ok(CopyFromCandidateDTO.from(resource, groups)).build();
    }

    @Path("/copyFrom")
    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Copy properties, templates, and relations from one resource to another - used by Angular")
    public Response copyFromResource(
            @RequestBody(description = "Copy from resource request", required = true) @Valid CopyFromResourceRequestDTO request) {

        CopyFromResourceCommand command = new CopyFromResourceCommand(request.getTargetResourceId(), request.getOriginResourceId());
        copyFromResourceUseCase.copyFromResource(command);
        return Response.ok().build();
    }
}
