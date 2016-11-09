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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted;

/**
 * GenerationModus
 */
public enum GenerationModus {
	
	/**
	 * Deployment will be executed after Generation
	 */
	DEPLOY("deploy", "Deployment"), 
	
	/**
	 * Deployment will be executed after Generation
	 */
	PREDEPLOY("predeploy", "Predeployment"), 
	
	/**
	 * Generation is only Simulated, the Deployment gets triggered with a Flag that indicates whether to execute Deployment or not
	 */
	SIMULATE("simulate", "Build"), 
	/**
	 * Generation is only executed for Validation purposes
	 */
	TEST("test", "test");
	
	private GenerationModus(String name, String action){
		this.setName(name);
		this.setAction(action);
	}
	
	private String name;
	private String action;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * Send Email Notification when an error occurs during deployment or predeployment
	 * @return
	 */
	public boolean isSendNotificationOnErrorGenerationModus(){
		return (DEPLOY.equals(this) || PREDEPLOY.equals(this));
	}

    /**
     * Send Email Success Notification only when Deployment
     * @return
     */
    public boolean isSendNotificationOnSuccessGenerationModus(){
        return (DEPLOY.equals(this));
    }
	
}
