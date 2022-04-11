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

package ch.mobi.itc.mobiliar.rest.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import ch.mobi.itc.mobiliar.rest.dtos.BatchJobInventoryDTO;
import ch.mobi.itc.mobiliar.rest.dtos.BatchResourceDTO;
import ch.mobi.itc.mobiliar.rest.dtos.BatchResourceRelationDTO;
import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.server.boundary.ServerView;
import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RequestScoped
@Path("/resources")
@Api(value = "/resources", description = "BatchJobs")
public class BatchJobRest {

    private static final String DB2 = "DB2";
    private static final String ORACLE = "Oracle";
    private static final String WEBSERVICE = "Webservice";
    private static final String FILE = "File";

    @Inject
    private ResourceLocator resourceLocator;

    @Inject
    private ServerView serverView;

    /**
     * Alle Consumed Resources zu dieser App <br>
     * App Details<br>
     * Job name<br>
     * Server<br>
     * consumed resources (short)
     */
    @Path("batchjobResources/{appName}")
    @Produces("application/json")
    @GET
    @ApiOperation(value = "Get batch job resources (only db2, oracle, ws, rest and file) of an app", notes = "Returns the consumed batchJob resources for this app")
    public List<BatchResourceDTO> getBatchJobResources(
            @ApiParam(value = "return resources for this app") @PathParam("appName") String app)
            throws ValidationException {
        if (app == null || app.isEmpty()) {
            throw new ValidationException("App must not be empty");
        }
        List<String> apps = new ArrayList<>();
        apps.add(app);
        List<BatchResourceDTO> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        for (ResourceEntity re : resourceLocator.getBatchJobConsumedResources(apps)) {
            for (ConsumedResourceRelationEntity r : re.getConsumedMasterRelations()) {
                mapResourceDTO(list, set, r.getSlaveResource().getResourceType().getId(),
                        r.getSlaveResource().getName(), true);
            }
        }
        set.clear();
        for (ResourceEntity re : resourceLocator.getBatchJobProvidedResources(apps)) {
            for (ProvidedResourceRelationEntity r : re.getProvidedMasterRelations()) {
                mapResourceDTO(list, set, r.getSlaveResource().getResourceType().getId(),
                        r.getSlaveResource().getName(), false);
            }
        }
        return list;
    }

    void mapResourceDTO(List<BatchResourceDTO> list, Set<String> set, int id, String name, Boolean consumed) {
        BatchResourceDTO dto = new BatchResourceDTO();
        dto.setConsumed(consumed);
        if (id == Constants.RESOURCETYPE_DB2) {
            dto.setType(DB2);
        }
        if (id == Constants.RESOURCETYPE_ORACLE) {
            dto.setType(ORACLE);
        }
        if (id == Constants.RESOURCETYPE_WS || id == Constants.RESOURCETYPE_REST) {
            dto.setType(WEBSERVICE);
        }
        if (id == Constants.RESOURCETYPE_FILE) {
            dto.setType(FILE);
        }
        if (!set.contains(name)) {
            set.add(name);
            dto.setName(name);
            list.add(dto);
        }
    }

