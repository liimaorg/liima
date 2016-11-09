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

public class GeneratorException extends AMWException {

	public static enum MISSING {
		NODE, CONTEXT, APPLICATION, TARGETPATH, APPSERVER, STS_TEMPLATE
	};

	private static final long serialVersionUID = 1L;

	private MISSING missingObject;

	private Integer resourceId;



	public GeneratorException(String message, MISSING missingObject) {
		this(message, missingObject, null);
	}

	public GeneratorException(String message, MISSING missingObject, Integer resourceId) {
		this(message, missingObject, resourceId, null);
	}
	public GeneratorException(String message, MISSING missingObject, Integer resourceId, Throwable e) {
		super(message, e);
		this.missingObject = missingObject;
		this.resourceId = resourceId;
	}

	public MISSING getMissingObject() {
		return missingObject;
	}

	public Integer getResourceId() {
		return resourceId;
	}

	public void setResourceId(Integer resourceId) {
		this.resourceId = resourceId;
	}
}
