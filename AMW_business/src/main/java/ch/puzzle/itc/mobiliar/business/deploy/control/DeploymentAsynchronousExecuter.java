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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.GenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorFileWriter;
import ch.puzzle.itc.mobiliar.business.generator.control.RunSystemCallService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.common.exception.ScriptExecutionException;

@Stateless
public class DeploymentAsynchronousExecuter {

	@Inject 
	private DeploymentExecutionResultHandlerService deploymentExecutionResultHandler;
	
	@Inject
	private Logger log;
	
	@Inject
	private RunSystemCallService systemCallService;
	
	@Inject
	protected GeneratorFileWriter generatorFileWriter;
	
	
	/**
	 * executes a Deployment for the given generationResult
	 * 
	 * @param generationResult
	 * @param deployment
	 * @param generationModus
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void executeDeployment(GenerationResult generationResult, DeploymentEntity deployment, GenerationModus generationModus) {
		try {
			log.log(Level.INFO, "Starting Deployment: " + deployment.getTrackingId()+" (tracking id: " + deployment.getTrackingId()+") " + generationModus.getName());
			execute(generationResult, generationModus);
			// Handle Result
			log.log(Level.INFO, "Deployment successful: "+deployment.getId()+" (tracking id: " + deployment.getTrackingId()+")");
			deploymentExecutionResultHandler.handleSuccessfulDeployment(generationModus, generationResult);
		}
		catch (ScriptExecutionException se) {
			log.log(Level.SEVERE, "Deployment not successful: " +deployment.getId()+" (tracking id: " + deployment.getTrackingId()+")", se);
			deploymentExecutionResultHandler.handleUnSuccessfulDeployment(generationModus, deployment, generationResult, se);
		}
		catch (Exception e) {
			log.log(Level.SEVERE, "Deployment not successful: " + deployment.getId()+" (tracking id: " + deployment.getTrackingId()+")", e);
			deploymentExecutionResultHandler.handleUnSuccessfulDeployment(generationModus, deployment, generationResult, e);
		}
	}
	
	private void execute(GenerationResult generationResult, GenerationModus generationModus) throws ScriptExecutionException{
		// We execute the deployment scripts sequentially!
		// This is very important since otherwise, all nodes would go down in parallel and the servers would
		// not be available anymore. Please also note, that if one deployment fails, the loop is
		// interrupted (since the execution method throws an exception).
		// If the deployment of the first node fails, the second one will not be deployed anymore.
		for (final String f : generationResult.getAllFoldersToExecute()) {
			systemCallService.getAndExecuteScriptFromGeneratedConfig(f);
		}
	}
}
