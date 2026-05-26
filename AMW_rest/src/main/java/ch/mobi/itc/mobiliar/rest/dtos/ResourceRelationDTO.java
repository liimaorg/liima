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

package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "relations")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class ResourceRelationDTO {

    private final static String CONSUMED = "consumed";
    private final static String PROVIDED = "provided";

    private Integer id;
    private Integer slaveId;
    private String relatedResourceName;
    private String type;
    private String relatedResourceRelease;

    private String relationName;
    private String relationType;
    private List<TemplateDTO> templates;
    private List<RelationReleaseDTO> availableReleases = new ArrayList<>();
    private String identifier;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationReleaseDTO {
        private Integer relationId;
        private String releaseName;
    }

    public ResourceRelationDTO(AbstractResourceRelationEntity relation) {
        id = relation.getId();
        slaveId = relation.getSlaveResource().getId();
        relatedResourceName = relation.getSlaveResource().getName();
        type = relation.getResourceRelationType().getResourceTypeB().getName();
        relatedResourceRelease = relation.getSlaveResource().getRelease().getName();
        relationName = relation.buildIdentifer();
        relationType = relation instanceof ConsumedResourceRelationEntity ? CONSUMED : PROVIDED;
        identifier = relation.getIdentifier();
    }

    public ResourceRelationDTO(ResourceEditRelation relation) {
        id = relation.getResRelId();
        slaveId = relation.getSlaveId();
        relatedResourceName = relation.getSlaveName();
        type = relation.getSlaveTypeName();
        relatedResourceRelease = relation.getSlaveReleaseName();
        relationName = relation.getDisplayName();
        relationType = relation.getMode() == ResourceEditRelation.Mode.CONSUMED ? CONSUMED : PROVIDED;
        identifier = relation.getIdentifier();
    }

}
