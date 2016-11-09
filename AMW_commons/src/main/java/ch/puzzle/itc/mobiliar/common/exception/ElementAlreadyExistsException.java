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


public class ElementAlreadyExistsException extends AMWException{
	// Wird geworfen, falls eine Resource nicht erstellt werden kann (f.ex. wenn eine unique Resource bereits existiert) 

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Class<?> existingObjectClass;
	private final String existingObjectName;
	
	public ElementAlreadyExistsException(){
		this(null, null, null);
	}
	
	public ElementAlreadyExistsException(String message, Class<?> existingObjectClass, String existingObjectName){
		this(message, null, existingObjectClass, existingObjectName);
	}
	
	public ElementAlreadyExistsException(String message, Throwable cause, Class<?> existingObjectClass, String existingObjectName) {
		super(message, cause);
		this.existingObjectClass = existingObjectClass;
		this.existingObjectName = existingObjectName;
	}

	public Class<?> getExistingObjectClass() {
		return existingObjectClass;
	}

	public String getExistingObjectName() {
		return existingObjectName;
	}

	
}
