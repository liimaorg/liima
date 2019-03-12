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

package ch.puzzle.itc.mobiliar.presentation.resourceRelation;

import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableAttributesDTO;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.entity.SoftlinkRelationEntity;


public class SoftlinkRelationModel implements Serializable {

    @Getter
    private SoftlinkRelationEntity softlinkRelationEntity;

    @Getter
    @Setter
    private ResourceEntity softlinkResolvingSlaveResource;

    public SoftlinkRelationModel(SoftlinkRelationEntity softlinkRelationEntity){
        this.softlinkRelationEntity = Objects.requireNonNull(softlinkRelationEntity, "softlinkRelation must not be null!");
    }

    public ForeignableAttributesDTO getForeignableAttributes(){
        return new ForeignableAttributesDTO(softlinkRelationEntity.getOwner(), softlinkRelationEntity.getExternalKey(), softlinkRelationEntity.getExternalLink());
    }

    public boolean hasResolvedSoftlinkRelation(){
        return softlinkResolvingSlaveResource != null;
    }
}
