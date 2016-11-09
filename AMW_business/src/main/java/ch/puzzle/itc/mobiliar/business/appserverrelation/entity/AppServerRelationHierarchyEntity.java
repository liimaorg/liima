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

package ch.puzzle.itc.mobiliar.business.appserverrelation.entity;

import static javax.persistence.CascadeType.ALL;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.envers.Audited;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;


/**
 * This entity represents overwritten relations (app server relations) which allow to overrule direct
 * relations between resources depending on the application server
 */
@Entity
@Audited
@Table(name = "TAMW_appServerRelHierarchy")
@NamedQuery(name=AppServerRelationHierarchyEntity.LOAD_ALL_ENTITIES_BY_OVERRIDDEN_RESSOURCE, query="select a from AppServerRelationHierarchyEntity a where a.overriddenSlaveResource.id=:resourceId")
public class AppServerRelationHierarchyEntity implements Serializable {

	public static final String LOAD_ALL_ENTITIES_BY_OVERRIDDEN_RESSOURCE = "LOAD_ALL_ENTITIES_BY_OVERRIDDEN_RESSOURCE";
	
	private static final long serialVersionUID = 1L;

	@TableGenerator(name = "asRelHierIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "id")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "asRelHierIdGen")
	@Id
	@Column(unique = true, nullable = false)
	@Getter
	private Integer id;

	/**
	 * Defines the parent relation and is therefore part of the navigation-path to the app server. If this
	 * value is null, this means that we have reached the first relation leaving from the app server.
	 */
	@ManyToOne(cascade = CascadeType.PERSIST)
	@Getter
	@Setter
	AppServerRelationHierarchyEntity parentRelation;

	@OneToMany(cascade = ALL, mappedBy = "parentRelation")
	@Getter
	Set<AppServerRelationHierarchyEntity> childRelations = new HashSet<AppServerRelationHierarchyEntity>();

	@ManyToOne
	@JoinColumn(name="consRel_ID")
	@Getter
	ConsumedResourceRelationEntity assignedConsumedResourceRelation;

	@ManyToOne
	@JoinColumn(name="restypeRel_ID")
	@Getter
	ResourceRelationTypeEntity assignedResourceTypeRelation;

	/**
	 * The overriddenSlaveResourceGroup is the slave resource group, the relation should actually be overridden with.
	 * This value is only set for the last entity within a relation path. For all intermediate-steps it is null.
	 */
	@ManyToOne
	@Getter
	@Setter
	@JoinColumn(name="slaveRes_ID")
	ResourceGroupEntity overriddenSlaveResource;

	@Getter
	@Version
	private long v;

	public HasContexts<? extends ContextDependency<?>> getRelation() {
		if (assignedConsumedResourceRelation != null) {
			return assignedConsumedResourceRelation;
		}
		else {
			return assignedResourceTypeRelation;
		}
	}

	public void setRelation(ConsumedResourceRelationEntity relation) {
		this.assignedResourceTypeRelation = null;
		this.assignedConsumedResourceRelation = relation;	
	}
	
	public void setRelation(ResourceRelationTypeEntity relation) {
		this.assignedConsumedResourceRelation = null;	
		this.assignedResourceTypeRelation = relation;
	}
	
	public boolean refersToRelation(ConsumedResourceRelationEntity relation){
		return getAssignedConsumedResourceRelation()!=null && getAssignedConsumedResourceRelation().getId().equals(((ConsumedResourceRelationEntity)relation).getId());		
	}
	
	public boolean refersToRelation(ResourceRelationTypeEntity relation){
		return getAssignedResourceTypeRelation()!=null && getAssignedResourceTypeRelation().getId().equals(((ResourceRelationTypeEntity)relation).getId());	
	}
	
	public ResourceEntity getApplicationServer(){
		AppServerRelationHierarchyEntity relation = this;
		while(relation.getParentRelation()!=null){
			relation = relation.getParentRelation();
		}
		if(relation!=null && relation.getAssignedConsumedResourceRelation()!=null){
			return relation.getAssignedConsumedResourceRelation().getMasterResource();
		}
		else{
			throw new RuntimeException("There is an issue with the appserver-relation "+id+": can't find an appropriate application server");
		}
	}
	
	/**
	 * @return the release of an app server relation is defined by the highest release involved within the path. 
	 */
	public ReleaseEntity getRelease(){
		ReleaseEntity maxRelease = null;
		AppServerRelationHierarchyEntity relation = this;
		while(relation!=null){
			//Only consumed resource relations are relevant for releasing - resourcetyperelations are not.
			if(relation.getAssignedConsumedResourceRelation()!=null && relation.getAssignedConsumedResourceRelation().getMasterResource()!=null && getAssignedConsumedResourceRelation().getMasterResource().getRelease()!=null){
				ReleaseEntity release = relation.getAssignedConsumedResourceRelation().getMasterResource().getRelease();
				if(maxRelease==null || release.compareTo(maxRelease)>0){
					maxRelease = release;
				}
			}
			relation = relation.getParentRelation();
		}
		return maxRelease;
		
	}
	
}
