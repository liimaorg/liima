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

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;

import ch.puzzle.itc.mobiliar.business.database.control.QueryUtils;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtPersistenceService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.security.boundary.Permissions;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import ch.puzzle.itc.mobiliar.common.exception.*;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

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
    private ResourcesScreenQueries queries;

    @Inject
    private ResourceGroupPersistenceService resourceGroupService;

    @Inject
    private ReleaseMgmtPersistenceService releaseService;

    @Inject
    private ForeignableService foreignableService;

    @Inject
    private Permissions permissionBoundry;

    /**
     * Create new instance resourceType (all resourceType): permitted to
     * config_admin
     */
    public Resource createNewResourceByName(ForeignableOwner creatingOwner, String newResourceName, Integer resourceTypeId, Integer releaseId)
            throws ResourceTypeNotFoundException,
            ElementAlreadyExistsException {

        ResourceTypeEntity resourceTypeEntity = commonService.getResourceTypeEntityById(resourceTypeId);

        if(!permissionBoundry.canCreateResourceInstance(resourceTypeEntity)){
            throw new NotAuthorizedException("Permission Denied");
        }

        ResourceEntity resourceEntity = createResourceEntityByNameForResourceType(creatingOwner, newResourceName,
                resourceTypeId, releaseId, false);

        Resource resource = Resource.createByResource(creatingOwner, resourceEntity, resourceTypeEntity,
                contextDomainService.getGlobalResourceContextEntity());
        entityManager.persist(resource.getEntity());
        entityManager.flush();
        log.info("Neue Resource " + newResourceName + "in DB persistiert");
        return resource;
    }

    /**
     * Creates a new resource for the given name, type and release
     * 
     * @param newResourceName
     * @param resourceTypeId
     * @param releaseId
     * @param canCreateReleaseOfExisting if true, resource will be created if resources with same name in other releases exist. if false an ElementAlreadyExistsException will thrown in that case
     * @return new created ResourceEntity
     * @throws ElementAlreadyExistsException
     * @throws ResourceTypeNotFoundException
     */
    private ResourceEntity createResourceEntityByNameForResourceType(ForeignableOwner creatingOwner, String newResourceName, Integer resourceTypeId, int releaseId, boolean canCreateReleaseOfExisting) throws ElementAlreadyExistsException,
            ResourceTypeNotFoundException {
        ReleaseEntity release = releaseService.getById(releaseId);
        ResourceTypeEntity type = commonService.getResourceTypeEntityById(resourceTypeId);
        ResourceGroupEntity group = resourceGroupService.loadUniqueGroupByNameAndType(newResourceName,
                resourceTypeId);
        ResourceEntity resourceEntity = null;
        if (group == null) {
            // if group does not exists a new resource with a new group can be created
            resourceEntity = ResourceFactory.createNewResourceForOwner(newResourceName, creatingOwner);
            log.info("Neue Resource " + newResourceName + "inkl. Gruppe erstellt für Release "
                    + release.getName());
        }
        else if(canCreateReleaseOfExisting)  {
            // check if group contains resource for release
            for (ResourceEntity r : group.getResources()) {
                if (r.getRelease().getId().equals(releaseId)) {
                    resourceEntity = r;
                    break;
                }
            }
            if (resourceEntity == null) {
                // if resource is null, a new resource for the existing group can be created
                resourceEntity = ResourceFactory.createNewResourceForOwner(group, creatingOwner);
                log.info("Neue Resource " + newResourceName
                        + "für existierende Gruppe erstellt für Release " + release.getName());
            }
            else {
                // if resource with given name, type and release already exists throw an exeption
                String message = "The "+type.getName()+" with name: " + newResourceName
                        + " already exists in release "+release.getName();
                log.info(message);
                throw new ElementAlreadyExistsException(message, Resource.class, newResourceName);
            }
        }else{
            // if it is not allowed to create a new release for existing throw an exception
            String message = "The "+type.getName()+" with name: " + newResourceName
                    + " already exists.";
            log.info(message);
            throw new ElementAlreadyExistsException(message, Resource.class, newResourceName);
        }
        resourceEntity.setRelease(release);
        return resourceEntity;
    }

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
    @HasPermission(permission = Permission.NEW_RES)
    public Resource getOrCreateNewResourceByName(ForeignableOwner creatingOwner, String newResourceName, Integer resourceTypeId, Integer releaseId)
            throws ResourceTypeNotFoundException,
            ElementAlreadyExistsException {
        ResourceEntity resourceEntity = getOrCreateResourceEntityByNameForResourceType(creatingOwner, newResourceName,
                resourceTypeId, releaseId);
        ResourceTypeEntity resourceTypeEntity = commonService.getResourceTypeEntityById(resourceTypeId);
        Resource resource = Resource.createByResource(creatingOwner, resourceEntity, resourceTypeEntity,
                contextDomainService.getGlobalResourceContextEntity());
        entityManager.persist(resource.getEntity());
        entityManager.flush();
        log.info("Neue Resource " + newResourceName + "in DB persistiert");
        return resource;
    }

    private ResourceEntity getOrCreateResourceEntityByNameForResourceType(ForeignableOwner creatingOwner, String newResourceName, Integer resourceTypeId, int releaseId) throws ElementAlreadyExistsException,
            ResourceTypeNotFoundException {
        ReleaseEntity release = releaseService.getById(releaseId);
        ResourceGroupEntity group = resourceGroupService.loadUniqueGroupByNameAndType(newResourceName, resourceTypeId);
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


    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @HasPermission(permission = Permission.DELETE_APPSERVER)
    public void deleteApplicationServerById(int id) throws ResourceNotFoundException,
            ResourceNotDeletableException, ElementAlreadyExistsException, ForeignableOwnerViolationException {
       doRemoveResourceEntity(ForeignableOwner.getSystemOwner(), id);
    }

    @HasPermission(permission = Permission.DELETE_RES)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void removeResource(ForeignableOwner deletingOwner, Integer resourceId) throws ResourceNotFoundException, ElementAlreadyExistsException, ForeignableOwnerViolationException {
        doRemoveResourceEntity(deletingOwner, resourceId);
    }

    @HasPermission(permission = Permission.DELETE_RES_INSTANCE_OF_DEFAULT_RESTYPE)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void removeResourceEntityOfDefaultResType(ForeignableOwner deletingOwner, Integer resourceId) throws ResourceNotFoundException, ElementAlreadyExistsException, ForeignableOwnerViolationException {
        doRemoveResourceEntity(deletingOwner, resourceId);
    }

    @HasPermission(permission = Permission.DELETE_APP)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void deleteApplicationById(ForeignableOwner deletingOwner, int applicationResId) throws ResourceNotFoundException,
            ResourceNotDeletableException, ElementAlreadyExistsException, ForeignableOwnerViolationException {
       doRemoveResourceEntity(deletingOwner, applicationResId);
    }

    private void doRemoveResourceEntity(ForeignableOwner deletingOwner, Integer resourceId) throws ResourceNotFoundException, ElementAlreadyExistsException, ForeignableOwnerViolationException {
        
        ResourceEntity resourceEntity = commonService.getResourceEntityById(resourceId);

        foreignableService.verifyDeletableByOwner(deletingOwner, resourceEntity);
        
        if (resourceEntity == null) {
            String message = "Die zu löschende Ressource ist nicht vorhanden";
            log.info(message);
            throw new ResourceNotFoundException(message);
        }
        Integer groupId = resourceEntity.getResourceGroup().getId();
         //Special logic for the removal of an application server: If the current application server instance is the only release previously consuming this resource, the relation has to be attached to the default application server container.
        if (resourceEntity.getResourceType().getName().equals(
                DefaultResourceTypeDefinition.APPLICATIONSERVER.name())) {
            ApplicationServer applicationCollectorGroup = commonService.createOrGetApplicationCollectorServer();
            List<ResourceEntity> resources = resourceEntity.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION);
            if (!resources.isEmpty()) {
                for (ResourceEntity res : resources) {
                   if (countNumberOfConsumedSlaveRelations(res) == 1) {
                      res.changeResourceRelation(resourceEntity, applicationCollectorGroup.getEntity(),
                              resourceTypeProvider.getOrCreateResourceRelationType(resourceEntity
                                              .getResourceType(),
                                      applicationCollectorGroup.getResourceType()
                                              .getEntity(), null));
                      log.info("Resource with id " + res.getId()
                              + " has been assigned to the default app server (applications without appservers)");
                   }
                }
            }

        }
        entityManager.remove(resourceEntity);
        log.info("Resource with id: " + resourceEntity.getId() + " is going to be removed from the database...");

        // delete group if deleted resource was the only group member
        ResourceGroupEntity group = resourceGroupService.getById(groupId);
        if (group != null
                && (group.getResources() == null || group.getResources().isEmpty() || (group
                        .getResources().size() == 1 && group.getResources().iterator().next().getId()
                        .equals(resourceEntity.getId())))) {
            resourceGroupService.deleteResourceGroupEntity(groupId);
        }
    }

    private long countNumberOfConsumedSlaveRelations(ResourceEntity res){
        return entityManager.createQuery("select count(a.id) from ResourceEntity r left join r.consumedSlaveRelations a where r=:res", Long.class).setParameter("res", res).getSingleResult();
    }
    
    /**
     * Benennt den ResourceType um
     * 
     * @param resourceTypeId
     * @param resourceTypeName
     * @throws ResourceNotFoundException
     * @throws SavePropertyException
     * @throws RenameException
     * @throws ElementAlreadyExistsException
     * @throws ResourceTypeNotFoundException
     */

    @HasPermission(permission = Permission.EDIT_RES_OR_RESTYPE_NAME)
    public void renameResourceType(Integer resourceTypeId, String resourceTypeName) throws ResourceNotFoundException, SavePropertyException, RenameException, ElementAlreadyExistsException, ResourceTypeNotFoundException {

        if (resourceTypeName == null || resourceTypeName.trim().isEmpty()) {
            throw new RenameException("Der ResourceTypename darf nicht leer sein", null);
        }
        ResourceType resourceType = ResourceType.createByResourceType(commonService.getResourceTypeEntityById(resourceTypeId), null);
        String oldName = resourceType.getName();
        if (resourceTypeName != null && !resourceTypeName.equals(oldName)) {

            List<ResourceTypeEntity> resourceTypeEntities = QueryUtils.fetch(ResourceTypeEntity.class, queries.searchResourceTypeByName(resourceTypeName), 0, -1);
            if (resourceTypeEntities.size() > 0) {
                String message = "Ein ResourceType mit dem Namen " + resourceTypeName + " existiert bereits";
                log.warning(message);
                throw new ElementAlreadyExistsException(message, ResourceType.class, resourceTypeName);
            }
            resourceType.setName(resourceTypeName);

            entityManager.persist(resourceType.getEntity());
            log.info("ResourceType " + oldName + " nach " + resourceTypeName + " umbenannt");

        }
    }

    

    public ResourceTypeProvider getResourceTypeProvider() {
        return resourceTypeProvider;
    }

    /**
     * Creates a new Application for given ApplicationServer in the given release.<br>
     * If ApplicationServer does not exist it will be also created
     * 
     * @param applicationName
     * @param appReleaseId
     * @return created application
     * @throws ElementAlreadyExistsException
     * @throws ResourceTypeNotFoundException
     */
    public Application createNewUniqueApplicationForAppServer(ForeignableOwner creatingOwner, String applicationName, Integer asGroupId, Integer appReleaseId, Integer asReleaseId)
            throws ElementAlreadyExistsException, ResourceNotFoundException, ResourceTypeNotFoundException {

        if(!permissionBoundry.canCreateAppAndAddToAppServer()){
            throw new NotAuthorizedException("Missing Permission");
        }

        ResourceEntity asResource = commonService.getResourceEntityByGroupAndRelease(asGroupId, asReleaseId);
        ApplicationServer as = ApplicationServer.createByResource(asResource, resourceTypeProvider,
                contextDomainService.getGlobalResourceContextEntity());
        Application app = createUniqueApplicationByName(creatingOwner, applicationName, appReleaseId, false);
        as.addApplication(app, creatingOwner);

        entityManager.persist(as.getEntity());
        entityManager.flush();
        log.info("Application " + applicationName + " für ApplicationServer " + asResource.getName()
                + " in DB persistiert");
        return app;
    }

    /**
     * Creates an application for the special applicationserver "Applications without application server"
     *
     * @param creatingOwner
     * @param fceKey
     * @param fceLink
     * @param applicationName
     * @param releaseId
     * @param canCreateReleaseOfExisting if true, resource will be created if resources with same name in other releases exist. if false an ElementAlreadyExistsException will thrown in that case
     * @return created application
     * @throws ElementAlreadyExistsException
     * @throws ResourceTypeNotFoundException
     */
    @HasPermission(permission = Permission.ADD_APP)
    public Application createNewApplicationWithoutAppServerByName(ForeignableOwner creatingOwner, String fceKey, String fceLink, String applicationName, Integer releaseId, boolean canCreateReleaseOfExisting)
            throws ElementAlreadyExistsException, ResourceTypeNotFoundException {
        Application app = createUniqueApplicationByName(creatingOwner, applicationName, releaseId, canCreateReleaseOfExisting);
        app.getEntity().setExternalKey(fceKey);
        app.getEntity().setExternalLink(fceLink);
        ApplicationServer applicationCollectorGroup = commonService.createOrGetApplicationCollectorServer();
        applicationCollectorGroup.addApplication(app, creatingOwner);
        entityManager.persist(applicationCollectorGroup.getEntity());
        entityManager.flush();
        return app;
    }

    /**
     * @param applicationName
     * @param releaseId
     * @param canCreateReleaseOfExisting if true, resource will be created if resources with same name in other releases exist. if false an ElementAlreadyExistsException will thrown in that case
     * @return
     * @throws ResourceTypeNotFoundException
     * @throws ElementAlreadyExistsException
     */
    private Application createUniqueApplicationByName(ForeignableOwner creatingOwner,String applicationName, int releaseId, boolean canCreateReleaseOfExisting)
            throws ResourceTypeNotFoundException, ElementAlreadyExistsException {
        ResourceTypeEntity resourceTypeEntity = resourceTypeProvider
                .getOrCreateDefaultResourceType(DefaultResourceTypeDefinition.APPLICATION);
        ResourceEntity resourceEntity = createResourceEntityByNameForResourceType(creatingOwner, applicationName,
                resourceTypeEntity.getId(), releaseId, canCreateReleaseOfExisting);
        Application application = Application.createByResource(resourceEntity, resourceTypeProvider,
                contextDomainService.getGlobalResourceContextEntity());
        return application;
    }

    /**
     * Updates (merges) an existing ResourceEntity
     *
     * @param resourceEntity
     */
    // TODO permission? which?
    public void updateResource(ResourceEntity resourceEntity) {
        if (resourceEntity.getId() != null) {
            entityManager.merge(resourceEntity);
        }
    }
}
