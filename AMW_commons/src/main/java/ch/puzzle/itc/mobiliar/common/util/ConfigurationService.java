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

package ch.puzzle.itc.mobiliar.common.util;

import lombok.Getter;

/**
 * Class that manages all the System Properties of AMW
 */
public class ConfigurationService {
	public enum ConfigKey {

		LOGS_PATH("amw.logsPath", false),
		
		/** Age of logs to be deleted in minutes */
		LOGS_CLEANUP_AGE("amw.logsLeanupAge", new Integer(7*24*60).toString(), false),
		LOGS_CLEANUP_SCHEDULER_DISABLED("amw.logsCleanupSchedulerDisabled", "false", false),
		/** Path where the generator writes the files */
		GENERATOR_PATH("amw.generatorPath", false),
		 /** Path where the generator writes the files for simulation modus */
		GENERATOR_PATH_SIMULATION("amw.generatorPath.simulation", false),
		/** Path where the generator writes the files for test modus */
		GENERATOR_PATH_TEST("amw.generatorPath.test", false),
		MAIL_DOMAIN("amw.mailDomain", false),
		DELIVER_MAIL("amw.deliverMail", false),
		ENCRYPTION_KEY("amw.encryptionKey", true), 
		LOGOUT_URL("amw.logoutUrl", false), 
		STM_PATH("amw.stmpath", false), 
		STM_REPO("amw.stmrepo", false), 
		TEST_RESULT_PATH("amw.testResultPath", false), 
		DEPLOYMENT_IN_PROGRESS_TIMEOUT("amw.deploymentInProgressTimeout", "3600", false),
		PREDEPLOYMENT_IN_PROGRESS_TIMEOUT("amw.predeploymentInProgressTimeout", "7200", false),
		DEPLOYMENT_PROCESSING_AMOUNT_PER_RUN("amw.deploymentProcessingAmountPerRun", "5", false),
		DEPLOYMENT_SIMULATION_AMOUNT_PER_RUN("amw.deploymentSimulationAmountPerRun", "5", false),
		DEPLOYMENT_PREDEPLOYMENT_AMOUNT_PER_RUN("amw.deploymentPredeploymentAmountPerRun", "5", false),
		DEPLOYMENT_SCHEDULER_DISABLED("amw.deploymentSchedulerDisabled", "false", false),
		DEPLOYMENT_CLEANUP_SCHEDULER_DISABLED("amw.deploymentCleanupSchedulerDisabled", "false", false),
		/** Age of folders to be deleted in minutes */
		DEPLOYMENT_CLEANUP_AGE("amw.deploymentCleanupAge", "240", false),
		VM_DETAIL_URL("amw.vmDetailUrl", false),
		VM_URL_PARAM("amw.vmUrlParam", false),
		CSV_SEPARATOR("amw.csvSeparator", ";", false),
		LOCAL_ENV("amw.localEnv", "Local", false),
        PROVIDABLE_SOFTLINK_RESOURCE_TYPES("amw.providableSoftlinkResourceTypes", false),
        CONSUMABLE_SOFTLINK_RESOURCE_TYPES("amw.consumableSoftlinkResourceTypes", false),
		EXTERNAL_RESOURCE_BACKLINK_SCHEMA("amw.externalResourceBacklinkSchema", false),
		EXTERNAL_RESOURCE_BACKLINK_HOST("amw.externalResourceBacklinkHost", false);


		@Getter
		private String value;
		@Getter
		private String defaultValue;
		@Getter
		private boolean secretValue;

		private ConfigKey(String value, boolean secretValue) {
			this.value = value;
			this.secretValue = secretValue;
		}
		
		private ConfigKey(String value, String defaultValue, boolean secretValue) {
			this(value, secretValue);
			this.defaultValue = defaultValue;
		}

	}

	private static String getPropertyValue(ConfigKey key) {
		return System.getProperty(key.getValue());
	}
	
	/**
	 * Returns the value for the given key if not available the defaultValue
	 * Supplied defaultValue takes precedence over the key default
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getProperty(ConfigKey key, String defaultValue) {
		String propertyValue = getPropertyValue(key);
		
		if(propertyValue != null){
			return propertyValue;
		}
		
		return defaultValue;
	}
	
	public static String getProperty(ConfigKey key) {
		return getProperty(key, key.getDefaultValue());
	}

	/**
	 * Return the default Value for a given ConfigKey
	 * @param key
	 * @return
	 */
	public static String getDefaultValue(ConfigKey key) {
		return key.getDefaultValue();
	}
	
	/**
	 * Returns the value for the given key if not available the defaultValue as Integer
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	protected static Integer getPropertyAsInt(ConfigKey key, Integer defaultValue) {
		String propValue = getPropertyValue(key);
		
		if(propValue != null){
			try{
				return Integer.parseInt(propValue);
			} catch(NumberFormatException e) {}
		}
		
		return defaultValue;
	}
	
	public static Integer getPropertyAsInt(ConfigKey key) {
		try{
			return getPropertyAsInt(key, Integer.parseInt(key.getDefaultValue()));				
		} catch(NumberFormatException e) {
			return getPropertyAsInt(key, null);
		}
	}
	
	/**
	 * Returns the value for the given key if not available the defaultValue as Boolean
	 * 
	 * true, TRUE, True, tRue ... counts as Boolean.TRUE 
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static Boolean getPropertyAsBoolean(ConfigKey key, Boolean defaultValue) {
		String propValue = getPropertyValue(key);
		
		if(propValue != null){
			return "true".equals(propValue.toLowerCase());
		}
		
		return defaultValue;
	}
	
	public static Boolean getPropertyAsBoolean(ConfigKey key) {
		String defaultValue = key.getDefaultValue();
		
		if (defaultValue != null) {
			return getPropertyAsBoolean(key, "true".equals(defaultValue.toLowerCase()));
		}
		
		return getPropertyAsBoolean(key, null);			
	}


}
