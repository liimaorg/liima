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


/**
 * Exception for invalid template properties
 */
public class TemplatePropertyException extends AMWException {

	public static enum CAUSE {
		INVALID_PROPERTY, INVALID_PROPERTYVALUE, WRONG_DATASTRUCTURE, PROCESSING_EXCEPTION
	};
	
	private static final long serialVersionUID = 1L;

	/**
	 * The cause of the template property exception
	 */
	private CAUSE cause;

	public TemplatePropertyException(String message, CAUSE failureCause) {
		super(message);
		this.cause = failureCause;
	}
	public TemplatePropertyException(String message, CAUSE failureCause, Exception e) {
		super(message,e);
		this.cause = failureCause;
	}

	public CAUSE getFailureCause() {
		return cause;
	}

}
