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

import java.util.*;

import lombok.Getter;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.utils.DomainObject;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

public class ResourceGroup extends DomainObject<ResourceGroupEntity> implements Comparable<ResourceGroup> {

	@Getter
	private String name;
	private Map<Integer, ResourceEntity> releaseToResourceMap = new HashMap<Integer, ResourceEntity>();
	private Integer selectedResourceId;
	private Integer selectedReleaseId;
	private ResourceEntity defaultResource;
	private SortedSet<ReleaseEntity> allGroupReleases = new TreeSet<ReleaseEntity>();
	private Map<Integer, ResourceEntity> resourcesMap = new HashMap<Integer, ResourceEntity>();
	private Map<Integer, Set<ResourceEntity>> appServerToAppMap = new HashMap<Integer, Set<ResourceEntity>>();
	private Set<ReleaseEntity> releasesToExclude = new HashSet<ReleaseEntity>();

	private ResourceGroup() {
		super(null);
	}

	public static ResourceGroup createByResource(ResourceGroupEntity r, ResourceDependencyResolverService dependencyResolver) {
		ResourceGroup resourceGroup = new ResourceGroup();
		if (r == null) {
			r = new ResourceGroupEntity();
		}
		resourceGroup.wrap(r);

		if (dependencyResolver != null) {
			ReleaseEntity defaultRelease = dependencyResolver.findMostRelevantRelease(resourceGroup.allGroupReleases, new Date());
			if (resourceGroup.releaseToResourceMap.containsKey(defaultRelease.getId())) {
				resourceGroup.defaultResource = resourceGroup.releaseToResourceMap.get(defaultRelease.getId());
			}
		}

		return resourceGroup;
	}

	public static ResourceGroup createByResource(ResourceGroupEntity r) {
		ResourceGroup resourceGroup = new ResourceGroup();
		if (r == null) {
			r = new ResourceGroupEntity();
		}
		resourceGroup.wrap(r);
		return resourceGroup;
	}

    public ResourceEntity getResourceForRelease(Integer releaseId){
        if (releaseToResourceMap.containsKey(releaseId)){
            return releaseToResourceMap.get(releaseId);
        }
        return null;
    }

	@Override
	public DomainObject<ResourceGroupEntity> wrap(ResourceGroupEntity entity) {
		setEntity(entity);
		this.name = entity.getName();
		for (ResourceEntity res : entity.getResources()) {
			releaseToResourceMap.put(res.getRelease().getId(), res);
			resourcesMap.put(res.getId(), res);
			allGroupReleases.add(res.getRelease());
		}
		return this;
	}

	public List<ReleaseEntity> getReleases() {
		List<ReleaseEntity> releases = new ArrayList<ReleaseEntity>();
		for (ReleaseEntity rel : allGroupReleases) {
			if (!releasesToExclude.contains(rel)) {
				releases.add(rel);
			}
		}
		return releases;
	}

	public SortedSet<ReleaseEntity> getSortedReleases() {
		return new TreeSet<ReleaseEntity>(getReleases());
	}

	public LinkedHashMap<String, Integer> getReleaseToResourceMap() {
		LinkedHashMap<String, Integer> relResMap = new LinkedHashMap<String, Integer>();
		for (ReleaseEntity rel : getSortedReleases()) {
			relResMap.put(rel.getName(), releaseToResourceMap.get(rel.getId())
					.getId());
		}
		return relResMap;
	}

	public void setSelectedReleaseId(Integer releaseId) {
		this.selectedReleaseId = releaseId;
		if (releaseToResourceMap.containsKey(releaseId)) {
			selectedResourceId = releaseToResourceMap.get(releaseId).getId();
		}
	}

	public Integer getId() {
		if (getEntity() == null) {
			return null;
		}
		return getEntity().getId();
	}

	public Integer getSelectedResourceId() {
		Integer defaultResourceId = defaultResource != null ? defaultResource.getId() : null;
		return selectedResourceId != null ? selectedResourceId : defaultResourceId;
	}

	public ResourceEntity getSelectedResource() {
		return getSelectedResourceId() != null ? resourcesMap.get(getSelectedResourceId()) : null;
	}

	public Integer getSelectedReleaseId(){
		Integer defaultReleaseId = defaultResource != null ? defaultResource.getRelease().getId() : null;
		return selectedReleaseId != null ? selectedReleaseId : defaultReleaseId;
	}

	public Set<ResourceEntity> collectApplicationsForAS(Integer asId,
			ResourceTypeProvider resourceTypeProvider) {
		if (!appServerToAppMap.containsKey(asId) && resourcesMap.containsKey(asId)) {
			ResourceEntity as = resourcesMap.get(asId);
			List<ResourceEntity> applications = as
					.getConsumedRelatedResourcesByResourceType((DefaultResourceTypeDefinition.APPLICATION));
			appServerToAppMap.put(asId, new HashSet<ResourceEntity>(applications));
		}
		return appServerToAppMap.containsKey(asId) ? appServerToAppMap.get(asId)
				: new HashSet<ResourceEntity>();
	}

	public Set<ResourceEntity> collectApplicationsForAllAS(ResourceTypeProvider resourceTypeProvider) {
		Set<ResourceEntity> appsForAs = new HashSet<ResourceEntity>();
		for (ResourceEntity res : resourcesMap.values()) {
			if (res.getResourceType().isApplicationServerResourceType()) {
				appsForAs.addAll(collectApplicationsForAS(res.getId(), resourceTypeProvider));
			}
		}
		return appsForAs;
	}

	@Override
	public int compareTo(ResourceGroup o) {
		if (getName() == null) {
			return -1;
		}
		if (o == null) {
			return 1;
		}
		if (getEntity() == null) {
			return -1;
		}
		return getEntity().getName().compareToIgnoreCase(o.getName());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO [cw]: wenn möglich equals nicht überschreiben!
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ResourceGroup other = (ResourceGroup) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		}
		else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ResourceGroup [getEntity()=" + getEntity() + "]";
	}

	public void addReleaseToExclude(ReleaseEntity release) {
		releasesToExclude.add(release);
	}
}
