/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.property.control;

import java.util.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.AbstractContext;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.exception.PropertyDescriptorNotDeletableException;

/**
 * This is a control service providing logic for property descriptors
 */
public class PropertyDescriptorService {

    @Inject
    EntityManager entityManager;

    @Inject
    ContextDomainService contextService;

    @Inject
    PermissionService permissionService;

    @Inject
    PropertyValidationService propertyValidationService;

    @Inject
    PropertyTagEditingService propertyTagEditingService;


    public List<PropertyDescriptorEntity> getPropertyDescriptorsForHasContextWithNullCardinality(
            HasContexts<? extends AbstractContext> hasContexts) {
        AbstractContext contextEntity = hasContexts.getOrCreateContext(contextService
                .getGlobalResourceContextEntity());
        List<PropertyDescriptorEntity> result = new ArrayList<>();
        if (contextEntity != null && contextEntity.getPropertyDescriptors() != null) {
            for (PropertyDescriptorEntity property : contextEntity.getPropertyDescriptors()) {
                if (property.getCardinalityProperty() == null
                        || property.getCardinalityProperty() == 0) {
                    result.add(property);
                }
            }
        }
        Collections.sort(result, PropertyDescriptorEntity.NAME_SORTING_COMPARATOR);
        return result;
    }

    /**
     * Verify if the change is allowed by given owner and persists a given PropertyDescriptor, handles its encryption/decryption and manages its PropertyTags
     *
     * @throws AMWException                       is thrown when technical key is invalid or another PropertyDescriptor with same technical key already exists
     * @throws ForeignableOwnerViolationException is thrown when the change is not permitted by changing owner
     */
    public PropertyDescriptorEntity savePropertyDescriptorForOwner(ForeignableOwner changingOwner, AbstractContext abstractContext, PropertyDescriptorEntity descriptor, List<PropertyTagEntity> tags, ResourceEntity resource) throws AMWException {
        checkForValidTechnicalKey(descriptor);

        if (descriptor.getId() == null) {
            preventDuplicateTechnicalKeys(abstractContext, descriptor);
            createNewPropertyDescriptor(changingOwner, descriptor, abstractContext, tags);
        } else {
            saveExistingPropertyDescriptor(descriptor, tags, resource);
        }

        return descriptor;
    }

    /**
     * Verify if the change is allowed by given owner and persists a given PropertyDescriptor, handles its encryption/decryption and manages its PropertyTags
     *
     * @throws AMWException                       is thrown when technical key is invalid or another PropertyDescriptor with same technical key already exists
     * @throws ForeignableOwnerViolationException is thrown when the change is not permitted by changing owner
     */
    public PropertyDescriptorEntity savePropertyDescriptorForOwner(ForeignableOwner changingOwner, AbstractContext abstractContext, PropertyDescriptorEntity descriptor, List<PropertyTagEntity> tags, ResourceTypeEntity resourceType) throws AMWException {
        checkForValidTechnicalKey(descriptor);

        if (descriptor.getId() == null) {
            preventDuplicateTechnicalKeys(abstractContext, descriptor);
            createNewPropertyDescriptor(changingOwner, descriptor, abstractContext, tags);
        } else {
            saveExistingPropertyDescriptor(descriptor, tags, resourceType);
        }

        return descriptor;
    }


    /**
     * Deletes PropertyDescriptors and their PropertyTags PropertyDescriptors to be deleted must not have any Properties
     * The owner is ignored - therefore this method deletes the property descriptor regardless of the foreignable ownership!
     */
    public void deletePropertyDescriptorByOwner(PropertyDescriptorEntity descriptorToDelete, AbstractContext abstractContext)
            throws AMWException {
        // only PropertyDescriptors without Properties (values) shall be deleted (but wee need to reload the descriptor to know it)
        PropertyDescriptorEntity descriptorToDeleteWithTags = getPropertyDescriptor(descriptorToDelete.getId());
        removePropertyDescriptorByOwner(descriptorToDeleteWithTags, abstractContext, false);
    }