    /**
     * Alle Apps mit Resourcen vom Typ 'batchjob' lesen: <br>
     * App Details<br>
     * Job name<br>
     * Server<br>
     * consumed resources (short)
     */
    @Path("resource/{envName}/{resourceTypeId : \\d+}")
    @Produces("application/json")
    @GET
    @ApiOperation(value = "Get batch job inventory", notes = "Returns the available batch applications and their related resources (only db2, oracle, ws, rest and file)")
    public BatchJobInventoryDTO getBatchJobInventar(
            @ApiParam(value = "return batch job inventory") @PathParam("envName") String env,
            @ApiParam(value = "id of the BatchJob resource type") @PathParam("resourceTypeId") int resourceType,
            @ApiParam(value = "Filter by Applicationname or -part") @QueryParam("app") String appFilter,
            @ApiParam(value = "Filter by Resource or -part") @QueryParam("job") String jobFilter,
            @ApiParam(value = "Filter by Release") @QueryParam("rel") String relFilter,
            @ApiParam(value = "Filter by consumed DB type") @QueryParam("db") String dbFilter,
            @ApiParam(value = "Filter by consumed WS name or part") @QueryParam("ws") String wsFilter) {
        BatchJobInventoryDTO inventory = new BatchJobInventoryDTO();

        // Applications und ihre Resourcen lesen
        List<ResourceEntity> reList = resourceLocator.getAllApplicationsWithResource(resourceType);

        List<String> appList = new ArrayList<>();
        for (ResourceEntity re : reList) {
            for (ConsumedResourceRelationEntity a : re.getConsumedMasterRelations()) {
                BatchResourceRelationDTO job = new BatchResourceRelationDTO(a);
                // filter by appname or part of name
                if (appFilter != null && !appFilter.isEmpty() && job.getBatchApp() != null) {
                    if (!job.getBatchApp().toLowerCase().contains(appFilter.toLowerCase())) {
                        // do not add to list
                        break;
                    }
                }

                // filter by release (must match either in app or in job)
                if (relFilter != null && !relFilter.isEmpty() &&                               //
                        (job.getBatchAppRelease() != null)) { // || job.getBatchJobRelease() != null)) {
                    if (!job.getBatchAppRelease().toLowerCase().contains(relFilter.toLowerCase())) {
                        // job-Release Filter ist nicht relevant, da dort der Release nur auf Stufe 'standardJob' wirkt
                        // || job.getBatchJobRelease().toLowerCase().contains(relFilter.toLowerCase()))) {
                        // do not add to list
                        break;
                    }
                }

                inventory.getBatchJobs().add(job);
                if (!appList.contains(job.getBatchApp())) {
                    appList.add(job.getBatchApp().toLowerCase());
                }

            }
        }

        // filter by jobname or part
        List<BatchResourceRelationDTO> jobsToRemove = new ArrayList<>();
        for (BatchResourceRelationDTO job : inventory.getBatchJobs()) {
            if (jobFilter != null && !jobFilter.isEmpty()) {
                if (job.getJobName() == null) {
                    // remove from list
                    jobsToRemove.add(job);
                } else if (!job.getJobName().toLowerCase().contains(jobFilter.toLowerCase())) {
                    // remove from list
                    jobsToRemove.add(job);
                }
            }
        }
        for (BatchResourceRelationDTO deleteMe : jobsToRemove) {
            inventory.getBatchJobs().remove(deleteMe);
        }

        // Alle 'JavaBatch' Server lesen
        String appServer = null; // alle lesen
        String runtime = "JavaBatch*"; // aber nur Batch
        String host = null;
        String node = null;
        List<ServerTuple> servers = serverView.getServers(host, appServer, runtime, node, env, true);
        List<String> appServerList = new ArrayList<>();
        for (ServerTuple t : servers) {
            appServerList.add(t.getAppServer().toLowerCase());
        }

        // Mapping von fofa_batch nach ch_mobi_fofa_fofa_selection, funktioniert nur falls Server zugeordnet
        // Map contains: <ch_mobi_fofa_fofa_selection, fofa_batch>
        Map<String, String> mapping = resourceLocator.getAppToAppServerMapping(appServerList);
        for (BatchResourceRelationDTO job : inventory.getBatchJobs()) {
            String appName = mapping.get(job.getBatchApp());
            job.setAppName(appName);
        }

        for (BatchResourceRelationDTO job : inventory.getBatchJobs()) {
            for (ServerTuple server : servers) {
                if (job.getAppName() != null && job.getAppName().equals(server.getAppServer())) {
                    job.setBatchServer(server.getHost());
                }
            }
        }

        // Flags fuer verwendete Ressourcen: DB2, Oracle, WS und File ermitteln
        List<ResourceEntity> resources = resourceLocator.getBatchJobConsumedResources(appList);
        if (resources != null && !resources.isEmpty()) {
            for (BatchResourceRelationDTO job : inventory.getBatchJobs()) {
                for (ResourceEntity re : resources) {

                    for (ConsumedResourceRelationEntity r : re.getConsumedMasterRelations()) {

                        if (r.getMasterResourceName().equals(job.getBatchApp())) {
                            if (r.getSlaveResourceTypeId().equals(Constants.RESOURCETYPE_DB2)) {
                                job.setDb2(true);
                            }
                            if (r.getSlaveResourceTypeId().equals(Constants.RESOURCETYPE_ORACLE)) {
                                job.setOracle(true);
                            }
                            if (r.getSlaveResourceTypeId().equals(Constants.RESOURCETYPE_WS)
                                    || r.getSlaveResourceTypeId().equals(Constants.RESOURCETYPE_REST)) {
                                job.setWs(true);
                                if (!job.getWsList().contains(r.getSlaveResource().getName())) {
                                    job.getWsList().add(r.getSlaveResource().getName());
                                }
                            }
                            if (r.getSlaveResourceTypeId().equals(Constants.RESOURCETYPE_FILE)) {
                                job.setFile(true);
                            }
                        }
                    }
                }
            }
        }

        // filter by used db (Empty, DB2 or Oracle from Constant Dropdown)
        // filter by consumed WS
        List<BatchResourceRelationDTO> deleteJobs = new ArrayList<>();
        for (BatchResourceRelationDTO job : inventory.getBatchJobs()) {

            if (dbFilter != null && !dbFilter.isEmpty()) {
                if (dbFilter.equalsIgnoreCase(DB2) && !job.getDb2()) {
                    // remove from result
                    deleteJobs.add(job);
                }
                if (dbFilter.equalsIgnoreCase(ORACLE) && !job.getOracle()) {
                    // remove from result
                    deleteJobs.add(job);
                }
            }
            if (wsFilter != null && !wsFilter.isEmpty()) {
                boolean found = false;
                for (String ws : job.getWsList()) {
                    if (ws.toLowerCase().contains(wsFilter)) {
                        found = true;
                    }
                }
                if (!found) {
                    if (!deleteJobs.contains(job)) {
                        deleteJobs.add(job);
                    }
                }
            }
        }
        for (BatchResourceRelationDTO job : deleteJobs) {
            inventory.getBatchJobs().remove(job);
        }

        return inventory;
    }


}
