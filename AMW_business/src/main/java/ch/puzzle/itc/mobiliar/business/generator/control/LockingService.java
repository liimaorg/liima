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

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity.shakedownTest_state;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;

@Stateless
public class LockingService
{
	
	@Inject
	EntityManager entityManager;
	
	@Inject
	Logger log;
	
	private int lockRetries = 3;
	
	
	/**
	 * Lock a deployment. Must be called in a transaction.
	 * 
	 * @param id Deployment id to lock
	 * @return The locked Deployment entity or null if not possible
	 */
	public DeploymentEntity lockDeployment(Integer id) {
		int count = 0;
		int backoff = 300;
		
		log.fine("Locking deployment " + id);
		for(; count <= lockRetries; count++) {
			try {
				return entityManager.find(DeploymentEntity.class, id, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
			} catch (LockTimeoutException e) {
				backoff = getBackoff(backoff);
				log.info("Retring to lock deployment " + id + " in " + backoff + " ms");
				try {
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					throw new AMWRuntimeException("Exception while waiting to lock deployment " + id, e1);
				}
			}
		}

		log.warning("Locking of deployments " + id + " not possible!");
		return null;
	}
	
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
		DeploymentEntity d = lockDeployment(id);
		
		if(d == null) {
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
		if (id != null) {
			try {
				boolean success = false;
				ShakedownTestEntity s = entityManager.find(ShakedownTestEntity.class, id, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
				
				if (!s.isExecuted()) {
					log.info("Test of ShakedownTest " + s.getId() + "...");
					s.setExecuted(true);
					s.setShakedownTestStateDisplayName(shakedownTest_state.inprogress);
					entityManager.persist(s);
					success=true;	
				} else {
					log.info("Test " + s.getId() + " was already executed.");
				}
				return success;
			} catch (Exception e) {
				log.info("Test " + id + " could not be locked.");
			}
		}
		return false;
	}
	
	/**
	 * Calculates a new backoff based on the previous backoff. Loosely based on
	 * a logarithmic algorithm.
	 */
	private int getBackoff(int currentBackofflMillis) {
	    double delta = 0.3d * currentBackofflMillis;
	    return (int) (1.5 * currentBackofflMillis + (Math.random() * (delta + 1)));
	  }
}
