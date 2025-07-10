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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(name = "resourceType")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class ResourceTypeDTO {

    private Integer id;
    private String name;
    private boolean hasChildren;
    private boolean hasParent;
    private List<ResourceTypeDTO> children;
    @JsonProperty(value="isApplication")
    private boolean isApplication;
    @JsonProperty(value="isDefaultResourceType")
    private boolean isDefaultResourceType;

    public ResourceTypeDTO(ResourceTypeEntity resourceType) {
        this.id = resourceType.getId();
        this.name = resourceType.getName();
        this.hasChildren = resourceType.hasChildren();
        this.hasParent = !resourceType.isRootResourceType();
        this.children = resourceType.getChildrenResourceTypes().stream()
                .map(ResourceTypeDTO::new)
                .collect(Collectors.toList());
        this.isApplication = resourceType.isApplicationResourceType();
        this.isDefaultResourceType = resourceType.isDefaultResourceType();
    }
}
