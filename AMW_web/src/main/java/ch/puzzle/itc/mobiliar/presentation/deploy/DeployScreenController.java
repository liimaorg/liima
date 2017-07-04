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

package ch.puzzle.itc.mobiliar.presentation.deploy;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.entity.CustomFilter;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFilterTypes;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.boundary.DeploymentParameterBoundary;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.DeploymentParameter;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.Key;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonFilterService.SortingDirectionType;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.common.util.Tuple;
import ch.puzzle.itc.mobiliar.presentation.deploy.DeployScreenDataProvider.deployscreenColDescriptor;
import ch.puzzle.itc.mobiliar.presentation.util.UserSettings;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.logging.Logger;

@Named
@RequestScoped
public class DeployScreenController {

    @Inject
    private DeploymentBoundary deploymentBoundary;

    @Inject
    private UserSettings userSettings;

    @Inject
    private ResourceGroupLocator resourceGroupLocator;

    @Inject
    protected Logger log;

    @Inject
    protected DeploymentParameterBoundary deploymentParameterBoundary;

    public Tuple<Set<DeploymentEntity>, Integer> loadPendingDeployments(
            boolean count, Integer startIndex, Integer maxResults,
            List<CustomFilter> filter,
            deployscreenColDescriptor sortingColumn,
            SortingDirectionType sortingDirection) {
        // TODO remove executed

        if (maxResults != null && maxResults == 0) {
            maxResults = null;
        }

        String colToSort = null;
        if (sortingColumn != null) {

            // Mapping zwischen anzeige col und in db zu filternde col
            switch (sortingColumn) {
                case ID:
                    colToSort = DeploymentFilterTypes.ID
                            .getFilterTabColumnName();
                    break;
                case TRACKING_ID:
                    colToSort = DeploymentFilterTypes.TRACKING_ID
                            .getFilterTabColumnName();
                    break;
                case STATE:
                    colToSort = DeploymentFilterTypes.DEPLOYMENT_STATE
                            .getFilterTabColumnName();
                    break;
                case APPSERVER_NAME:
                    colToSort = DeploymentFilterTypes.APPSERVER_NAME
                            .getFilterTabColumnName();
                    break;
                case RELEASE:
                    colToSort = DeploymentFilterTypes.RELEASE
                            .getFilterTabColumnName();
                    break;
                case ENV:
                    colToSort = DeploymentFilterTypes.ENVIRONMENT_NAME
                            .getFilterTabColumnName();
                    break;
                case DEPLOY_TIME:
                    colToSort = DeploymentFilterTypes.DEPLOYMENT_DATE
                            .getFilterTabColumnName();
                    break;
                default:
                    break;
            }
        }

        return deploymentBoundary.getFilteredDeployments(count,
                startIndex, maxResults, filter, colToSort,
                sortingDirection, userSettings.getMyAMWFilter());
    }

    public List<ResourceGroupEntity> loadAppServerGroups() {
        return resourceGroupLocator.getGroupsForType(
                DefaultResourceTypeDefinition.APPLICATIONSERVER.name(),
                userSettings.getMyAMWFilter(), false, true);
    }

    public List<ResourceGroupEntity> loadAppGroups() {
        return resourceGroupLocator.getGroupsForType(
                DefaultResourceTypeDefinition.APPLICATION.name(),
                userSettings.getMyAMWFilter(), false, true);
    }

    public List<ResourceGroupEntity> loadRuntimesGroups() {
        return resourceGroupLocator.getGroupsForType(
                ResourceTypeEntity.RUNTIME,
                userSettings.getMyAMWFilter(), false, true);
    }

    public List<Key> loadAllParameterKeys() {
        return deploymentParameterBoundary.findAllKeys();
    }

    public List<DeploymentParameter> loadAllParameterForDeployment(DeploymentEntity deployment) {
        return deploymentParameterBoundary.findAllDeploymentParameterFor(deployment.getId());
    }

    public List<ApplicationWithVersion> getAppsWithVersion(ResourceEntity appServer, List<Integer> contexts, ReleaseEntity release) {
        List<ApplicationWithVersion> apps = deploymentBoundary.getVersions(appServer, contexts, release);

        Collections.sort(apps, new Comparator<ApplicationWithVersion>() {
            @Override
            public int compare(ApplicationWithVersion app1, ApplicationWithVersion app2) {
                return app1.getApplicationName().toLowerCase().
                        compareTo(app2.getApplicationName().toLowerCase());
            }
        });

        return apps;
    }

    public ResourceGroupEntity getResourceGroupWithResourceRelations(int groupId) {
        return resourceGroupLocator.getResourceGroupForCreateDeploy(groupId);
    }

    public Integer createDeploymentReturnTrackingId(Integer appServerGroupId, Integer releaseId,
                                                    List<Integer> contextIds, Date stateDate,
                                                    Date executionDate,
                                                    List<ApplicationWithVersion> appsWithVersion,
                                                    List<DeploymentParameter> deployParams,
                                                    boolean sendEmail, boolean requestOnly,
                                                    boolean doSimulate, boolean doExecuteShakedownTest,
                                                    boolean doNeighbourhoodTest) {
        return deploymentBoundary.createDeploymentReturnTrackingId(appServerGroupId, releaseId,
                        executionDate, stateDate, contextIds,
                        appsWithVersion, deployParams, sendEmail,
                        requestOnly, doSimulate,
                        doExecuteShakedownTest,
                        doNeighbourhoodTest);
    }

    public Integer createDeploymentReturnTrackingId(List<DeploymentEntity> selectedDeployments,
                                                    List<Integer> contextIds, Date stateDate,
                                                    Date executionDate,
                                                    List<DeploymentParameter> deployParams,
                                                    boolean sendEmail, boolean requestOnly,
                                                    boolean doSimulate, boolean doExecuteShakedownTest,
                                                    boolean doNeighbourhoodTest) {
        return deploymentBoundary
                .createDeploymentsReturnTrackingId(selectedDeployments,
                        executionDate, stateDate, deployParams, contextIds,
                        sendEmail,
                        requestOnly, doSimulate,
                        doExecuteShakedownTest,
                        doNeighbourhoodTest);
    }

    public String getDeploymentLog(String logName) throws IllegalAccessException {
        return deploymentBoundary.getDeploymentLog(logName);
    }


    public String[] getLogFileNames(int deploymentId) {
        return deploymentBoundary.getLogFileNames(deploymentId);
    }

}
