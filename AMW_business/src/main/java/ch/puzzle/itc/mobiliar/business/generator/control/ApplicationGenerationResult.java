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

import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import lombok.Getter;
import lombok.Setter;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

/**
 * This Class holds the Result of a Generation for an Application
 */
public class ApplicationGenerationResult {
	@Getter
	@Setter
	private ResourceEntity application;
	
	@Getter
	@Setter
	private List<GenerationUnitGenerationResult> generationResults = new ArrayList<GenerationUnitGenerationResult>();

	/**
	 * checks if there were errors during generation for both applicationResults
	 * 
	 * @return
	 */
	public boolean hasErrors() {
		for (GenerationUnitGenerationResult result : generationResults) {
			if(!result.isSuccess()){
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the Errors as String
	 */
	public String getErrorMessageAsString() {
		StringBuilder sb = new StringBuilder();
		if(generationResults != null && !generationResults.isEmpty() && hasErrors()){
			sb.append("Application Template Errors\n");
			for (GenerationUnitGenerationResult result : generationResults) {
				if(!result.isSuccess()){
					sb.append(result.getErrorMessageAsString());
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Returns all generated Templates
	 * @return
	 */
	public List<GeneratedTemplate> getGeneratedTemplates() {
		List<GeneratedTemplate> templates = new ArrayList<GeneratedTemplate>();
		if(generationResults != null && !generationResults.isEmpty()){
			for (GenerationUnitGenerationResult result : generationResults) {
				templates.addAll(result.getGeneratedTemplates());
			}
		}
		return templates;
	}
	public List<TemplatePropertyException> getPreprocessResults() {
		List<TemplatePropertyException> result = new ArrayList<>();
		for (GenerationUnitGenerationResult generationResult : generationResults) {
			result.addAll(generationResult.getPreprocessResults());
		}
		return result;
	}

}
