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

package ch.puzzle.itc.mobiliar.business.applicationinfo.boundary;

import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ApplicationBuildInfo;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ApplicationBuildInfoKeyValue;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ApplicationConfigurationInfo;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ConfigurationKeyValuePair;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigKey;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * this class holds the version info of the whole AMW Application
 */
@Startup
@Singleton
public class ApplicationVersionService {

	final static String OBFUSCATED = "***************";

	@Inject
	private Logger log;
	
	@Getter
	private ApplicationBuildInfo applicationBuildInfo;
	
	@Getter
	private ApplicationConfigurationInfo applicationConfigurationInfo;
	
	
	@PostConstruct
	public void init() {
		applicationBuildInfo = readApplicationBuildInfo();
		applicationConfigurationInfo = readApplicationConfigurationInfo();
	}

	public List<ConfigurationKeyValuePair> getObfuscatedApplicationConfigurationKeyValuePairs() {
		List<ConfigurationKeyValuePair> obfuscatedConfigurationKeyValuePairs = new ArrayList<>(applicationConfigurationInfo.getConfigurationKeyValuePairs().size());
		for (ConfigurationKeyValuePair pair : applicationConfigurationInfo.getConfigurationKeyValuePairs()) {
			ConfigurationKeyValuePair newPair = new ConfigurationKeyValuePair();
			newPair.setKey(pair.getKey());
			if (pair.getKey().isSecretValue()) {
				newPair.setValue(OBFUSCATED);
				newPair.setDefaultValue(OBFUSCATED);
			} else {
				newPair.setValue(pair.getValue());
				newPair.setDefaultValue(pair.getDefaultValue());
			}
			obfuscatedConfigurationKeyValuePairs.add(newPair);
		}
		return obfuscatedConfigurationKeyValuePairs;
	}

	private ApplicationBuildInfo readApplicationBuildInfo(){
		
		Manifest businessManifest = getBusinessManifest("AMW_business");
		
		ApplicationBuildInfoKeyValue version = getMavenVersionClass(getClass(), "Implementation-Version", businessManifest);
		ApplicationBuildInfoKeyValue buildUser = getMavenInfoForClass(businessManifest, "Built-By");
		ApplicationBuildInfoKeyValue builddate = getMavenInfoForClass(businessManifest, "Build-Timestamp");
		ApplicationBuildInfoKeyValue buildjdk = getMavenInfoForClass(businessManifest, "Build-Jdk");
		ApplicationBuildInfoKeyValue buildJavaVendor = getMavenInfoForClass(businessManifest, "Java-Vendor");
		
		return new ApplicationBuildInfo(version, buildUser, builddate, buildjdk, buildJavaVendor);
	}
	
	private ApplicationBuildInfoKeyValue getMavenInfoForClass(Manifest manifest, String field){
		
		String value = "";
		
		if(manifest != null){
			Attributes attributes = manifest.getMainAttributes();
			if (attributes != null) {
				value = attributes.getValue(field);
			}
		}
		return new ApplicationBuildInfoKeyValue(field, value);
	}
	
	private Manifest getBusinessManifest(String module){
		
		Manifest manifest = null;
		
		Enumeration<URL> resources = null;
		try {
			resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
		}
		catch (IOException e) {
			 log.log(Level.SEVERE, "Not able to load META-INF/MANIFEST.MF", e);
		}
		if(resources != null){
			while (resources.hasMoreElements()) {
			    try {
			      manifest = new Manifest(resources.nextElement().openStream());
			      Attributes attributes = manifest.getMainAttributes();
			      
			      if(attributes != null && attributes.containsValue(module)){
			     	 return manifest;
			      }
			    } catch (IOException e) {
				    log.log(Level.SEVERE, "Not able to load META-INF/MANIFEST.MF", e);
			    }
			}
		}
		return manifest;
	}
	
	private ApplicationBuildInfoKeyValue getMavenVersionClass(Class<?> clazz, String backupField, Manifest manifest){
		String ret = "";
		if(clazz != null){
			Package pack = clazz.getPackage();
			if(pack != null){
				ret = pack.getImplementationVersion();  
				if (ret==null) {
				    return getMavenInfoForClass(manifest, backupField);
				}
			}
		}
		
		return new ApplicationBuildInfoKeyValue(backupField, ret);
	}
	
	private ApplicationConfigurationInfo readApplicationConfigurationInfo() {
		ApplicationConfigurationInfo config = new ApplicationConfigurationInfo();
		for (ConfigKey configKey : ConfigKey.values()) {
			ConfigurationKeyValuePair c = new ConfigurationKeyValuePair();
			c.setKey(configKey);
			
			String property = ConfigurationService.getProperty(configKey);
			if(property != null){
				c.setValue(property);
			}
			c.setDefaultValue(ConfigurationService.getDefaultValue(configKey));
			
			config.addConfigurationKeyValuePair(c);
		}
		
		return config;
	}

}
