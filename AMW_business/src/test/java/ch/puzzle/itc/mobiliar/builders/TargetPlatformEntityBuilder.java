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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * 
 * @author cweber
 */
public class TargetPlatformEntityBuilder extends BaseEntityBuilder {

	/**
	 * @param platformName
	 * @return mocked TargetPlatformEntity
	 */
	public ResourceGroupEntity mockTargetPlatformEntity(String platformName) {
		ResourceGroupEntity mock = mock(ResourceGroupEntity.class);
		lenient().when(mock.getId()).thenReturn(getNextId());
		lenient().when(mock.getName()).thenReturn(DefaultResourceTypeDefinition.RUNTIME.name());
		return mock;
	}

	/**
	 * @param platformName
	 * @return TargetPlatformEntity
	 */
	public ResourceGroupEntity buildTargetPlatformEntity(String platformName) {
		ResourceGroupEntity targetPlatform = new ResourceGroupEntity();
		targetPlatform.setId(getNextId());
		targetPlatform.setName(DefaultResourceTypeDefinition.RUNTIME.name());
		return targetPlatform;
	}

}
