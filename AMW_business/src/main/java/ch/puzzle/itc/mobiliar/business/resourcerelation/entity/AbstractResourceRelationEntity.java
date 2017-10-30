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

import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.PERSIST;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.envers.Audited;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.environment.entity.HasTypeContext;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.Foreignable;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;

@Audited
@MappedSuperclass
public abstract class AbstractResourceRelationEntity extends HasContexts<ResourceRelationContextEntity>
		implements Identifiable, HasTypeContext<ResourceRelationTypeEntity>, Foreignable<AbstractResourceRelationEntity> {

	/**
	 * This is the ID for all subclasses. ATTENTION: The application code assumes, that ids for subclasses are
	 * exclusive! If you have to switch to another id generation mechanism than the shared sequence, please
	 * ensure that an id can not direct to multiple subtypes of this class!
	 */
	@Getter
	@Setter
	@TableGenerator(name = "resourceRelationIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "resourceRelationId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "resourceRelationIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;

	@Getter
	@Setter
	@ManyToOne(optional = true, cascade = PERSIST)
	private ResourceRelationTypeEntity resourceRelationType;

	@Getter
	@Setter
	@ManyToOne(cascade = { PERSIST, DETACH })
	private ResourceEntity masterResource;

	@Getter
	@Setter
	@ManyToOne(cascade = { PERSIST, DETACH })
	private ResourceEntity slaveResource;

	@Version
	@Getter
	private long v;

	@Getter
	@Setter
	@Column(nullable = true)
	private String identifier;

	@Enumerated(EnumType.STRING)
	private ForeignableOwner fcOwner;

	private String fcExternalKey;
	private String fcExternalLink;

	public ForeignableOwner getOwner() {
		return fcOwner;
	}

	public void setOwner(ForeignableOwner owner) {
		this.fcOwner = owner;
	}

	/**
	 * Creates new entity object with default system owner
	 */
	public AbstractResourceRelationEntity() {
		this(ForeignableOwner.getSystemOwner());
	}

	public AbstractResourceRelationEntity(ForeignableOwner owner) {
		this.fcOwner = Objects.requireNonNull(owner, "Owner must not be null");
	}

    protected abstract int foreignableRelationFieldHashCode();

	public final static Comparator<AbstractResourceRelationEntity> COMPARE_BY_SLAVE_NAME = new Comparator<AbstractResourceRelationEntity>(){

		@Override
		public int compare(AbstractResourceRelationEntity o1, AbstractResourceRelationEntity o2) {
			if(o1!=null && o2!=null){
				int result = 0;
				if(o1.slaveResource==null || o1.slaveResource.getName()==null){
					result = o2.slaveResource==null || o2.slaveResource.getName()==null ? 0 : 1;
				}
				else if(o2.slaveResource==null || o2.slaveResource.getName()==null){
					result = -1;
				}
				else{
					result = o1.slaveResource.getName().compareTo(o2.slaveResource.getName());
				}
				if(result==0){
					if(o1.getId()==null){
						return o2.getId()==null ? 0 : -1;
					}
					else{
						return o2.getId()==null ? 1 : o1.getId().compareTo(o2.getId());
					}
				}
				return result;
			}
			return 0;
		}
		
	};

	public void addContext(ResourceRelationContextEntity relationToContext) {
		if (getContexts() == null) {
			setContexts(new HashSet<ResourceRelationContextEntity>());
		}
		getContexts().add(relationToContext);
	}

	public void removeContext(ResourceRelationContextEntity relationToContext) {
		if (getContexts() != null) {
			getContexts().remove(relationToContext);
		}
	}

	public ResourceRelationContextEntity getResourceRelationContext(ContextEntity context) {
		if (getContexts() != null) {
			for (ResourceRelationContextEntity c : getContexts()) {
				if (c != null && c.getContext() != null && c.getContext().getId().equals(context.getId())) {
					return c;
				}
			}
		}
		return null;
	}

	@Override
	public ResourceRelationContextEntity createContext() {
		ResourceRelationContextEntity context = new ResourceRelationContextEntity();
		context.setContextualizedObject(this);
		return context;
	}

	@Override
	public String toString() {
		return "AbstractResourceRelationEntity [id=" + id + ", masterResource=" + masterResource
				+ ", slaveResource=" + slaveResource + "]";
	}

	public String buildIdentifer() {
		String typeIdentifier = getResourceRelationType().getIdentifierOrTypeBName();

		if (StringUtils.isNotBlank(getIdentifier()) && !StringUtils.isNumeric(getIdentifier())){
			return getIdentifier();
		} else if (masterResource.getResourceType().isDefaultResourceType()) {
			// use localPortId if available
			typeIdentifier = (slaveResource.getLocalPortId() != null) ? slaveResource.getLocalPortId() : slaveResource.getName();
		}

		if (StringUtils.isNotBlank(getIdentifier()) && StringUtils.isNumeric(getIdentifier())) {
			typeIdentifier += "_" + getIdentifier();
		}
		return typeIdentifier;
	}

	public boolean isMasterResource(ResourceEntity resource){
		return resource.equals(getMasterResource());
	}

	@Override
	public ResourceRelationTypeEntity getTypeContext() {
		return resourceRelationType;
	}

	@Override
	public String getExternalLink() {
		return fcExternalLink;
	}

	@Override
	public void setExternalLink(String externalLink) {
		this.fcExternalLink = externalLink;
	}

	@Override
	public String getExternalKey() {
		return fcExternalKey;
	}

	@Override
	public void setExternalKey(String externalKey) {
		this.fcExternalKey = externalKey;
	}

	@Override
	public String getForeignableObjectName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public abstract AbstractResourceRelationEntity getCopy(AbstractResourceRelationEntity target, CopyUnit copyUnit);


    @Override
    public int foreignableFieldHashCode() {
        HashCodeBuilder eb = new HashCodeBuilder();

        eb.append(this.id);
        eb.append(this.fcOwner);
        eb.append(this.fcExternalKey);
        eb.append(this.fcExternalLink);
        eb.append(this.identifier);
        eb.append(this.masterResource != null ? this.masterResource.getId() : null);
        eb.append(this.slaveResource != null ? this.slaveResource.getId() : null);
        eb.append(foreignableRelationFieldHashCode());
        return eb.toHashCode();
    }
}
