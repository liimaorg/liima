package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class RemoveResourceTypeRelationService implements RemoveResourceTypeRelationUseCase {

    @Inject
    RelationEditor relationEditor;

    @Override
    public void removeResourceTypeRelation(RemoveResourceTypeRelationCommand command)
            throws ResourceTypeNotFoundException {
        relationEditor.removeResourceTypeRelation(command.getResourceTypeRelationId());
    }
}
