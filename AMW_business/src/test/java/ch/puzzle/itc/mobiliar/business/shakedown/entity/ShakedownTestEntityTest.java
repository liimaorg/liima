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

import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity.ApplicationsFromApplicationServer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShakedownTestEntityTest
{

	@Test
	public void setApplicationsFromApplicationServer() {
		// given
		ShakedownTestEntity ste = new ShakedownTestEntity();

		List<ApplicationsFromApplicationServer> input = new ArrayList<ShakedownTestEntity.ApplicationsFromApplicationServer>();
		ApplicationsFromApplicationServer a1 = new ApplicationsFromApplicationServer("test1", Integer.valueOf(1));
		ApplicationsFromApplicationServer a2 = new ApplicationsFromApplicationServer("test1", Integer.valueOf(1));
		ApplicationsFromApplicationServer a3 = new ApplicationsFromApplicationServer("test1", Integer.valueOf(1));

		input.add(a1);
		input.add(a2);
		input.add(a3);

		// when
		ste.setApplicationsFromApplicationServer(input);

		List<ApplicationsFromApplicationServer> result = ste.getApplicationsFromApplicationServer();

		// then
		assertEquals(input.size(), result.size());
		assertEquals(3, result.size());
		assertEquals(input.get(0).getApplicationId(), result.get(0).getApplicationId());
		assertEquals(input.get(0).getApplicationName(), result.get(0).getApplicationName());
		assertEquals(input.get(1).getApplicationId(), result.get(1).getApplicationId());
		assertEquals(input.get(1).getApplicationName(), result.get(1).getApplicationName());
		assertEquals(input.get(2).getApplicationId(), result.get(2).getApplicationId());
		assertEquals(input.get(2).getApplicationName(), result.get(2).getApplicationName());
	}

	@Test
	public void setApplicationsFromApplicationServer_empty() {
		// given
		ShakedownTestEntity ste = new ShakedownTestEntity();

		List<ApplicationsFromApplicationServer> input = new ArrayList<ShakedownTestEntity.ApplicationsFromApplicationServer>();

		// when

		ste.setApplicationsFromApplicationServer(input);

		List<ApplicationsFromApplicationServer> result = ste.getApplicationsFromApplicationServer();

		// then
		assertEquals(input.size(), result.size());
		assertEquals(0, result.size());
	}

}
