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
import org.hibernate.envers.Audited;

import javax.persistence.*;

import static javax.persistence.CascadeType.PERSIST;

@Entity
@Audited
@Table(name="TAMW_resourceContext")
@AssociationOverrides({
	@AssociationOverride(name="properties", joinTable=@JoinTable(name="TAMW_resourceCtx_prop", 
			joinColumns=@JoinColumn(name="TAMW_RESOURCECONTEXT_ID"), 
			inverseJoinColumns=@JoinColumn(name="PROPERTIES_ID"))),
	@AssociationOverride(name="propertyDescriptors", joinTable=@JoinTable(name="TAMW_resourceCtx_propDesc", 
			joinColumns=@JoinColumn(name="TAMW_RESOURCECONTEXT_ID"), 
			inverseJoinColumns=@JoinColumn(name="PROPERTYDESCRIPTORS_ID"))),
	@AssociationOverride(name="templates", joinTable=@JoinTable(name="TAMW_resourceCtx_tmplDesc", 
			joinColumns=@JoinColumn(name="TAMW_RESOURCECONTEXT_ID"), 
			inverseJoinColumns=@JoinColumn(name="TEMPLATES_ID")))
})
public class ResourceContextEntity extends ContextDependency<ResourceEntity> {
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(cascade = PERSIST)
	private ResourceEntity resource;
		
	@Override
	public ResourceEntity getContextualizedObject() {
		return resource;
	}
	@Override
	public void setContextualizedObject(ResourceEntity contextualizedObject) {
		this.resource = contextualizedObject;
	}
	
}
