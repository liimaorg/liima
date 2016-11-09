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

package ch.puzzle.itc.mobiliar.builders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

public class DeploymentEntityBuilder extends BaseEntityBuilder {

	public DeploymentEntity mockDeploymentEntity(Integer trackingId, ReleaseEntity release, ResourceGroupEntity group, ResourceEntity appServer, boolean isBuildSuccess,
			ContextEntity context) {
		DeploymentEntity mock = mock(DeploymentEntity.class);
		int id = getNextId();
		when(mock.getId()).thenReturn(id);
		when(mock.getTrackingId()).thenReturn(trackingId);
		when(mock.getRelease()).thenReturn(release);
		when(mock.getResourceGroup()).thenReturn(group);
		when(mock.getResource()).thenReturn(appServer);
		when(mock.isBuildSuccess()).thenReturn(isBuildSuccess);
		when(mock.getContext()).thenReturn(context);

		return mock;
	}

	public DeploymentEntity buildDeploymentEntity(Integer trackingId, ReleaseEntity release, ResourceGroupEntity group, ResourceEntity appServer, boolean isBuildSuccess,
			ContextEntity context, boolean withId) {
		DeploymentEntity entity = new DeploymentEntity();
		if (withId) {
			Integer id = getNextId();
			entity.setId(id);
		}
		entity.setTrackingId(trackingId);
		entity.setRelease(release);
		entity.setResourceGroup(group);
		entity.setResource(appServer);
		entity.setBuildSuccess(isBuildSuccess);
		entity.setContext(context);
		entity.setApplicationsWithVersion(new ArrayList<ApplicationWithVersion>());

		return entity;
	}

}
