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
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.entity.ResourceTypePermission;
import ch.puzzle.itc.mobiliar.business.security.entity.RestrictionEntity;
import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "restriction")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class RestrictionDTO {

    @Getter @Setter
    private Integer id;
    @Getter @Setter
    private String roleName;
    @Getter @Setter
    private String userName;
    @Getter @Setter
    @ApiModelProperty(required = true)
    private Permission permission;
    @Getter @Setter
    private Integer resourceGroupId;
    @Getter @Setter
    private String resourceTypeName;
    @Getter @Setter
    private ResourceTypePermission resourceTypePermission;
    @Getter @Setter
    private String contextName;
    @Getter @Setter
    private Action action;

    RestrictionDTO(){}

    public RestrictionDTO(Integer id, String roleName, String  userName, Permission permission, Integer resourceGroupId, String resourceTypeName, ResourceTypePermission resourceTypePermission, String contextName, Action action) {
        this.id = id;
        this.roleName = roleName;
        this.userName = userName;
        this.permission = permission;
        this.resourceGroupId = resourceGroupId;
        this.resourceTypeName = resourceTypeName;
        this.resourceTypePermission = resourceTypePermission;
        this.contextName = contextName;
        this.action = action;
    }

    public RestrictionDTO(RestrictionEntity restrictionEntity) {
        this.id = restrictionEntity.getId();
        this.roleName = restrictionEntity.getRole() != null ? restrictionEntity.getRole().getName() : null;
        this.userName = restrictionEntity.getUser() != null ? restrictionEntity.getUser().getName() : null;
        this.permission = Permission.valueOf(restrictionEntity.getPermission().getValue());
        this.resourceGroupId = restrictionEntity.getResourceGroup() != null ? restrictionEntity.getResourceGroup().getId() : null;
        this.resourceTypeName = restrictionEntity.getResourceType() != null ? restrictionEntity.getResourceType().getName() : null;
        this.resourceTypePermission = restrictionEntity.getResourceTypePermission();
        this.contextName = restrictionEntity.getContext() != null ? restrictionEntity.getContext().getName() : null;
        this.action = restrictionEntity.getAction();
    }
}
