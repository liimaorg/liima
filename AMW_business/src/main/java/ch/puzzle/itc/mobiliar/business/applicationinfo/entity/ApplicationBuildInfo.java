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

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ApplicationBuildInfo {
	
	@Getter
	ApplicationBuildInfoKeyValue version;
	@Getter
	ApplicationBuildInfoKeyValue buildUser;
	@Getter
	ApplicationBuildInfoKeyValue builddate;
	@Getter
	ApplicationBuildInfoKeyValue buildjdk;
	@Getter
	ApplicationBuildInfoKeyValue javaVendor;
	
	public ApplicationBuildInfo(ApplicationBuildInfoKeyValue version, ApplicationBuildInfoKeyValue buildUser, ApplicationBuildInfoKeyValue builddate, ApplicationBuildInfoKeyValue buildjdk, ApplicationBuildInfoKeyValue javaVendor) {
		this.version = version;
		this.buildUser = buildUser;
		this.builddate = builddate;
		this.buildjdk= buildjdk;
		this.javaVendor = javaVendor;
	}
	
	public List<ApplicationBuildInfoKeyValue> getAsList(){
		List<ApplicationBuildInfoKeyValue> list = new ArrayList<ApplicationBuildInfoKeyValue>();
		
		list.add(version);
		list.add(buildUser);
		list.add(builddate);
		list.add(buildjdk);
		list.add(javaVendor);
		
		return list;
		
	}

}
