package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

public interface GetResourceUseCase {

    public ResourceEntity getWithGroupAndRelatedResources(Integer resourceId) throws ResourceNotFoundException;
}
