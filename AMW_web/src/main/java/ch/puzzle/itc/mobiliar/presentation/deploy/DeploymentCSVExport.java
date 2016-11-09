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

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.DeploymentParameter;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.Tuple;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;
import ch.puzzle.itc.mobiliar.presentation.util.UserSettings;

@WebServlet("/deploymentCsvExport")
public class DeploymentCSVExport extends HttpServlet{

	private static final long serialVersionUID = 1L;

	@Inject DeployScreenDataProvider deployScreenDataProvider;
	@Inject DeployScreenController deployScreenController;
	@Inject UserSettings userSettings;

	private Writer w;
	private static final String SEPARATOR = ConfigurationService.getProperty(ConfigKey.CSV_SEPARATOR);
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		Tuple<Set<DeploymentEntity>, Integer> deployments = deployScreenController.loadPendingDeployments(false, null, null, deployScreenDataProvider.getAppliedFilterList(), deployScreenDataProvider.getSortingColumn(), deployScreenDataProvider.getSortingDirection());
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition", "attachment; filename=deployments_"+new SimpleDateFormat("yyyy-MM-dd_HHmm").format(new Date())+".csv");
		// workaround for bug in IE8 an lower: http://support.microsoft.com/kb/323308
		resp.setHeader("Pragma", "cache");
		resp.setHeader("Cache-Control", "private, must-revalidate");

		w = resp.getWriter();
		w.append("Id").append(SEPARATOR).
		append("Tracking Id").append(SEPARATOR).
		append("Deployment state").append(SEPARATOR).
		append("Build success").append(SEPARATOR).
		append("Deployment executed").append(SEPARATOR).
		append("App server").append(SEPARATOR).
		append("Applications").append(SEPARATOR).
		append("Deployment release").append(SEPARATOR).
		append("Environment").append(SEPARATOR).
		append("Target platform").append(SEPARATOR).
		append("Deployment parameters").append(SEPARATOR).
		append("Creation date").append(SEPARATOR).
		append("Request user").append(SEPARATOR).
		append("Deployment date").append(SEPARATOR).
		append("Configuration to deploy").append(SEPARATOR).
		append("Deployment confirmed").append(SEPARATOR).
		append("Confirmation date").append(SEPARATOR).
		append("Confirmation user").append(SEPARATOR).
		append("Cancel date").append(SEPARATOR).
		append("Cancel user").append(SEPARATOR).
		append("Status message").append(SEPARATOR)
		.append('\n');

		for(DeploymentEntity d : deployments.getA()){
			write(d.getId());
			write(d.getTrackingId());
			write(d.getDeploymentState().toString());
			write(d.isBuildSuccess());
			write(d.isExecuted());
			write(d.getResourceGroup().getName());
			write(formatAppsWithVersion(d.getApplicationsWithVersion()));
			write(d.getRelease().getName());
			write(d.getContext().getName());
			write(d.getRuntime() != null ? d.getRuntime().getName() : null);
			write(formatDeploymentParameters(d.getDeploymentParameters()));
			write(d.getDeploymentJobCreationDate());
			write(d.getDeploymentRequestUser());
			write(d.getDeploymentDate());
			write(d.getStateToDeploy());
			write(d.getDeploymentConfirmed());
			write(d.getDeploymentConfirmationDate());
			write(d.getDeploymentConfirmationUser());
			write(d.getDeploymentCancelDate());
			write(d.getDeploymentCancelUser());
			write(formatStateMessage(d.getStateMessage()));
			w.append('\n');
		}
	}

	private String formatAppsWithVersion(List<ApplicationWithVersion> appsWithVersion) {
		StringBuilder buffer = new StringBuilder();

		for (int index=0; index < appsWithVersion.size(); index++){
			ApplicationWithVersion app = appsWithVersion.get(index);
			buffer.append(app.getApplicationName()).append(' ').append(app.getVersion());
			if (index+1 != appsWithVersion.size()) {
				buffer.append('\n');
			}
		}

		return buffer.toString();
	}
	
	private String formatDeploymentParameters(List<DeploymentParameter> deployParams) {
		StringBuilder buffer = new StringBuilder();
		
		for (int index=0; index < deployParams.size(); index++){
			DeploymentParameter param = deployParams.get(index);
			buffer.append(param.getKey()).append(' ').append(param.getValue());
			if (index+1 != deployParams.size()) {
				buffer.append('\n');
			}
		}

		return buffer.toString();
		
	}

	private String formatStateMessage(String stateMessage){
		if (stateMessage != null){
			stateMessage = stateMessage.replaceAll("\"", "");
			stateMessage = stateMessage.replaceAll("\n", "");
		}

		return stateMessage;
	}


	private void write(Object o) throws IOException{
		if(o instanceof Integer){
			w.append(String.valueOf(o)).append(SEPARATOR);
		}
		else if(o instanceof Boolean){
			w.append(o.toString()).append(SEPARATOR);
		}
		else if(o instanceof Date){
			w.append('\"').append(DATE_FORMAT.format(o)).append('\"').append(SEPARATOR);
		}
		else{
			w.append('\"').append(o==null ? "" : String.valueOf(o)).append('\"').append(SEPARATOR);
		}
	}





}
