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

/*
 * To change this license header, choose License Headers in Project Properties. To change this template file,
 * choose Tools | Templates and open the template in the editor.
 */
package ch.puzzle.itc.mobiliar.presentation.propertyEdit;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupPersistenceService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.RelationEditor;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.ResourceRelationBoundary;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.presentation.common.ResourceTypeDataProvider;
import ch.puzzle.itc.mobiliar.presentation.resourceRelation.ResourceRelationModel;
import ch.puzzle.itc.mobiliar.presentation.resourcesedit.DataProviderHelper;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import lombok.Getter;
import lombok.Setter;

import javax.ejb.EJBException;
import javax.enterprise.event.Observes;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

/**
 * DataProvider to add and remove relations
 *
 * @author cweber
 */
@Named
@ViewScoped
public class RelationDataProvider implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    PermissionService permissionService;

    private Identifiable resourceOrType;

    @Inject
    private RelationEditor relationEditor;

    @Inject
    private ResourceRelationModel resourceRelationModel;

    @Inject
    private ResourceTypeDataProvider resourceTypeDataProvider;

    @Inject
    ResourceTypeDomainService resourceTypeDomainService;

    @Inject
    ResourceGroupLocator resourceGroupLocator;

    @Inject
    ResourceGroupPersistenceService resourceGroupPersistenceService;

    @Inject
    ResourceRelationBoundary resourceRelationBoundary;

    @Getter
    @Setter
    private boolean allowNodes = false;

    private List<NamedIdentifiable> selectableItems;

    private String identifier;

    @Setter
    @Getter
    private String softlinkReferenceName;

    @Getter
    private List<ResourceType> resourceTypes;

    private ResourceTypeEntity currentResourceType;

    public Integer getCurrentResourceTypeId() {
        return currentResourceType != null ? currentResourceType.getId() : null;
    }

    protected DataProviderHelper helper = new DataProviderHelper();

    @Getter
    private boolean addApplicationToAppServerMode;

    @Getter
    private boolean addRuntimeToAppServerMode;

    private boolean canAddResourceRelation;

    @Getter
    private boolean canAddResourceTypeRelation;

    public void onChangedResource(@Observes ResourceEntity resourceEntity) {
        resourceOrType = resourceEntity;
        canAddResourceRelation = permissionService.hasPermission(Permission.RESOURCE, null, Action.UPDATE, resourceEntity.getResourceGroup(), null);
    }

    public void onChangedResourceType(@Observes ResourceTypeEntity resourceTypeEntity) {
        resourceOrType = resourceTypeEntity;
        canAddResourceTypeRelation = permissionService.hasPermission(Permission.RESOURCETYPE, null, Action.UPDATE, null, resourceTypeEntity);
    }

    public boolean canAddAsConsumedRelation(NamedIdentifiable slaveResourceGroup) {
        return canAddAsResourceRelation(slaveResourceGroup);
    }

    public boolean canAddAsProvidedRelation(NamedIdentifiable slaveResourceGroup) {
        // Only applications are allowed to have provided resources
        return canAddAsResourceRelation(slaveResourceGroup) && getResourceType().isApplicationResourceType();
    }

    public boolean canAddAsResourceTypeRelation(NamedIdentifiable slaveResourceType) {
        if (slaveResourceType instanceof ResourceTypeEntity) {
            return canAddResourceTypeRelation && permissionService.hasPermission(Permission.RESOURCETYPE, null, Action.READ, null, (ResourceTypeEntity) slaveResourceType);
        }
        return false;
    }

    private boolean canAddAsResourceRelation(NamedIdentifiable slaveResourceGroup) {
        if (slaveResourceGroup instanceof ResourceGroupEntity) {
            return canAddResourceRelation && permissionService.hasPermission(Permission.RESOURCE, null, Action.READ, (ResourceGroupEntity) slaveResourceGroup, null);
        }
        return false;
    }

    public List<Application> loadAllApplicationsWithoutServer() {
        return resourceGroupPersistenceService.getAllApplicationsNotBelongingToAServer();
    }

    public void loadResourceGroupsForApplication() {
        addApplicationToAppServerMode = true;
        addRuntimeToAppServerMode = false;
        ResourceType t = resourceTypeDataProvider.getByName(DefaultResourceTypeDefinition.APPLICATION
                .name());
        resourceTypes = Collections.emptyList();
        currentResourceType = t != null ? t.getEntity() : null;
        // First, we load all applications without servers...
        List<Application> applications = loadAllApplicationsWithoutServer();
        selectableItems = new ArrayList<>();

        for (Application a : applications) {
            ResourceGroupEntity appGroup = a.getEntity().getResourceGroup();
            if (!selectableItems.contains(appGroup)) {
                selectableItems.add(appGroup);
            }
        }
        Set<Integer> alreadyDefinedGroups = new HashSet<>();
        if (resourceRelationModel.getConsumedApplications() != null) {
            for (ResourceEditRelation rel : resourceRelationModel.getConsumedApplications()) {
                alreadyDefinedGroups.add(rel.getSlaveGroupId());
            }
        }
        Set<ResourceGroupEntity> applicationsFromOtherResourcesInGroup = relationEditor
                .getApplicationsFromOtherResourcesInGroup(getResource());
        for (ResourceGroupEntity a : applicationsFromOtherResourcesInGroup) {
            if (!alreadyDefinedGroups.contains(a.getId()) && !selectableItems.contains(a)) {
                selectableItems.add(a);
            }
        }
    }

    public void loadResourceTypes() {
        addApplicationToAppServerMode = false;
        addRuntimeToAppServerMode = false;
        List<ResourceTypeEntity> resourceTypes = resourceTypeDomainService.getResourceTypes();
        selectableItems = new ArrayList<>();
        selectableItems.addAll(resourceTypes);
    }

    /**
     * @param typeName
     */
    public void loadResourceGroupsForType(String typeName, String identifier) {
        addApplicationToAppServerMode = false;
        addRuntimeToAppServerMode = false;
        this.identifier = identifier;
        ResourceType t = resourceTypeDataProvider.getByName(typeName);
        if (getResourceType().isDefaultResourceType()) {
            // Default types can add any resource type except runtime,
            // runtime is added in a separate dialog only for as
            resourceTypes = new ArrayList<>(resourceTypeDataProvider.getRootResourceTypes());
            Collections.sort(resourceTypes);

            if (getResourceType().isApplicationServerResourceType()) {
                // The application server can additionally add nodes
                resourceTypes = new ArrayList<>(resourceTypes);
                resourceTypes.add(0, resourceTypeDataProvider.getByName(DefaultResourceTypeDefinition.NODE.name()));
            }
            // If the type is not chosen yet, the first one is selected by default
            if (t == null && resourceTypes != null && !resourceTypes.isEmpty()) {
                t = resourceTypes.get(0);
            }
            if (t != null) {
                selectableItems = this.unwrapList(this.loadResourceGroupsByResourceTypeId(
                        t.getId(), new ArrayList<Integer>()));
                currentResourceType = t.getEntity();
            }
        } else {
            // Non-default types can only define those relations which are defined as "unresolved"
            selectableItems = (List<NamedIdentifiable>) relationEditor.loadResourceGroupsForType(typeName, resourceOrType.getId());
            resourceTypes = Collections.singletonList(t);
            currentResourceType = t.getEntity();
        }
    }

    private List<ResourceGroup> loadResourceGroupsByResourceTypeId(Integer id, List<Integer> excludedResourceIds) {
        List<ResourceGroup> resourcesByResourceType = new ArrayList<>();

        if (id == null) {
            String message = "No resourcetype selected.";
            GlobalMessageAppender.addErrorMessage(message);
        } else if (excludedResourceIds == null) {
            String message = "No resource for exclusion selected.";
            GlobalMessageAppender.addErrorMessage(message);
        } else {
            // #4361 don't use the user settings for adding resources
            for (ResourceGroupEntity resourceGroupEntity : resourceGroupLocator.getGroupsForType(id,  true)) {
                resourcesByResourceType.add(ResourceGroup.createByResource(resourceGroupEntity));
            }
        }
        return resourcesByResourceType;
    }

    private List<NamedIdentifiable> unwrapList(List<ResourceGroup> groups) {
        if (groups == null) {
            return null;
        }
        List<NamedIdentifiable> result = new ArrayList<>();
        for (ResourceGroup resource : groups) {
            if (resource.getEntity() != null) {
                result.add(resource.getEntity());
            }
        }
        return result;
    }

    public void loadResourceGroupsForRuntime() {
        addApplicationToAppServerMode = false;
        addRuntimeToAppServerMode = true;

        ResourceType t = resourceTypeDataProvider.getByName(DefaultResourceTypeDefinition.RUNTIME.name());
        resourceTypes = Collections.emptyList();
        currentResourceType = t != null ? t.getEntity() : null;

        selectableItems = loadAllRuntimeEnvironments();
        if (resourceRelationModel.getRuntimeRelations() != null
                && !resourceRelationModel.getRuntimeRelations().isEmpty()) {
            Iterator<NamedIdentifiable> resourceGroupsIterator = selectableItems.iterator();
            while (resourceGroupsIterator.hasNext()) {
                if (resourceGroupsIterator.next().getId()
                        .equals(resourceRelationModel.getRuntimeRelations().values().iterator().next().get(0).getSlaveGroupId())) {
                    resourceGroupsIterator.remove();
                }
            }
        }
    }

    public List<NamedIdentifiable> loadAllRuntimeEnvironments() {
        List<NamedIdentifiable> result = new ArrayList<>();
        result.addAll(resourceGroupPersistenceService.loadGroupsForTypeName(
                DefaultResourceTypeDefinition.RUNTIME.name()));
        Collections.sort(result, nameComparator);

        return result;
    }

    public void addResourceTypeRelation(Integer resourceTypeId) throws ResourceTypeNotFoundException {
        relationEditor.addResourceTypeRelation(getResourceType(), resourceTypeId);
        resourceRelationModel.reloadValues();
    }

    public boolean addConsumedResource(ResourceGroupEntity resourceGroupEntity) {
        Integer slaveResourceGroupId = resourceGroupEntity.getId();
        String prefix = resourceGroupEntity.getName();
        if (getResourceType().isDefaultResourceType() && !addRuntimeToAppServerMode) {
            // If it is a default resource type, we can add multiple relations to a resource separated by a identifier
            List<ResourceEditRelation> relationsWithSameSlaveGroup = helper.relationsWithSameSlaveGroup(helper.flattenMap(
                    resourceRelationModel.getConsumedRelations()), slaveResourceGroupId);
            if (!relationsWithSameSlaveGroup.isEmpty()) {
                identifier = helper.nextFreeIdentifierForResourceEditRelations(relationsWithSameSlaveGroup, slaveResourceGroupId, prefix);
            }
        }
        return addResourceRelation(slaveResourceGroupId, false, identifier, null);
    }

    private boolean isAlreadyProvided(ResourceGroupEntity resourceGroupEntity) {
        // provided resources can only be added once
        List<ResourceEditRelation> relations = helper.flattenMap(resourceRelationModel.getProvidedRelations());
        Integer slaveResourceGroupId = resourceGroupEntity.getId();
        String prefix = resourceGroupEntity.getName();
        return !helper.nextFreeIdentifierForResourceEditRelations(relations, slaveResourceGroupId, prefix).equals(prefix);
    }

    public boolean canBeAddedAsProvidedResource(NamedIdentifiable slaveResourceGroup) {
        if (slaveResourceGroup instanceof ResourceGroupEntity) {
            if (isAlreadyProvided((ResourceGroupEntity) slaveResourceGroup)) {
                String message = "A resource can only be provided once";
                GlobalMessageAppender.addErrorMessage(message);
                return false;
            }
            if (!canAddResourceRelation || !permissionService.hasPermission(Permission.RESOURCE, null, Action.READ, (ResourceGroupEntity) slaveResourceGroup, null)) {
                String message = "You do not have the permission to add this relation";
                GlobalMessageAppender.addErrorMessage(message);
                return false;
            }
            if (!resourceRelationBoundary.isAddableAsProvidedResourceToResourceGroup((ResourceEntity) resourceOrType, slaveResourceGroup.getName())) {
                String message = "This resource is already provided by another resource";
                GlobalMessageAppender.addErrorMessage(message);
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean addProvidedResource(NamedIdentifiable slaveResourceGroup) {
        if (canBeAddedAsProvidedResource(slaveResourceGroup)) {
            return addResourceRelation(slaveResourceGroup.getId(), true, null, identifier);
        } else {
            return false;
        }
    }

    private boolean addResourceRelation(Integer slaveGroupId, boolean provided, String relationName, String typeIdentifier) {
        boolean isSuccessful = false;
        try {
            if (resourceOrType == null) {
                String message = "No resource selected.";
                GlobalMessageAppender.addErrorMessage(message);
            } else if (slaveGroupId == null) {
                String message = "No related resource selected.";
                GlobalMessageAppender.addErrorMessage(message);
            } else {
                try {
                    relationEditor.addRelation(resourceOrType.getId(), slaveGroupId, provided,
                            relationName, ForeignableOwner.getSystemOwner());
                    resourceRelationModel.reloadValues();
                    String message = "Resource successfully added.";
                    GlobalMessageAppender.addSuccessMessage(message);
                    isSuccessful = true;
                } catch (EJBException e) {
                    if (e.getCause() instanceof NotAuthorizedException) {
                        GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
                    } else {
                        throw e;
                    }
                }
            }
        } catch (ResourceNotFoundException e) {
            String message = "Could not find selected Resource.";
            GlobalMessageAppender.addErrorMessage(message);
        } catch (ElementAlreadyExistsException e) {
            String message = "Relation already exists.";
            GlobalMessageAppender.addErrorMessage(message);
        }
        return isSuccessful;
    }

    public boolean removeResourceRelation() {
        boolean isSuccessful = false;

        try {
            if (isEditResource() && getResource() == null) {
                String message = "No resource selected.";
                GlobalMessageAppender.addErrorMessage(message);
            }

            if (!isEditResource() && getResourceType() == null) {
                String message = "No resource type selected.";
                GlobalMessageAppender.addErrorMessage(message);
            } else {
                ResourceEditRelation resourceRelationToRemove = resourceRelationModel.getRemoveResourceRelation();
                if (resourceRelationToRemove == null) {
                    String message = "No related resource selected.";
                    GlobalMessageAppender.addErrorMessage(message);
                } else {
                    try {
                        if (isEditResource()) {
                            relationEditor.removeRelation(ForeignableOwner.getSystemOwner(), resourceRelationToRemove
                                    .getResRelId());
                        } else {
                            relationEditor.removeResourceTypeRelation(resourceRelationToRemove.getResRelTypeId());
                        }
                        resourceRelationModel.doRemoveRelation();
                        resourceRelationModel.reloadValues();
                        String message = "Relation successfully removed.";
                        GlobalMessageAppender.addSuccessMessage(message);
                        isSuccessful = true;
                    } catch (EJBException e) {
                        if (e.getCause() instanceof NotAuthorizedException) {
                            GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
                        } else {
                            throw e;
                        }
                    } catch (ElementAlreadyExistsException | ResourceTypeNotFoundException e) {
                        GlobalMessageAppender.addErrorMessage(e.getMessage());
                    }
                }
            }
        } catch (ResourceNotFoundException e) {
            String message = "The selected resource can not be found.";
            GlobalMessageAppender.addErrorMessage(message);
        } catch (ForeignableOwnerViolationException e) {
            GlobalMessageAppender.addErrorMessage("Relation can not be deleted by owner " + e.getViolatingOwner());
        }
        return isSuccessful;
    }

    public boolean isCurrentType(Integer type) {
        return type.equals(getCurrentResourceTypeId());
    }

    public boolean isChildCurrentType(Integer parentTypeId) {
        return currentResourceType != null && currentResourceType.getParentResourceType() != null && currentResourceType.getParentResourceType().getId().equals(parentTypeId);
    }

    private boolean isEditResource() {
        return resourceOrType instanceof ResourceEntity;
    }

    private boolean isEditResourceType() {
        return resourceOrType instanceof ResourceTypeEntity;
    }

    private ResourceEntity getResource() {
        if (isEditResource()) {
            return (ResourceEntity) resourceOrType;
        }
        return null;
    }

    private ResourceTypeEntity getResourceType() {
        if (isEditResource()) {
            return ((ResourceEntity) resourceOrType).getResourceType();
        } else if (isEditResourceType()) {
            return ((ResourceTypeEntity) resourceOrType);
        }
        return null;
    }

    public List<NamedIdentifiable> getSelectableItems() {
        if (selectableItems == null) {
            selectableItems = new ArrayList<>();
        }
        Collections.sort(selectableItems, nameComparator);
        return selectableItems;
    }

    private final Comparator<NamedIdentifiable> nameComparator = new Comparator<NamedIdentifiable>() {
        @Override
        public int compare(NamedIdentifiable namedIdentifiable, NamedIdentifiable namedIdentifiable2) {
            if (namedIdentifiable == null) {
                return namedIdentifiable2 == null ? 0 : 1;
            } else if (namedIdentifiable2 == null) {
                return -1;
            } else {
                if (namedIdentifiable.getName() == null) {
                    return namedIdentifiable2.getName() == null ? 0 : 1;
                } else if (namedIdentifiable2.getName() == null) {
                    return -1;
                }
                return namedIdentifiable.getName().compareToIgnoreCase(namedIdentifiable2.getName());
            }
        }
    };
}
