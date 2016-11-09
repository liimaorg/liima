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
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.STS;
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.TestSet;
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.TestSet.OverallStatus;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Service to asynchronously execute the Shakedown Tests
 */
@Stateless
public class ShakedownTestAsynchronousExecuter {

	@Inject
	private ShakedownTestExecutionResultHandlerService shakedownTestExecutionResultHandlerService;

	@Inject
	private ShakedownTestRunner shakedownTestRunner;

	@Inject
	private Logger log;


	/**
	 * Executes the ShakedownTest
	 * 
	 * @param generationResult
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void executeShakedownTest(ShakedownTestGenerationResult generationResult) {
		ShakedownTestEntity shakedownTest = generationResult.getShakedownTestEntity();

		if (!generationResult.hasErrors()) {
			try {
				log.log(Level.INFO, "Starting ShakedownTest: " + shakedownTest.getTrackingId());
				ShakedownTestEntity result = execute(generationResult);
				// Handle Result
				if (result.isSuccess()) {
					shakedownTestExecutionResultHandlerService.handleSuccessfulShakedownTest(result);
				} else {
					shakedownTestExecutionResultHandlerService.handleUnsuccessfulShakedownTest(result.getTestResult(), shakedownTest.getId());
				}
			} catch (Exception e) {
				shakedownTestExecutionResultHandlerService.handleUnsuccessfulShakedownTest(e, shakedownTest.getId());
			}
		} else {
			shakedownTestExecutionResultHandlerService.handleUnsuccessfulShakedownTest(generationResult.getErrorMessage(), shakedownTest.getId());
		}
	}

	private ShakedownTestEntity execute(ShakedownTestGenerationResult generationResult) {
		List<STS> testingTemplates = generationResult.getTestingTemplates();
		ShakedownTestEntity finalShake = generationResult.getShakedownTestEntity();

		if (testingTemplates == null || testingTemplates.isEmpty()) {
			finalShake.setTestResult("No STS available");
			finalShake.setShakedownTestStateDisplayName(shakedownTest_state.failed);
			log.info("STS " + finalShake.getId() + ": No STS available");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("<table>");
			OverallStatus state = OverallStatus.success;
			for (STS s : testingTemplates) {
				sb.append("<tr><td>").append(s.getRemoteHost()).append("</td><td>");
				if (s.getShakedowntests() == null || s.getShakedowntests().isEmpty()) {
					sb.append("No tests available");
					if (state != OverallStatus.failed) {
						state = OverallStatus.warning;
					}
				} else {
					TestSet set = shakedownTestRunner.executeShakedownTest(s);
					sb.append(set.getTestMessage());
					if (set.getOverallStatus() == OverallStatus.warning && state != OverallStatus.failed) {
						state = OverallStatus.warning;
					} else if (set.getOverallStatus() == OverallStatus.failed) {
						state = OverallStatus.failed;
					}
				}
				sb.append("</td></tr>");
			}
			sb.append("</table>");
			log.info("STS " + finalShake.getId() + ": " + sb.toString());
			shakedownTest_state shakedownState = null;
			switch (state) {
			case success:
				shakedownState = shakedownTest_state.success;
				break;
			case failed:
				shakedownState = shakedownTest_state.failed;
				break;
			case warning:
				shakedownState = shakedownTest_state.warning;
				break;
			}
			finalShake.setTestResult(sb.toString());
			finalShake.setShakedownTestStateDisplayName(shakedownState);
		}

		return finalShake;
	}
}
