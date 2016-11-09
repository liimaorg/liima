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

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.common.exception.GeneratorException;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentGenerationResult {

	@Getter
	@Setter
	private GenerationContext generationContext;

	@Getter
	private List<NodeGenerationResult> nodeGenerationResults = new ArrayList<NodeGenerationResult>();

	@Getter
	@Setter
	private GeneratorException environmentException;


     public void omitAllTemplates(){
	    if(nodeGenerationResults!=null) {
		   for (NodeGenerationResult nodeGenerationResult : nodeGenerationResults) {
			  nodeGenerationResult.omitAllTemplates();
		   }
	    }
	}

	/**
	 * adds an NodeGenerationResult to the ResultList
	 * @param nodeGenerationResult
	 */
	public void addNodeGenerationResult(NodeGenerationResult nodeGenerationResult){
		nodeGenerationResults.add(nodeGenerationResult);
	}

	/**
	 * @return all folders to be executed
	 */
	public List<String> getFoldersToExecute(){
		List<String> foldersToExecute = new ArrayList<String>();
		for (NodeGenerationResult result : nodeGenerationResults) {
			if(result.isNodeEnabled()){
				foldersToExecute.add(result.getFolderToExecute());
			}
		}
		return foldersToExecute;
	}
	/**
	 * checks if there were generation Errors on all Nodes
	 * @return
	 */
	public boolean hasErrors() {
		for (NodeGenerationResult result : nodeGenerationResults) {
			if (result.hasErrors()) {
				return true;
			}
		}
		if (environmentException != null) {
			return true;
		}
		return allNodesDisabled();
	}

    private boolean allNodesDisabled(){
	   for (NodeGenerationResult result : nodeGenerationResults) {
		  if(result.isNodeEnabled()){
			 return false;
		  }
	   }
	   return true;
    }

	public List<TemplatePropertyException> getPreprocessResults(){
		List<TemplatePropertyException> result = new ArrayList<>();
		for (NodeGenerationResult nodeGenerationResult : nodeGenerationResults) {
			result.addAll(nodeGenerationResult.getPreprocessResults());
		}
		return result;
	}

	public String getErrorMessage() {
		StringBuilder sb = new StringBuilder();
		for (NodeGenerationResult result : nodeGenerationResults) {
			if(result.hasErrors()){
				if (generationContext != null && generationContext.getContext() != null) {
					sb.append("Context: ");
					sb.append(generationContext.getContext().getName());
					sb.append(System.lineSeparator());
				}
				sb.append(result.getErrorMessage());
			}
		}
		if(environmentException != null){
			sb.append(environmentException.getMessage());
		}
		if(allNodesDisabled()){
			sb.append("No nodes enabled");
		}
		return sb.toString();
	}

	/**
	 * Returns the Node Info for this Environment
	 * @return
	 */
	public String getGeneratedNodeInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("Context: ");
		sb.append(generationContext.getContext().getName());
		sb.append(System.lineSeparator());
		for (NodeGenerationResult result : nodeGenerationResults) {
			sb.append(" - ");
			sb.append(result.getGeneratedNodeInfo());
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
