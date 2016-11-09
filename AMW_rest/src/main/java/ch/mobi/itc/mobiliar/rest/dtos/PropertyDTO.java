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

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class PropertyDTO {

    @Getter @Setter
    private String name;
    @Getter @Setter
    private String value;
    @Getter @Setter
    private String replacedValue;
    @Getter @Setter
    private String generalComment;
    @Getter @Setter
    private String valueComment;
    @Getter @Setter
    private String context;

    PropertyDTO(){}

    public PropertyDTO(ResourceEditProperty property, String context){
        this.name = property.getTechnicalKey();
        this.value = property.getDecryptedPropertyValue();
        this.replacedValue = property.getReplacedValue();
        this.generalComment = property.getPropertyComment();
        this.context = context;
    }
}
