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

package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFailureReason;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.deploy.entity.NodeJobEntity;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.DeploymentParameter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement(name = "deployment")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Data
@NoArgsConstructor
public class DeploymentDTO {

	private Integer id;
	private Integer trackingId;
	private DeploymentState state;
	private Date deploymentDate;
	private Date deploymentJobCreationDate;
	private Date deploymentConfirmationDate;
	private Date deploymentCancelDate;
	private DeploymentFailureReason reason;
	private String appServerName;
	private Integer appServerId;
	private Integer resourceId;
	private List<AppWithVersionDTO> appsWithVersion = new LinkedList<>();
	private List<DeploymentParameterDTO> deploymentParameters = new LinkedList<>();
	private String environmentName;
	private String environmentNameAlias;
	private String releaseName;
	private String runtimeName;
	private String requestUser;
	private String confirmUser;
	private String cancelUser;
	private boolean deploymentDelayed;
	private Set<NodeJobDTO> nodeJobs = new HashSet<>();
    private DeploymentActionsDTO actions;
    // for csv export
	private boolean buildSuccess;
	private boolean deploymentExecuted;
	private String targetPlatform;
	private String statusMessage;
	private Date stateToDeploy;
	private boolean deploymentConfirmed;

	private boolean executed;
	private boolean sendEmailWhenDeployed;
	private boolean simulateBeforeDeployment;
	private boolean shakedownTestsWhenDeployed;
	private boolean neighbourhoodTest;

	public DeploymentDTO(DeploymentEntity entity) {
		setPreservedValues(entity, new PreservedProperties());
	}

	public void setPreservedValues(DeploymentEntity entity, PreservedProperties properties) {
		this.id = entity.getId();
		this.trackingId = entity.getTrackingId();
		this.state = entity.getDeploymentState();
		this.appServerName = properties.getAppServerName() != null ? properties.getAppServerName() : entity.getResourceGroup().getName();
		this.appServerId = properties.getAppServerId() != null ? properties.getAppServerId() : entity.getResourceGroup().getId();
		// this is not a typo
		this.resourceId = properties.getResourceId() != null ? null : entity.getResource().getId();
		for (ApplicationWithVersion app : entity.getApplicationsWithVersion()) {
			this.appsWithVersion.add(new AppWithVersionDTO(app.getApplicationName(), app.getApplicationId(), app.getVersion()));
		}
		for (DeploymentParameter param : entity.getDeploymentParameters()) {
			this.deploymentParameters.add(new DeploymentParameterDTO(param.getKey(), param.getValue()));
		}
		this.deploymentDate = entity.getDeploymentDate();
		this.deploymentJobCreationDate = entity.getDeploymentJobCreationDate();
		this.deploymentConfirmationDate = entity.getDeploymentConfirmationDate();
		this.deploymentCancelDate = entity.getDeploymentCancelDate();
		this.reason = entity.getReason();
		this.environmentName = properties.getEnvironmentName() != null ? properties.getEnvironmentName() : entity.getContext().getName();
		this.environmentNameAlias = properties.getEnvironmentNameAlias() != null ? properties.getEnvironmentNameAlias() : entity.getContext().getNameAlias();
		this.releaseName = properties.getReleaseName() != null ? properties.getReleaseName() : entity.getRelease().getName();
		this.runtimeName = properties.getRuntimeName() != null ? properties.getRuntimeName() : entity.getRuntime().getName();
		this.requestUser = entity.getDeploymentRequestUser();
		this.confirmUser = entity.getDeploymentConfirmationUser();
		this.cancelUser = entity.getDeploymentCancelUser();
		this.deploymentDelayed = entity.isDeploymentDelayed();
		for (NodeJobEntity job : entity.getNodeJobs()) {
			this.nodeJobs.add(new NodeJobDTO(job));
		}

		this.buildSuccess = entity.isBuildSuccess();
		this.deploymentExecuted = entity.isExecuted();
		this.targetPlatform = properties.getRuntimeName() != null ? properties.getRuntimeName() : entity.getRuntime().getName();
		this.statusMessage = entity.getStateMessage();
		this.stateToDeploy = entity.getStateToDeploy();
		this.deploymentConfirmed = entity.getDeploymentConfirmed() != null ? entity.getDeploymentConfirmed() : false;

		this.setExecuted(entity.isExecuted());
		this.setSendEmailWhenDeployed(entity.isSendEmail());
		this.setSimulateBeforeDeployment(entity.isSimulating());
		this.setShakedownTestsWhenDeployed(entity.isCreateTestAfterDeployment());
		this.setNeighbourhoodTest(entity.isCreateTestForNeighborhoodAfterDeployment());
	}

	@Data
	@NoArgsConstructor
	public class PreservedProperties {

		private String appServerName;
		private Integer appServerId;
		private Integer resourceId;
		private String environmentName;
		private String environmentNameAlias;
		private String releaseName;
		private String runtimeName;
	}

}
