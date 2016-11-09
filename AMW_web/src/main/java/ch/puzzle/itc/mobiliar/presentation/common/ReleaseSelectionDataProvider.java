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

package ch.puzzle.itc.mobiliar.presentation.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;

/**
 * Provides functionality for release selection
 */
@Named
@SessionScoped
public class ReleaseSelectionDataProvider implements Serializable {
	private static final long serialVersionUID = -28013815668470929L;

	@Inject
	ReleaseMgmtService releaseService;

	@Inject
	ResourceDependencyResolverService dependencyResolverService;

	private Integer upcomingReleaseId;
	private LinkedHashMap<Integer, ReleaseEntity> releaseMap;
	private Date upcomingReleaseDate;

	/**
	 * Makes sure that the upcomingRelease is initialized
	 *
	 * @return id of the upcoming release
	 */
	public Integer getUpcomingReleaseId() {
		if (upcomingReleaseId == null) {
			this.upcomingReleaseId = dependencyResolverService.findMostRelevantRelease(
					new TreeSet<ReleaseEntity>(getAllReleases()), new Date()).getId();
		}
		return upcomingReleaseId;
	}

	/**
	 * Makes sure that the upcomingRelease is initialized
	 *
	 * @return date of the upcoming release
	 */
	public Date getUpcomingReleaseDate() {
		if (upcomingReleaseDate == null) {
			this.upcomingReleaseDate = dependencyResolverService.findMostRelevantRelease(
					new TreeSet<ReleaseEntity>(getAllReleases()), new Date()).getInstallationInProductionAt();
		}
		return upcomingReleaseDate;
	}

	/**
	 * @return map of all releases with the releaseId as key
	 */
	public LinkedHashMap<Integer, ReleaseEntity> getReleaseMap() {
		if (releaseMap == null) {
			List<ReleaseEntity> result = releaseService.loadAllReleases(false);
			releaseMap = new LinkedHashMap<Integer, ReleaseEntity>();
			for (ReleaseEntity r : result) {
				releaseMap.put(r.getId(), r);
			}
		}
		return releaseMap;
	}

	/**
	 * @return list of all releases
	 */
	public List<ReleaseEntity> getAllReleases() {
		return new ArrayList<ReleaseEntity>(getReleaseMap().values());
	}

	/**
	 * workaround for session scope
	 */
	public void reset() {
		releaseMap = null;
		upcomingReleaseId = null;
		upcomingReleaseDate = null;
	}
}
