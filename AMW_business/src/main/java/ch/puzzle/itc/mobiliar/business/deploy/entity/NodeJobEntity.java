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

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.DeploymentState;
import ch.puzzle.itc.mobiliar.common.exception.DeploymentStateException;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TAMW_NODEJOB")
public class NodeJobEntity {

	@Getter
	@Setter
	@TableGenerator(name = "nodeJobIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "nodeJobId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "nodeJobIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;
	
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DeploymentState deploymentState;

	@Getter
	@Setter
	@ManyToOne
	@JoinColumn(name = "deployment_id", nullable=false)
	private DeploymentEntity deployment;
	
	public enum NodeJobStatus {
		//the names of the enums are used in the database
		SUCCESS, FAILED, RUNNING(SUCCESS, FAILED);
		
		@Getter
		private List<NodeJobStatus> allowedTransitions;

		NodeJobStatus(NodeJobStatus... allowedTransitions){
			this.allowedTransitions = Arrays.asList(allowedTransitions);
		}
		
		public boolean isTransitionAllowed(NodeJobStatus newState){
			return allowedTransitions.contains(newState);
		}
		
		public static NodeJobStatus getNodeJobStatusByString(String statusStr){
			NodeJobStatus status = null;
	    	for (NodeJobStatus nodeStatus : NodeJobStatus.values()) {
				if(nodeStatus.name().equalsIgnoreCase(statusStr)){
					status = nodeStatus;
				}
			}
			return status;
		}
	};
	
	/** Deployment state this job belongs to  */
	@Getter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NodeJobStatus status;

	public void setStatus(NodeJobStatus newStatus) throws DeploymentStateException {
		if(status != null && !this.status.isTransitionAllowed(newStatus)){
			throw new DeploymentStateException("Can't set status: " + newStatus +". Allowed states: " + status.allowedTransitions);
		}
		
		status = newStatus;
	}
	
	
}
