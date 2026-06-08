package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class RemoveResourceRelationService implements RemoveResourceRelationUseCase {

    @Inject
    ResourceRelationService resourceRelationService;

    @Override
    public void removeRelation(RemoveResourceRelationCommand command)
            throws ResourceNotFoundException {
        AbstractResourceRelationEntity relation = resourceRelationService.getResourceRelation(command.getRelationId());
        if (relation == null) {
            throw new ResourceNotFoundException("Relation with ID " + command.getRelationId() + " not found");
        }
        try {
            resourceRelationService.removeRelation(relation);
        } catch (ElementAlreadyExistsException e) {
            throw new ResourceNotFoundException("Failed to remove relation: " + e.getMessage());
        }
    }
}
