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

package ch.puzzle.itc.mobiliar.business.shakedown.entity;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.persistence.*;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

/**
 * Entity implementation class for Entity: Deployment
 * 
 */
@Entity
@Table(name = "TAMW_shakedownTest")
public class ShakedownTestEntity implements Serializable {
	public static final String SEQ_NAME = "trackingId";

	public enum shakedownTest_state {
		success("success"), failed("failed"), warning("warning"), scheduled("scheduled"), inprogress("progress");

		private String displayName;
		private shakedownTest_state(String displayName){
			this.displayName = displayName;
		}
		public String getDisplayName(){
			return this.displayName;
		}
		public void setDisplayName(String displayName){
			this.displayName = displayName;
		}
	}

	private static final long serialVersionUID = 1L;;



	@Getter
	@Setter
	@TableGenerator(name = "shakedownTestIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "shakedownTestId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "shakedownTestIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;

	@Getter
	@Setter
	private Integer trackingId;

	@Getter
	@Setter
	@ManyToOne
	private DeploymentEntity deployment;

	@Getter
	@Setter
	@ManyToOne
	private ResourceEntity applicationServer;

	@Getter
	@Setter
	@ManyToOne
	private ContextEntity context;

	@Getter
	@Setter
	@Column(nullable = false)
	private boolean isExecuted;

	@Getter
	@Setter
	@Column(length = 65536)
	@Lob
	private String testResult;

	private String shakedownTestState;

	@Column(length = 65536)
	@Lob
	private String appsFromAppServer;

	@ManyToOne
	@Getter
	@Setter
	private ReleaseEntity release;

	@ManyToOne
	@Getter
	@Setter
	private ResourceGroupEntity resourceGroup;

	@Getter
	@Version
	private long v;

	@Getter
	@Setter
	private Date testDate;

	public static class ApplicationsFromApplicationServer {

		@Getter
		@Setter
		private String applicationName;

		@Getter
		@Setter
		private Integer applicationId;

		@JsonCreator
		public ApplicationsFromApplicationServer(@JsonProperty("applicationName") String applicationName,
												 @JsonProperty("applicationId") Integer applicationId) {
			this.applicationId = applicationId;
			this.applicationName = applicationName;
		}

	}

	public ShakedownTestEntity() {
		this.isExecuted = false;
		this.testResult = "";
		this.shakedownTestState = shakedownTest_state.scheduled.displayName;
	}


	@SneakyThrows
	public List<ApplicationsFromApplicationServer> getApplicationsFromApplicationServer() {
		return new ObjectMapper().readValue(new StringReader(appsFromAppServer), new TypeReference<List<ApplicationsFromApplicationServer>>() {});
	}

	@SneakyThrows
	public void setApplicationsFromApplicationServer(List<ApplicationsFromApplicationServer> applicationsFromApplicationServer) {
		this.appsFromAppServer = new ObjectMapper().writeValueAsString(applicationsFromApplicationServer);
	}

	public shakedownTest_state getShakedownTestState() {
		for (shakedownTest_state status : shakedownTest_state.values()) {
			if (shakedownTestState != null && shakedownTestState.equals(status.getDisplayName())){
				return status;
			}
		}
		return null;
	}

	public void setShakedownTestStateDisplayName(shakedownTest_state shakedownTestState) {
		this.shakedownTestState = shakedownTestState.getDisplayName();
	}

	public boolean isSuccess() {
		return shakedownTest_state.success.name().equals(shakedownTestState);
	}

	public boolean isFailed() {
		return shakedownTest_state.failed.name().equals(shakedownTestState);
	}

}
