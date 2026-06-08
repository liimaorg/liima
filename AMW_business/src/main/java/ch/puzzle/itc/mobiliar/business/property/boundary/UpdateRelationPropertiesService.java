package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.auditview.control.AuditService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyValueService;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.utils.ValidationHelper;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@Stateless
public class UpdateRelationPropertiesService implements UpdateRelationPropertiesUseCase {

    @Inject
    EntityManager entityManager;

    @Inject
    ResourceRelationService resourceRelationService;

    @Inject
    PropertyEditor propertyEditor;

    @Inject
    PropertyValueService propertyValueService;

    @Inject
    PermissionBoundary permissionBoundary;

    @Inject
    AuditService auditService;

    @Override
    public void setPropertyOnResourceRelation(Integer relationId, Integer contextId, String propertyName, String value)
            throws ResourceNotFoundException, ValidationException {
        ValidationHelper.validateNotNullOrEmptyChecked(propertyName, value);

        AbstractResourceRelationEntity relation = getResourceRelation(relationId);
        ContextEntity context = entityManager.find(ContextEntity.class, contextId);
        AbstractResourceRelationEntity relationMerged = entityManager.merge(relation);
        ContextEntity contextMerged = entityManager.merge(context);
        ResourceEntity masterResource = relationMerged.getMasterResource();

        if (!permissionBoundary.hasPermission(Permission.RESOURCE, contextMerged, Action.UPDATE, masterResource, null)) {
            throw new NotAuthorizedException("Not allowed to set property value on relation");
        }

        ResourceEditRelation resourceEditRelation = toResourceEditRelation(relationMerged);
        List<ResourceEditProperty> properties = propertyEditor.getPropertiesForRelatedResource(
                masterResource.getId(), resourceEditRelation, contextId);
        ResourceEditProperty property = findByName(propertyName, properties);
        property.setPropertyValue(value);
        propertyValueService.setPropertyValue(
                relationMerged.getOrCreateContext(contextMerged), property.getDescriptorId(), property.getUnobfuscatedValue());
        auditService.storeIdInThreadLocalForAuditLog(relationMerged.getOrCreateContext(contextMerged));
    }

    @Override
    public void resetPropertyOnResourceRelation(Integer relationId, Integer contextId, String propertyName)
            throws ResourceNotFoundException, ValidationException {
        ValidationHelper.validateNotNullOrEmptyChecked(propertyName);

        AbstractResourceRelationEntity relation = getResourceRelation(relationId);
        ContextEntity context = entityManager.find(ContextEntity.class, contextId);
        AbstractResourceRelationEntity relationMerged = entityManager.merge(relation);
        ContextEntity contextMerged = entityManager.merge(context);
        ResourceEntity masterResource = relationMerged.getMasterResource();

        if (!permissionBoundary.hasPermission(Permission.RESOURCE, contextMerged, Action.UPDATE, masterResource, null)) {
            throw new NotAuthorizedException("Not allowed to reset property value on relation");
        }

        ResourceEditRelation resourceEditRelation = toResourceEditRelation(relationMerged);
        List<ResourceEditProperty> properties = propertyEditor.getPropertiesForRelatedResource(
                masterResource.getId(), resourceEditRelation, contextId);
        ResourceEditProperty property = findByName(propertyName, properties);
        propertyValueService.resetPropertyValue(relationMerged.getOrCreateContext(contextMerged), property.getDescriptorId());
        auditService.storeIdInThreadLocalForAuditLog(relationMerged.getOrCreateContext(contextMerged));
    }

    @Override
    public void setPropertyOnResourceTypeRelation(Integer relTypeId, Integer contextId, String propertyName, String value)
            throws NotFoundException, ValidationException {
        ValidationHelper.validateNotNullOrEmptyChecked(propertyName, value);

        ResourceRelationTypeEntity relationType = getRelationType(relTypeId);
        ContextEntity context = entityManager.find(ContextEntity.class, contextId);
        ResourceRelationTypeEntity relationTypeMerged = entityManager.merge(relationType);
        ContextEntity contextMerged = entityManager.merge(context);

        if (!permissionBoundary.hasPermission(Permission.RESOURCETYPE, contextMerged, Action.UPDATE, null, relationTypeMerged.getResourceTypeA())) {
            throw new NotAuthorizedException("Not allowed to set property value on resource type relation");
        }

        List<ResourceEditProperty> properties = getTypeRelationProperties(relationTypeMerged, contextId);
        ResourceEditProperty property = findByName(propertyName, properties);
        property.setPropertyValue(value);
        propertyValueService.setPropertyValue(
                relationTypeMerged.getOrCreateContext(contextMerged), property.getDescriptorId(), property.getUnobfuscatedValue());
        auditService.storeIdInThreadLocalForAuditLog(relationTypeMerged.getOrCreateContext(contextMerged));
    }

