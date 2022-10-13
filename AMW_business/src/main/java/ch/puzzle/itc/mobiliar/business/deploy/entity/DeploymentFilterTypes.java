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

package ch.puzzle.itc.mobiliar.business.deploy.entity;

import java.util.Set;

import static ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFilterTypes.QLConstants.*;

public enum DeploymentFilterTypes {
    ID("Id", DEPLOYMENT_QL_ALIAS + ".id", FilterType.IntegerType, true),
    BUILD_SUCCESS("Build success", DEPLOYMENT_QL_ALIAS + ".buildSuccess", FilterType.booleanType, true),
    CONFIRMATION_DATE("Confirmed on", DEPLOYMENT_QL_ALIAS + ".deploymentConfirmationDate", FilterType.DateType, true),
    CONFIRMATION_USER("Confirmed by", DEPLOYMENT_QL_ALIAS + ".deploymentConfirmationUser", FilterType.StringType, true),
    DEPLOYMENT_CONFIRMED("Confirmed", DEPLOYMENT_QL_ALIAS + ".deploymentConfirmed", FilterType.booleanType, true),
    DEPLOYMENT_DATE("Deployment date", DEPLOYMENT_QL_ALIAS + ".deploymentDate", FilterType.DateType, true),
    REQUEST_USER("Requested by", DEPLOYMENT_QL_ALIAS + ".deploymentRequestUser", FilterType.StringType, true),
    JOB_CREATION_DATE("Created on", DEPLOYMENT_QL_ALIAS + ".deploymentJobCreationDate", FilterType.DateType, true),
    CANCEL_USER("Canceled by", DEPLOYMENT_QL_ALIAS + ".deploymentCancelUser", FilterType.StringType, true),
    CANCEL_DATE("Canceled on", DEPLOYMENT_QL_ALIAS + ".deploymentCancelDate", FilterType.DateType, true),
    DEPLOYMENT_STATE("State", DEPLOYMENT_QL_ALIAS + ".deploymentState", FilterType.ENUM_TYPE, true),
    DEPLOYMENT_MESSAGE("Message", DEPLOYMENT_QL_ALIAS + ".stateMessage", FilterType.StringType, true),
    ENVIRONMENT_NAME("Environment", ENV_QL + ".name", FilterType.StringType, true),
    APPSERVER_NAME("Application server", GROUP_QL + ".name", FilterType.StringType, true),
    RELEASE("Release", RELEASE_QL + ".installationInProductionAt", FilterType.LabeledDateType, true),
    APPLICATION_NAME("Application", DEPLOYMENT_QL_ALIAS + ".applicationsWithVersion", FilterType.StringType, true),
    TARGETPLATFORM("Targetplatform", TARGET_PLATFORM_QL + ".name", FilterType.StringType, true),
    LASTDEPLOYJOBFORASENV("Latest deployment job for App Server and Env", "", FilterType.SpecialFilterType, true),
    TRACKING_ID("Tracking Id", DEPLOYMENT_QL_ALIAS + ".trackingId", FilterType.IntegerType, true),
    DEPLOYMENT_PARAMETER("Deployment parameter", "p.key", "join d.deploymentParameters p", FilterType.StringType, true),
    DEPLOYMENT_PARAMETER_VALUE("Deployment parameter value", "p.value", "join d.deploymentParameters p", FilterType.StringType, true),
    DEPLOYMENT_REASON("Reason", DEPLOYMENT_QL_ALIAS + ".reason", FilterType.ENUM_TYPE, true),
    ENVIRONMENT_EX("ExEnvironment", DEPLOYMENT_QL_ALIAS + ".exContextId", FilterType.IntegerType, false);


    public final static Set<DeploymentFilterTypes> DEPLOYMENT_FILTER_TYPES_FOR_ORDER = Set.of(TRACKING_ID, DEPLOYMENT_STATE, APPSERVER_NAME, RELEASE, ENVIRONMENT_NAME, DEPLOYMENT_DATE);

    public static class QLConstants {
        public static final String DEPLOYMENT_QL_ALIAS = "d";
        public static final String GROUP_QL = DEPLOYMENT_QL_ALIAS + ".resourceGroup";
        public static final String ENV_QL = DEPLOYMENT_QL_ALIAS + ".context";
        public static final String RELEASE_QL = DEPLOYMENT_QL_ALIAS + ".release";
        public static final String TARGET_PLATFORM_QL = DEPLOYMENT_QL_ALIAS + ".runtime";
    }

    private final String filterDisplayName;
    private final String filterTabColName;
    private final String filterTableJoining;
    private final FilterType filterType;
    private final boolean selectable;

    DeploymentFilterTypes(String filterDisplayName, String filterTabColName, FilterType filterType, boolean selectable) {
        this(filterDisplayName, filterTabColName, "", filterType, selectable);
    }

    DeploymentFilterTypes(String filterDisplayName, String filterTabColName, String filterTableJoining,
                          FilterType filterType, boolean selectable) {
        this.filterDisplayName = filterDisplayName;
        this.filterTabColName = filterTabColName;
        this.filterType = filterType;
        this.filterTableJoining = filterTableJoining;
        this.selectable = selectable;
    }

    public String getFilterDisplayName() {
        return filterDisplayName;
    }

    public String getFilterTabColumnName() {
        return filterTabColName;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public String getFilterTableJoining() {
        return filterTableJoining;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public static DeploymentFilterTypes getByDisplayName(String displayName) {
        for (DeploymentFilterTypes filterType : values()) {
            if (filterType.getFilterDisplayName().equals(displayName)) {
                return filterType;
            }
        }
        return null;
    }

}
