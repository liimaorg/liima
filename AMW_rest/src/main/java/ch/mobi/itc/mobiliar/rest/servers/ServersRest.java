package ch.mobi.itc.mobiliar.rest.servers;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceGroupDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ResourceTypeDTO;
import ch.mobi.itc.mobiliar.rest.dtos.ServerDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupPersistenceService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.server.boundary.GetServersUseCase;
import ch.puzzle.itc.mobiliar.business.server.boundary.ServerView;
import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;

@Stateless
@Path("/servers")
@Tag(name = "/servers")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class ServersRest {

    @Inject
    GetServersUseCase getServersUseCase;

    @Inject
    ResourceGroupPersistenceService resourceGroupPersistenceService;

    @Inject
    ResourceGroupLocator resourceGroupLocator;


    @Inject
    ServerView serverView;

    @GET
    @Operation(summary = "Get servers")
    public Response getServers() {
        return Response.status(OK).entity(getServersUseCase.all()).build();
    }

    @GET
    @Path("/runtimes")
    @Operation(summary = "Get runtimes")
    public List<ResourceGroupDTO> getRuntimes() {
        return resourceGroupPersistenceService.loadGroupsForTypeName(DefaultResourceTypeDefinition.RUNTIME.name())
                .stream()
                .map(resourceGroupEntity -> new ResourceGroupDTO(resourceGroupEntity, new ArrayList<>()))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/appServersSuggestions")
    @Operation(summary = "Get app servers suggestions")
    public List<String> getAppServersSuggestions() {
        return resourceGroupLocator.getGroupsForType(DefaultResourceTypeDefinition.APPLICATIONSERVER.name(), false, true)
                .stream()
                .map(ResourceGroupEntity::getName)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/filter")
    @Operation(summary = "Get servers with params")
    public Response getServers(@QueryParam("environment") String environment,
                              @QueryParam("runtime") String runtime,
                              @QueryParam("appServer") String appServer,
                               @QueryParam("host") String host,
                              @QueryParam("node") String node) {

        List<ServerDTO> servers =  serverView.getServers(host,appServer,runtime, node, environment,true).stream().map(ServerDTO::new).collect(Collectors.toList());

        return Response.status(OK).entity(servers).build();
    }
}
