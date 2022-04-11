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

import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.ApplicationWithVersionEntity;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.DeploymentParameter;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.Key;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.presentation.ViewBackingBean;
import ch.puzzle.itc.mobiliar.presentation.common.ReleaseSelectionDataProvider;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@ViewBackingBean
public class CreateDeploymentView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CreateDeploymentController controller;
    @Inject
    private Event<DomainEvent> domainEvent;

    @Inject
    ReleaseSelectionDataProvider releaseDataProvider;

    private ResourceGroupEntity selectedApplicationServerGroup;
    @Getter
    private ReleaseEntity selectedRelease;

    @Getter
    private List<ResourceGroupEntity> applicationServerGroups = new LinkedList<>();
    @Getter
    private List<ReleaseEntity> releasesForAs = new LinkedList<>();
    @Getter
    private List<ContextEntity> environments = new LinkedList<>();
    @Getter
    List<Key> allKeys = new LinkedList<>();
    @Getter
    private List<ApplicationWithVersionEntity> appsWithVersion = new LinkedList<>();
    @Getter
    private ResourceEntity selectedAppServer;
    @Getter
    @Setter
    private Date deploymentDate;
    @Getter
    @Setter
    private Long selectedStateDate;
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
    @Getter
    List<DeploymentParameter> allSelectedDeploymentParameters = new ArrayList<>();
    @Getter
    private List<DeploymentEntity> selectedDeploymentsForRedeployment = new ArrayList<>();
    @Getter
    private Map<String, List<ContextEntity>> domainEnvironments = new HashMap<>();
    @Getter
    private List<Domain> domains = new LinkedList<>();
    @Setter
    @Getter
    private String deployParameterValue;
    @Getter
    @Setter
    private String selectedKeyName;
    private List<String> availableKeyNames = null;

    @Inject
    private DeployScreenDataProvider deployScreenDataProvider;

    @PostConstruct
    public void init() {
        environments = controller.loadEnvironments();
        domainEnvironments = controller.loadEnvironmentsPerDomain();

        for (Map.Entry<String, List<ContextEntity>> domain : domainEnvironments.entrySet()) {
            Domain d = new Domain(domain.getKey(), domainEvent);
            domains.add(d);
        }

        applicationServerGroups = controller.loadAppServerGroups();
        allKeys = controller.loadAllParameterKeys();
        selectedDeploymentsForRedeployment = controller
                .loadNewestDeploymentsPerAppserver(getDeploymentIdsFromContext());

        if (hasOnlyOneDeploymentSelected()) {
            DeploymentEntity deploymentEntity = selectedDeploymentsForRedeployment.get(0);
            allSelectedDeploymentParameters = new ArrayList<>(deploymentEntity.getDeploymentParameters());

            activateContext(deploymentEntity.getContext(), domains);
        }
    }

    private void activateContext(ContextEntity context, List<Domain> domains){
        for(Domain domain: domains){
            if(context.getParent().getName().equals(domain.getName())){
                domain.addSelectedContextIds(context.getId().toString());
            }
        }
    }

    /**
     * @return title for view - either create new deployment or view selected for redeployment
     */
    public String getTitle() {
        if (isRedeployment()) {
            return "Redeploy";
        }
        return "Create new deployment";
    }

    public Integer getSelectedApplicationServerGroup() {
        return isApplicationserverSelected() ? selectedApplicationServerGroup.getId() : null;
    }

    public List<Integer> getSelectedApplicationServerGroupForRedeployment() {
        List<Integer> appServerGroupIds = new ArrayList<>();
        for (DeploymentEntity deploymentEntity : selectedDeploymentsForRedeployment) {
            appServerGroupIds.add(deploymentEntity.getResourceGroup().getId());
        }
        return appServerGroupIds;
    }

    public boolean isApplicationserverSelected() {
        return selectedApplicationServerGroup != null;
    }

    public Integer getSelectedReleaseId() {
        return selectedRelease != null ? selectedRelease.getId() : null;
    }

    public List<ContextEntity> getContextsForDomain(String domain) {
        if (domainEnvironments.containsKey(domain)) {
            return domainEnvironments.get(domain);
        }
        return null;
    }

    public List<String> getSelectedContextIds() {
        List<String> selectedContextIds = new ArrayList<>();
        if(domains != null){
            for (Domain domain: domains){
                selectedContextIds.addAll(domain.getSelectedContextIds());
            }
        }
        return selectedContextIds;
    }


    public boolean isContextSelected() {
        return getSelectedContextIds() != null && !getSelectedContextIds().isEmpty();
    }

    public boolean hasAppsToShow() {
        return (appsWithVersion != null && !appsWithVersion.isEmpty());
    }

    public void handleDomainChangeEvent(@Observes() DomainEvent event) {
        loadApplicationsForSelecedAppServer();
    }

    private void loadApplicationsForSelecedAppServer() {
        if (isApplicationserverSelected() && isReleaseSelected()) {
            selectedAppServer = controller.loadApplicationserverForRelease(selectedApplicationServerGroup,
                    getSelectedRelease());
            appsWithVersion = controller.getAppsWithVersion(selectedAppServer, getSelectedContextIds(),
                    getSelectedRelease());
        }
    }

    public boolean isReleaseSelected() {
        return getSelectedRelease() != null;
    }

    public boolean isContextSelectionEnabled(){
        if(isRedeployment()){
            return true;
        }else {
            return isReleaseSelected() && isApplicationserverSelected();
        }
    }

    private ReleaseEntity getDefaultRelease() {
        Integer upcomingReleaseId = releaseDataProvider.getUpcomingReleaseId();
        releaseDataProvider.reset();
        for (ReleaseEntity release : releasesForAs) {
            if (release.getId().equals(upcomingReleaseId)) {
                return release;
            }
        }
        // fallback
        return !releasesForAs.isEmpty() ? releasesForAs.get(0) : null;
    }

    public void setSelectedApplicationServerGroup(Integer selectedGroupId) {
        deploymentDate = null;

        ResourceGroupEntity newSelectedGroup = null;
        for (ResourceGroupEntity group : applicationServerGroups) {
            if (group.getId().equals(selectedGroupId)) {
                newSelectedGroup = group;
                break;
            }
        }
        if (newSelectedGroup != null) {

            boolean hasChanged = selectedApplicationServerGroup == null
                    || !selectedGroupId.equals(selectedApplicationServerGroup.getId());

            if (hasChanged) {
                // load all relations of all resources so we can work with them on the gui
                selectedApplicationServerGroup = controller
                        .getResourceGroupWithResourceRelations(newSelectedGroup.getId());
                loadReleasesForAs();
                selectedRelease = getDefaultRelease();
                loadApplicationsForSelecedAppServer();
            }
        }
        else {
            selectedApplicationServerGroup = null;
            selectedAppServer = null;
            appsWithVersion = new LinkedList<>();
        }
    }

    public void setSelectedReleaseId(Integer selectedReleaseId) {
        ReleaseEntity newSelectedRelease = null;

        for (ReleaseEntity release : releasesForAs) {
            if (release.getId().equals(selectedReleaseId)) {
                newSelectedRelease = release;
                break;
            }
        }

        if (newSelectedRelease != null) {
            if (selectedRelease == null || !selectedReleaseId.equals(selectedRelease.getId())) {
                selectedRelease = newSelectedRelease;
                loadApplicationsForSelecedAppServer();
            }
        }
        else {
            this.selectedRelease = null;
        }
    }

    public String getRuntimeOfCurrentAppserver() {
        return isAppserverSelected() && selectedAppServer.getRuntime() != null
                ? selectedAppServer.getRuntime().getName() : StringUtils.EMPTY;
    }

    public boolean isAppserverSelected() {
        return selectedAppServer != null || !selectedDeploymentsForRedeployment.isEmpty();
    }

    public Map<String, Long> getTags() {
        // sorted by tag name
        Map<String, Long> sortedTags = new TreeMap<>();
        if (selectedAppServer != null) {
            Set<ResourceTagEntity> tags = selectedAppServer.getResourceTags();

            for (ResourceTagEntity tag : tags) {
                sortedTags.put(tag.getLabel(), tag.getTagDate().getTime());
            }
        }
        return sortedTags;
    }

    public boolean isServerAndContextSelected() {
        return isAppserverSelected() && isContextSelected();
    }

    /**
     * loads a subSet of releases greater or equal to the first release in the selected group or an empty list if no
     * group is selected
     */
    private void loadReleasesForAs() {
        if (isApplicationserverSelected()) {
            releasesForAs = controller.getReleasesForApppserver(selectedApplicationServerGroup);
        }
        else {
            releasesForAs = Collections.emptyList();
        }
    }

    public boolean isRedeployment() {
        return !selectedDeploymentsForRedeployment.isEmpty();
    }

    public String createNewDeployment() {
        return createDeployment(false);
    }

    public String createDeploymentRequest() {
        return createDeployment(true);
    }

    // erstelle einen neuen deploymentJob
    private String createDeployment(boolean isRequest) {
        Integer trackingId;

        if (isRedeployment()) {
            trackingId = controller.createDeploymentReturnTrackingId(selectedDeploymentsForRedeployment,
                    getSelectedContextIds(), getSelectedStateDateAsDate(), deploymentDate,
                    allSelectedDeploymentParameters, sendEmail, isRequest, simulateGeneration, executeShakedownTest,
                    neighbourhoodTest);
        }
        else {
            trackingId = controller.createDeploymentReturnTrackingId(selectedApplicationServerGroup.getId(),
                    selectedRelease.getId(), getSelectedContextIds(), getSelectedStateDateAsDate(), deploymentDate,
                    new HashSet<>(appsWithVersion), allSelectedDeploymentParameters, sendEmail, isRequest, simulateGeneration,
                    executeShakedownTest, neighbourhoodTest);
        }

        if (trackingId != null) {
            deployScreenDataProvider.setFilterForDeployment(trackingId);
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            try {
                ec.redirect(ec.getRequestContextPath()+ "/pages/deploy.xhtml");
            } catch (IOException e) {}
            return "deploy";
        }
        GlobalMessageAppender.addErrorMessage("Could not create deployment");
        return null;
    }

    private Date getSelectedStateDateAsDate() {
        if (selectedStateDate != null) {
            if (selectedStateDate > 1) {
                return new Date(selectedStateDate);
            }
        }
        return null;
    }

    private String[] getDeploymentIdsFromContext() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String deploymentIds = params.get("deploymentIdArray");
        if (deploymentIds != null) {
            return deploymentIds.split(",");
        }
        return new String[] {};
    }

    public boolean hasOnlyOneDeploymentSelected() {
        return selectedDeploymentsForRedeployment.size() == 1;
    }

    public void createNewParameter() {
        if (selectedKeyName != null && !selectedKeyName.trim().isEmpty()) {
            DeploymentParameter deploymentParameter = new DeploymentParameter(selectedKeyName.trim(), deployParameterValue);
            if (getContainingDeploymentParameter(allSelectedDeploymentParameters, deploymentParameter) == null) {
                allSelectedDeploymentParameters.add(deploymentParameter);
            }
        }
        selectedKeyName = null;
        deployParameterValue = null;
    }

    public void removeParameter(DeploymentParameter parameter) {
        if (parameter != null) {
            allSelectedDeploymentParameters.remove(parameter);
        }
    }

    private List<Key> getAllSelectableKeys() {
        List<Key> allSelectableKeys = new ArrayList<>(allKeys);

        for (DeploymentParameter parameter : allSelectedDeploymentParameters) {
            Key keyFromList = getContainingKeyByKeyName(allSelectableKeys, parameter.getKey());
            if (keyFromList != null) {
                allSelectableKeys.remove(keyFromList);
            }
        }
        return allSelectableKeys;
    }

    public List<String> getAvailableKeys(String prefix){
        if(availableKeyNames == null) {
            availableKeyNames = new ArrayList<>();
            for (Key key : getAllSelectableKeys()) {
                availableKeyNames.add(key.getName());
            }
        }
        return availableKeyNames;
    }

    private Key getContainingKeyByKeyName(List<Key> allSelectableKeys, String keyName) {
        for (Key key : allSelectableKeys) {
            if (key.getName().equals(keyName)) {
                return key;
            }
        }
        return null;
    }

    private DeploymentParameter getContainingDeploymentParameter(List<DeploymentParameter> parameters, DeploymentParameter needle) {
        for (DeploymentParameter parameter : parameters) {
            if (parameter.getKey().equals(needle.getKey())) {
                return parameter;
            }
        }
        return null;
    }
}
