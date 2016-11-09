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

import java.util.List;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestOrder;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

public class ShakedownTestOrderBuilder {

	public static ShakedownTestOrder mockShakedownTestOrder(ContextEntity context, ReleaseEntity release, List<ResourceGroupEntity> groups) {
		ShakedownTestOrder order = mock(ShakedownTestOrder.class);
		when(order.getApplicationServerName()).thenReturn("as");
		when(order.getEnvironment()).thenReturn(context);
		when(order.getEnvironmentName()).thenReturn(context.getName());
		when(order.getRelease()).thenReturn(release);
		when(order.getReleaseName()).thenReturn(release.getName());
		when(order.getResourceGroups()).thenReturn(groups);
		return order;
	}

}
