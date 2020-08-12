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
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;

public class ShakedownTestEntityBuilder {

	public static ShakedownTestEntity mockShakedownTestEntity(ContextEntity context, ResourceEntity appServer, DeploymentEntity dep, Integer id, ResourceGroupEntity group) {
		ShakedownTestEntity test = mock(ShakedownTestEntity.class);
		when(test.getContext()).thenReturn(context);
		when(test.getApplicationServer()).thenReturn(appServer);
		when(test.getDeployment()).thenReturn(dep);
		when(test.getResourceGroup()).thenReturn(group);
		when(test.getId()).thenReturn(id);
		when(test.getRelease()).thenReturn(appServer.getRelease());

		return test;
	}

}
