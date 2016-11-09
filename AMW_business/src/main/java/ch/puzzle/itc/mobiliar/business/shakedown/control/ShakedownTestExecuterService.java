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

package ch.puzzle.itc.mobiliar.business.shakedown.control;

import ch.puzzle.itc.mobiliar.business.generator.control.LockingService;
import ch.puzzle.itc.mobiliar.business.generator.control.ShakedownTestGeneratorDomainService;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service to start ShakedownTest asynchronously for the generated Configuration
 */
@Stateless
public class ShakedownTestExecuterService {

	@Inject
	private ShakedownTestService shakedownTestService;

	@Inject
	private ShakedownTestGeneratorDomainService generatorDomainService;

	@Inject
	private ShakedownTestAsynchronousExecuter shakedownTestAsynchronousExecuter;

	@Inject
	private LockingService lockingService;

	@Inject
	private EntityManager entityManager;

	@Inject
	private Logger log;

	/**
	 * Executes the ShakedownTest
	 * 
	 * @param shakeDownTestId
	 */
	@Asynchronous
	public void generateConfigurationAndExecuteShakedownTest(Integer shakeDownTestId) {
		ShakedownTestEntity shakedownTest = shakedownTestService.getShakedownTestById(shakeDownTestId);

		if(shakedownTest != null){
			// lock ShakedownTest
			boolean lockSuccessful = lockingService.lockShakedownTestForTesting(shakeDownTestId);
			if (lockSuccessful) {
				log.info("Locking of ShakedownTest " + shakedownTest.getId() + " successful");

				// Reload the entity because it has been changed by another
				// transaction within the locking mechanism.
				entityManager.clear();
				shakedownTest = entityManager.find(ShakedownTestEntity.class, shakedownTest.getId());

				// generate Config
				ShakedownTestGenerationResult result = generatorDomainService.generateConfigurationForShakedownTest(shakedownTest);

				if (result != null && !result.hasErrors()) {
					// execute tests
					shakedownTestAsynchronousExecuter.executeShakedownTest(result);
				}
			}else{
				log.log(Level.SEVERE, "No ShakedownTestEntity found for shakeDownTestId " + shakeDownTestId);
			}
		}
	}
}