    /**
     * Deletes PropertyDescriptors and their PropertyTags PropertyDescriptors including all its Properties
     * The owner is ignored - therefore this method deletes the property descriptor regardless of the foreignable ownership!
     */
    public void deletePropertyDescriptorByOwnerIncludingPropertyValues(PropertyDescriptorEntity descriptorToDelete, AbstractContext abstractContext, HasContexts attachedResource)
            throws AMWException {
        PropertyDescriptorEntity descriptorToDeleteWithTags = getPropertyDescriptor(descriptorToDelete.getId());
        Set<PropertyEntity> propertiesToBeDeleted = descriptorToDeleteWithTags.getProperties();
        Set<ContextDependency> resourceContexts = attachedResource.getContexts();
        for (ContextDependency context : resourceContexts) {
            if (context.getProperties().size() > 0) {
                for (PropertyEntity property : propertiesToBeDeleted) {
                    context.removeProperty(property);
                }
            }
            if (context.getPropertyDescriptors().size() > 0) {
                context.removePropertyDescriptor(descriptorToDelete);
            }
        }
        removePropertyDescriptorByOwner(descriptorToDeleteWithTags, abstractContext, true);
    }

    private void removePropertyDescriptorByOwner(PropertyDescriptorEntity descriptorToDeleteWithTags, AbstractContext abstractContext, boolean includingPropertyValues)
            throws AMWException {
        if (descriptorToDeleteWithTags.getProperties().isEmpty() || includingPropertyValues) {
            abstractContext.removePropertyDescriptor(descriptorToDeleteWithTags);
            List<PropertyTagEntity> tags = descriptorToDeleteWithTags.getPropertyTags();
            for (PropertyTagEntity tag : tags) {
                PropertyTagEntity pt = entityManager.find(PropertyTagEntity.class, tag.getId());
                entityManager.remove(pt);
            }
            entityManager.remove(descriptorToDeleteWithTags);
        } else {
            throw new PropertyDescriptorNotDeletableException("The propertydescriptor " + descriptorToDeleteWithTags.getPropertyDescriptorDisplayName()
                    + " was marked to be deleted but still contains property values.<br>If you force the deletion, all those property values will be deleted as well");
        }
    }

    private void checkForValidTechnicalKey(PropertyDescriptorEntity propertyDescriptor) throws AMWException {
        boolean isValid = propertyDescriptor != null && propertyDescriptor.getPropertyName() != null;

        if (!isValid || !propertyValidationService.isValidTechnicalKey(propertyDescriptor.getPropertyName().trim())) {
            throw new AMWException("The propertyname ('"
                    + ((propertyDescriptor == null || propertyDescriptor.getPropertyName() == null) ? "" : propertyDescriptor.getPropertyName())
                    + "') is invalid!");
        }
    }

    private void preventDuplicateTechnicalKeys(AbstractContext abstractContext, PropertyDescriptorEntity propertyDescriptor) throws AMWException {
        if (abstractContext.getPropertyDescriptors() != null) {
            for (PropertyDescriptorEntity existingPropertyDescriptorEntity : abstractContext.getPropertyDescriptors()) {
                if (existingPropertyDescriptorEntity.getPropertyName().equals(propertyDescriptor.getPropertyName().trim())) {
                    throw new AMWException("The propertyname ('" + propertyDescriptor.getPropertyName() + "') already exists!");
                }
            }
        }
        if (abstractContext instanceof ContextDependency  && ((ContextDependency) abstractContext).getContextualizedObject() instanceof ResourceTypeEntity) {
            ResourceTypeEntity type = (ResourceTypeEntity) ((ContextDependency) abstractContext).getContextualizedObject();
            if (type.getParentResourceType() != null && !type.getParentResourceType().getContexts().isEmpty()) {
                preventDuplicateTechnicalKeysFromParent(propertyDescriptor, type);
            }
        }
    }

    private void preventDuplicateTechnicalKeysFromParent(PropertyDescriptorEntity propertyDescriptor, ResourceTypeEntity type) throws AMWException {
        for (PropertyDescriptorEntity existingPropertyDescriptorEntity : type.getParentResourceType().getContexts().iterator().next().getPropertyDescriptors()) {
            if (existingPropertyDescriptorEntity.getPropertyName().equals(propertyDescriptor.getPropertyName().trim())) {
                throw new AMWException("The propertyname ('" + propertyDescriptor.getPropertyName() + "') already exists on parent!");
            }
        }
    }

    private void createNewPropertyDescriptor(ForeignableOwner owner, PropertyDescriptorEntity descriptor, AbstractContext abstractContext, List<PropertyTagEntity> tags) {
        descriptor.setOwner(owner);
        abstractContext.addPropertyDescriptor(descriptor);
        entityManager.persist(descriptor);
        propertyTagEditingService.updateTags(tags, descriptor);
        entityManager.persist(abstractContext);
    }

