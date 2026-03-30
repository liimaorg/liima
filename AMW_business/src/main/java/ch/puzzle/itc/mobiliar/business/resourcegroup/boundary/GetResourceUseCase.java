package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

public interface GetResourceUseCase {

    ResourceEntity getResourceById(ResourceIdCommand command) throws ResourceNotFoundException;

    ResourceEntity getWithGroupAndRelatedResources(Integer resourceId) throws ResourceNotFoundException;
}
