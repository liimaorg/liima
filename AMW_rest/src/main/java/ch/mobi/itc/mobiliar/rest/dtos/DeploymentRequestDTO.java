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

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "deploymentRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Data
public class DeploymentRequestDTO {
	
	private String appServerName;
	private Date deploymentDate; // optional
	private Date stateToDeploy; // optional
	private String environmentName;
	private List<AppWithVersionDTO> appsWithVersion;
	private Boolean requestOnly = false; // optional
	private Boolean simulate = false; // optional
	private Boolean executeShakedownTest = false; // optional
	private Boolean neighbourhoodTest = false; // optional
	private Boolean sendEmail = false; // optional
	private String releaseName; // optional
	private List<DeploymentParameterDTO> deploymentParameters; // optional
	private List<Integer> contextIds; // optional

	public DeploymentRequestDTO() {	
	}
	
	//copy constructor
	public DeploymentRequestDTO(DeploymentRequestDTO deploymentRequestDto) {
		appServerName = deploymentRequestDto.appServerName;
		// Date is mutable
		if (deploymentRequestDto.getDeploymentDate() == null) {
			deploymentDate = new Date(deploymentRequestDto.getDeploymentDate().getTime());
		}
		if (deploymentRequestDto.getStateToDeploy() != null) {
			stateToDeploy = new Date(deploymentRequestDto.getStateToDeploy().getTime());
		}
		environmentName = deploymentRequestDto.getEnvironmentName();
		appsWithVersion = new LinkedList<>();
		for(AppWithVersionDTO app : deploymentRequestDto.getAppsWithVersion()) {
			appsWithVersion.add(new AppWithVersionDTO(app.getApplicationName(), app.getVersion()));
		}
		requestOnly = deploymentRequestDto.getRequestOnly();
		simulate = deploymentRequestDto.getSimulate();
		executeShakedownTest = deploymentRequestDto.getExecuteShakedownTest();
		neighbourhoodTest = deploymentRequestDto.getNeighbourhoodTest();
		sendEmail = deploymentRequestDto.getSendEmail();
		releaseName = deploymentRequestDto.getReleaseName();

		if (deploymentRequestDto.getDeploymentParameters() != null && !deploymentRequestDto.getDeploymentParameters().isEmpty()) {
			deploymentParameters = new LinkedList<>();
			for (DeploymentParameterDTO parameter : deploymentRequestDto.getDeploymentParameters()) {
				deploymentParameters.add(new DeploymentParameterDTO(parameter.getKey(), parameter.getValue()));
			}
		}
		contextIds = deploymentRequestDto.getContextIds();
	}

	public void addDeploymentParameter(String keyName, String value){
		if (deploymentParameters == null) {
			deploymentParameters = new LinkedList<>();
		}
		deploymentParameters.add(new DeploymentParameterDTO(keyName, value));
	}
	
	
}
