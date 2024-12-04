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


public class NameChecker {

	private static final String ERR_MSG_SUFIX = "The name must not contain any other than alphanumerical and \"_\" characters.";
	private static final String ERR_MSG_INVALID_NAME_CHARACTERS = "The name must not contain any other than alphanumerical and \"_\" / \"-\" characters.";

	private static final String REGEXP_NON_EMPTY_STRING = "\\S*";
	private static final String REGEXP_ALPHANUMERIC_WITH_UNDERSCORE_HYPHEN = "^[a-zA-Z0-9_-]+$";

	/**
	 * Ein Resourcename @parm name wird auf seine Gültigkeit geprüft. Der name
	 * darf keine leerzeichen " " enthalten. Null ist
	 * gültig!
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isNameValid(String name) {

		boolean isValid = true;

		if (name != null) {
			isValid = name.matches(REGEXP_NON_EMPTY_STRING);
		}

		return isValid;
	}

	/**
	 * Ein Resourcename @parm name wird auf seine Gültigkeit geprüft.
	 * Der Name darf keine anderen als alphanumerische Zeichen und "_" und "-" enthalten.
	 * Null ist gültig!
	 *
	 * @param name
	 * @return
	 */
	public static boolean isValidAlphanumericWithUnderscoreHyphenName(String name) {
		return name == null || name.matches(REGEXP_ALPHANUMERIC_WITH_UNDERSCORE_HYPHEN);
	}

	/**
	 * Gibt Fehlermeldung abhängig vom resourcetypen der gegebenen resource
	 * zurück.
	 * 
	 * @param type
	 * @return
	 */
	public static String getErrorText(String type) {
		return getErrorText("resource type", type);
	}

	public static String getErrorText(String type, String name) {
		return "Invalid " + type + " name \"" + name + "\"! " + ERR_MSG_SUFIX;
	}

	public static String getErrorTextAlphanumericWithUnderscoreHyphenName(String type, String name) {
		return "Invalid " + type + " name \"" + name + "\"! " + ERR_MSG_INVALID_NAME_CHARACTERS;
	}

	public static String getErrorTextForResourceType(String resourceTypeName, String name) {
		return getErrorText(getResourceTypeScreenName(resourceTypeName), name);
	}

	public static String getErrorTextForInvalidResourceName(String resourceTypeName, String name) {
		return getErrorTextAlphanumericWithUnderscoreHyphenName(getResourceTypeScreenName(resourceTypeName), name);
	}
	
	public static String getErrorTextForResourceType(String resourceTypeName) {
		return getErrorText(getResourceTypeScreenName(resourceTypeName));
	}

	private static String getResourceTypeScreenName(String resourceTypeName) {
		String result = "resource";

		if (resourceTypeName != null) {
			if (resourceTypeName.equalsIgnoreCase(DefaultResourceTypeDefinition.APPLICATION.name())) {
				result = "application";
			} else if (resourceTypeName.equalsIgnoreCase(DefaultResourceTypeDefinition.APPLICATIONSERVER.name())) {
				result = "application server";
			}
			if (resourceTypeName.equalsIgnoreCase(DefaultResourceTypeDefinition.NODE.name())) {
				result = "node";
			}
		}
		return result;
	}
}
