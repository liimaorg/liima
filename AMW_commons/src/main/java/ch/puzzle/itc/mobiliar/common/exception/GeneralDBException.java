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

package ch.puzzle.itc.mobiliar.common.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback=true)
public class GeneralDBException extends Exception{

	// Diese Exception dient zum Markieren, falls ein command/Manipulation auf der DB fehlgeschlagen hat

	public static final String ERRORMESSAGE = "Something went wrong with the database access. Please contact your application administrator.";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GeneralDBException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public GeneralDBException(String message) {
		this(message, null);
	}
	
	public String getErrorMessage(){
		return ERRORMESSAGE;
	}

}
