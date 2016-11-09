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

package ch.puzzle.itc.mobiliar.presentation.dependencies;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ViewScoped;
import javax.inject.Named;

import ch.puzzle.itc.mobiliar.presentation.resourceDependencies.DependencyModel;
import ch.puzzle.itc.mobiliar.presentation.resourceRelations.ResourceRelations;
import lombok.Getter;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

/**
 * DataProvider to list the consumed and provided relations for a resource
 *
 * Deprecated: use {@link ResourceRelations} instead
 * 
 * @author cweber
 */
@Named
@ViewScoped
@Deprecated
public class DependenciesDataProvider implements Serializable {
	private static final long serialVersionUID = 1L;

	@Getter
	private ResourceEntity resource;

	@Getter @Deprecated
	List<DependencyModel> consumedRelations;

	@Getter @Deprecated
	List<DependencyModel> providedRelations;

	public void init(ResourceEntity resource, List<DependencyModel> consumedRelations, List<DependencyModel> providedRelations) {
		this.resource = resource;
		this.consumedRelations = consumedRelations;
		this.providedRelations = providedRelations;
	}

}
