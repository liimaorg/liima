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

package ch.puzzle.itc.mobiliar.business.generator.control;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity.shakedownTest_state;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import java.util.logging.Logger;

@Stateless
public class LockingService
{
	
	@Inject
	EntityManager entityManager;
	
	@Inject
	Logger log;
	

	/**
	 * Locks the deployment entity and updates it's status to make sure that the deployment is only executed once.
	 * It requires a new transaction so the changes are committed to the database at the end of the method.
	 * 
	 * @param id
	 * @param generationModus
	 * @return true if the deployment has been locked for this application server and can be executed, false otherwise
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean markDeploymentAsRunning(Integer id, GenerationModus generationModus){
		DeploymentEntity d;
		
		log.fine("Locking deployment " + id);
		try {
			d = entityManager.find(DeploymentEntity.class, id, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
		} catch (LockTimeoutException e) {
			log.warning("Locking of deployments " + id + " not possible!");
			return false;
		}
		
		if(d.isRunning()) {
			log.info("Deployment " + id + " is already running, skipping!");
			return false;
		}

		log.info("Update state of deployment " + d.getId());
		if (GenerationModus.DEPLOY.equals(generationModus)) {
			d.setDeploymentState(DeploymentState.progress);
		}else if(GenerationModus.SIMULATE.equals(generationModus)){
			d.setDeploymentState(DeploymentState.simulating);
		}else if(GenerationModus.PREDEPLOY.equals(generationModus)){
			d.setDeploymentState(DeploymentState.PRE_DEPLOYMENT);
		}else{
			throw new RuntimeException("Generation Modus " + generationModus + " can not be executed and locked");
		}
		
		entityManager.persist(d);
		
		return true;
	}
	

	/**
	 * Locks a shakedown test on the database (therefore it runs in its own transaction) to make sure,
	 * that the test is only executed once
	 * 
	 * @param id
	 * @return true if the test has successfully been locked and can be executed, false otherwise
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean markShakedownTestAsRunning(Integer id){
		ShakedownTestEntity s;
		log.fine("Locking ShakedownTest " + id);
		try{
			s = entityManager.find(ShakedownTestEntity.class, id, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
		}
		catch (LockTimeoutException e) {
			log.warning("Locking of ShakedownTest " + id + " not possible!");
			return false;
		}

		if (s.isExecuted()) {
			log.info("Test " + s.getId() + " was already executed.");
			return false;
		}
		
		s.setExecuted(true);
		s.setShakedownTestStateDisplayName(shakedownTest_state.inprogress);
		entityManager.persist(s);
		return true;	
	}
}
