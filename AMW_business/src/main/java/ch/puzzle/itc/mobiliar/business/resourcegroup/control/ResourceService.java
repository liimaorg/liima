package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.GetResourceUseCase;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceIdCommand;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;

@Stateless
public class ResourceService implements GetResourceUseCase {

    @Inject
    private ResourceRepository resourceRepository;

    @Inject
    private ResourceLocator resourceLocator;

    @Override
    public ResourceEntity getResourceById(ResourceIdCommand command) throws ResourceNotFoundException {
        try {
            return resourceRepository.find(command.getResourceId());
        } catch (NoResultException e) {
            throw new ResourceNotFoundException("Resource with id " + command.getResourceId() + "not found!");
        }
    }

    @Override
    public ResourceEntity getWithGroupAndRelatedResources(Integer resourceId) throws ResourceNotFoundException {
        ResourceEntity resource = resourceLocator.getResourceWithGroupAndRelatedResources(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException("Resource with id " + resourceId + " not found");
        }
        return resource;
    }
}