    private void saveExistingPropertyDescriptor(PropertyDescriptorEntity descriptor, List<PropertyTagEntity> tags, ResourceEntity resource) {

        PropertyDescriptorEntity oldDescriptor = entityManager.find(PropertyDescriptorEntity.class, descriptor.getId());
        List<Integer> encryptedPropertyIds = new ArrayList<>();
        if (oldDescriptor.isEncrypt()) {
            encryptedPropertyIds.add(oldDescriptor.getId());
        }

        PropertyDescriptorEntity mergedDescriptor = entityManager.merge(descriptor);
        propertyTagEditingService.updateTags(tags, mergedDescriptor);

        boolean canDecrypt = false;
        // decryption required - check permission
        if (!mergedDescriptor.isEncrypt() && encryptedPropertyIds.contains(mergedDescriptor.getId())) {
            // context?
            canDecrypt = permissionService.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, null, Action.ALL, resource.getResourceGroup(), null);
        }

        manageChangeOfEncryptedPropertyDescriptor(mergedDescriptor, encryptedPropertyIds, canDecrypt);
    }

    private void saveExistingPropertyDescriptor(PropertyDescriptorEntity descriptor, List<PropertyTagEntity> tags, ResourceTypeEntity resourceType) {

        PropertyDescriptorEntity oldDescriptor = entityManager.find(PropertyDescriptorEntity.class, descriptor.getId());
        List<Integer> encryptedPropertyIds = new ArrayList<>();
        if (oldDescriptor.isEncrypt()) {
            encryptedPropertyIds.add(oldDescriptor.getId());
        }

        PropertyDescriptorEntity mergedDescriptor = entityManager.merge(descriptor);
        propertyTagEditingService.updateTags(tags, mergedDescriptor);

        boolean canDecrypt = false;
        // decryption required - check permission
        if (!mergedDescriptor.isEncrypt() && encryptedPropertyIds.contains(mergedDescriptor.getId())) {
            // context?
            canDecrypt = permissionService.hasPermission(Permission.RESOURCETYPE_PROPERTY_DECRYPT, null, Action.ALL, null, resourceType);
        }

        manageChangeOfEncryptedPropertyDescriptor(mergedDescriptor, encryptedPropertyIds, canDecrypt);
    }


    public PropertyDescriptorEntity getPropertyDescriptor(Integer propertyDescriptorId) {
        PropertyDescriptorEntity propertyDescriptor;

        TypedQuery<PropertyDescriptorEntity> createQuery = entityManager.createQuery("from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ", PropertyDescriptorEntity.class);
        createQuery.setParameter("propertyDescriptorId", propertyDescriptorId);
        try {
            propertyDescriptor = createQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
        return propertyDescriptor;
    }

    /**
     * Finds a PropertyDescriptorEntity identified by its technical key
     *
     * @param technicalKey
     * @return
     */
    public PropertyDescriptorEntity findPropertyDescriptorByName(String technicalKey) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery();
        Root<PropertyDescriptorEntity> from = query.from(PropertyDescriptorEntity.class);
        CriteriaQuery<Object> select = query.select(from);
        Predicate predicate = cb.equal(from.get("propertyName"), technicalKey);
        query.where(predicate);
        TypedQuery<Object> tq = entityManager.createQuery(select);
        List<Object> result = tq.getResultList();
        return result.isEmpty() ? null : (PropertyDescriptorEntity) result.get(0);
    }


    /**
     * if the value for encryption of a propertydescriptor has been changed, the values of the already
     * existing properties have to be adapted (either be encrypted or decrypted).
     *
     * @param descr
     * @param encryptedPropertyIds
     * @param canDecrypt whether or not the caller has the permission to decrypt encrypted property values
     */
    void manageChangeOfEncryptedPropertyDescriptor(PropertyDescriptorEntity descr, List<Integer> encryptedPropertyIds, boolean canDecrypt) {
        boolean encrypt = descr.isEncrypt() && !encryptedPropertyIds.contains(descr.getId());
        boolean decrypt = !descr.isEncrypt() && encryptedPropertyIds.contains(descr.getId());
        if (encrypt || decrypt) {
            TypedQuery<PropertyEntity> propertyQuery = entityManager.createQuery("select p from PropertyEntity p where p.descriptor=:descriptor", PropertyEntity.class).setParameter("descriptor", descr);
            List<PropertyEntity> properties = propertyQuery.getResultList();
            for (PropertyEntity property : properties) {
                if (encrypt) {
                    property.encrypt();
                } else {
                    if (!canDecrypt) {
                        throw new NotAuthorizedException("decrypt properties");
                    }
                    property.decrypt();
                }
                entityManager.persist(property);
            }
        }
    }

}
