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

package ch.puzzle.itc.mobiliar.business.shakedown.entity;


import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;



public class ShakedownTestOrder {

	public static final String allAppServerSelectedText = "All application server on selected environment";
	public static final String releaseOfLastDeployText = "Release from last deploy";

	@Getter
	private ContextEntity environment;
	@Getter
	@Setter
	private boolean createTestForNeighbourhood;
	@Getter
	private ReleaseEntity release;
	@Setter
	@Getter
	private List<ResourceGroupEntity> resourceGroups;

	public ShakedownTestOrder(ContextEntity environment, ReleaseEntity release, List<ResourceGroupEntity> resourceGroups) {
		this.environment = environment;
		this.createTestForNeighbourhood = false;
		this.release = release;
		this.resourceGroups = resourceGroups;
	}

	public String getEnvironmentName(){
		if (environment != null){
			return environment.getName();
		}
		return "No environment selected";
	}

	public String getApplicationServerName(){
		if (resourceGroups != null && !resourceGroups.isEmpty()) {
			if (resourceGroups.size() > 1) {
				return allAppServerSelectedText;
			} else {
				return resourceGroups.get(0).getName();
			}
		}
		return "No application server selected";
	}

	public String getReleaseName() {
		return release != null ? release.getName() : releaseOfLastDeployText;
	}

}
