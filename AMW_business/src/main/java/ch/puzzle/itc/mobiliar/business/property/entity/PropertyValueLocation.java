package ch.puzzle.itc.mobiliar.business.property.entity;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import lombok.Getter;

@Getter
public class PropertyValueLocation {

    public enum DefinedOn {
        RESOURCE, RESOURCE_TYPE, CONSUMED_RELATION, PROVIDED_RELATION, RESOURCE_TYPE_RELATION
    }

    private final Integer propertyId;
    private final Integer descriptorId;
    private final String propertyName;
    private final String propertyValue;
    private final DefinedOn definedOn;
    private final Integer ownerId;
    private final String ownerName;
    private Integer targetId;
    private String targetName;
    private final Integer contextId;
    private final String contextName;
    private Integer relationId;

    public PropertyValueLocation(PropertyEntity property, ContextDependency<?> definition) {
        propertyId = property.getId();
        descriptorId = property.getDescriptor().getId();
        propertyName = property.getDescriptor().getPropertyName();
        propertyValue = property.getValue();
        contextId = definition.getContext().getId();
        contextName = definition.getContext().getName();

        if (definition instanceof ResourceContextEntity) {
            ResourceEntity resource = ((ResourceContextEntity) definition).getResource();
            definedOn = DefinedOn.RESOURCE;
            ownerId = resource.getId();
            ownerName = resource.getName();
        } else if (definition instanceof ResourceTypeContextEntity) {
            ResourceTypeEntity type = ((ResourceTypeContextEntity) definition).getResourceTypeEntity();
            definedOn = DefinedOn.RESOURCE_TYPE;
            ownerId = type.getId();
            ownerName = type.getName();
        } else if (definition instanceof ResourceRelationContextEntity) {
            AbstractResourceRelationEntity relation =
                    ((ResourceRelationContextEntity) definition).getContextualizedObject();
            definedOn = relation instanceof ConsumedResourceRelationEntity
                    ? DefinedOn.CONSUMED_RELATION : DefinedOn.PROVIDED_RELATION;
            ownerId = relation.getMasterResource().getId();
            ownerName = relation.getMasterResource().getName();
            targetId = relation.getSlaveResource().getId();
            targetName = relation.getSlaveResource().getName();
            relationId = relation.getId();
        } else if (definition instanceof ResourceRelationTypeContextEntity) {
            ResourceRelationTypeEntity relation =
                    ((ResourceRelationTypeContextEntity) definition).getContextualizedObject();
            definedOn = DefinedOn.RESOURCE_TYPE_RELATION;
            ownerId = relation.getResourceTypeA().getId();
            ownerName = relation.getResourceTypeA().getName();
            targetId = relation.getResourceTypeB().getId();
            targetName = relation.getResourceTypeB().getName();
            relationId = relation.getId();
        } else {
            throw new IllegalArgumentException("Unsupported property definition: " + definition.getClass().getName());
        }
    }
}
