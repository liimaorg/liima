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
 * A script execution exception. Attention! This exception does explicitly not roll-back the transaction, because 
 * a failing script execution is written in the database!
 * 
 * @author "Oliver Schmid"
 *
 */
public class ScriptExecutionException extends Exception {
	
public static enum REASON {EXECUTIONEXCEPTION, NOTAVAILABLE, PERMISSION, GENERIC};
	
	private Integer deploymentId;
	private REASON reason;
	private static final long serialVersionUID = 1L;

	public ScriptExecutionException(String message, REASON reason){
		this(message, reason, null);
	}
	
	public ScriptExecutionException(String message, REASON reason, Throwable cause) {
		super(message, cause);
		this.reason = reason;
	}
	
	public REASON getReason() {
		return reason;
	}

	public Integer getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(Integer deploymentId) {
		this.deploymentId = deploymentId;
	}

}
