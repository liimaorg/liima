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

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourceactivation.entity.ResourceActivationEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.util.ApplicationServerContainer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Entity implementation class for Entity: Resource
 */
@Entity
@Audited
@Table(name = "TAMW_resourceGroup")
@EqualsAndHashCode(exclude = { "resources", "runtimeTemplateDescriptors", "resourceActivationEntities" })
@NamedQuery(name = ResourceGroupEntity.ALLRESOURCESBYTYPE_QUERY, query = "select rg from ResourceGroupEntity rg left join rg.resourceType rt where rt is not null and rt.name=:restype")
public class ResourceGroupEntity implements Serializable, Comparable<ResourceGroupEntity>, NamedIdentifiable {

	public final static String ALLRESOURCESBYTYPE_QUERY = "ALLRESOURCESBYTYPE_QUERY";

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	@TableGenerator(name = "resourceGroupIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "resourceGroupId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "resourceGroupIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;

	@Getter
	@Setter
	@ManyToOne
	private ReleaseEntity outOfServiceRelease;

	@Getter
	@Setter
	@OneToMany(mappedBy = "resourceGroup", cascade = CascadeType.REMOVE)
	private Set<ResourceEntity> resources = new HashSet<ResourceEntity>();

	@ManyToOne(cascade = CascadeType.PERSIST)
	@Getter
	private ResourceTypeEntity resourceType;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "resourceGroup")
	@Getter
	private Set<ResourceActivationEntity> resourceActivationEntities;

	/**
	 * This field is for entity mapping only - it represents the template descriptor assigned to this runtime
	 * resource group (if it is one) - otherwise null (and should stay null).
	 */
	@ManyToMany
	@JoinTable(name = "TAMW_tmplDesc_targetPlat", joinColumns = { @JoinColumn(name = "RESGROUP_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "TEMPLATEDESCRIPTORS_ID", referencedColumnName = "ID") })
	private Set<TemplateDescriptorEntity> runtimeTemplateDescriptors;

	@Getter
    @Column(unique = true)
	private String name;

	@Getter
	@Version
	private long v;

    private String fcExternalKey;
    private String fcExternalLink;

	/**
	 * We currently have to ensure, that the names of the resources are updated as well - therefore we have
	 * to iterate through all of them and change their name. As soon as all queries are adapted, this is not
	 * required anymore and can be removed. We execute this logic in the prepersist state because we need to
	 * ensure that we could load the resources lazy if required.
	 */
	public void setName(String name) {
		for (ResourceEntity resource : getResources()) {
			resource.updateName(name);
		}
		this.name = name;
	}

	public void setResourceType(ResourceTypeEntity resourceType) {
		for (ResourceEntity resource : getResources()) {
			resource.updateResourceType(resourceType);
		}
		this.resourceType = resourceType;
	}

	/**
	 * @return all existing releases for this group
	 */
	public SortedSet<ReleaseEntity> getReleases() {
		SortedSet<ReleaseEntity> releases = new TreeSet<ReleaseEntity>();
		if (resources != null) {
			for (ResourceEntity resource : resources) {
				releases.add(resource.getRelease());
			}
		}
		return releases;
	}

	/**
	 * @return the newest release entity existing for this resource group or null if no release/resources
	 *         exist.
	 */
	public ReleaseEntity getNewestRelease() {
		ReleaseEntity newestRelease = null;
		if (resources != null) {
			for (ResourceEntity resource : resources) {
				if (newestRelease == null || newestRelease.compareTo(resource.getRelease()) < 0) {
					newestRelease = resource.getRelease();
				}
			}
		}
		return newestRelease;
	}

	/**
	 * @return the first release, this resource group exists in or null if no releases / resources exist for
	 *         this group
	 */
	public ReleaseEntity getFirstRelease() {
		ReleaseEntity firstRelease = null;
		if (resources != null) {
			for (ResourceEntity resource : resources) {
				if (firstRelease == null || firstRelease.compareTo(resource.getRelease()) > 0) {
					firstRelease = resource.getRelease();
				}
			}
		}
		return firstRelease;
	}

	@Override
	public String toString() {
		return "ResourceGroupEntity [name=" + getName() + " id=" + getId() + "]";
	}

	public boolean isAppServerContainer() {
		return ApplicationServerContainer.APPSERVERCONTAINER.getDisplayName().equals(getName());
	}

	/**
	 * Compare the resource groups by name null
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ResourceGroupEntity arg0) {
		if (arg0 == null) {
			return 1;
		}
		else {
			if (getName() == null) {
				return arg0.getName() == null ? 0 : -1;
			}
			else {
				return arg0.getName() == null ? 1 : getName().compareTo(arg0.getName());
			}
		}
	}

    protected String getFcExternalKey() {
        return fcExternalKey;
    }

    protected void setFcExternalKey(String fcExternalKey) {
        this.fcExternalKey = fcExternalKey;
    }

    protected String getFcExternalLink() {
        return fcExternalLink;
    }

    protected void setFcExternalLink(String fcExternalLink) {
        this.fcExternalLink = fcExternalLink;
    }

}
