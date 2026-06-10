package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Stateless
public class AddResourceTypeRelationService implements AddResourceTypeRelationUseCase {

    @Inject
    EntityManager entityManager;

    @Inject
    RelationEditor relationEditor;

    @Override
    public void addResourceTypeRelation(AddResourceTypeRelationCommand command)
            throws ResourceTypeNotFoundException {
        ResourceTypeEntity masterType = entityManager.find(ResourceTypeEntity.class, command.getMasterResourceTypeId());
        if (masterType == null) {
            throw new ResourceTypeNotFoundException("Resource type with ID " + command.getMasterResourceTypeId() + " not found");
        }
        relationEditor.addResourceTypeRelation(masterType, command.getSlaveResourceTypeId());
    }
}
