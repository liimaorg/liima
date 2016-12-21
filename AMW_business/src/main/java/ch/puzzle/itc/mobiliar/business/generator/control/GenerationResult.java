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

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;

/**
 * The GenerationResult holds all Results of an entire Generation, which means:
 * List of EnvironmentGenerationResult
 *         List of Node Results
 *              ---
 */
public class GenerationResult {
	
	@Getter
	private List<EnvironmentGenerationResult> environmentGenerationResults = new ArrayList<EnvironmentGenerationResult>();
	
	@Setter
	@Getter
	private DeploymentEntity deployment;
	
	/**
	 * adds an EnvironmentGeneratiopnResult to the ResultList
	 * @param environmentGenerationResult
	 */
	public void addEnvironmentGenerationResult(EnvironmentGenerationResult environmentGenerationResult){
		environmentGenerationResults.add(environmentGenerationResult);
	}

	/**
	 * checks if there were generation Errors on all Environments
	 * @return
	 */
	public boolean hasErrors() {
		for (EnvironmentGenerationResult result : environmentGenerationResults) {
			if(result.hasErrors()){
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the Error Message as String for the whole generation
	 * @return
	 */
	public String getErrorMessage() {
		StringBuilder sb = new StringBuilder();
		for (EnvironmentGenerationResult result : environmentGenerationResults) {
			if(result.hasErrors()){
				sb.append(result.getErrorMessage());
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the Node infos for the GenerationResult
	 * @return
	 */
	public String getGeneratedNodeInfo() {
		StringBuilder sb = new StringBuilder();
		for (EnvironmentGenerationResult result : environmentGenerationResults) {
			sb.append(result.getGeneratedNodeInfo());
		}
		return sb.toString();
	}
	
}
