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

package ch.puzzle.itc.mobiliar.business.deploy.control;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.generator.control.GenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service to handle DeploymentExecution Results after a Deploment
 */
@Stateless
public class DeploymentExecutionResultHandlerService {

	@Inject
	private Logger log;

	@Inject
	private DeploymentBoundary deploymentBoundary;


	/**
	 * Handles a successful Deployment of a generation
	 * 
	 * @param generationModus
	 * @param generationResult
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void handleSuccessfulDeployment(GenerationModus generationModus, GenerationResult generationResult) {

		DeploymentEntity deployment = generationResult.getDeployment();

		deploymentBoundary.updateDeploymentInfoAndSendNotification(generationModus, deployment.getId(), null, deployment.getResource() != null ? deployment.getResource()
				.getId() : null, generationResult);

		if (deployment.isCreateTestAfterDeployment()) {
			deploymentBoundary.createShakedownTestForTrackinIdOfDeployment(deployment.getTrackingId());
		}
	}

	/**
	 * Handles a unsuccessful Deployment of a generation
	 * 
	 * @param generationModus
	 * @param deployment
	 * @param generationResult
	 * @param e
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void handleUnSuccessfulDeployment(GenerationModus generationModus, DeploymentEntity deployment, GenerationResult generationResult, Exception e) {	
		String msg = generationModus.getAction() + "("+deployment.getId()+") failed \n" + e.getMessage();
		log.log(Level.SEVERE, msg, e);
		deploymentBoundary.updateDeploymentInfoAndSendNotification(generationModus, deployment.getId(), msg, deployment.getResource() != null ? deployment.getResource()
				.getId() : null, generationResult);
	}

}
