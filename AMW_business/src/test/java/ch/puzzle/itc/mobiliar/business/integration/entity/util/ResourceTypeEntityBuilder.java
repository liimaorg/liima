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

package ch.puzzle.itc.mobiliar.business.integration.entity.util;

import lombok.Setter;
import lombok.experimental.Accessors;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

/**
 * Testdata Builder for {@see ResourceTypeEntity}
 */
@Accessors(fluent = true, chain = true)
@Setter
public class ResourceTypeEntityBuilder {
	private Integer id;
	private String name;
    private ResourceTypeEntity parentResourceType;

	public static final ResourceTypeEntity APPLICATION_TYPE = new ResourceTypeEntityBuilder()
			.id(Integer.valueOf(1)).name(DefaultResourceTypeDefinition.APPLICATION.name()).build();

	public static final ResourceTypeEntity APPLICATION_SERVER_TYPE = new ResourceTypeEntityBuilder()
			.id(Integer.valueOf(2)).name(DefaultResourceTypeDefinition.APPLICATIONSERVER.name()).build();

	public static final ResourceTypeEntity NODE_TYPE = new ResourceTypeEntityBuilder()
			.id(Integer.valueOf(3)).name(DefaultResourceTypeDefinition.NODE.name()).build();

	/**
	 * Builds a {@link Zielgruppe} with all defined properties.
	 * 
	 * @return the build {@link Zielgruppe}.
	 */
	public ResourceTypeEntity build() {
		ResourceTypeEntity rt = new ResourceTypeEntity();
		rt.setId(id);
		rt.setName(name);
        rt.setParentResourceType(parentResourceType);
		return rt;
	}
}
