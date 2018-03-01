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

package ch.puzzle.itc.mobiliar.business.resourcerelation.entity;

import ch.puzzle.itc.mobiliar.business.resourceactivation.entity.ResourceActivationEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.CascadeType.PERSIST;

@Entity
@Audited
@Table(name="TAMW_resRelContext")
@AssociationOverrides({
	@AssociationOverride(name="properties", joinTable=@JoinTable(name="TAMW_resRelCtx_prop", 
			joinColumns=@JoinColumn(name="TAMW_RESRELCONTEXT_ID"), 
			inverseJoinColumns=@JoinColumn(name="PROPERTIES_ID"))),
	@AssociationOverride(name="propertyDescriptors", joinTable=@JoinTable(name="TAMW_resRelCtx_propDesc",
			joinColumns=@JoinColumn(name="TAMW_RESRELCONTEXT_ID"), 
			inverseJoinColumns=@JoinColumn(name="PROPERTYDESCRIPTORS_ID"))),
	@AssociationOverride(name="templates", joinTable=@JoinTable(name="TAMW_resRelCtx_tmplDesc",
			joinColumns=@JoinColumn(name="TAMW_RESRELCONTEXT_ID"), 
			inverseJoinColumns=@JoinColumn(name="TEMPLATES_ID")))
})
public class ResourceRelationContextEntity extends ContextDependency<AbstractResourceRelationEntity> {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = PERSIST)
	@Getter
	private ConsumedResourceRelationEntity consumedResourceRelation;
	
	@ManyToOne(cascade = PERSIST)
	@Getter
	private ProvidedResourceRelationEntity providedResourceRelation;


	@Getter
	@Setter
	@OneToMany(mappedBy="resourceRelationContext", cascade = CascadeType.REMOVE)
	private Set<ResourceActivationEntity> resourceActivationEntities;

	private AbstractResourceRelationEntity getResourceRelation() {
		return consumedResourceRelation!=null ? consumedResourceRelation : providedResourceRelation;
	}
	
	private void setResourceRelation(AbstractResourceRelationEntity resourceRelation) {
		if(resourceRelation instanceof ConsumedResourceRelationEntity) {
			this.consumedResourceRelation = ((ConsumedResourceRelationEntity)resourceRelation);
			this.providedResourceRelation = null;
		} else if(resourceRelation instanceof ProvidedResourceRelationEntity) {
			this.providedResourceRelation = ((ProvidedResourceRelationEntity)resourceRelation);
			this.consumedResourceRelation = null;
		}
	}
	
	@Override
	public AbstractResourceRelationEntity getContextualizedObject() {
		return getResourceRelation();
	}
	@Override
	public void setContextualizedObject(AbstractResourceRelationEntity contextualizedObject) {
		setResourceRelation(contextualizedObject);
	}
	
}
