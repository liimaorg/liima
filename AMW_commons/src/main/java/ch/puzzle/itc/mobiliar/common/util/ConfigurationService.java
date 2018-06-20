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


/**
 * Class that manages all the System Properties of AMW
 */
public class ConfigurationService {

    private static String getPropertyValue(ConfigKey key) {
        String configPropertyValue = System.getProperty(key.getValue());
        // if the systemProperty is not set, check corresponding ENV
        if(configPropertyValue == null){
            configPropertyValue = RuntimeEnvironment.getValueOfEnvironmentVariable(key.getEnvName());
        }

        return configPropertyValue;
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
