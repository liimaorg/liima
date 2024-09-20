package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.common.util.NameChecker;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.ejb.Stateless;
import javax.inject.Inject;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.CREATE;

@Stateless
@AllArgsConstructor
@NoArgsConstructor
public class AddAppServerUseCase {

    @Inject
    private ResourceBoundary resourceBoundary;

    @Inject
    private PermissionBoundary permissionBoundary;

    @Inject
    private ResourceTypeRepository resourceTypeRepository;

    @Inject
    private ReleaseLocator releaseLocator;

    @HasPermission(permission = Permission.RESOURCE, action = CREATE)
    public Integer add(String name, Integer releaseId) throws NotFoundException {
        ResourceTypeEntity resourceType = resourceTypeRepository.getByName(String.valueOf(DefaultResourceTypeDefinition.APPLICATIONSERVER));

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("AppServer name must be provided.");
        } else if (!NameChecker.isNameValid(name)) {
                    throw new IllegalArgumentException(NameChecker.getErrorTextForResourceType(
                            (resourceType != null) ? resourceType.getName() : null, name));
        } else if (releaseId == null || releaseId < 0) {
            throw new IllegalArgumentException("Release id must be provided.");
        }

        ReleaseEntity release = releaseLocator.getReleaseById(releaseId);
        try {
            Resource resource = resourceBoundary.createNewResourceByName(ForeignableOwner.getSystemOwner(), name,
                    resourceType.getId(), release.getId());
            permissionBoundary.createAutoAssignedRestrictions(resource.getEntity());
            return resource.getId();
        } catch (ElementAlreadyExistsException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (AMWException e) {
            throw new RuntimeException(e);
        }

    }
}
