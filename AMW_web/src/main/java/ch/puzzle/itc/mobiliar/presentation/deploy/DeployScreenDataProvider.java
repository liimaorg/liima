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
import ch.puzzle.itc.mobiliar.business.deploy.entity.*;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.Key;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonFilterService.SortingDirectionType;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceEditService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.NamedIdentifiable;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.common.exception.DeploymentStateException;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.Tuple;
import ch.puzzle.itc.mobiliar.presentation.common.ContextDataProvider;
import ch.puzzle.itc.mobiliar.presentation.common.ReleaseSelectionDataProvider;
import ch.puzzle.itc.mobiliar.presentation.components.impl.CustomFilterComp;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Named
@SessionScoped
public class DeployScreenDataProvider implements Serializable {

    private static final int DEFAULT_ITEMS_PER_PAGE = 10;
    private static final String COMMA = ",";
    private static final long serialVersionUID = -6418412617050387765L;
    @Getter
    private String filterApplicationNameViewParam;
    @Getter
    private String filterAppServerNameViewParam;
    @Getter
    private String filterEnvironmentNameParam;
    @Getter
    private String filterTrackingIdParam;


    public enum DeploymentAction {
        CHANGE_DATE("Change Date"),
        CONFIRM("Confirm"),
        REJECT("Reject"),
        CANCEL("Cancel");

        private String label;

        DeploymentAction(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
        // TODO check Action.U
    }

    /*
     * Falls enum angepasst oder refactored wird, so muss dies zwingend im
     * deploy.xhtml nachgetragen werden!
     */
    public enum deployscreenColDescriptor {
        ID, STATE, APPSERVER_NAME, RELEASE, ENV, DEPLOY_TIME, TRACKING_ID
    }

    private static final String DEFAULT = "default";

    // Deployments
    @Getter
    @Setter
    private List<DeploymentEntity> pendingDeployments;
    private DeploymentEntity selectedDeployment;
    private Integer selectedDeploymentId;
    @Getter
    @Setter
    private Map<Integer, Boolean> checkedDeployments = new HashMap<>();
    private int countAllDeployments;
    @Getter
    @Setter
    private Date deploymentDate;
    @Getter
    @Setter
    private DeploymentAction bulkAction;
    @Getter
    private Boolean toggleDeploymentsSelection;
    @Setter
    @Getter
    private boolean simulateGeneration;
    @Setter
    @Getter
    private boolean sendEmail;
    @Setter
    @Getter
    private boolean sendEmailConfirmation;
    @Setter
    @Getter
    private boolean executeShakedownTest;
    @Setter
    @Getter
    private boolean neighbourhoodTest;


    //Filter lists, lazy loaded
    private List<ResourceGroupEntity> applicationGroups = new LinkedList<>();
    private List<ResourceGroupEntity> applicationServerGroups = new LinkedList<>();
    private SortedSet<ReleaseEntity> releases = new TreeSet<>();
    private List<ResourceGroupEntity> runtimesGroups = new LinkedList<>();
    private List<Key> allDeploymentParameterKeys = new LinkedList<>();

    // Filters and sorting
    private DeploymentFilterTypes selectedFilter;

    // defaultmässig soll nach deployment datum absteigend sortiert werden
    // solange nichts anderes gewählt wird vom user
    private SortingDirectionType sortingDirection = SortingDirectionType.DESC;
    private deployscreenColDescriptor sortingColumn = deployscreenColDescriptor.DEPLOY_TIME;

    private static final boolean useAngular = !Boolean.parseBoolean(ConfigurationService.getProperty(ConfigurationService.ConfigKey.FEATURE_DISABLE_ANGULAR_GUI));

    @Getter
    @Setter
    private Long selectedDate;

    // Pagination
    private int currentPage;
    private int itemsPerPage;


    @Inject
    ReleaseSelectionDataProvider releaseDataProvider;

    @Inject
    DeployScreenController controller;

    @Inject
    ResourceEditService resourceEditService;

    @Inject
    ContextDataProvider contextDataProvider;

    @Inject
    DeploymentBoundary deploymentBoundary;

    @Inject
    PermissionService permissionService;

    @Inject
    ReleaseMgmtService releaseMgmtService;

