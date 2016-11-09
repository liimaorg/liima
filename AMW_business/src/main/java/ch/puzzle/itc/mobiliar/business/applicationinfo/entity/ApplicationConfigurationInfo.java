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

package ch.puzzle.itc.mobiliar.business.applicationinfo.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * This Class is used to display the configuration as list in the Gui
 */
public class ApplicationConfigurationInfo {
	@Getter
	private List<ConfigurationKeyValuePair> configurationKeyValuePairs = new ArrayList<ConfigurationKeyValuePair>();
	
	/**
	 * adds a ConfigurationKeyValuePair to the config list
	 * 
	 * @param configurationKeyValuePair
	 */
	public void addConfigurationKeyValuePair(ConfigurationKeyValuePair configurationKeyValuePair){
		this.configurationKeyValuePairs.add(configurationKeyValuePair);
	}

}
