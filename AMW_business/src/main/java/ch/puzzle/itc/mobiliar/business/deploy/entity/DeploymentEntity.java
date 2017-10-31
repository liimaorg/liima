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

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.BatchSize;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.DeploymentParameter;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.common.exception.DeploymentStateException;
import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Entity implementation class for Entity: Deployment
 */
@Entity
@Table(name = "TAMW_deployment")
@NamedQuery(name = DeploymentEntity.LAST_SUCCESSFUL_DEPLOYMENT, query = "select d from DeploymentEntity d where d.deploymentDate=(select max(t.deploymentDate) from DeploymentEntity t where t.context=:context and t.resourceGroup=:resourceGroup and t.release=:release and t.deploymentState=:deploymentState)")
public class DeploymentEntity implements Serializable {
    public static final String LAST_SUCCESSFUL_DEPLOYMENT = "lastSuccessfulDeployment";
    public static final String SEQ_NAME = "trackingId";

    @Getter
    @Setter
    @TableGenerator(name = "deploymentIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "deploymentId")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "deploymentIdGen")
    @Id
    @Column(unique = true, nullable = false)
    private Integer id;

    @Getter
    private Date deploymentDate;

    /**
     * The release for which the deployment has been issued. Please note, that the ReleaseEntity can be deleted and
     * therefore might be nulled later. The information about which release has been deployed with that instance has
     * therefore to be stored somewhere else. (e.g. within the stateMessage)
     */
    @Getter
    @Setter
    @ManyToOne
    private ReleaseEntity release;

    public enum DeploymentState {
        // the names of the enums are used in the database
        success("success"),
        failed("failed"),
        canceled("canceled"),
        rejected("rejected"),
        READY_FOR_DEPLOYMENT("ready_for_deploy"),
        PRE_DEPLOYMENT("pre_deploy"),
        progress("progress"),
        simulating("simulating"),
        delayed("delayed"),
        scheduled("scheduled"),
        requested("requested");

        static {
            PRE_DEPLOYMENT.setAllowedTransitions(success, failed, READY_FOR_DEPLOYMENT, progress);
            READY_FOR_DEPLOYMENT.setAllowedTransitions(canceled, rejected, progress);
            progress.setAllowedTransitions(failed, success);
            simulating.setAllowedTransitions(canceled, rejected, progress, delayed, scheduled, requested);
            delayed.setAllowedTransitions(canceled, rejected, progress, simulating, scheduled, PRE_DEPLOYMENT);
            scheduled.setAllowedTransitions(canceled, rejected, progress, simulating, delayed, PRE_DEPLOYMENT);
            requested.setAllowedTransitions(canceled, rejected, simulating, delayed, scheduled);
        }

        @Getter
        private String displayName;
        @Getter
        private List<DeploymentState> allowedTransitions = new ArrayList<>();

        private void setAllowedTransitions(DeploymentState... allowedTransitions) {
            this.allowedTransitions = Arrays.asList(allowedTransitions);
        }

        private DeploymentState(String displayName) {
            this.displayName = displayName;
        }

        public boolean isTransitionAllowed(DeploymentState newState) {
            return allowedTransitions.contains(newState);
        }

        public static DeploymentState getByString(String statusStr) {
            for (DeploymentState state : DeploymentState.values()) {
                if (state.name().equalsIgnoreCase(statusStr)) {
                    return state;
                }
            }
            return null;
        }

		@Override
		public String toString() {
			return displayName;
		}

    }

    // TODO: Redundant to deploymentState?
    // results in getDeploymentConfirmed not isDeploymentConfirmed
    @Getter
    private Boolean deploymentConfirmed;

    @Getter
    @Setter
    private Date stateToDeploy;

    @Getter
    @Column(length = 65536)
    @Lob
    private String stateMessage;

    @Getter
    @Setter
    @ManyToOne
    private ResourceEntity resource;

    @ManyToOne
    @Getter
    @Setter
    private ResourceGroupEntity resourceGroup;

    @Getter
    @Setter
    @ManyToOne
    private ContextEntity context;

    @Getter
    @Setter
    private Date deploymentJobCreationDate;

    @Getter
    @Enumerated(EnumType.STRING)
    private DeploymentState deploymentState;

    @Getter
    @Setter
    @Column(nullable = false)
    private boolean buildSuccess;

    @Getter
    @Setter
    @Column(nullable = false)
    private boolean simulating;

    @Getter
    @Setter
    @Column(nullable = false)
    private boolean sendEmail;

    @Getter
    @Setter
    @Column(nullable = false)
    private boolean sendEmailConfirmation;

    @Getter
    @Setter
    @Column(nullable = false)
    private boolean createTestAfterDeployment;

    @Column(nullable = false)
    private boolean neighborhoodTest;

    @Getter
    @Setter
    @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL)
    private Set<ShakedownTestEntity> shakedownTests;

