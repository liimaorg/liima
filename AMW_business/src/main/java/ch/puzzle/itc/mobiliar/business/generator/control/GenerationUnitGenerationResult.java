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

import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * This Class holds the Generation Result for an entire Generation of a GenerationUnit 
 */
public class GenerationUnitGenerationResult {
	@Getter
	@Setter
	private List<GeneratedTemplate> generatedTemplates = new ArrayList<GeneratedTemplate>();

	@Getter
	private List<TemplatePropertyException> errorMessages = new ArrayList<TemplatePropertyException>();

	@Getter
	@Setter
	private GenerationUnitPreprocessResult generationUnitPreprocessResult;
	
	/**
	 * adds an general Unit Exception
	 * @param e
	 */
	public void addErrorMessage(TemplatePropertyException e){
		errorMessages.add(e);
	}
	
	public boolean isSuccess() {
		if(errorMessages != null && !errorMessages.isEmpty()){
			return false;
		}
		return !hasErrorsInGeneratedTemplates() && !hasErrorsInPreprocessing();
	}

	private boolean hasErrorsInGeneratedTemplates() {
		for (GeneratedTemplate generatedTemplate : generatedTemplates) {
			if(generatedTemplate.hasErrors()){
				return true;
			}
		}
		return false;
	}

	private boolean hasErrorsInPreprocessing() {
		return (generationUnitPreprocessResult != null && generationUnitPreprocessResult.hasErrors());
	}

	/**
	 * @return the Errors as String
	 */
	public String getErrorMessageAsString() {
		StringBuilder sb = new StringBuilder();
		if(!isSuccess()){
			if(hasErrorsInPreprocessing()){
				sb.append(generationUnitPreprocessResult.getErrorMessageAsString());
			}
			if(errorMessages != null && !errorMessages.isEmpty()){
				sb.append("General Unit Template Errors\n");
				for (TemplatePropertyException e : errorMessages) {
					sb.append(e.getMessage()+ "\n");
				}
			}
			if(generatedTemplates != null && !generatedTemplates.isEmpty()){
				if(hasErrorsInGeneratedTemplates()){
					sb.append("Template Errors\n");
					for (GeneratedTemplate generatedTemplate : generatedTemplates) {
						if(generatedTemplate.hasErrors()){
							sb.append(generatedTemplate.getErrorMessageAsString());
						}
					}
				}
			}
		}
		return sb.toString();
	}

	public List<TemplatePropertyException> getPreprocessResults() {
		if(hasErrorsInPreprocessing()){
			return generationUnitPreprocessResult.getErrorMessages();
		}
		return new ArrayList<>();
	}
}
