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

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.NodeJobEntity;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.DeploymentParameter;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "deployment")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Getter @Setter
public class DeploymentDTO {

	private Integer id;
	private Integer trackingId;
	private DeploymentEntity.DeploymentState state;
	private Date deploymentDate;
	private String appServerName;
	private List<AppWithVersionDTO> appsWithVersion = new LinkedList<>();
	private List<DeploymentParameterDTO> deploymentParams = new LinkedList<>();
	private String environmentName;
	private String releaseName;
	private String runtimeName;
	private String requestUser;
	private String confirmUser;
	private String cancelUser;
	private Set<NodeJobDTO> nodeJobs = new HashSet<>();
	
	public DeploymentDTO() {}
	

	public DeploymentDTO(DeploymentEntity entity) {
		this.id = entity.getId();
		this.trackingId = entity.getTrackingId();
		this.state = entity.getDeploymentState();
		this.appServerName = entity.getResourceGroup().getName();
		for (ApplicationWithVersion app : entity.getApplicationsWithVersion()) {
			appsWithVersion.add(new AppWithVersionDTO(app.getApplicationName(), app.getVersion()));
		}
		for (DeploymentParameter param : entity.getDeploymentParameters()) {
			deploymentParams.add(new DeploymentParameterDTO(param.getKey(), param.getValue()));
		}
		this.deploymentDate = entity.getDeploymentDate();
		this.environmentName = entity.getContext().getName();
		this.setReleaseName(entity.getRelease().getName());
		this.setRuntimeName(entity.getRuntime().getName());
		this.setRequestUser(entity.getDeploymentRequestUser());
		this.setConfirmUser(entity.getDeploymentConfirmationUser());
		this.setCancelUser(entity.getDeploymentCancelUser());
		for (NodeJobEntity job : entity.getNodeJobs()) {
			nodeJobs.add(new NodeJobDTO(job));
		}
	}

	/**
	 * @deprecated Only here for backwards compatibility of the rest API
	 */
	@Deprecated
	public List<AppWithMvnVersionDTO> getAppsWithMvnVersion() {
		List<AppWithMvnVersionDTO> appsWithMvnVersion = new LinkedList<>();

		for(AppWithVersionDTO app : this.appsWithVersion) {
			appsWithMvnVersion.add(new AppWithMvnVersionDTO(app.getApplicationName(), app.getVersion()));
		}
		
		return appsWithMvnVersion;
	}
	
	/**
	 * @deprecated Only here for backwards compatibility of the rest API
	 */
	@Deprecated
	public String getCancleUser() {
		return this.cancelUser;
	}
}
