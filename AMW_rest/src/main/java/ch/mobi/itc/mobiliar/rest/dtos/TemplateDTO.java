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

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "template")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class TemplateDTO {

    @Getter @Setter
    private String relatedResourceIdentifier;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String targetPath;
    @Getter @Setter
    private Set<String> targetPlatforms;
    @Getter @Setter
    private String fileContent;

    TemplateDTO(){}

    public TemplateDTO(TemplateDescriptorEntity template){
        this.relatedResourceIdentifier = template.getRelatedResourceIdentifier();
        this.name = template.getName();
        this.targetPath = template.getTargetPath();
        targetPlatforms = new HashSet<>();
        for (ResourceGroupEntity pf : template.getTargetPlatforms()) {
            targetPlatforms.add(pf.getName());
        }

        this.fileContent = template.getFileContent();
    }
}