    @Override
    public void resetPropertyOnResourceTypeRelation(Integer relTypeId, Integer contextId, String propertyName)
            throws NotFoundException, ValidationException {
        ValidationHelper.validateNotNullOrEmptyChecked(propertyName);

        ResourceRelationTypeEntity relationType = getRelationType(relTypeId);
        ContextEntity context = entityManager.find(ContextEntity.class, contextId);
        ResourceRelationTypeEntity relationTypeMerged = entityManager.merge(relationType);
        ContextEntity contextMerged = entityManager.merge(context);

        if (!permissionBoundary.hasPermission(Permission.RESOURCETYPE, contextMerged, Action.UPDATE, null, relationTypeMerged.getResourceTypeA())) {
            throw new NotAuthorizedException("Not allowed to reset property value on resource type relation");
        }

        List<ResourceEditProperty> properties = getTypeRelationProperties(relationTypeMerged, contextId);
        ResourceEditProperty property = findByName(propertyName, properties);
        propertyValueService.resetPropertyValue(relationTypeMerged.getOrCreateContext(contextMerged), property.getDescriptorId());
        auditService.storeIdInThreadLocalForAuditLog(relationTypeMerged.getOrCreateContext(contextMerged));
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE, action = Action.UPDATE)
    public void updateResourceRelationIdentifier(Integer relationId, String newIdentifier)
            throws ResourceNotFoundException, ValidationException {
        ValidationHelper.validateNotNullOrEmptyChecked(newIdentifier);
        AbstractResourceRelationEntity relationEntity = resourceRelationService.getResourceRelation(relationId);
        if (relationEntity == null) {
            throw new ResourceNotFoundException("Relation with id " + relationId + " not found");
        }
        AbstractResourceRelationEntity resourceRelation = resourceRelationService.getResourceRelation(relationId);
        ResourceEditRelation relation = toResourceEditRelation(resourceRelation);
        handleRelationIdentifierUpdate(relation, resourceRelation, newIdentifier);
    }

    private void handleRelationIdentifierUpdate(ResourceEditRelation relation, AbstractResourceRelationEntity resourceRelation, String newIdentifier) throws ResourceNotFoundException {
        String previousIdentifier = null;
        String slaveName = null;
        if (relation.hasIdentifierChanged(newIdentifier)) {
            previousIdentifier = relation.getQualifiedIdentifier();
            slaveName = relation.getSlaveName();
            resourceRelation.setIdentifier(newIdentifier);
        }
        if (previousIdentifier != null && resourceRelation instanceof ConsumedResourceRelationEntity) {
            // Only consumed relations need identifier update propagation to other relations with same slave
            // if the previousIdentifier equals to the slaveName, then the identifier of the relation has been empty before
            ConsumedResourceRelationEntity consumedResourceRelation = (ConsumedResourceRelationEntity) resourceRelation;
            if (previousIdentifier.equals(slaveName)) {
                if (consumedResourceRelation.getIdentifier() == null
                        && consumedResourceRelation.getSlaveResource().getName().equals(previousIdentifier)) {
                    consumedResourceRelation.setIdentifier(newIdentifier);
                    entityManager.merge(consumedResourceRelation);
                }
            } else {
                if (consumedResourceRelation.getIdentifier() != null
                        && consumedResourceRelation.getIdentifier().equals(previousIdentifier)) {
                    consumedResourceRelation.setIdentifier(newIdentifier);
                    entityManager.merge(consumedResourceRelation);
                }
            }
        }
    }


