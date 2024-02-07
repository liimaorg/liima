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

package ch.puzzle.itc.mobiliar.business.releasing.boundary;

import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtPersistenceService;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseRepository;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.ConcurrentModificationException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import lombok.NonNull;

import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.*;
import java.util.logging.Logger;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.*;

@Stateless
public class ReleaseLocator {

    // TODO add permission check
    @Inject
    protected Logger log;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    ReleaseMgmtPersistenceService persistenceService;

    public ReleaseEntity getReleaseByName(String name) {
        return releaseRepository.getReleaseByName(name);
    }

    @HasPermission(permission = Permission.RELEASE, action = READ)
    public ReleaseEntity getReleaseById(@NonNull Integer id) throws NotFoundException {
        ReleaseEntity entity = releaseRepository.find(id);
        this.requireNotNull(entity);
        return entity;
    }

    private void requireNotNull(ReleaseEntity entity) throws NotFoundException {
        if (entity == null) {
            throw new NotFoundException("Release not found.");
        }
    }

    public List<ReleaseEntity> getReleasesForResourceGroup(ResourceGroupEntity resourceGroup) {
        return releaseRepository.getReleasesForResourceGroup(resourceGroup);
    }

    /**
     * @return the number of existing releases
     */
    @HasPermission(permission = Permission.RELEASE, action = READ)
    public int countReleases() {
        return persistenceService.count();
    }

    /**
     * Returns a list of releases for management operations. This means, we
     * don't care about relations to resources or deployments but only want to
     * create, edit or delete plain release-instances
     */
    @HasPermission(permission = Permission.RELEASE, action = READ)
    public List<ReleaseEntity> loadReleasesForMgmt(Integer startIndex, Integer length, boolean sortDesc) {
        return persistenceService.loadReleaseEntities(startIndex, length, sortDesc);
    }

    /**
     * Load all releases
     */
    @HasPermission(permission = Permission.RELEASE, action = READ)
    public List<ReleaseEntity> loadAllReleases(boolean sortDesc) {

        return persistenceService.loadAllReleaseEntities(sortDesc);
    }

    @HasPermission(permission = Permission.RELEASE, action = READ)
    public ReleaseEntity getDefaultRelease() {
        return persistenceService.getDefaultRelease();
    }


    @HasPermission(permission = Permission.RELEASE, action = READ)
    public List<ResourceEntity> getResourcesForRelease(Integer releaseId) {
        return persistenceService.getResourcesForRelease(releaseId);
    }

    /**
     * Persists the given new release.
     */
    @HasPermission(permission = Permission.RELEASE, action = CREATE)
    public boolean create(ReleaseEntity release) {
        return persistenceService.saveReleaseEntity(release);
    }

    /**
     * Persists the given release - the already existing instance will be updated.
     */
    @HasPermission(permission = Permission.RELEASE, action = UPDATE)
    public boolean update(ReleaseEntity release) throws ConcurrentModificationException {
        try {
            return persistenceService.saveReleaseEntity(release);
        } catch (EJBTransactionRolledbackException e) {
            throw new ConcurrentModificationException("Concurrent update prevented! Please reload before updating release: " + release);
        }
    }

    @HasPermission(permission = Permission.RELEASE, action = DELETE)
    public void delete(ReleaseEntity release) {
        releaseRepository.removeRelease(release);
    }

    @HasPermission(permission = Permission.RELEASE, action = READ)
    public SortedMap<String, SortedSet<ResourceEntityDto>> loadResourcesAndDeploymentsForRelease(Integer releaseId) {
        SortedMap<String, SortedSet<ResourceEntityDto>> resourcesForCurrentRelease;
        List<ResourceEntity> result = this.getResourcesForRelease(releaseId);
        resourcesForCurrentRelease = new TreeMap();
        for (ResourceEntity r : result) {
            ResourceTypeEntityDto typeDto = new ResourceTypeEntityDto(r.getResourceType().getId(), r.getResourceType().getName());
            ResourceEntityDto entityDto = new ResourceEntityDto(r.getId(), r.getName(), typeDto);
            if (!resourcesForCurrentRelease.containsKey(typeDto.getName())) {
                resourcesForCurrentRelease.put(typeDto.getName(), new TreeSet<>());
            }
            resourcesForCurrentRelease.get(typeDto.getName()).add(entityDto);
        }
        return resourcesForCurrentRelease;
    }
}
