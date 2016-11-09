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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

@ApplicationScoped
public class ResourceReleaseComparator implements Comparator<ResourceEntity> {

		@Override
		public int compare(ResourceEntity resource1, ResourceEntity resource2) {
			if (resource1.getRelease() == null && resource2.getRelease() == null) {
				return 0;
			}
			if (resource1.getRelease() == null) {
				return 1;
			}
			if (resource2.getRelease() == null) {
				return -1;
			}
			return resource1.getRelease().compareTo(resource2.getRelease());
		}
}
