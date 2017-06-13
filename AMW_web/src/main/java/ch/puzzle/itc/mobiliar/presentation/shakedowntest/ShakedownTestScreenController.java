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

package ch.puzzle.itc.mobiliar.presentation.shakedowntest;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentService;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonFilterService.SortingDirectionType;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupPersistenceService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.control.ShakedownTestService;
import ch.puzzle.itc.mobiliar.business.shakedown.control.ShakedownTestService.ShakedownTestFilterTypes;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity.shakedownTest_state;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestOrder;
import ch.puzzle.itc.mobiliar.business.shakedown.event.ShakedownTestEvent;
import ch.puzzle.itc.mobiliar.business.shakedown.event.ShakedownTestEvent.ShakedownTestEventType;
import ch.puzzle.itc.mobiliar.business.deploy.entity.CustomFilter;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.common.util.Tuple;
import ch.puzzle.itc.mobiliar.presentation.shakedowntest.ShakedownTestScreenDataProvider.shakedownTestScreenColDescriptor;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import ch.puzzle.itc.mobiliar.presentation.util.UserSettings;

@Named
@RequestScoped
public class ShakedownTestScreenController{

	@Inject
	private ShakedownTestService shakedownTestService;

	@Inject
	private UserSettings userSettings;

	@Inject
	private DeploymentService deploymentService;

	@Inject
	private ResourceGroupPersistenceService resourceGroupService;

	@Inject
	private Event<ShakedownTestEvent> shakedownTestEvent;



	public Tuple<Set<ShakedownTestEntity>, Integer> loadPendingShakedownTests(boolean count, Integer startIndex, Integer maxResults, List<CustomFilter> filter,
			shakedownTestScreenColDescriptor sortingColumn, SortingDirectionType sortingDirection) {
		// TODO remove executed

		if (maxResults != null && maxResults == 0) {
			maxResults = null;
		}

		String colToSort = null;
		if (sortingColumn != null) {

			// TODO implementieren
			// Mapping zwischen anzeige col und in db zu filternde col
			switch (sortingColumn) {
			case ID:
				colToSort = ShakedownTestFilterTypes.ID.getFilterTabColumnName();
				break;
			case TRACKING_ID:
				colToSort = ShakedownTestFilterTypes.TRACKING_ID.getFilterTabColumnName();
				break;
			case STATE:
				colToSort = ShakedownTestFilterTypes.TEST_STATE.getFilterTabColumnName();
				break;
			case APPSERVER_NAME:
				colToSort = ShakedownTestFilterTypes.APPSERVER_NAME.getFilterTabColumnName();
				break;
			case RELEASE:
				colToSort = ShakedownTestFilterTypes.APPSERVER_RELEASE.getFilterTabColumnName();
				break;
			case ENV:
				colToSort = ShakedownTestFilterTypes.ENVIRONMENT_NAME.getFilterTabColumnName();
				break;
			case TESTDATE:
				colToSort = ShakedownTestFilterTypes.TEST_DATE.getFilterTabColumnName();
				break;
			default:
				break;
			}
		}

		return shakedownTestService.getFilteredShakedownTests(count, startIndex, maxResults, filter, colToSort, sortingDirection, userSettings.getMyAMWFilter());
	}

	public List<ResourceGroupEntity> loadAppServers() {
		return resourceGroupService.loadGroupsForTypeName(DefaultResourceTypeDefinition.APPLICATIONSERVER.name(), userSettings.getMyAMWFilter());
	}
	
	public List<ResourceGroupEntity> loadAllApplications(){
		return shakedownTestService.getAllApplications();
	}
	
	

	public Integer createShakedownTestOrder(List<ShakedownTestOrder> shakedownTestOrder) {
		Integer trackingId = null;
		if (shakedownTestOrder != null && !shakedownTestOrder.isEmpty()) {
			trackingId = shakedownTestService.createShakedownTestOrderReturnsTrackingId(shakedownTestOrder);

			if (trackingId == null) {
				GlobalMessageAppender.addErrorMessage("Could not create shakedown test order.");
			} else {
				GlobalMessageAppender.addSuccessMessage("Shakedown test order successfully created.");
				// fire the Event to execute the Shakedowntests
				shakedownTestEvent.fire(new ShakedownTestEvent(ShakedownTestEventType.NEW, shakedownTest_state.scheduled));
			}
		}

		return trackingId;
	}

	public List<Object[]> getAllLastSucessfullDeployments() {
		return deploymentService.getEssentialListOfLastDeploymentsForAppServerAndContext(true);
	}

}