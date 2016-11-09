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

import java.util.Date;

import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;

public class ReleaseEntityBuilder extends BaseEntityBuilder {

	public ReleaseEntity mockReleaseEntity(String name, Date installationDate) {
		ReleaseEntity mock = mock(ReleaseEntity.class);
		int id = getNextId();
		when(mock.getId()).thenReturn(id);
		when(mock.getName()).thenReturn(getName(name, ReleaseEntity.class, id));
		when(mock.getInstallationInProductionAt()).thenReturn(installationDate);
		return mock;
	}

	public ReleaseEntity buildReleaseEntity(String name, Date installationDate, boolean withId) {
		ReleaseEntity entity = new ReleaseEntity();
		if (withId) {
			Integer id = getNextId();
			entity.setId(id);
		}
		entity.setName(name);
		entity.setInstallationInProductionAt(installationDate);

		return entity;
	}

	public static ReleaseEntity createMainReleaseEntity(String release, Integer releaseId, Date releaseProductionDate) {
		ReleaseEntity releaseEntity = new ReleaseEntity();
		releaseEntity.setName(release);
		releaseEntity.setMainRelease(true);
		releaseEntity.setId(releaseId);
		releaseEntity.setInstallationInProductionAt(releaseProductionDate);
		return releaseEntity;
	}

	public static ReleaseEntity createMinorReleaseEntity(String release, Integer releaseId, Date releaseProductionDate) {
		ReleaseEntity releaseEntity = new ReleaseEntity();
		releaseEntity.setName(release);
		releaseEntity.setMainRelease(false);
		releaseEntity.setId(releaseId);
		releaseEntity.setInstallationInProductionAt(releaseProductionDate);
		return releaseEntity;
	}

	public static ReleaseEntity createMainReleaseEntity(String release, Integer releaseId) {
		return createMainReleaseEntity(release, releaseId, null);
	}
}
