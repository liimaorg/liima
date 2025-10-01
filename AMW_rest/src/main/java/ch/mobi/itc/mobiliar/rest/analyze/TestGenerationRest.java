package ch.mobi.itc.mobiliar.rest.analyze;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import ch.mobi.itc.mobiliar.rest.dtos.EnvironmentGenerationResultDTO;
import ch.mobi.itc.mobiliar.rest.exceptions.ExceptionDto;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.EnvironmentGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorDomainServiceWithAppServerRelations;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/analyze")
@Tag(name = "/analyze", description = "Analyze test generation")
public class TestGenerationRest {

    @Inject
    private ResourceLocator resourceLocator;

    @Inject
    private ReleaseLocator releaseLocator;

    @Inject
    private ContextLocator contextLocator;

    @Inject
    private PermissionService permissionService;

    @Inject
    private GeneratorDomainServiceWithAppServerRelations generatorDomainServiceWithAppServerRelations;

    @GET
    @Path("/{resourceGroupName}/{releaseName}/{env}")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Generate test configuration for a resource",
               description = "Generates test configuration for the specified resource group, release, and environment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test generation successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnvironmentGenerationResultDTO.class))),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "422", description = "Template rendering failed - unprocessable entity", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnvironmentGenerationResultDTO.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public Response testGeneration(@Parameter(description = "Resource group name") @PathParam("resourceGroupName") String resourceGroupName,
                                   @Parameter(description = "Release name") @PathParam("releaseName") String releaseName,
                                   @Parameter(description = "Environment name") @PathParam("env") String env) throws IOException, ValidationException {
        ResourceEntity resource = resourceLocator.getResourceByGroupNameAndRelease(resourceGroupName, releaseName);
        if (resource == null) {
            return Response.status(NOT_FOUND).entity(new ExceptionDto("Resource not found")).build();
        }
        ResourceEntity appServer = resource.getResourceType().isApplicationResourceType() ? resourceLocator.getApplicationServerForApplication(resource) : resource;
        Integer releaseId = releaseLocator.getReleaseByName(releaseName).getId();
        ContextEntity context = contextLocator.getContextByName(env);
        permissionService.checkPermissionAndFireException(Permission.RESOURCE_TEST_GENERATION, context, Action.READ, appServer.getResourceGroup(), null, "test generate");
        EnvironmentGenerationResult environmentGenerationResult;
        try {
            environmentGenerationResult = generatorDomainServiceWithAppServerRelations.generateApplicationServerForTest(context.getId(), appServer.getId(), releaseId, null);
        } catch (AMWException e) {
            return Response.status(INTERNAL_SERVER_ERROR).entity(new ExceptionDto(e.getMessage())).build();
        }
        if (environmentGenerationResult.hasErrors()) {
            return Response.status(422).entity(new EnvironmentGenerationResultDTO(environmentGenerationResult)).build();
        }
        return Response.ok(new EnvironmentGenerationResultDTO(environmentGenerationResult)).build();
    }
}
