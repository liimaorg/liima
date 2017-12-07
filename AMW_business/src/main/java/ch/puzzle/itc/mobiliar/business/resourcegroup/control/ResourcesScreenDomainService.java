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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtPersistenceService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import java.util.logging.Logger;

/**
 * USe {@link ResourceRepository} to fulfill CEC pattern. Move all methods!
 */
// TODO move to boundary
@Interceptors(HasPermissionInterceptor.class)
@Stateless
@Deprecated
public class ResourcesScreenDomainService {

    @Inject
    private EntityManager entityManager;

    @Inject
    private CommonDomainService commonService;

    @Inject
    private Logger log;

    @Inject
    private ContextDomainService contextDomainService;

    @Inject
    private ResourceTypeProvider resourceTypeProvider;

    @Inject
    private ResourceGroupRepository resourceGroupRepository;

    @Inject
    private ReleaseMgmtPersistenceService releaseService;

    @Inject
    private PermissionBoundary permissionBoundary;

    /**
     * Returns or creates a resource with the given name.
     * It will also create a new ApplicationGroup if needed
     *
     * @param creatingOwner
     * @param newResourceName
     * @param resourceTypeId
     * @param releaseId
     * @return
     * @throws ResourceTypeNotFoundException
     * @throws ElementAlreadyExistsException
     */
    @HasPermission(permission = Permission.RESOURCE, action = Action.CREATE)
    public Resource getOrCreateNewResourceByName(ForeignableOwner creatingOwner, String newResourceName, Integer resourceTypeId, Integer releaseId)
            throws ResourceTypeNotFoundException, ElementAlreadyExistsException {
        ResourceEntity resourceEntity = getOrCreateResourceEntityByNameForResourceType(creatingOwner, newResourceName, resourceTypeId, releaseId);
        ResourceTypeEntity resourceTypeEntity = commonService.getResourceTypeEntityById(resourceTypeId);
        Resource resource = Resource.createByResource(creatingOwner, resourceEntity, resourceTypeEntity, contextDomainService.getGlobalResourceContextEntity());
        entityManager.persist(resource.getEntity());
        entityManager.flush();
        log.info("Neue Resource " + newResourceName + "in DB persistiert");
        return resource;
    }

    private ResourceEntity getOrCreateResourceEntityByNameForResourceType(ForeignableOwner creatingOwner, String newResourceName, Integer resourceTypeId, int releaseId)
            throws ElementAlreadyExistsException, ResourceTypeNotFoundException {
        ReleaseEntity release = releaseService.getById(releaseId);
        ResourceGroupEntity group = resourceGroupRepository.loadUniqueGroupByNameAndType(newResourceName, resourceTypeId);
        ResourceEntity resourceEntity = null;
        if (group == null) {
            // if group does not exists a new resource with a new group can be created
            resourceEntity = ResourceFactory.createNewResourceForOwner(newResourceName, creatingOwner);
            log.info("Neue Resource " + newResourceName + "inkl. Gruppe erstellt für Release "
                    + release.getName());
        }
        else {
            // check if group contains resource for this release
            for (ResourceEntity r : group.getResources()) {
                if (r.getRelease().getId().equals(releaseId)) {
                    resourceEntity = r;
                    break;
                }
            }
            if (resourceEntity == null) {
                // if resource is null, a new resource for this release can be created in the existing group
                resourceEntity = ResourceFactory.createNewResourceForOwner(group, creatingOwner);
                log.info("Neue Resource " + newResourceName
                        + "für existierende Gruppe erstellt für Release " + release.getName());
            }
        }
        resourceEntity.setRelease(release);
        return resourceEntity;
    }

    public ResourceTypeProvider getResourceTypeProvider() {
        return resourceTypeProvider;
    }

}
