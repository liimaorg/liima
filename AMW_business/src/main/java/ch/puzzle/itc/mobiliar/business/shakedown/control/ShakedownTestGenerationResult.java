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

import ch.puzzle.itc.mobiliar.business.generator.control.GenerationResult;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.STS;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ShakedownTestGenerationResult extends GenerationResult {

	@Getter
	@Setter
	private ShakedownTestEntity shakedownTestEntity;

	@Getter
	@Setter
	private List<STS> testingTemplates;

	public void addTestingTemplate(STS testingTemplate) {
		if (testingTemplates == null) {
			testingTemplates = new ArrayList<STS>();
		}
		testingTemplates.add(testingTemplate);
	}

	public void addAllTestingTemplates(List<STS> stsTemplates) {
		if (this.testingTemplates == null) {
			this.testingTemplates = new ArrayList<STS>();
		}
		if(stsTemplates != null){
			this.testingTemplates.addAll(stsTemplates);
		}
	}

	@Override
	public boolean hasErrors() {
		return super.hasErrors() || shakedownTestEntity.isFailed();
	}

	@Override
	public String getErrorMessage() {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isEmpty(shakedownTestEntity.getTestResult())) {
			sb.append(shakedownTestEntity.getTestResult());
			sb.append("\n");
		}
		sb.append(super.getErrorMessage());
		return sb.toString();
	}

}
