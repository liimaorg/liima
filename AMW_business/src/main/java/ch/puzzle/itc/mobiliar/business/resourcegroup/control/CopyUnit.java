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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntityHolder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.utils.CopyHelper;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds relevant informations for copy / releasing
 */
public class CopyUnit {
    @Getter
    private ResourceEntity targetResource;

    @Getter
    @Setter
    private ResourceEntity originResource;

    @Getter
    @Setter
    private CopyResourceDomainService.CopyMode mode;

    @Getter
    @Setter
    private CopyResourceResult result;

    @Getter
    @Setter
    private ForeignableOwner actingOwner;

	public CopyUnit(ResourceEntity originResource, ResourceEntity targetResource,
			CopyResourceDomainService.CopyMode mode, ForeignableOwner actingOwner) throws AMWException {
		Map<CopyResourceDomainService.CopyMode, Set<ForeignableOwner>> validCopyModeOwnerCombinations = CopyHelper
				.getValidModeOwnerCombinationsMap();
		for (CopyResourceDomainService.CopyMode copyMode : validCopyModeOwnerCombinations.keySet()) {
			if (mode == copyMode && !validCopyModeOwnerCombinations.get(copyMode).contains(actingOwner)) {
				throw new AMWException("Copy in " + mode.name() + " mode can not be executed by owner "
						+ actingOwner);
			}
		}

		this.targetResource = targetResource;
		this.originResource = originResource;
		this.mode = mode;
		this.actingOwner = actingOwner;
		this.result = new CopyResourceResult(targetResource != null ? targetResource.getName():null);
	}

	public void setTargetResource(ResourceEntity targetResource) {
		this.targetResource = targetResource;
		if(targetResource != null){
			this.result.setTargetResourceName(targetResource.getName());
		}
	}
}
