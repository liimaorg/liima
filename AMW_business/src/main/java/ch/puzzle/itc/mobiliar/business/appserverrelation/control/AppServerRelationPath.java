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

package ch.puzzle.itc.mobiliar.business.appserverrelation.control;

import ch.puzzle.itc.mobiliar.business.appserverrelation.entity.AppServerRelationCapable;
import ch.puzzle.itc.mobiliar.business.appserverrelation.entity.AppServerRelationHierarchyEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class AppServerRelationPath {

	public AppServerRelationPath(List<AppServerRelationCapable> path) {
		super();
		this.path = path;
		this.lastRelationOfPath = path != null && !path.isEmpty() ? path.get(path.size() - 1) : null;
		this.displayablePath = extractDisplayablePath();
	}

	private String extractDisplayablePath() {
		StringBuilder sb = new StringBuilder();
		AppServerRelationCapable previousP = null;
		for (AppServerRelationCapable p : path) {		
			if(p.getBaseClass()==ResourceRelationTypeEntity.class && previousP!=null){
				sb.append(previousP.getSlaveResource().getName()).append(':');
			}
			else{
				sb.append(p.getMasterResourceName()!=null ? p.getMasterResourceName() : p.getMasterResourceTypeName()).append(':');
			}
			previousP = p;
		}
		if (lastRelationOfPath != null) {			
			String identifier = lastRelationOfPath.getRelationIdentifier();
			if(lastRelationOfPath.isMasterDefaultResourceType()){
				if(lastRelationOfPath.getSlaveResource()!=null){
					sb.append(lastRelationOfPath.getSlaveResource().getName());
				}
				if(identifier!=null && StringUtils.isNotBlank(identifier) && StringUtils.isNumeric(identifier)){
					sb.append('_').append(identifier);
				}
			}
			else{
				sb.append(identifier==null ? lastRelationOfPath.getSlaveResourceTypeName().toLowerCase() : identifier);
			}

		}
		return sb.toString();
	}

	@Getter
	@Setter
	AppServerRelationHierarchyEntity appserverRelation;

	@Getter
	final List<AppServerRelationCapable> path;

	@Getter
	final AppServerRelationCapable lastRelationOfPath;
	
	/**
	 * Represents a temporarily selected resource group
	 */
	@Getter
	@Setter
	ResourceGroupEntity selectedResourceGroup;
	
	/**
	 * To distinct between "not redefined" resource groups and such which shall be reset, we set this flag.
	 */
	@Getter
	@Setter
	boolean removeRelation;

	@Getter
	final String displayablePath;

	public String getMasterName() {
		return getLastRelationOfPath() != null ? getLastRelationOfPath().getMasterResourceName()
				: null;
	}
	
	public String getMasterRelease(){
		return getLastRelationOfPath() != null ? getLastRelationOfPath().getMasterRelease()	: null;
	}

	public Integer getMasterId() {
		return getLastRelationOfPath() != null ? getLastRelationOfPath().getMasterResourceId() : null;
	}

	public String getMasterTypeName() {
		return getLastRelationOfPath() != null ? getLastRelationOfPath().getMasterResourceTypeName() : null;
	}

	public Integer getMasterTypeId() {
		return getLastRelationOfPath() != null ? getLastRelationOfPath().getMasterResourceTypeId() : null;
	}

	public boolean isMasterDefaultType() {
		return getLastRelationOfPath() != null	&& getLastRelationOfPath().isMasterDefaultResourceType();
	}

	public boolean isRedefined() {
		return appserverRelation != null && appserverRelation.getOverriddenSlaveResource() != null;
	}
	
	public boolean isUndefined() {
		return !isRedefined() && getLastRelationOfPath().getSlaveResource()==null;
	}

	public ResourceGroupEntity getSelectedResource() {
		//If a resource has been selected (without yet being persisted), this is the resource we would like to use
		if(selectedResourceGroup!=null){
			return selectedResourceGroup;
		}
		//Otherwise, if the path already contains a (persisted) selected resource, we use this one.
		else if(appserverRelation != null && appserverRelation.getOverriddenSlaveResource() != null){
			return appserverRelation.getOverriddenSlaveResource();
		}
		//If no resource has been selected, we use the slave resource of the last relation path element.
		else{
			return getLastRelationOfPath()!=null && getLastRelationOfPath().getSlaveResource()!=null ? getLastRelationOfPath().getSlaveResource().getResourceGroup() : null;
		}
	}
	
	public ResourceEntity getSelectedResourceEntityIfAvailable(){
		return getLastRelationOfPath()!=null && getLastRelationOfPath().getSlaveResource()!=null && getSelectedResource()==getLastRelationOfPath().getSlaveResource().getResourceGroup() ? getLastRelationOfPath().getSlaveResource() : null;
	}
	
	public Integer getSelectedResourceId(){
		ResourceEntity resource = getSelectedResourceEntityIfAvailable();
		return resource==null ? null : resource.getId();
	}
	
	public String getSelectedResourceName(){
		if(getSelectedResource()==null){
			return null;
		}
		ResourceEntity resource = getSelectedResourceEntityIfAvailable();
		if(resource!=null){
			return getSelectedResource().getName()+" ("+resource.getRelease().getName()+")";
		}
		else{
			return getSelectedResource().getName();
		}		
	}
	
	
	public String getSelectedResourceTypeName(){
		return getSelectedResource()==null ? getLastRelationOfPath().getSlaveResourceTypeName() : getSelectedResource().getResourceType().getName();
	}
	
	public Integer getSelectedResourceTypeId(){
		return getSelectedResource()==null ? getLastRelationOfPath().getSlaveResourceTypeId() : getSelectedResource().getResourceType().getId();
	}

	public String getSlaveTypeName() {
		ResourceGroupEntity slave = getSelectedResource();
		return slave != null && slave.getResourceType() != null ? slave.getResourceType().getName() : getLastRelationOfPath().getSlaveResourceTypeName();

	}

	public String getSlaveName() {
		ResourceGroupEntity slave = getSelectedResource();
		return slave != null ? slave.getName() : null;

	}
	
	public static AppServerRelationPath findPathByResourceNames(List<AppServerRelationPath> paths, String... resources){
		for(AppServerRelationPath p : paths){
			if(resources!=null && resources.length==p.getPath().size()+1){
				int index = 0;
				for(AppServerRelationCapable capable : p.getPath()){
					if(!capable.getMasterResourceName().equals(resources[index])){
						break;
					}
					if(p.getPath().size()==index+1 && capable.getSlaveResource().getName().equals(resources[index+1])){
						return p;
					}					
					index++;
				}
			}		
		}
		return null;
	}
}
