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

import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity.shakedownTest_state;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the Result of the ShakedownTest Execution
 *
 */
@Stateless
public class ShakedownTestExecutionResultHandlerService {

	@Inject
	private Logger log;


	@Inject
	private ShakedownTestService shakedownTestService;

	/**
	 * Handles a successful Execution
	 * 
	 * @param shakedownTest
	 */
	public void handleSuccessfulShakedownTest(ShakedownTestEntity shakedownTest) {
		ShakedownTestEntity test = shakedownTestService.updateShakedownInfo(shakedownTest.getId(), shakedownTest.getTestResult(), shakedownTest_state.success);
		log.info("Shakedown test " + test.getId() + " executed and result parsed");
	}

	/**
	 * Handles an unsuccessful Execution
	 * 
	 * @param testResult
	 * @param testId
	 */
	public void handleUnsuccessfulShakedownTest(String testResult, Integer testId) {
		ShakedownTestEntity test = shakedownTestService.updateShakedownInfo(testId, testResult, shakedownTest_state.failed);
		log.log(Level.WARNING, "Shakedown test " + test.getId() + " had an issue with its generation", testResult);
	}


	/**
	 * Handles an unsuccessful Execution
	 * 
	 * @param e
	 */
	public void handleUnsuccessfulShakedownTest(Exception e, Integer testId) {
		if (testId != null) {
			ShakedownTestEntity test = shakedownTestService.updateShakedownInfo(testId, e.getMessage(), shakedownTest_state.failed);
			log.log(Level.SEVERE, "Shakedown test " + test.getId() + " execution failed!", e);
		} else {
			log.log(Level.SEVERE, "Shakedown test execution failed!", e);
		}
	}

}
