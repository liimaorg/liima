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

package ch.puzzle.itc.mobiliar.business.security.entity;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;

import javax.persistence.*;

import java.io.Serializable;

import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name="TAMW_restriction")
public class RestrictionEntity implements Serializable {

    private static final long serialVersionUID = 7865243852438L;
    @Getter
    @Setter
    @TableGenerator(name = "restrictionIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME,
            valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "restrictionId")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "restrictionIdGen")
    @Id
    @Column(unique = true, nullable = false)
    private Integer id;

    @Getter
    @Version
    private long v;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserRestrictionEntity user;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "permission_id")
    private PermissionEntity permission;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Action action;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private ResourceTypePermission resourceTypePermission;

    @Getter
    @Setter
    @ManyToOne
    @OnDelete(action = CASCADE)
    @JoinColumn(name = "context_id")
    private ContextEntity context;

    @Getter
    @Setter
    @ManyToOne
    @OnDelete(action = CASCADE)
    @JoinColumn(name = "resourcetype_id")
    private ResourceTypeEntity resourceType;

    @Getter
    @Setter
    @ManyToOne
    @OnDelete(action = CASCADE)
    @JoinColumn(name = "resourcegroup_id")
    private ResourceGroupEntity resourceGroup;

    public RestrictionEntity() {
        resourceTypePermission = ResourceTypePermission.ANY;
    }
}
