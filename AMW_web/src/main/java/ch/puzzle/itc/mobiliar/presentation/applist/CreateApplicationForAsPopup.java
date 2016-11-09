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

package ch.puzzle.itc.mobiliar.presentation.applist;

import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupPersistenceService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.common.util.ApplicationServerContainer;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.presentation.CompositeBackingBean;
import ch.puzzle.itc.mobiliar.presentation.common.ApplicationCreatorDataProvider;
import ch.puzzle.itc.mobiliar.presentation.common.ReleaseSelectionDataProvider;
import ch.puzzle.itc.mobiliar.presentation.common.ReleaseSelector;
import ch.puzzle.itc.mobiliar.presentation.common.UnsuccessfulActionException;
import ch.puzzle.itc.mobiliar.presentation.resourcesedit.CreateResourceController;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@CompositeBackingBean
public class CreateApplicationForAsPopup implements Serializable {
	private static final long serialVersionUID = 1L;

	@Inject
	@Getter
	private ReleaseSelectionDataProvider releaseDataProvider;

	@Inject
	CreateResourceController createResourceController;

	@Inject
	ResourceGroupPersistenceService resourceGroupService;

	private List<ResourceGroup> asGroupsForSelectBox;
	private Map<Integer, ResourceGroup> asGroupsForSelectBoxMap;
	@Getter
	private ReleaseSelector appReleaseSelector;

	@Getter
	@Setter
	private String appName;
	@Getter
	private Integer appServerGroupId;

	private Integer asReleaseId;
	
	public void setAsReleaseId(Integer releaseId){
		this.asReleaseId=releaseId;
	}
	
	public Integer getAsReleaseId(){
		return this.asReleaseId;
	}
	
	@Getter
	boolean loadList = false;

	public void init() {
		// workaround while other dataproviders are still session scoped
		appReleaseSelector = new ReleaseSelector(releaseDataProvider.getUpcomingReleaseId(), releaseDataProvider.getReleaseMap());
		asGroupsForSelectBox = null;
		appName = null;
		appServerGroupId = null;
		asReleaseId = null;
		loadList = true;
	}

	public List<ResourceGroup> getAsGroupsForSelectBox() {
		if (loadList) {
			asGroupsForSelectBox = new ArrayList<ResourceGroup>(getApplicationServerGroupsForSelectBox());
			asGroupsForSelectBoxMap = new HashMap<Integer, ResourceGroup>();
			for (ResourceGroup g : asGroupsForSelectBox) {
				asGroupsForSelectBoxMap.put(g.getId(), g);
			}
		}
		return asGroupsForSelectBox;
	}

	public List<ReleaseEntity> getReleasesForAs() {
		if (appServerGroupId != null && asGroupsForSelectBoxMap.containsKey(appServerGroupId)) {
			return asGroupsForSelectBoxMap.get(appServerGroupId).getReleases();
		}
		asReleaseId = null;
		return null;
	}

	public void createAppAndAppServer(ApplicationCreatorDataProvider parentDataProvider)
			throws UnsuccessfulActionException {
		if(createResourceController.createAppAndAppServer(appName, appServerGroupId, appReleaseSelector.getSelectedRelease(), asReleaseId)&&parentDataProvider!=null){
			parentDataProvider.afterAddingAppOrAs();
		}
	     init();

	}

	public void setAppServerGroupId(Integer appServerGroupId) {
		// jsf returns 0 for null, therefore we better use -1 to indicate nothing selected
		if (appServerGroupId != null && appServerGroupId >= 0) {
			this.appServerGroupId = appServerGroupId;
		}
		else {
			this.appServerGroupId = null;
		}
	}

	private Set<ResourceGroup> getApplicationServerGroupsForSelectBox(){
		SortedSet<ResourceGroup> groups = new TreeSet<>();
		List<ResourceGroupEntity> result;
			result = resourceGroupService.loadGroupsForTypeName(DefaultResourceTypeDefinition.APPLICATIONSERVER.name(), null);
			for (ResourceGroupEntity g : result) {
				if (!ApplicationServerContainer.APPSERVERCONTAINER.getDisplayName().equals(g.getName())) {
					groups.add(ResourceGroup.createByResource(g));
				}
			}
		return groups;
	}

}
