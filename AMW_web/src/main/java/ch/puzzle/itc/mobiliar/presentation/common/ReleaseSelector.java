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

import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;

import java.util.LinkedHashMap;

/**
 * @author cweber
 */
public class ReleaseSelector {

	private Integer selectedReleaseId;
	private final Integer defaultReleaseId;
	private ReleaseChangeHandler releaseChangeHandler;
	private final LinkedHashMap<Integer, ReleaseEntity> releaseMap;

	public static interface ReleaseChangeHandler {
		public void onChange(ReleaseEntity selectedRelease);
	}

	public ReleaseSelector(Integer defaultReleaseId, LinkedHashMap<Integer, ReleaseEntity> releaseMap) {
		this.defaultReleaseId = defaultReleaseId;
		this.releaseMap = releaseMap;
	}

	/**
	 * @param selectedReleaseId
	 */
	public void setSelectedReleaseId(Integer selectedReleaseId) {
	    	if(selectedReleaseId!=null && selectedReleaseId.intValue()==0){
		    selectedReleaseId=null;
		}
		boolean hasChanged = (this.selectedReleaseId == null && selectedReleaseId != null)
				|| (this.selectedReleaseId != null && !this.selectedReleaseId.equals(selectedReleaseId));
		this.selectedReleaseId = selectedReleaseId;
		if (hasChanged && releaseChangeHandler != null) {
			releaseChangeHandler.onChange(getSelectedRelease());
		}
	}

	/**
	 * @return the id of selected release or the upcoming release if none is selected
	 */
	public Integer getSelectedReleaseId() {
		return selectedReleaseId != null ? selectedReleaseId : defaultReleaseId;
	}

	/**
	 * @return the selected release or the upcoming release if none is selected
	 */
	public ReleaseEntity getSelectedRelease() {
		return getSelectedReleaseId() != null ? releaseMap.get(getSelectedReleaseId()) : null;
	}

	/**
	 * Handler will be called if selected release changes
	 * 
	 * @param releaseChangeHandler
	 */
	public void setReleaseChangeHandler(ReleaseChangeHandler releaseChangeHandler) {
		this.releaseChangeHandler = releaseChangeHandler;
	}

}
