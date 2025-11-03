/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.mobi.itc.mobiliar.rest.servers;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.AppServerRelationsTemplateProcessor;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.server.boundary.ServerView;
import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestScoped
@Path("/hostNames")
@Tag(name = "/hostNames", description = "Set hostnames on nodes and application server relations")
public class HostNamesRest {

	@Inject
	PropertyEditor propertyEditor;

	@Inject
	private ServerView serverView;

	@PUT
	@Path("/{node}/{release}/{env}")
	@Consumes(TEXT_PLAIN)
	@Produces(APPLICATION_JSON)
	@Operation(summary = "Set hostname value on a given node in a specific node release and environment")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Hostname set successfully"),
		@ApiResponse(responseCode = "400", description = "Validation error")
	})
	public Response setHostNameOnNode(@Parameter(description = "node name") @PathParam("node") String nodeName,
			@Parameter(description = "release of node") @PathParam("release") String nodeRelease,
			@Parameter(description = "environment") @PathParam("env") String environmentName,
			@Parameter(description = "value of the hostname") String hostNameValue) throws ValidationException {
		
		String hostName = AppServerRelationsTemplateProcessor.HOST_NAME;
		propertyEditor.setPropertyValueOnResourceForContext(nodeName, nodeRelease, environmentName, hostName,
				hostNameValue);
		return Response.status(Response.Status.OK).build();
	}

	@PUT
	@Path("/{node}/{release}/{env}/{appServer}")
	@Consumes(TEXT_PLAIN)
	@Produces(APPLICATION_JSON)
	@Operation(summary = "Set hostname value on all relations between a node release and all releases of an application server where no hostname is defined yet.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Hostnames set successfully on all applicable relations"),
		@ApiResponse(responseCode = "400", description = "Validation error")
	})
	public Response setHostNameBetweenAppServerAndNodesForAllReleasesWhereNotYetDefined(
			@Parameter(description = "node name") @PathParam("node") String nodeName,
			@Parameter(description = "release of node") @PathParam("release") String nodeRelease,
			@Parameter(description = "environment") @PathParam("env") String environmentName,
			@Parameter(description = "application server name") @PathParam("appServer") String appServerName,
			@Parameter(description = "value of the hostname") String hostNameValue) throws ValidationException {
		
		String hostName = AppServerRelationsTemplateProcessor.HOST_NAME;
		propertyEditor.setPropertyValueOnAllResourceRelationsForContextWhereNotYetSet(appServerName, nodeName, nodeRelease, environmentName, hostName, hostNameValue);
		return Response.status(Response.Status.OK).build();
	}

	@PUT
	@Path("/{node}/{release}/{env}/{appServer}/{asRelease}")
	@Consumes(TEXT_PLAIN)
	@Produces(APPLICATION_JSON)
	@Operation(summary = "Set hostname value on the relation between a node release and an application server release.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Hostname set successfully on the specific relation"),
		@ApiResponse(responseCode = "400", description = "Validation error")
	})
	public Response setHostNameBetweenAppServerAndNodesForSpecifiedReleases(
			@Parameter(description = "node name") @PathParam("node") String nodeName,
			@Parameter(description = "release of node") @PathParam("release") String nodeRelease,
			@Parameter(description = "environment") @PathParam("env") String environmentName,
			@Parameter(description = "application server name") @PathParam("appServer") String appServerName,
			@Parameter(description = "application server release") @PathParam("asRelease") String appServerRelease,
			@Parameter(description = "value of the hostname") String hostNameValue) throws ValidationException {

		String hostName = AppServerRelationsTemplateProcessor.HOST_NAME;
		propertyEditor.setPropertyValueOnResourceRelationForContext(appServerName, appServerRelease, nodeName, nodeRelease, environmentName, hostName, hostNameValue);
		return Response.status(Response.Status.OK).build();
	}

    // Old method kept for backward compatibility with old rest clients
    @GET
    @Operation(summary = "Get hostnames", description = "Returns all hostnames matching the optional filter Query Params")
    @Produces({ APPLICATION_JSON, "text/csv" })
    public List<ServerTuple> getHostNames(
            @Parameter(description = "Application server name") @QueryParam("appServer") String appServer,
            @Parameter(description = "Runtime name") @QueryParam("runtime") String runtime,
            @Parameter(description = "Environment name") @QueryParam("environment") String environment,
            @Parameter(description = "Host name") @QueryParam("host") String host,
            @Parameter(description = "Node name") @QueryParam("node") String node,
            @Parameter(description = "Merge releases") @QueryParam("disableMerge") @DefaultValue("false") boolean disableMerge) {
        return serverView.getServers(host, appServer, runtime, node, environment, !disableMerge);
    }

}
