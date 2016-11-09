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

package ch.puzzle.itc.mobiliar.builders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.STS;
import ch.puzzle.itc.mobiliar.business.shakedown.control.ShakedownTestGenerationResult;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;

public class ShakedownTestGenerationResultBuilder {

	public static ShakedownTestGenerationResult mockGenerationResultWithErrors(ShakedownTestEntity test, DeploymentEntity deployment) {
		ShakedownTestGenerationResult result = mock(ShakedownTestGenerationResult.class);
		when(result.getShakedownTestEntity()).thenReturn(test);
		when(result.getDeployment()).thenReturn(deployment);

		when(result.hasErrors()).thenReturn(true);
		when(result.getErrorMessage()).thenReturn("errormessage");

		return result;
	}

	public static ShakedownTestGenerationResult buildGenerationResultSuccess(ShakedownTestEntity test, DeploymentEntity deployment) {
		ShakedownTestGenerationResult result = new ShakedownTestGenerationResult();
		result.setShakedownTestEntity(test);
		result.setDeployment(deployment);
		STS sts = new STS();
		List<String> shakedowntests = new ArrayList<String>();
		shakedowntests.add("foo");
		sts.setShakedowntests(shakedowntests );
		result.addTestingTemplate(sts);

		return result;
	}

	public static ShakedownTestGenerationResult buildGenerationResultSuccessEmptySTS(ShakedownTestEntity test, DeploymentEntity deployment) {
		ShakedownTestGenerationResult result = new ShakedownTestGenerationResult();
		result.setShakedownTestEntity(test);
		result.setDeployment(deployment);
		result.setTestingTemplates(Collections.<STS> emptyList());

		return result;
	}

}
