package ch.mobi.itc.mobiliar.rest.servers;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceGroupDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupPersistenceService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.server.boundary.ServerView;
import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Stateless
@Tag(name = "/servers", description = "Get servers/hostNames from nodes and application server relations")
@Path("/servers")
public class ServersRest {

    @Inject
    ResourceGroupPersistenceService resourceGroupPersistenceService;

    @Inject
    ResourceGroupLocator resourceGroupLocator;

    @Inject
    ServerView serverView;

    @GET
    @Path("/runtimes")
    @Operation(summary = "Get runtimes")
    @Produces(APPLICATION_JSON)
    public List<ResourceGroupDTO> getRuntimes() {
        return resourceGroupPersistenceService.loadGroupsForTypeName(DefaultResourceTypeDefinition.RUNTIME.name())
                .stream()
                .map(resourceGroupEntity -> new ResourceGroupDTO(resourceGroupEntity, new ArrayList<>()))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/appServersSuggestions")
    @Operation(summary = "Get app servers suggestions")
    @Produces(APPLICATION_JSON)
    public List<String> getAppServersSuggestions() {
        return resourceGroupLocator
                .getGroupsForType(DefaultResourceTypeDefinition.APPLICATIONSERVER.name(), false, true)
                .stream()
                .map(ResourceGroupEntity::getName)
                .collect(Collectors.toList());
    }

    @GET
    @Operation(summary = "Get all servers")
    @Produces({ APPLICATION_JSON, "text/csv" })
    public List<ServerTuple> getServers() {
        return serverView.getAllServers();
    }

    // the same as HostNamesRest#getHostNames
    @GET
    @Path("/filter")
    @Operation(summary = "Get servers with params")
    @Produces({ APPLICATION_JSON, "text/csv" })
    public List<ServerTuple> getServers(
            @Parameter(description = "Application server name") @QueryParam("appServer") String appServer,
            @Parameter(description = "Runtime name") @QueryParam("runtime") String runtime,
            @Parameter(description = "Environment name") @QueryParam("environment") String environment,
            @Parameter(description = "Host name") @QueryParam("host") String host,
            @Parameter(description = "Node name") @QueryParam("node") String node,
            @Parameter(description = "Merge releases") @QueryParam("disableMerge") @DefaultValue("false") boolean disableMerge) {
        return serverView.getServers(host, appServer, runtime, node, environment, true);
    }

}
