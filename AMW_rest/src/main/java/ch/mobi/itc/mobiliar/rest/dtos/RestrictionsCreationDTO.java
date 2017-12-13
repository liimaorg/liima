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

import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.ResourceTypePermission;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "restrictionsCreation")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class RestrictionsCreationDTO {

    private String roleName;

    private List<String> userNames;

    private List<String> permissions;

    private List<Integer> resourceGroupIds;

    private List<String> resourceTypeNames;

    private ResourceTypePermission resourceTypePermission;

    private List<String> contextNames;

    private List<Action> actions;

    RestrictionsCreationDTO(){}

    public RestrictionsCreationDTO(String roleName, List<String>  userNames, List<String> permissions,
                                   List<Integer> resourceGroupIds, List<String> resourceTypeNames,
                                   ResourceTypePermission resourceTypePermission, List<String> contextNames,
                                   List<Action> actions) {
        this.roleName = roleName;
        this.userNames = userNames;
        this.permissions = permissions;
        this.resourceGroupIds = resourceGroupIds;
        this.resourceTypeNames = resourceTypeNames;
        this.resourceTypePermission = resourceTypePermission;
        this.contextNames = contextNames;
        this.actions = actions;
    }

}
