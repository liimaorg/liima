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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import lombok.Getter;
import lombok.Setter;

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

    public CopyUnit(ResourceEntity originResource, ResourceEntity targetResource, CopyResourceDomainService.CopyMode mode) {
        this.targetResource = targetResource;
        this.originResource = originResource;
        this.mode = mode;
        this.result = new CopyResourceResult(targetResource != null ? targetResource.getName() : null);
    }

    public void setTargetResource(ResourceEntity targetResource) {
        this.targetResource = targetResource;
        if (targetResource != null) {
            this.result.setTargetResourceName(targetResource.getName());
        }
    }
}
