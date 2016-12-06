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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.common.exception.ScriptExecutionException;
import ch.puzzle.itc.mobiliar.common.exception.ScriptExecutionException.REASON;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;
import ch.puzzle.itc.mobiliar.common.util.SystemCallTemplate;

/**
 * A class to handle the execution of native system calls (e.g. the execution of
 * deployment scripts and similar).
 * 
 * The provided methods handle the whole feedback-chain in case of errors when
 * executing scripts.
 * 
 */
public class RunSystemCallService
{

	private String lineSeparator = System.getProperty("line.separator");

	@Inject
	protected Logger log;

	/**
	 * Looks up a given folder of configurations, looks up the script files
	 * to be executed, makes them executable and tries to invoke them as
	 * native system processes. If multiple scripts exist and one of them
	 * fails, the method interrupts after the first failing.
	 * 
	 * @param folder
	 *               - the relative path of the configuration-folder for
	 *               which the execution scripts shall be invoked.
	 * @return Result Output of Script
	 * @throws ScriptExecutionException
	 *                - if the execution of one of the scripts was not
	 *                successful. If the script was executed but had errors
	 *                (exited with an error code), the message of this
	 *                exception should contain the given information provided
	 *                by the process itself.
	 */
	public String getAndExecuteScriptFromGeneratedConfig(String folder)
			throws ScriptExecutionException {

		List<File> scriptPath = getScriptFiles(folder);

		StringBuilder result = new StringBuilder();

		for (File file : scriptPath) {
			String filePath = makeScriptExecutableAndGetAbsolutePath(file);
			if (filePath != null) {
				result.append("output of Script " + filePath);
				result.append(lineSeparator);
				result.append(executeScript(filePath));
			} else {
				String message = "File permissions of "
						+ file.getName()
						+ " could not be set";
				log.log(Level.WARNING, message);
				throw new ScriptExecutionException(message,
						REASON.PERMISSION);
			}
		}
		return result.toString();
	}

	/**
	 * (Tries to) execute the script with the given script path.
	 * 
	 * @param scriptPath
	 *               - the abolute file path of the script to be executed
	 * @return String result
	 * @throws ScriptExecutionException
	 *                - an exception if anything fails in the execution
	 *                process.
	 */
	private String executeScript(String scriptPath)
			throws ScriptExecutionException {
		if (scriptPath != null) {
			String result = runSystemCall(scriptPath);
			log.info(scriptPath + " was excuted successfully.");
			return result;
		} else {
			throw new ScriptExecutionException(
					"Undefined script path", REASON.NOTAVAILABLE);
		}
	}

	/**
	 * This method executes the given command as a native process and traces
	 * the system output. All data that shall be logged as error messages
	 * should be wrapped with " @ ... } ". (e.g. " @ there was a failure
	 * somewhere} ") The message then is recorded and attached to the thrown
	 * {@link ScriptExecutionException} if the process exits with an error
	 * code (!=0)
	 * 
	 * @param command
	 *               - the command to be executed natively.lso includes the
	 *               error stack trace (System.err)
	 * @return parsed result as Script
	 * @throws ScriptExecutionException
	 *                - if the script was not able to execute properly or
	 *                exited with an error code. Includes the recorded error
	 *                messages as part of the message.
	 */
	private String runSystemCall(String command)
			throws ScriptExecutionException {
		Runtime r = Runtime.getRuntime();
		try {
			log.info("Execute command: \"" + command + "\"");
			Process p = r.exec(command);
			InputStream in = p.getInputStream();
			BufferedInputStream buf = new BufferedInputStream(in);
			InputStreamReader inread = new InputStreamReader(buf);
			BufferedReader bufferedreader = new BufferedReader(inread);
			StringBuilder sb = new StringBuilder();
			StringBuilder systemCallResult = new StringBuilder();
			String line;
			while ((line = bufferedreader.readLine()) != null) {
				// TODO schreibe resultat in eine logdatei
				// TODO ev output parsen und fehler loggen
				log.info("" + line);
				if (line.startsWith("@{") && line.endsWith("}")) {
					sb.append(line.substring(2,
							line.length() - 1)).append(
							'\n');
				}
				systemCallResult.append(line);
				systemCallResult.append(lineSeparator);
			}
			try {
				if (p.waitFor() != 0) {
					throw new ScriptExecutionException(
							sb.toString().trim().isEmpty() ? "exit value = "
									+ p.exitValue()
									: sb.toString()
											.trim(),
							REASON.EXECUTIONEXCEPTION);
				}
				return systemCallResult.toString();
			} catch (InterruptedException e) {
				throw new ScriptExecutionException(
						"Script execution interrupted",
						REASON.GENERIC, e);
			} finally {
				// Close the InputStream
				bufferedreader.close();
				inread.close();
				buf.close();
				in.close();
			}
		} catch (IOException e) {
			throw new ScriptExecutionException(
					"Could not execute systemcall",
					REASON.GENERIC, e);
		}
	}

	/**
	 * Takes a script file, tries to make it executable and returns the
	 * absolute path of the file
	 * 
	 * @param scriptFile
	 *               - the file to be made executable
	 * @return - the absolute file path or null if it was not possible to
	 *         make the script executable
	 */
	private String makeScriptExecutableAndGetAbsolutePath(File scriptFile) {
		String script = null;

		if (scriptFile.isFile() && scriptFile.exists()) {
			try {
				// script muss ausführbar sein!
				scriptFile.setExecutable(true);
				script = scriptFile.getAbsolutePath();
			} catch (SecurityException e) {
				log.log(Level.WARNING, "File " + scriptFile.getName() + " can't be set executable: " + e.getMessage());
			}

		}
		return script;
	}

	/**
	 * Takes a relative folder name (without separator at the end) as
	 * argument, combines it with the defined path to which the config files
	 * should be generated to (Property {@link ConfigKey#GENERATOR_PATH}) and
	 * tries to find the script files to be executed.
	 * 
	 * @param folder
	 *               - a relative folder name
	 * @return a list of Scriptfiles within the given folder.
	 */
	private List<File> getScriptFiles(String folder) {
		List<File> scriptFiles = new ArrayList<File>();

		String targetPath = ConfigurationService
				.getProperty(ConfigKey.GENERATOR_PATH);

		if (targetPath == null) {
			log.log(Level.WARNING, "Property "+ ConfigKey.GENERATOR_PATH + " not set!");
		} else {
			File file = new File(targetPath + File.separator + folder);
			scriptFiles = findScripts(file);
		}
		return scriptFiles;
	}

	/**
	 * Checks if the file is defined to be executed (
	 * {@link SystemCallTemplate#isSystemCallTemplateByName(String)}) and
	 * recursively steps into the file hierarchy and adds these executable
	 * script files to the result list if the given parameter is a directory
	 * 
	 * @param file
	 *               - a file or directory which chall be checked for
	 *               executable scripts
	 * @return - a list of executable script files
	 */
	private List<File> findScripts(File file) {
		List<File> files = new ArrayList<File>();
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				files.addAll(findScripts(subFile));
			}
		} else {
			if (SystemCallTemplate.isSystemCallTemplateByName(file
					.getName())) {
				files.add(file);
			}
		}
		return files;
	}

}