    @Inject
    Logger log;

    /**
     * Initialize view through prerenderview event on xhtml page
     */
    public void initView() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            initFilterFromViewParameter();
            reloadDeployments(true);
        }
    }

    public void setFilterApplicationNameViewParam(String filterApplicationNameViewParam) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            this.filterApplicationNameViewParam = filterApplicationNameViewParam;
        }
    }

    public void setFilterAppServerNameViewParam(String filterAppServerNameViewParam) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            this.filterAppServerNameViewParam = filterAppServerNameViewParam;
        }
    }

    public void setFilterEnvironmentNameParam(String filterEnvironmentNameParam) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            this.filterEnvironmentNameParam = filterEnvironmentNameParam;
        }
    }

    public void setFilterTrackingIdParam(String filterTrackingIdParam) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            this.filterTrackingIdParam = filterTrackingIdParam;
        }
    }

    private void initFilterFromViewParameter() {
        if (hasFilterViewParameter()) {
            // reset previous filterselection
            getCustomFilterComp().removeAllFilter();
        }

        if (hasFilterViewParam(filterAppServerNameViewParam)) {
            creatAndAddNewFilter(DeploymentFilterTypes.APPSERVER_NAME, filterAppServerNameViewParam);
            filterAppServerNameViewParam = null;
        }

        if (hasFilterViewParam(filterApplicationNameViewParam)) {
            creatAndAddNewFilter(DeploymentFilterTypes.APPLICATION_NAME, filterApplicationNameViewParam);
            filterApplicationNameViewParam = null;
        }

        if (hasFilterViewParam(filterEnvironmentNameParam)) {
            creatAndAddNewFilter(DeploymentFilterTypes.ENVIRONMENT_NAME, filterEnvironmentNameParam);
            filterEnvironmentNameParam = null;
        }

        if (hasFilterViewParam(filterTrackingIdParam)) {
            creatAndAddNewFilter(DeploymentFilterTypes.TRACKING_ID, filterTrackingIdParam);
            filterTrackingIdParam = null;
        }
    }

    private boolean hasFilterViewParam(String filterViewParam) {
        return filterViewParam != null && !filterViewParam.isEmpty();
    }

    private boolean hasFilterViewParameter() {
        return hasFilterViewParam(filterApplicationNameViewParam) || hasFilterViewParam(filterAppServerNameViewParam) || hasFilterViewParam(filterEnvironmentNameParam) || hasFilterViewParam(filterTrackingIdParam);
    }

    private void creatAndAddNewFilter(DeploymentFilterTypes deploymentFilterType, String value) {
        CustomFilter filter = CustomFilter.builder(deploymentFilterType).build();
        filter.setValue(value);
        getCustomFilterComp().getSelectedFilterList().add(filter);
    }

    public boolean isAngularEnabled() {
        return useAngular;
    }

    class MyComparator implements Comparator<DeploymentFilterTypes> {
        @Override
        public int compare(DeploymentFilterTypes a, DeploymentFilterTypes b) {

            return a.getFilterDisplayName().compareTo(b.getFilterDisplayName());
        }
    }

    @Getter
    private CustomFilterComp customFilterComp = new CustomFilterCompImpl();


    private class CustomFilterCompImpl extends CustomFilterComp {


        @Override
        public void addSelectedFilter() {

            selectedFilter = getSelectedEnumType(getSelectedFilterItemEnumName());

            if (selectedFilter != null) {
                CustomFilter filter;
                filter = CustomFilter.builder(selectedFilter).build();
                if (selectedFilter.getFilterType().equals(FilterType.IntegerType)) {
                    filter.setComparatorSelection(ComparatorFilterOption.equals);
                } else {
                    ComparatorFilterOption comperatorSelection = filter.getTypedComparatorSelectionList().isEmpty() ?
                            null :
                            filter.getTypedComparatorSelectionList().get(0);
                    filter.setComparatorSelection(comperatorSelection);
                }
                if (selectedFilter.equals(DeploymentFilterTypes.LASTDEPLOYJOBFORASENV) && hasAlreadySpecialTypeFilter(getSelectedFilterList())) {
                    GlobalMessageAppender.addErrorMessage("This filter is already set.");
                } else if (selectedFilter.equals(DeploymentFilterTypes.RELEASE)) {
                    filter = setFilterDropDownOptions(filter);
                    // select the most relevant one (nearest future or most recent past)
                    filter.setValue(CustomFilter.convertDateToString(releaseDataProvider.getUpcomingReleaseDate()));
                    releaseDataProvider.reset();
                    getSelectedFilterList().add(filter);
                } else if (selectedFilter.equals(DeploymentFilterTypes.DEPLOYMENT_PARAMETER)) {
                    filter = setFilterDropDownOptions(filter);
                    filter.setAlwaysAutoComplete(true);
                    getSelectedFilterList().add(filter);
                } else {
                    filter = setFilterDropDownOptions(filter);
                    getSelectedFilterList().add(filter);
                }
            }
            selectedFilter = null;
        }

        private DeploymentFilterTypes getSelectedEnumType(String selectedFilter) {
            DeploymentFilterTypes result = null;
            for (DeploymentFilterTypes filterType : DeploymentFilterTypes.values()) {
                if (filterType.name().equals(selectedFilter)) {
                    result = filterType;
                    break;
                }
            }

            return result;
        }

        @Override
        protected void loadFilterList() {
            List<FilterSelectionItem> filterSelectionItemList = new ArrayList<>();
            for (DeploymentFilterTypes filterType : DeploymentFilterTypes.values()) {
                filterSelectionItemList.add(new FilterSelectionItem(filterType.name(), filterType.getFilterDisplayName()));
            }
            setFilterSelectionList(filterSelectionItemList);
        }

        private <K extends NamedIdentifiable> List<String> converToStringList(List<K> namedIdentifiables) {
            ArrayList<String> stringList = new ArrayList<>();

            for (K namedIdentifiable : namedIdentifiables) {
                stringList.add(namedIdentifiable.getName());
            }
            return stringList;
        }

        @Override
        protected CustomFilter setFilterDropDownOptions(CustomFilter filter) {


            if (filter.getFilterDisplayName().equals(DeploymentFilterTypes.APPSERVER_NAME.getFilterDisplayName())) {
                filter.setDropDownItems(converToStringList(getApplicationServerGroups()));
            } else if (filter.getFilterDisplayName().equals(DeploymentFilterTypes.ENVIRONMENT_NAME.getFilterDisplayName())) {
                ArrayList<String> envs = new ArrayList<>();
                for (ContextEntity ctx : contextDataProvider.getEnvironments()) {
                    envs.add(ctx.getName());
                }
                Collections.sort(envs);
                filter.setDropDownItems(envs);
            } else if (filter.getFilterDisplayName().equals(DeploymentFilterTypes.APPLICATION_NAME.getFilterDisplayName())) {
                filter.setDropDownItems(converToStringList(getApplicationGroups()));
            } else if (filter.getFilterDisplayName().equals(DeploymentFilterTypes.TARGETPLATFORM.getFilterDisplayName())) {
                filter.setDropDownItems(converToStringList(getRuntimesGroups()));
            } else if (filter.getFilterDisplayName().equals(DeploymentFilterTypes.DEPLOYMENT_STATE.getFilterDisplayName())) {
                TreeMap<String, String> states = new TreeMap<>();
                for (DeploymentState state : DeploymentState.values()) {
                    states.put(state.getDisplayName(), state.name());
                }
                filter.setDropDownItemsMap(states);
                filter.setEnumType(DeploymentState.class);
            } else if (filter.getFilterDisplayName().equals(DeploymentFilterTypes.RELEASE.getFilterDisplayName())) {
                Map<String, String> releaseMap = new LinkedHashMap<>();
                for (ReleaseEntity r : getReleases()) {
                    releaseMap.put(r.getName(), CustomFilter.convertDateToString(r.getInstallationInProductionAt()));
                }
                filter.setDropDownItemsMap(releaseMap);
            } else if (filter.getFilterDisplayName().equals(DeploymentFilterTypes.DEPLOYMENT_PARAMETER.getFilterDisplayName())) {
                filter.setDropDownItems(converToStringList(getAllDeployParamKeys()));
            }

            return filter;
        }
    }

    /**
     * Hier werden die Filter gesetzt, welche nach dem erstellen eines
     * Deployments automatisch gesetzt werden um den Fokus auf das erstellte
     * Deployment zu setzen.
     */
    public void setFilterForDeployment(Integer trackingId) {
        if (trackingId != null) {

            getCustomFilterComp().removeAllFilter();
            creatAndAddNewFilter(DeploymentFilterTypes.TRACKING_ID, trackingId.toString());

            reloadDeployments(true);
        }
    }

    public void changeSortOrderForSelectedColumn(String enumName) {
        deployscreenColDescriptor col = getEnumValueForString(enumName);
        if (col != null) {
            if (sortingColumn.equals(col)) {
                if (sortingDirection == null) {
                    // set to asc
                    sortingDirection = SortingDirectionType.ASC;
                } else {
                    switch (sortingDirection) {
                        case ASC:
                            // set to desc
                            sortingDirection = SortingDirectionType.DESC;
                            break;
                        case DESC:
                            // set to asc
                            sortingDirection = SortingDirectionType.ASC;
                            break;
                    }
                }
            } else {
                // auswahl einer neuen spalte beginnt immer mit asc sortierung
                sortingColumn = col;
                sortingDirection = SortingDirectionType.ASC;
            }
        }
        // do sorting
        reloadDeployments(false);
    }

    public void goToScreen(Integer screen) {
        currentPage = Math.min(Math.max(0, screen), maxScreens() - 1);
        reloadDeployments(false);
    }

    public void nextScreen() {
        currentPage = Math.min(++currentPage, maxScreens() - 1);
        reloadDeployments(false);
    }

    public void lastScreen() {
        currentPage = maxScreens() - 1;
        reloadDeployments(false);
    }

    public void setItemsPerPage(int itemsPerPage) {
        if (itemsPerPage < 0) {
            itemsPerPage = 0;
        }
        if (itemsPerPage != this.itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            this.currentPage = 0;
            reloadDeployments(false);
        }
    }

    public void previousScreen() {
        currentPage = Math.max(0, --currentPage);
        reloadDeployments(false);
    }

    public void firstScreen() {
        currentPage = 0;
        reloadDeployments(false);
    }

    public void applyFilterSearch() {
        reloadDeployments(true);
        GlobalMessageAppender.addSuccessMessage("Search processed.");
    }

    public DeploymentEntity doConfirmAction(DeploymentEntity deployment, boolean reload) {
        try {
            deployment = deploymentBoundary.confirmDeployment(deployment.getId(), deployment.isSendEmailConfirmation(), deployment.isCreateTestAfterDeployment(),
                    deployment.isCreateTestForNeighborhoodAfterDeployment(), deployment.isSimulating());
            GlobalMessageAppender.addSuccessMessage("Deployment " + deployment.getId() + " confirmed");

            if (reload) {
                reloadDeployments(true);
            }
        } catch (DeploymentStateException e) {
            GlobalMessageAppender.addErrorMessage(e);
        }

        return deployment;
    }

    public DeploymentEntity doConfirmAction(DeploymentEntity deployment) {
        return doConfirmAction(deployment, true);
    }

    private List<DeploymentEntity> getSelectedDeployments() {
        LinkedList<DeploymentEntity> result = new LinkedList<>();
        for (DeploymentEntity deployment : pendingDeployments) {
            if (checkedDeployments.get(deployment.getId()) != null && checkedDeployments.get(deployment.getId())) {
                result.add(deployment);
            }
        }

        return result;
    }

    public String getSelectedDeploymentIdsAsCommaseparatedList(){
        String commaseparatedArgument = "";
        for(DeploymentEntity deploymentEntity : getSelectedDeployments()){
            commaseparatedArgument += deploymentEntity.getId() + COMMA;
        }
        if (commaseparatedArgument.endsWith(COMMA)){
            commaseparatedArgument = commaseparatedArgument.substring(0, commaseparatedArgument.length()-1);
        }
        return commaseparatedArgument;
    }

    public void bulkUpdateDeployments() {
        for (DeploymentEntity deployment : this.getSelectedDeployments()) {

            switch (this.bulkAction) {
                case CANCEL:
                    cancelDeployment(deployment, false);
                    break;
                case REJECT:
                    doRejectAction(deployment, false);
                    break;
                case CONFIRM:
                    if (deploymentDate != null) {
                        deployment.setDeploymentDate(deploymentDate);
                        changeDeploymentTime(deployment, false);
                    }
                    deployment.setSendEmailConfirmation(isSendEmailConfirmation());
                    deployment.setCreateTestAfterDeployment(isExecuteShakedownTest());
                    deployment.setCreateTestForNeighborhoodAfterDeployment(isNeighbourhoodTest());
                    deployment.setSimulating(isSimulateGeneration());
                    doConfirmAction(deployment, false);
                    break;
                case CHANGE_DATE:
                    deployment.setDeploymentDate(deploymentDate);
                    changeDeploymentTime(deployment, false);
                    break;
            }
        }

        reloadDeployments(true);
        this.clearEditDeploymentValues();
    }

    public void setToggleDeploymentsSelection(Boolean checked) {
        toggleDeploymentsSelection = checked;
        for (DeploymentEntity deployment : pendingDeployments) {
            if (isRedeployPossible(deployment)) {
                checkedDeployments.put(deployment.getId(), checked);
            }
        }
    }

    public DeploymentAction[] getDeploymentActions() {
        return DeploymentAction.values();
    }

    public void doRejectAction(DeploymentEntity deployment, boolean reload) {
        try {
            deploymentBoundary.rejectDeployment(deployment.getId());
            GlobalMessageAppender.addSuccessMessage("Deployment " + deployment.getId() + " rejeced");
            if (reload) {
                reloadDeployments(true);
            }
        } catch (DeploymentStateException e) {
            GlobalMessageAppender.addErrorMessage(e);
        }
    }

    public void doRejectAction(DeploymentEntity deployment) {
        doRejectAction(deployment, true);
    }


    public void cancelDeployment(DeploymentEntity deployment, boolean reload) {
        try {
            deploymentBoundary.cancelDeployment(deployment.getId());
            GlobalMessageAppender.addSuccessMessage("Deployment " + deployment.getId() + " canceled");
            if (reload) {
                reloadDeployments(true);
            }
        } catch (DeploymentStateException e) {
            GlobalMessageAppender.addErrorMessage(e);
        }
    }

    public void cancelDeployment(DeploymentEntity deployment) {
        cancelDeployment(deployment, true);
    }


    public void changeDeploymentTime(DeploymentEntity deployment, boolean reload) {
        try {
            deploymentBoundary.changeDeploymentDate(deployment.getId(), deployment.getDeploymentDate());
            GlobalMessageAppender.addSuccessMessage("Date of deployment " + deployment.getId() + " changed");
            if (reload) {
                reloadDeployments(true);
            }
        } catch (DeploymentStateException e) {
            GlobalMessageAppender.addErrorMessage(e);
        }
    }

    public void changeDeploymentTime(DeploymentEntity deployment) {
        changeDeploymentTime(deployment, true);
    }


    public boolean isCancelPossible(DeploymentEntity deployment) {
        return !deployment.isPreserved()  && deploymentBoundary.isCancelPossible(deployment).isPossible();
    }

    public boolean isConfirmPossible(DeploymentEntity deployment) {
        return !deployment.isPreserved()  && deploymentBoundary.isConfirmPossible(deployment).isPossible()&& permissionService.hasPermissionForDeploymentUpdate(deployment);
    }

    public boolean isRejectPossible(DeploymentEntity deployment) {
        return !deployment.isPreserved()  && deploymentBoundary.isConfirmPossible(deployment).isPossible() && permissionService.hasPermissionForDeploymentReject(deployment);
    }

    public boolean isChangeDeploymentDatePossible(DeploymentEntity deployment) {
        return !deployment.isPreserved()  && deploymentBoundary.isChangeDeploymentDatePossible(deployment).isPossible() && permissionService.hasPermissionForDeploymentUpdate(deployment);
    }

    public boolean isRedeployPossible(DeploymentEntity deployment) {
        return !deployment.isPreserved() && permissionService.hasPermissionForDeploymentCreation(deployment);
    }

    public void reloadDeployments(boolean countAgain) {
        if (countAgain) {
            currentPage = 0;
        }
        Tuple<Set<DeploymentEntity>, Integer> result = controller.loadPendingDeployments(countAgain, currentStartIndex(), getItemsPerPage(), getAppliedFilterList(), sortingColumn, sortingDirection);
        if (result.getB() != null) {
            countAllDeployments = result.getB();
        }

        checkedDeployments.clear();
        toggleDeploymentsSelection = false;
        pendingDeployments = new ArrayList<>(result.getA());
    }

    public List<CustomFilter> getAppliedFilterList() {
        List<CustomFilter> appliedFilter = new ArrayList<>();

        for (CustomFilter filter : getCustomFilterComp().getSelectedFilterList()) {

            if (filter.isSelected()) {
                appliedFilter.add(filter);
            }
        }

        return appliedFilter;
    }


    private int currentStartIndex() {
        return currentPage * getItemsPerPage();
    }

    public List<Integer> availablePages() {
        List<Integer> list = new ArrayList<>();
        list.add(0);
        for (int i = 1; i < maxScreens(); i++) {
            list.add(i);
        }
        return list;
    }

    public boolean isLastPage() {
        return currentPage >= maxScreens() - 1;
    }

    private int maxScreens() {
        return (int) Math.ceil(((double) countAllDeployments / (double) Math.max(itemsPerPage, getItemsPerPage())));
    }

    public int getItemsPerPage() {
        return itemsPerPage < 1 ? DEFAULT_ITEMS_PER_PAGE : itemsPerPage;
    }

    public String getSortingIconForCol(String enumName) {
        String result = "icon icon-sortnone";
        deployscreenColDescriptor col = getEnumValueForString(enumName);

        if (col != null) {
            if (sortingDirection != null && sortingColumn != null && sortingColumn.equals(col)) {
                if (sortingDirection.equals(SortingDirectionType.ASC)) {
                    result = "icon icon-sortasc";
                } else {
                    result = "icon icon-sortdesc";
                }
            }
        }
        return result;
    }

    private deployscreenColDescriptor getEnumValueForString(String enumName) {
        deployscreenColDescriptor col = null;
        try {
            col = deployscreenColDescriptor.valueOf(enumName);
        } catch (IllegalArgumentException | NullPointerException ie) {
            // no special treatment
        }
        return col;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage != 0 && currentPage != this.currentPage) {
            this.currentPage = currentPage;
        }
    }

    private SortedSet<ReleaseEntity> getReleases() {
        if (releases.isEmpty()) {
            releases = new TreeSet<>(releaseMgmtService.loadAllReleases(false));
        }
        return releases;
    }

    private List<Key> getAllDeployParamKeys() {
        if (allDeploymentParameterKeys.isEmpty()){
            allDeploymentParameterKeys = controller.loadAllParameterKeys();
        }
        return allDeploymentParameterKeys;
    }

    private List<ResourceGroupEntity> getApplicationServerGroups() {
        if (applicationServerGroups.isEmpty()) {
            applicationServerGroups = controller.loadAppServerGroups();
        }
        return applicationServerGroups;
    }

    private List<ResourceGroupEntity> getRuntimesGroups() {
        if (runtimesGroups.isEmpty()) {
            runtimesGroups = controller.loadRuntimesGroups();
        }
        return runtimesGroups;
    }

    private List<ResourceGroupEntity> getApplicationGroups() {
        if (applicationGroups.isEmpty()) {
            applicationGroups = controller.loadAppGroups();
        }
        return applicationGroups;
    }

    public void setSelectedDeploymentId(Integer deploymentId) {
        for (DeploymentEntity d : pendingDeployments) {
            if (d.getId().equals(deploymentId)) {
                setSelectedDeployment(d);
                break;
            }
        }
        this.selectedDeploymentId = deploymentId;
    }

    public void setSelectedDeployment(DeploymentEntity selectedDeployment) {
        this.selectedDeployment = selectedDeployment;
    }

    public boolean hasLogFiles(int deploymentId) {
        return controller.getLogFileNames(deploymentId).length > 0;
    }

    public boolean hasFilterSelected() {
        return selectedFilter != null;
    }

    public String getDefaultAsValueForNullableFilterValues(CustomFilter filter) {
        if (filter.hasValidNullValue()) {
            return DEFAULT;
        } else {
            return filter.getValue();
        }
    }

    public String getDeploymentStateFrame(DeploymentEntity deployment) {
        String result;

        switch (deployment.getDeploymentState()) {
            case success:
                result = "state success";
                break;
            case failed:
                result = "state failed";
                break;
            case canceled:
                result = "state canceled";
                break;
            case requested:
                result = "state requested";
                break;
            case scheduled:
                result = "state scheduled";
                break;
            case rejected:
                result = "state rejected";
                break;
            case progress:
                result = "state inprogress";
                break;
            case PRE_DEPLOYMENT:
                result = "state predeployment";
                break;
            case READY_FOR_DEPLOYMENT:
                result = "state readyfordeployment";
                break;
            case simulating:
                result = "state simulating";
                break;
            case delayed:
                result = "state delayed";
                break;
            default:
                result = "state not available";
                break;
        }

        return result;
    }

    public String getDeploymentDelayedClass(DeploymentEntity deployment) {
        return deployment.isDeploymentDelayed() ? "deployment-delayed" : StringUtils.EMPTY;
    }

    public String getDeploymentStateText(DeploymentEntity deployment) {
        return deployment.getDeploymentState().getDisplayName();
    }

    public boolean showBuildFailed(DeploymentEntity deployment) {
        return !deployment.isBuildSuccess() && !deployment.isExecuted();
    }

    public Date getCurrentDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1);
        return c.getTime();
    }


    public int getCurrentPage() {
        return currentPage;
    }

    public DeploymentEntity getSelectedDeployment() {
        return selectedDeployment;
    }

    public Integer getSelectedDeploymentId() {
        return selectedDeploymentId;
    }

    public boolean isDeploymentCanceled(DeploymentEntity deployment) {
        return deployment != null && deployment.getDeploymentState() != null && deployment.getDeploymentState().equals(DeploymentState.canceled);
    }

    public boolean isDeploymentDelayed(DeploymentEntity deployment) {
        return deployment != null && deployment.isDeploymentDelayed();
    }

    public SortingDirectionType getSortingDirection() {
        return sortingDirection;
    }

    public void setSortingDirection(SortingDirectionType sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    public deployscreenColDescriptor getSortingColumn() {
        return sortingColumn;
    }

    public void setSortingColumn(deployscreenColDescriptor sortingColumn) {
        this.sortingColumn = sortingColumn;
    }

    public String formatDate(Date date) {
        Format formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return date != null ? formatter.format(date) : StringUtils.EMPTY;
    }

    public void clearEditDeploymentValues() {
        bulkAction = DeploymentAction.CHANGE_DATE;
        this.deploymentDate = null;

        this.setSendEmail(false);
        this.setExecuteShakedownTest(false);
        this.setNeighbourhoodTest(false);
    }

    public boolean canEditDeployments() {
        boolean isEditAllowed = !getSelectedDeployments().isEmpty();

        for (DeploymentEntity selectedDeployment : getSelectedDeployments()) {
            if (!isChangeDeploymentDatePossible(selectedDeployment)) {
                isEditAllowed = false;
                break;
            }
        }

        return isEditAllowed && (permissionService.hasPermissionToCreateDeployment() || permissionService.hasPermissionToEditDeployment());
    }

    public boolean canRedeploy() {
        return permissionService.hasPermissionToCreateDeployment() && !getSelectedDeployments().isEmpty();
    }

    public boolean hasDeploymentParameter() {
        return selectedDeployment != null && selectedDeployment.getDeploymentParameters() != null && !selectedDeployment.getDeploymentParameters().isEmpty();
    }

    public String getContextName(DeploymentEntity deployment) {
        return deploymentBoundary.getDeletedContextName(deployment);
    }

    public String getResourceName(DeploymentEntity deployment) {
        return deploymentBoundary.getDeletedResourceName(deployment);
    }

    public String getResourceGroupName(DeploymentEntity deployment) {
        return deploymentBoundary.getDeletedResourceGroupName(deployment);
    }

    public String getReleaseName(DeploymentEntity deployment) {
        return deploymentBoundary.getDeletedReleaseName(deployment);
    }

}
