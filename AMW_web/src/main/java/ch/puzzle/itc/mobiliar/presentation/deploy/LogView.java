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
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.presentation.ViewBackingBean;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey.FEATURE_DISABLE_ANGULAR_DEPLOYMENT_GUI;

@ViewBackingBean
public class LogView implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	Integer deploymentId;

	@Getter
	String file;

	@Getter
	String fileContent;

	@Getter
	List<String> availableLogFiles;

	@Inject
    DeploymentBoundary deploymentBoundary;

	@Inject
	PermissionService permissionService;

	@Inject
	Logger log;

	DeploymentEntity deployment;

	public boolean isShowAngularBacklink() {
		return ! ConfigurationService.getPropertyAsBoolean(FEATURE_DISABLE_ANGULAR_DEPLOYMENT_GUI);
	}

	public String getApplicationServerName() {
		if (deployment != null && deployment.getResource() != null) {
			return deployment.getResource().getName() + " " + deployment.getRelease().getName();
		}
		else {
			return StringUtils.EMPTY;
		}
	}

	public void setFile(String file) {
		if (isParameterSet("file", file) || StringUtils.isNotEmpty(file)) {
			this.file = file;
			loadFileContent();
		}
	}

	private boolean isParameterSet(String key, Object value) {
		Object existing = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
		return existing != null && existing.equals(value);
	}

	public void setDeploymentId(Integer deploymentId) {
		this.deploymentId = deploymentId;
		if (deploymentId != null) {
			deployment = deploymentBoundary.getDeploymentById(deploymentId);
			if (canShowDeployment()) {
				findAvailableLogFiles();
				loadFileContent();
			}
			else {
				GlobalMessageAppender.addErrorMessage("Not permitted to show this deployment");
			}
		}
		else {
			GlobalMessageAppender.addErrorMessage("A deployment identifier is required to show log files");
		}
	}

	boolean canShowDeployment() {
		//TODO To the very moment, there has no permission be defined for "showing logs" - according to https://redmine.puzzle.ch/issues/8759, even viewers should be allowed to watch these logs...
		return true;
	}

	void findAvailableLogFiles() {
		availableLogFiles = Arrays.asList(deploymentBoundary.getLogFileNames(deploymentId));
		if (file == null && availableLogFiles.size() > 0) {
			setFile(availableLogFiles.get(0));
		}
		else if (file == null) {
			GlobalMessageAppender.addErrorMessage("No files found for this deployment!");
		}
	}

	void loadFileContent() {
		if (file != null && deploymentId != null && availableLogFiles != null) {
			if (file != null && !availableLogFiles.contains(file)) {
				fileContent = null;
				GlobalMessageAppender
						.addErrorMessage("The requested file does not belong to the given deployment");
			}
			else {
				try {
					fileContent = deploymentBoundary.getDeploymentLog(file);
				}
				catch (IllegalAccessException e) {
					GlobalMessageAppender.addErrorMessage(e);
					log.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}
}
