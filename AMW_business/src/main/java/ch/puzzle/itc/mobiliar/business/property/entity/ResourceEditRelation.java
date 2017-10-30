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

package ch.puzzle.itc.mobiliar.business.property.entity;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableAttributesDTO;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;
import java.util.Date;

/**
 * Read only entity for the resource edit screen to display relations
 */
public class ResourceEditRelation {
	
	public enum Mode {
		CONSUMED, PROVIDED, TYPE
	}

	@Getter
	private Integer resRelId;
	@Getter
	private Integer slaveId;
	@Getter
	private String slaveName;
	@Getter
	private Integer slaveGroupId;
	@Getter
	private Integer slaveReleaseId;
	@Getter
	private String slaveReleaseName;
	@Getter
	private Integer slaveTypeId;
	@Getter
	private String slaveTypeName;
	@Getter
	private String masterTypeName;
	@Getter
	private Integer resRelTypeId;

	private String typeIdentifier;
	@Getter
	private String identifier;

    @Getter
	private Mode mode;

	@Getter
	private Date slaveReleaseDate;

    @Getter
    private ForeignableAttributesDTO sourceForeignableAttributes;

    @Getter
    private ForeignableAttributesDTO relationForeignableAttributes;

    @Getter
    private ForeignableAttributesDTO targetForeignableAttributes;

    /**
     * Constructor must have the same number of arguments as the result of the SQL query and the result types
     * must match. Location of the query: ResourceEditService.loadResourceEditRelations
     **
     * @param resRelId
     * @param slaveId
     * @param identifier
     * @param relationFcOwner
     * @param relationFcExternalKey
     * @param relationFcExternalLink
     * @param slaveName
     * @param slaveFcOwner
     * @param slaveFcExternalKey
     * @param slaveFcExternalLink
     * @param slaveGroupId
     * @param slaveReleaseId
     * @param slaveReleaseName
     * @param slaveTypeId
     * @param slaveTypeName
     * @param masterFcOwner
     * @param masterFcExternalKey
     * @param masterFcExternalLink
     * @param masterTypeName
     * @param resRelTypeId
     * @param typeIdentifier
     * @param relationMode
     * @param slaveReleaseDate
     */
	public ResourceEditRelation(Integer resRelId, Integer slaveId, String identifier, String relationFcOwner, String relationFcExternalKey, String relationFcExternalLink, String slaveName, String slaveFcOwner, String slaveFcExternalKey, String slaveFcExternalLink,
			Integer slaveGroupId, Integer slaveReleaseId, String slaveReleaseName, Integer slaveTypeId,
			String slaveTypeName, String masterFcOwner, String masterFcExternalKey, String masterFcExternalLink, String masterTypeName, Integer resRelTypeId, String typeIdentifier,
			String relationMode, Date slaveReleaseDate) {
		this.mode = Mode.valueOf(relationMode);
		this.resRelId = resRelId;
		this.slaveId = slaveId;
		this.identifier = identifier;
		this.slaveName = slaveName;
        this.sourceForeignableAttributes = createForeignableAttributes(masterFcOwner, masterFcExternalKey, masterFcExternalLink);
        this.relationForeignableAttributes = createForeignableAttributes(relationFcOwner, relationFcExternalKey, relationFcExternalLink);
        this.targetForeignableAttributes = createForeignableAttributes(slaveFcOwner, slaveFcExternalKey, slaveFcExternalLink);
		this.slaveGroupId = slaveGroupId;
		this.slaveReleaseId = slaveReleaseId;
		this.slaveReleaseName = slaveReleaseName;
		this.resRelTypeId = resRelTypeId;
		this.typeIdentifier = typeIdentifier;
		this.slaveTypeId = slaveTypeId;
		this.masterTypeName = masterTypeName;
		this.slaveTypeName = slaveTypeName;
		this.slaveReleaseDate = slaveReleaseDate;
	}

    private ForeignableAttributesDTO createForeignableAttributes(String owner, String fcExternalKey, String fcExternalLink){
        if (owner != null){
            return new ForeignableAttributesDTO(ForeignableOwner.valueOf(owner), fcExternalKey, fcExternalLink);
        }
        // create system owned not foreignable
        return new ForeignableAttributesDTO();
    }

	/**
	 * @return name + relation-identifier (ad_1) or type-identifier (adXY) or null if any of this is
	 *         available
	 */
	public String getQualifiedIdentifier() {
		String idString = typeIdentifier;
		if (isDefaultResourceType(masterTypeName)) {
			idString = slaveName;
		}
		if (StringUtils.isNotBlank(identifier)) {
			idString = identifier;
		}
		return idString != null ? idString : "";
	}

	private boolean isDefaultResourceType(String typeName) {
		return DefaultResourceTypeDefinition.contains(typeName);
	}

     public boolean hasIdentifierChanged(String newIdentifier){
		 return !getQualifiedIdentifier().equals(newIdentifier);
	}

	public String getDisplayName(){
		StringBuilder sb = new StringBuilder();
	    if(isResourceTypeRelation()){
		   return StringUtils.isEmpty(typeIdentifier) ? getSlaveTypeName().toLowerCase() : typeIdentifier;
	    }
	    else{
		    if (!StringUtils.isEmpty(getSlaveName())) {
			  sb.append(getSlaveName());
			  if (!getQualifiedIdentifier().isEmpty() && !getQualifiedIdentifier().equals(getSlaveName())) {
				 sb.append(" (");
				 sb.append(getQualifiedIdentifier());
				 sb.append(")");
			  }
			  return sb.toString();
		   }
		   return !StringUtils.isEmpty(this.typeIdentifier) ? this.typeIdentifier : getSlaveTypeName().toLowerCase();
	    }
	}

	public static Comparator<ResourceEditRelation> releaseComparator() {
		return new Comparator<ResourceEditRelation>() {

			@Override
			public int compare(ResourceEditRelation rel1, ResourceEditRelation rel2) {
				if (rel2 == null || rel2.getSlaveReleaseDate() == null) {
					return rel1 == null ? 0 : -1;
				}
				return rel1 == null ? 1 : rel1.getSlaveReleaseDate()
						.compareTo(rel2.getSlaveReleaseDate());
			}
		};
	}

     public boolean isResourceTypeRelation(){
	    return resRelId==null;
	}

     public Integer getUniqueIdentifier(){
	    if(isResourceTypeRelation()){
		   return resRelTypeId;
	    }
	    else{
		   return resRelId;
	    }
	}

}
