package ch.puzzle.itc.mobiliar.business.shakedown.entity;

import ch.puzzle.itc.mobiliar.business.deploy.entity.FilterType;

public enum ShakedownTestFilterTypes {
    ID("Test Id", QLConstants.SHAKEDOWN_TEST_QL_ALIAS + ".id", FilterType.IntegerType),
    TRACKING_ID("Test tracking Id", QLConstants.SHAKEDOWN_TEST_QL_ALIAS + ".trackingId", FilterType.IntegerType),
    TEST_STATE("Test state", QLConstants.SHAKEDOWN_TEST_QL_ALIAS + ".shakedownTestState", FilterType.StringType),
    APPSERVER_NAME("Application server", QLConstants.GROUP_QL + ".name", FilterType.StringType),
    APPSERVER_RELEASE("Application server release", QLConstants.RELEASE_QL + ".installationInProductionAt", FilterType.LabeledDateType),
    ENVIRONMENT_NAME("Environment", QLConstants.ENV_QL + ".name", FilterType.StringType),
    APPLICATION_NAME("Application", QLConstants.SHAKEDOWN_TEST_QL_ALIAS + ".appsFromAppServer", FilterType.StringType),
    TEST_DATE("Test date", QLConstants.SHAKEDOWN_TEST_QL_ALIAS + ".testDate", FilterType.DateType);

    private String filterDisplayName;
    private String filterTabColName;
    private FilterType filterType;

    public static class QLConstants {
        public static final String SHAKEDOWN_TEST_QL_ALIAS = "s";
        public static final String SHAKEDOWN_ENTITY_QL = "ShakedownTestEntity";
        public static final String GROUP_QL = SHAKEDOWN_TEST_QL_ALIAS + ".resourceGroup";
        public static final String RELEASE_QL = SHAKEDOWN_TEST_QL_ALIAS + ".release";
        public static final String ENV_QL = SHAKEDOWN_TEST_QL_ALIAS + ".context";
    }

    ShakedownTestFilterTypes(String filterDisplayName, String filterTabColName, FilterType filterType) {
        this.filterDisplayName = filterDisplayName;
        this.filterTabColName = filterTabColName;
        this.filterType = filterType;
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
}