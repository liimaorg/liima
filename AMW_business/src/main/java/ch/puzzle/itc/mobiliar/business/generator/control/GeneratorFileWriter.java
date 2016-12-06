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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.common.exception.GeneratorException;
import ch.puzzle.itc.mobiliar.common.exception.GeneratorException.MISSING;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;

public class GeneratorFileWriter {

	@Inject
	protected Logger log;

	public void createTemporaryFolder(String tempDirectory) throws IOException {
		File tempDir = null;
		tempDir = createFolder(tempDirectory);

		tempDir.deleteOnExit();
	}

	private File createFolder(String pathName) throws IOException {
		File folder = null;
		folder = new File(pathName);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}

	private String removeWindowsChars(String file) {
		return file.replace("\r", "");
	}

	public void generateFileStructure(List<GenerationUnitGenerationResult> generationResults, String basePath, String applicationName)
			throws FileExistsException, IOException {
		generateFileStructure(chooseFilesOfGeneratedTemplates(generationResults), basePath, applicationName);
	}

	
	public void generateFileStructure(Map<String, String> fileMap, String basePath, String applicationName) throws FileExistsException,
			IOException {

		String applicationPath = basePath + (applicationName != null ? File.separator + applicationName : "");

		log.info("Writing files in folder: " + applicationPath);
		String tempDirectory = System.getProperty("java.io.tmpdir");
		createTemporaryFolder(tempDirectory);
		createFolder(basePath);
		createFolder(applicationPath);

		File applicationFolder = null;
		try {
			if (applicationPath != null) {
				applicationFolder = createFolder(tempDirectory + File.separator + Math.random());
			}

			for (String fileName : fileMap.keySet()) {
				generateTemporaryFile(applicationFolder.getAbsolutePath() + File.separator + fileName,
						removeWindowsChars(fileMap.get(fileName)));
			}

			File target = new File(applicationPath);
			if (!target.exists()) {
				target.mkdirs();
			}

			// TODO Eine schönere Lösung finden! (Transaktionalität)
			for (File child : applicationFolder.listFiles()) {
				org.apache.commons.io.FileUtils.moveToDirectory(child, target, true);
			}
		}
		catch (FileExistsException ex) {
			String message = "The file already exist";
			log.log(Level.WARNING, message);
			throw ex;
		}
		finally {
			// Delete temp files
			FileUtils.deleteDirectory(applicationFolder);
		}
	}

	public void generateTemporaryFile(String fileName, String content) throws IOException, FileNotFoundException {

		File temp = new File(fileName);

		File directory = new File(temp.getParent());
		if (!directory.exists()) {
			directory.mkdirs();
		}

		if (temp.isFile() && temp.exists()) {
			String message = "The file already exist: "+ temp.getName();
			log.log(Level.WARNING, message);
		}
		else {
			if (temp.createNewFile()) {
				writeFile(temp, content);
				log.info("File created: " + temp.getName());
			}
			else {
				String message = "The file was not created: " + temp.getName();
				log.log(Level.WARNING, message);
			}
		}

	}

	public void writeFile(File temp, String content) throws IOException, FileNotFoundException {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temp));
		bufferedWriter.write(content);
		bufferedWriter.close();
	}

	private Map<String, String> chooseFilesOfGeneratedTemplates(final List<GenerationUnitGenerationResult> generationResults) {
		
		List<GeneratedTemplate> templates = new ArrayList<GeneratedTemplate>();
		
		for (GenerationUnitGenerationResult generationUnitGenerationResult : generationResults) {
			templates.addAll(generationUnitGenerationResult.getGeneratedTemplates());
		}
		
		final Map<String, String> result = new HashMap<String, String>();
		for (final GeneratedTemplate t : templates) {
			if (t.getPath() != null && !t.getPath().isEmpty()) {
				result.put(t.getPath(), t.getContent());
			}
		}
		return result;

	}

	/**
	 * Returns the folder in which the configuration is generated given the current context (including deployment id)
	 * @param context
	 * @return
	 */
	public String getGenerationSubFolderForContext(GenerationContext context){
		return getGenerationFolderForContextSubFolderPerNode(context) + '_' + getDeploymentSubFolder(context);
	}

	/**
	 * Returns the (absolute) folder where the generation is generated in
	 * 
	 * @param context
	 * @return
	 * @throws GeneratorException
	 */
	public String getGenerationFolderForContext(GenerationContext context) throws GeneratorException {
			// Get Folder where the Generation is generated in
			return getGeneratorTargetPath(context) + File.separator + getGenerationSubFolderForContext(context);
	}
	
	private String getDeploymentSubFolder(GenerationContext context){
		
		String deploymentSubFolder = "";
		
		if(context != null){
			if(GenerationModus.TEST.equals(context.getGenerationModus())){
				if(context.getDeploymentDate() != null){
					deploymentSubFolder = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.S").format(context.getDeploymentDate());
					deploymentSubFolder = deploymentSubFolder + '_' + context.getGenerationModus().getName();
				}
			}else{
				if(context.getDeploymentId() != null){
					deploymentSubFolder = String.valueOf(context.getDeploymentId()) + '_' + context.getGenerationModus().getName();
				}
				return deploymentSubFolder;
			}
		}
		return deploymentSubFolder;
	}
	
	/**
	 * returns the sub folder from AS to Node
	 * 
	 * amw/B/node_01
	 * 
	 * @param generationContext
	 * @return
	 */
	public String getGenerationFolderForContextSubFolderPerNode(GenerationContext generationContext) {
		return generationContext.getApplicationServer().getName() + File.separator
				+ generationContext.getContext().getName() + '_' + generationContext.getNode().getName();
	}
	
	/**
	 * Returns the configured Path where the Generation is saved to.
	 * @param context
	 * @return
	 * @throws GeneratorException
	 */
	protected String getGeneratorTargetPath(GenerationContext context) throws GeneratorException {
		
		// default path is the Main Generator_PATH
		String targetPathDeploy = ConfigurationService.getProperty(ConfigKey.GENERATOR_PATH);
		if(context != null){
			if(GenerationModus.SIMULATE.equals(context.getGenerationModus())){
				// if not set take the default Path
				targetPathDeploy = ConfigurationService.getProperty(ConfigKey.GENERATOR_PATH_SIMULATION, targetPathDeploy);
			}else if(GenerationModus.TEST.equals(context.getGenerationModus())){
				// if not set take the default Path
				targetPathDeploy = ConfigurationService.getProperty(ConfigKey.GENERATOR_PATH_TEST, targetPathDeploy);
			}
		}
		
		if (targetPathDeploy == null) {
			final String message = "Generator target path not configured. Please set the system property \""
					+ ConfigKey.GENERATOR_PATH + "\"";
			log.log(Level.WARNING, message);
			throw new GeneratorException(message, MISSING.TARGETPATH);
		}
		return targetPathDeploy;
	}
	

}
