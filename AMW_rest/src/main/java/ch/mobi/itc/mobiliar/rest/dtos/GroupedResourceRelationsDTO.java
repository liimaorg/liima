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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Groups the relations shown on the resource edit screen, mirroring the JSF ResourceRelationModel:
 *  - runtime: consumed relations with slave type RUNTIME
 *  - consumed: consumed relations excluding APPLICATION and RUNTIME
 *  - provided: provided relations
 *  - unresolved: type-level relations that have no concrete resource instance yet
 */
@XmlRootElement(name = "groupedResourceRelations")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupedResourceRelationsDTO {

    private List<ResourceRelationDTO> runtime = new ArrayList<>();
    private List<ResourceRelationDTO> consumed = new ArrayList<>();
    private List<ResourceRelationDTO> provided = new ArrayList<>();
    private List<UnresolvedRelationDTO> unresolved = new ArrayList<>();
}
