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
 * Definiert ResourceType, welche im Bezug mit einer Applikation stehen. Diese
 * Resourcentypen werden NIE in einer Aufzälung von ResourcenTypen in einem
 * Screen angezeigt.
 * 
 * @author bschwaller
 * 
 */
public enum DefaultResourceTypeDefinition {
	//Runtime is defined on ResourceTypeEntity for some reason
	APPLICATIONSERVER("Applicationserver", true), APPLICATION("Application", true), NODE("Node", false);

	private final String displayName;
	private final boolean exclude;

	private DefaultResourceTypeDefinition(String displayName, boolean exclude) {
		this.displayName = displayName;
		this.exclude = exclude;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isExclude() {
		return exclude;
	}

	public static boolean contains(String name) {
		DefaultResourceTypeDefinition[] values = values();
		for (int i = 0; i < values.length; i++) {
			if (values[i].name().equals(name)) {
				return true;
			}
		}
		return false;
	}

}
