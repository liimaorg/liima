package ch.puzzle.itc.mobiliar.business.deploy.entity;


import static ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFilterTypes.QLConstants.*;

public enum DeploymentFilterTypes {
    ID("Id", DEPLOYMENT_QL_ALIAS + ".id", FilterType.IntegerType),
    BUILD_SUCCESS("Build success", DEPLOYMENT_QL_ALIAS + ".buildSuccess", FilterType.booleanType),
    CONFIRMATION_DATE("Confirmed on", DEPLOYMENT_QL_ALIAS + ".deploymentConfirmationDate", FilterType.DateType),
    CONFIRMATION_USER("Confirmed by", DEPLOYMENT_QL_ALIAS + ".deploymentConfirmationUser", FilterType.StringType),
    DEPLOYMENT_CONFIRMED("Confirmed", DEPLOYMENT_QL_ALIAS + ".deploymentConfirmed", FilterType.booleanType),
    DEPLOYMENT_DATE("Deployment date", DEPLOYMENT_QL_ALIAS + ".deploymentDate", FilterType.DateType),
    REQUEST_USER("Requested by", DEPLOYMENT_QL_ALIAS + ".deploymentRequestUser", FilterType.StringType),
    JOB_CREATION_DATE("Created on", DEPLOYMENT_QL_ALIAS + ".deploymentJobCreationDate", FilterType.DateType),
    CANCEL_USER("Canceled by", DEPLOYMENT_QL_ALIAS + ".deploymentCancelUser", FilterType.StringType),
    CANCEL_DATE("Canceled on", DEPLOYMENT_QL_ALIAS + ".deploymentCancelDate", FilterType.DateType),
    DEPLOYMENT_STATE("State", DEPLOYMENT_QL_ALIAS + ".deploymentState", FilterType.ENUM_TYPE),
    DEPLOYMENT_MESSAGE("Message", DEPLOYMENT_QL_ALIAS + ".stateMessage", FilterType.StringType),
    ENVIRONMENT_NAME("Environment", ENV_QL + ".name", FilterType.StringType),
    APPSERVER_NAME("Application server", GROUP_QL + ".name", FilterType.StringType),
    //RELEASE("Release", RELEASE_QL + ".installationInProductionAt", FilterType.LabeledDateType),
    RELEASE("Release", RELEASE_QL + ".installationInProductionAt", FilterType.LabeledDateType),
    APPLICATION_NAME("Application", DEPLOYMENT_QL_ALIAS + ".applicationsWithVersion", FilterType.StringType),
    TARGETPLATFORM("Targetplatform", TARGET_PLATFORM_QL + ".name", FilterType.StringType),
    LASTDEPLOYJOBFORASENV("Latest deployment job for App Server and Env", "", FilterType.SpecialFilterType),
    TRACKING_ID("Tracking Id", DEPLOYMENT_QL_ALIAS + ".trackingId", FilterType.IntegerType),
    DEPLOYMENT_PARAMETER("Deployment parameter", "p.key", "join d.deploymentParameters p", FilterType.StringType),
    DEPLOYMENT_PARAMETER_VALUE("Deployment parameter value", "p.value", "join d.deploymentParameters p", FilterType.StringType);

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

    DeploymentFilterTypes(String filterDisplayName, String filterTabColName, FilterType filterType) {
        this(filterDisplayName, filterTabColName, "", filterType);
    }

    DeploymentFilterTypes(String filterDisplayName, String filterTabColName, String filterTableJoining,
                          FilterType filterType) {
        this.filterDisplayName = filterDisplayName;
        this.filterTabColName = filterTabColName;
        this.filterType = filterType;
        this.filterTableJoining = filterTableJoining;
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
}
