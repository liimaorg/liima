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
	private List<AppWithVersionDTO> appsWithVersion = new LinkedList<>();
	private List<DeploymentParameterDTO> deploymentParameters = new LinkedList<>();
	private String environmentName;
	private String releaseName;
	private String runtimeName;
	private String requestUser;
	private String confirmUser;
	private String cancelUser;
	private boolean deploymentDelayed;
	private Set<NodeJobDTO> nodeJobs = new HashSet<>();

    private DeploymentActionsDTO actions;

	public DeploymentDTO(DeploymentEntity entity) {
		this.id = entity.getId();
		this.trackingId = entity.getTrackingId();
		this.state = entity.getDeploymentState();
		this.appServerName = entity.getResourceGroup().getName();
		this.appServerId = entity.getResourceGroup().getId();
		for (ApplicationWithVersion app : entity.getApplicationsWithVersion()) {
			appsWithVersion.add(new AppWithVersionDTO(app.getApplicationName(), app.getApplicationId(), app.getVersion()));
		}
		for (DeploymentParameter param : entity.getDeploymentParameters()) {
			deploymentParameters.add(new DeploymentParameterDTO(param.getKey(), param.getValue()));
		}
		this.deploymentDate = entity.getDeploymentDate();
		this.deploymentJobCreationDate = entity.getDeploymentJobCreationDate();
		this.deploymentConfirmationDate = entity.getDeploymentConfirmationDate();
		this.deploymentCancelDate = entity.getDeploymentCancelDate();
		this.reason = entity.getReason();
		this.environmentName = entity.getContext().getName();
		this.setReleaseName(entity.getRelease().getName());
		this.setRuntimeName(entity.getRuntime().getName());
		this.setRequestUser(entity.getDeploymentRequestUser());
		this.setConfirmUser(entity.getDeploymentConfirmationUser());
		this.setCancelUser(entity.getDeploymentCancelUser());
		this.setDeploymentDelayed(entity.isDeploymentDelayed());
		for (NodeJobEntity job : entity.getNodeJobs()) {
			nodeJobs.add(new NodeJobDTO(job));
		}
	}

}
