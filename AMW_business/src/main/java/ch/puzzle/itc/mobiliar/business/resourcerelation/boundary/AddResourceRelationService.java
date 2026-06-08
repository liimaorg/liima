package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class AddResourceRelationService implements AddResourceRelationUseCase {

    @Inject
    ResourceRelationService resourceRelationService;

    @Override
    public void addRelation(AddResourceRelationCommand command)
            throws ResourceNotFoundException, ElementAlreadyExistsException {
        resourceRelationService.addRelationByGroup(
                command.getMasterResourceId(),
                command.getSlaveResourceGroupId(),
                command.getProvided(),
                command.getRelationName(),
                null);
    }
}
