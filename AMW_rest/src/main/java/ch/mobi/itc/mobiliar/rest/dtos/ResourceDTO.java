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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "release")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class ResourceDTO {

    private Integer id;
    private String release;
    private List<ResourceRelationDTO> relations;
    private List<PropertyDTO> properties;
    private List<ResourceTagDTO> resourceTags;
    private List<TemplateDTO> templates;

    public ResourceDTO(ResourceEntity resource, List<ResourceRelationDTO> relations, List<PropertyDTO> properties, List<TemplateDTO> templates){
        this.id = resource.getRelease().getId();
        this.release = resource.getRelease().getName();
        this.relations = relations;
        this.properties = properties;
        this.templates = templates;
    }

    public ResourceDTO(ReleaseEntity release, List<ResourceRelationDTO> relations, List<PropertyDTO> properties, List<TemplateDTO> templates){
        this.id = release.getId();
        this.release = release.getName();
        this.relations = relations;
        this.properties = properties;
        this.templates = templates;
    }

    public ResourceDTO(ReleaseEntity release) {
        this.id = release.getId();
        this.release = release.getName();
    }

    public ResourceDTO(ResourceEntity resource, List<ResourceRelationDTO> relations) {
        this.id = resource.getRelease().getId();
        this.release = resource.getRelease().getName();
        this.relations = relations;
        this.resourceTags = new ArrayList<>();
        if (resource.getResourceTags() != null) {
            for (ResourceTagEntity resourceTagEntity : resource.getResourceTags()) {
                this.resourceTags.add(new ResourceTagDTO(resourceTagEntity));
            }
        }
    }
}