    private ResourceEditRelation toResourceEditRelation(AbstractResourceRelationEntity relation) throws ResourceNotFoundException {
        ResourceEditRelation.Mode mode;
        if (relation instanceof ConsumedResourceRelationEntity) {
            mode = ResourceEditRelation.Mode.CONSUMED;
        } else if (relation instanceof ProvidedResourceRelationEntity) {
            mode = ResourceEditRelation.Mode.PROVIDED;
        } else {
            throw new ResourceNotFoundException("Relation with id " + relation.getId() + " is not a consumed or provided relation");
        }

        return new ResourceEditRelation(
                relation.getId(),
                relation.getSlaveResource().getId(),
                relation.buildIdentifer(),
                relation.getSlaveResource().getName(),
                relation.getSlaveResource().getResourceGroup().getId(),
                relation.getSlaveResource().getRelease().getId(),
                relation.getSlaveResource().getRelease().getName(),
                relation.getResourceRelationType().getResourceTypeB().getId(),
                relation.getResourceRelationType().getResourceTypeB().getName(),
                relation.getResourceRelationType().getResourceTypeA().getName(),
                relation.getResourceRelationType().getId(),
                relation.getResourceRelationType().getIdentifier(),
                mode.name(),
                relation.getSlaveResource().getRelease().getInstallationInProductionAt());
    }

    @Override
    @HasPermission(permission = Permission.RESOURCETYPE, action = Action.UPDATE)
    public void updateResourceTypeRelationIdentifier(Integer relTypeId, String newIdentifier)
            throws NotFoundException, ValidationException {
        ValidationHelper.validateNotNullOrEmptyChecked(newIdentifier);

        ResourceRelationTypeEntity relationType = getRelationType(relTypeId);
        ResourceEditRelation relation = createTypeResourceEditRelation(relationType);
        handleTypeRelationIdentifierUpdate(relation, newIdentifier, relationType);
    }

    private void handleTypeRelationIdentifierUpdate(ResourceEditRelation relation, String newIdentifier, ResourceRelationTypeEntity relationType) {
        if (relation.hasIdentifierChanged(newIdentifier)) {
            relationType.setIdentifier(newIdentifier);
            entityManager.merge(relationType);
        }
    }

    private ResourceEditRelation createTypeResourceEditRelation(ResourceRelationTypeEntity relationType) {
        return new ResourceEditRelation(
                null, // resRelId - not applicable for type relations
                null, // slaveId
                null, // identifier
                null, // slaveName
                null, // slaveGroupId
                null, // slaveReleaseId
                null, // slaveReleaseName
                relationType.getResourceTypeB() != null ? relationType.getResourceTypeB().getId() : null, // slaveTypeId
                relationType.getResourceTypeB() != null ? relationType.getResourceTypeB().getName() : null, // slaveTypeName
                relationType.getResourceTypeA() != null ? relationType.getResourceTypeA().getName() : null, // masterTypeName
                relationType.getId(), // resRelTypeId
                relationType.getIdentifier(), // typeIdentifier
                ResourceEditRelation.Mode.TYPE.name(),
                null // slaveReleaseDate
        );
    }

    private AbstractResourceRelationEntity getResourceRelation(Integer relationId) throws ResourceNotFoundException {
        AbstractResourceRelationEntity relation = resourceRelationService.getResourceRelation(relationId);
        if (relation == null) {
            throw new ResourceNotFoundException("Relation with id " + relationId + " not found");
        }
        if (!(relation instanceof ConsumedResourceRelationEntity) && !(relation instanceof ProvidedResourceRelationEntity)) {
            throw new ResourceNotFoundException("Relation with id " + relationId + " is not a consumed or provided relation");
        }
        return relation;
    }

    private ResourceRelationTypeEntity getRelationType(Integer relTypeId) throws NotFoundException {
        ResourceRelationTypeEntity relationType = entityManager.find(ResourceRelationTypeEntity.class, relTypeId);
        if (relationType == null) {
            throw new NotFoundException("ResourceRelationType with id " + relTypeId + " not found");
        }
        return relationType;
    }

    private List<ResourceEditProperty> getTypeRelationProperties(ResourceRelationTypeEntity relationType, Integer contextId) {
        ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation editRelation =
                new ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation(
                        null, null, null, null, null, null, null, null,
                        null, null, relationType.getId(), null,
                        ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation.Mode.TYPE.name(), null);
        return propertyEditor.getPropertiesForRelatedResourceType(editRelation, contextId);
    }

    private ResourceEditProperty findByName(String propertyName, List<ResourceEditProperty> properties) {
        return properties.stream()
                .filter(p -> p.getTechnicalKey().equals(propertyName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Property '" + propertyName + "' not found"));
    }
}