    @Getter
    @Setter
    @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @BatchSize(size = 10)
    private Set<NodeJobEntity> nodeJobs;

    @Getter
    @Setter
    private String deploymentRequestUser;

    @Getter
    @Setter
    private String deploymentConfirmationUser;

    @Getter
    @Setter
    private String deploymentCancelUser;

    @Getter
    @Setter
    private Date deploymentCancelDate;

    @Getter
    @Setter
    private Date deploymentConfirmationDate;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "runtime_resource_id")
    private ResourceEntity runtime;

    @Column(length = 65536)
    @Lob
    private String applicationsWithVersion;

    // cache for the converted applicationsWithVersion
    @Transient
    private List<ApplicationWithVersion> applicationsWithVersionList;

    @Getter
    @Setter
    private Integer trackingId;

    @Getter
    @Setter
    @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DeploymentParameter> deploymentParameters;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private DeploymentFailureReason reason;

    @Getter
    @Version
    private long v;

    private static final long serialVersionUID = 1L;

    public DeploymentEntity() {
        super();
    }

    public boolean isRunning() {
        if (deploymentState == DeploymentState.PRE_DEPLOYMENT || deploymentState == DeploymentState.simulating
                || deploymentState == DeploymentState.progress) {
            return true;
        }
        return false;
    }

    public boolean isExecuted() {
        if (deploymentState == DeploymentState.failed || deploymentState == DeploymentState.success
                || deploymentState == DeploymentState.canceled || deploymentState == DeploymentState.rejected) {
            return true;
        }
        return false;
    }

    public boolean isMutable() {
        return deploymentState == null || deploymentState == DeploymentState.requested
                || deploymentState == DeploymentState.scheduled || deploymentState == DeploymentState.simulating
                || deploymentState == DeploymentState.READY_FOR_DEPLOYMENT;
    }

    public String getStateMessageAsHtml() {
        String replacedMessage = "";
        if (stateMessage != null) {
            replacedMessage = stateMessage.replaceAll("\n", "<br>");
        }

        return replacedMessage;
    }

    public void appendStateMessage(String stateMessage) {
        if (this.stateMessage == null) {
            this.stateMessage = stateMessage;
        }
        else {
            this.stateMessage += "\n\n" + stateMessage;
        }
    }

    /*
     * This methods don't check if user has the right to call it, Use the DeploymentBoundary for that.
     */
    public void confirm(String username) {
        setDeploymentState(DeploymentState.scheduled);
        setDeploymentConfirmationDate(new Date());
        setDeploymentConfirmationUser(username);
        deploymentConfirmed = true;
    }

    public void reject(String username) {
        setDeploymentState(DeploymentState.rejected);
        setDeploymentConfirmationDate(new Date());
        setDeploymentConfirmationUser(username);
        deploymentConfirmed = false;
    }

    public void cancel(String username) {
        setDeploymentState(DeploymentState.canceled);
        appendStateMessage("Deployment canceled at " + new Date());
        setDeploymentCancelUser(username);
        setDeploymentCancelDate(new Date());
    }

	public void setDeploymentState(DeploymentState newDeploymentState) {
		if (deploymentState != null && !deploymentState.isTransitionAllowed(newDeploymentState)) {
			throw new DeploymentStateException("Can't set status from " + deploymentState + " to " + newDeploymentState
					+ " of deployment " + getId() + ". Allowed transitions: " + deploymentState.allowedTransitions);
		}

        deploymentState = newDeploymentState;
    }

    public void setDeploymentDate(Date newDate) {
        if (!isMutable()) {
            throw new DeploymentStateException("Date can't be changed anymore");
        }

        deploymentDate = newDate;
    }

    public List<ApplicationWithVersion> getApplicationsWithVersion() {
        if (applicationsWithVersionList != null) {
            return applicationsWithVersionList;
        }
        List<ApplicationWithVersion> result = new ArrayList<DeploymentEntity.ApplicationWithVersion>();
        if(applicationsWithVersion !=  null) {
            JSONArray o1 = JSONArray.fromObject(applicationsWithVersion);
            for (Object object : o1) {
                JSONObject o = JSONObject.fromObject(object);
                Object appName = o.get("applicationName");
                Object appId = o.get("applicationId");
                Object version = o.get("version");
                result.add(new ApplicationWithVersion(appName.toString(), (Integer) appId, version.toString()));
            }
            // sort the apps
            Collections.sort(result, new Comparator<ApplicationWithVersion>() {
                @Override
                public int compare(ApplicationWithVersion app1, ApplicationWithVersion app2) {
                    return app1.getApplicationName().toLowerCase().compareTo(app2.getApplicationName().toLowerCase());
                }
            });
        }

        applicationsWithVersionList = result;
        return result;
    }

    public void setApplicationsWithVersion(List<ApplicationWithVersion> applicationsWithVersion) {
        JSON json = JSONSerializer.toJSON(applicationsWithVersion);
        this.applicationsWithVersion = json.toString();
        applicationsWithVersionList = null;
    }

    public static class ApplicationWithVersion {

        @Getter
        @Setter
        private String applicationName;

        @Getter
        @Setter
        private Integer applicationId;

        @Getter
        @Setter
        private String version;

        public ApplicationWithVersion(String applicationName, Integer applicationId, String version) {
            this.applicationId = applicationId;
            this.applicationName = applicationName;
            this.version = version;
        }

        @Override
        public String toString() {
            return "ApplicationWithVersion [applicationName=" + applicationName + ", applicationId=" + applicationId
                    + ", version=" + version + "]";
        }

    }

    ;

    public boolean isDeploymentDelayed() {
        return !isExecuted()
                && (getDeploymentState() == null || (!getDeploymentState().equals(DeploymentState.rejected)
                        && !getDeploymentState().equals(DeploymentState.progress)
                        && !getDeploymentState().equals(DeploymentState.failed)))
                && getDeploymentDate() != null && new Date().after(getDeploymentDate());
    }

    public boolean isCreateTestForNeighborhoodAfterDeployment() {
        return neighborhoodTest;
    }

    public void setCreateTestForNeighborhoodAfterDeployment(boolean createTestForNeighborhoodAfterDeployment) {
        this.neighborhoodTest = createTestForNeighborhoodAfterDeployment;
    }

    /**
     * @return the State Date which is used to read the AS from history
     */
    public Date getDeploymentStateDate() {
        return getStateToDeploy() != null ? getStateToDeploy() : new Date();
    }

    public void addDeploymentParameter(DeploymentParameter deploymentParameter) {
        if (this.deploymentParameters == null) {
            deploymentParameters = new ArrayList<>();
        }
        deploymentParameters.add(deploymentParameter);
    }

	public boolean isPredeploymentFinished() {
        boolean hasPredeploymnets = false;
        boolean allFinished = true;

		for (NodeJobEntity job : this.getNodeJobs()) {
            if (DeploymentState.PRE_DEPLOYMENT.equals(job.getDeploymentState())) {
                hasPredeploymnets = true;
                if (NodeJobEntity.NodeJobStatus.RUNNING.equals(job.getStatus())) {
                    allFinished=false;
                    break;
                }
			}
		}
		return hasPredeploymnets && allFinished;
	}

	public boolean isPredeploymentSuccessful() {
        boolean hasPredeploymnets = false;
        boolean allSuccess = true;

		for (NodeJobEntity job : this.getNodeJobs()) {
            if (DeploymentState.PRE_DEPLOYMENT.equals(job.getDeploymentState())) {
                hasPredeploymnets = true;
                if (!NodeJobEntity.NodeJobStatus.SUCCESS.equals(job.getStatus())) {
                    allSuccess=false;
                    break;
                }
			}
		}
		return hasPredeploymnets && allSuccess;
	}

	public NodeJobEntity findNodeJobEntity(Integer nodeJobId) {
		for (NodeJobEntity nodeJob : this.getNodeJobs()) {
			if (nodeJobId.equals(nodeJob.getId())) {
				return nodeJob;
			}
		}
		return null;
	}
}
