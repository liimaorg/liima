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

package ch.puzzle.itc.mobiliar.business.resourcegroup.entity;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import lombok.Getter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Entity
@Audited
@Table(name = "TAMW_resourceTypeContext")
@AssociationOverrides({
        @AssociationOverride(name = "properties", joinTable = @JoinTable(name = "TAMW_resTypeCtx_prop",
                joinColumns = @JoinColumn(name = "TAMW_RESOURCETYPECONTEXT_ID"),
                inverseJoinColumns = @JoinColumn(name = "PROPERTIES_ID"))),
        @AssociationOverride(name = "propertyDescriptors", joinTable = @JoinTable(name = "TAMW_resTypeCtx_propDesc",
                joinColumns = @JoinColumn(name = "TAMW_RESOURCETYPECONTEXT_ID"),
                inverseJoinColumns = @JoinColumn(name = "PROPERTYDESCRIPTORS_ID"))),
        @AssociationOverride(name = "templates", joinTable = @JoinTable(name = "TAMW_resTypeCtx_tmplDesc",
                joinColumns = @JoinColumn(name = "TAMW_RESOURCETYPECONTEXT_ID"),
                inverseJoinColumns = @JoinColumn(name = "TEMPLATES_ID")))
})
public class ResourceTypeContextEntity extends ContextDependency<ResourceTypeEntity> {
    private static final long serialVersionUID = 1L;

    @Getter
    @ManyToOne(cascade = CascadeType.PERSIST)
    private ResourceTypeEntity resourceTypeEntity;

    @Override
    public ResourceTypeEntity getContextualizedObject() {
        return resourceTypeEntity;
    }

    @Override
    public void setContextualizedObject(ResourceTypeEntity contextualizedObject) {
        this.resourceTypeEntity = contextualizedObject;
    }


}
