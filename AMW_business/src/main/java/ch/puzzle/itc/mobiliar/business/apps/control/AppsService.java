package ch.puzzle.itc.mobiliar.business.apps.control;

import ch.puzzle.itc.mobiliar.business.apps.boundary.*;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceRelations;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Application;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceWithRelations;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.common.util.Tuple;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.CREATE;

@Stateless
public class AppsService implements ListAppsUseCase, AddAppServerUseCase, AddAppUseCase, AddAppWithServerUseCase {

    @Inject
    private ReleaseLocator releaseLocator;

    @Inject
    private ResourceRelations resourceRelations;

    @Inject
    private ResourceBoundary resourceBoundary;

    @Inject
    private PermissionBoundary permissionBoundary;

    @Inject
    private ResourceTypeRepository resourceTypeRepository;

    @Override
    public Tuple<List<ResourceWithRelations>, Long> appsFor(Integer startIndex, Integer maxResults, String filter, Integer releaseId) throws NotFoundException {
        ReleaseEntity release = releaseLocator.getReleaseById(releaseId);
        return resourceRelations.getAppServersWithApplications(startIndex, maxResults, filter, release);
    }


    @Override
    @HasPermission(permission = Permission.RESOURCE, action = CREATE)
    public Integer add(AddAppCommand command) throws NotFoundException, IllegalStateException {

            try {
                Application app = resourceBoundary.createNewApplicationWithoutAppServerByName(
                        ForeignableOwner.getSystemOwner(), null, null, command.getAppName(), command.getReleaseId(), false);
                permissionBoundary.createAutoAssignedRestrictions(app.getEntity());

                return app.getId();

            } catch (ElementAlreadyExistsException e) {
                throw new IllegalStateException(e.getMessage());
            } catch (ResourceTypeNotFoundException e) {
                throw new IllegalStateException("A resource with group name \"" + command.getAppName() + "\" already exist and can not be created!");
            } catch (AMWException e) {
                throw new IllegalStateException("Failed to create auto assigned restrictions for app: " + command.getAppName(), e);
            }
        }

    @Override
    @HasPermission(permission = Permission.RESOURCE, action = CREATE)
    public Integer add(AddAppWithServerCommand command) throws NotFoundException, IllegalStateException {
        try {
            Application app = resourceBoundary.createNewUniqueApplicationForAppServer(
                    ForeignableOwner.getSystemOwner(),
                    command.getAppName(),
                    command.getAppServerId(),
                    command.getReleaseId(),
                    command.getAppServerReleaseId());
            permissionBoundary.createAutoAssignedRestrictions(app.getEntity());

            return app.getId();

        } catch (ElementAlreadyExistsException  e) {
            String type = e.getExistingObjectClass() == Application.class ? "application" : "application server";
            throw new IllegalStateException("An " + type + " with the name " + e.getExistingObjectName()
                    + " already exists.");
        } catch (ResourceTypeNotFoundException e) {
            throw new IllegalStateException("A resource with group name \"" + command.getAppName() + "\" already exist and can not be created!");
        } catch (AMWException e) {
            throw new IllegalStateException("Failed to create auto assigned restrictions for app: " + command.getAppName(), e);
        }
    }


    @Override
    @HasPermission(permission = Permission.RESOURCE, action = CREATE)
    public Integer add(AppServerCommand command) throws NotFoundException, IllegalStateException {
        ResourceTypeEntity resourceType = resourceTypeRepository.getByName(String.valueOf(DefaultResourceTypeDefinition.APPLICATIONSERVER));
        ReleaseEntity release = releaseLocator.getReleaseById(command.getReleaseId());
        try {
            Resource resource = resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), command.getAppServerName(),
                    resourceType.getId(), release.getId());
            permissionBoundary.createAutoAssignedRestrictions(resource.getEntity());

            return resource.getId();

        } catch (ElementAlreadyExistsException | ResourceTypeNotFoundException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (AMWException e) {
            throw new IllegalStateException("Failed to create auto assigned restrictions for app: " + command.getAppServerName(), e);
        }

    }
}

