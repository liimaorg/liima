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

package ch.puzzle.itc.mobiliar.presentation.propertyEdit;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableAttributesDTO;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.AppServerRelationsTemplateProcessor;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.NamedIdentifiable;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.boundary.SoftlinkRelationBoundary;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.entity.SoftlinkRelationEntity;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.GeneralDBException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.presentation.CompositeBackingBean;
import ch.puzzle.itc.mobiliar.presentation.Selected;
import ch.puzzle.itc.mobiliar.presentation.resourceRelation.ResourceRelationModel;
import ch.puzzle.itc.mobiliar.presentation.resourceRelation.events.ChangeSelectedRelationEvent;
import ch.puzzle.itc.mobiliar.presentation.util.TestingMode;
import ch.puzzle.itc.mobiliar.presentation.util.UserSettings;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@CompositeBackingBean
public class PropertyEditDataProvider implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    PropertyEditor editor;

    @Inject
    PermissionBoundary permissionBoundary;

    @Inject
    private UserSettings userSettings;

    @Inject
    @Selected
    private ContextEntity currentContext;

    List<ResourceEditProperty> resourceEditProperties;

    List<ResourceEditProperty> filteredResourceProperties;

    List<ResourceEditProperty> currentRelationProperties;

    List<ResourceEditProperty> filteredRelationProperties;

    @Getter
    private boolean editableProperties = false;

    @Getter
    private boolean allowedToEditProperties = false;

    @Getter
    private boolean canChangeRuntime = false;

    @Inject
    ResourceRelationModel resourceRelation;

    @Inject
    ActiveApplicationsDataProvider activeApplications;

    @Inject
    SoftlinkRelationBoundary softlinkRelationBoundary;

    @Getter
    private ResourceGroup group;

    @Inject
    @TestingMode
    private Boolean testing;

    @TestingMode
    public void onChangedTestingMode(@Observes Boolean isTesting) {
        this.testing = isTesting;
    }

    @Getter
    @Setter
    String typeRelationIdentifier;

    private NamedIdentifiable resourceOrResourceType;

    private ResourceEditRelation currentRelation;

    /**
     * @return true if testing is tue and initialized, otherwise false
     */
    public boolean isTesting() {
        return testing != null && testing;
    }

    public void onContextChanged(@Observes ContextEntity contextEntity) throws GeneralDBException {
        if (isCurrentFocusOnResource()) {
            onChangedResource((ResourceEntity) resourceOrResourceType);
        } else if (isCurrentFocusOnResourceType()) {
            onChangedResourceType((ResourceTypeEntity) resourceOrResourceType);
        }
        this.currentContext = contextEntity;
    }

    public void onChangedResource(@Observes ResourceEntity resourceEntity) throws GeneralDBException {
        filteredResourceProperties = new ArrayList<>();
        resourceEditProperties = userSettings.filterTestingProperties(editor.getPropertiesForResource(
                resourceEntity.getId(), getContextId()));
        filterHostNameAndActiveFromNode();
        editableProperties = permissionBoundary.hasPermissionToEditPropertiesByResourceAndContext(resourceEntity.getId(),
                currentContext, userSettings.isTestingMode());
        canChangeRuntime = permissionBoundary.hasPermission(Permission.RESOURCE, currentContext, Action.UPDATE, resourceEntity, null);
        group = ResourceGroup.createByResource(resourceEntity.getResourceGroup());

        resourceOrResourceType = resourceEntity;

        loadResourceRelationEditProperties();
    }

    public void onChangedResourceType(@Observes ResourceTypeEntity resourceTypeEntity) throws GeneralDBException {
        filteredResourceProperties = new ArrayList<>();
        resourceEditProperties = userSettings.filterTestingProperties(editor.getPropertiesForResourceType(
                resourceTypeEntity.getId(), getContextId()));
        editableProperties = permissionBoundary.hasPermission(Permission.RESOURCETYPE, currentContext, Action.UPDATE,
                null, resourceTypeEntity);
        canChangeRuntime = false;
        group = null;
        resourceOrResourceType = resourceTypeEntity;

        loadResourceRelationEditProperties();
    }

    public Integer getContextId() {
        return currentContext != null ? currentContext.getId() : null;
    }

    public void onChangedRelation(@Observes ChangeSelectedRelationEvent relation) {
        this.currentRelation = relation.getRelation();
        if (resourceOrResourceType != null) {
            loadResourceRelationEditProperties();
        }
    }

    private void loadResourceRelationEditProperties() {
        filteredRelationProperties = new ArrayList<ResourceEditProperty>();

        if (currentRelation != null) {
            if (currentRelation.isResourceTypeRelation()) {
                currentRelationProperties = userSettings.filterTestingProperties(editor
                        .getPropertiesForRelatedResourceType(currentRelation, getContextId()));
                typeRelationIdentifier = currentRelation.getDisplayName(); // TODO displayname!?
            } else {
                currentRelationProperties = userSettings.filterTestingProperties(editor
                        .getPropertiesForRelatedResource(getResourceId(), currentRelation, getContextId()));
                filterHostNameAndActiveFromRelatedNode(currentRelation);
            }
        } else {
            currentRelationProperties = Collections.emptyList();
        }
    }

    private void filterHostNameAndActiveFromNode() {
        if (isNode() && !currentContext.isEnvironment() && resourceEditProperties != null) {
            Iterator<ResourceEditProperty> it = resourceEditProperties.iterator();
            while (it.hasNext()) {
                ResourceEditProperty p = it.next();
                if (AppServerRelationsTemplateProcessor.HOST_NAME.equals(p.getTechnicalKey())
                        || AppServerRelationsTemplateProcessor.NODE_ACTIVE.equals(p.getTechnicalKey())) {
                    filteredResourceProperties.add(p);
                    it.remove();
                }
            }
        }
    }

    private void filterHostNameAndActiveFromRelatedNode(ResourceEditRelation relation) {
        if (relation.getSlaveTypeName().equals(DefaultResourceTypeDefinition.NODE.name())
                && !currentContext.isEnvironment() && currentRelationProperties != null) {
            Iterator<ResourceEditProperty> it = currentRelationProperties.iterator();
            while (it.hasNext()) {
                ResourceEditProperty p = it.next();
                if (AppServerRelationsTemplateProcessor.HOST_NAME.equals(p.getTechnicalKey())
                        || AppServerRelationsTemplateProcessor.NODE_ACTIVE.equals(p.getTechnicalKey())) {
                    filteredRelationProperties.add(p);
                    it.remove();
                }
            }
        }
    }

    public void save() throws GeneralDBException, AMWException, ForeignableOwnerViolationException, ValidationException {
        // play back the filtered properties - otherwise they will be deleted.
        resourceEditProperties.addAll(filteredResourceProperties);
        if (currentRelationProperties != null && filteredRelationProperties != null) {
            currentRelationProperties.addAll(filteredRelationProperties);
        }

        List<ResourceEditProperty> relationPropertiesToSave = new ArrayList<>();
        if (currentRelationProperties != null) {
            relationPropertiesToSave.addAll(currentRelationProperties);
        }

        activeApplications.save();


        if (isCurrentFocusOnResource()) {

            // save softlinkrelation
            SoftlinkRelationEntity softlinkRelation = ((ResourceEntity) resourceOrResourceType).getSoftlinkRelation();
            if (softlinkRelation != null) {
                softlinkRelationBoundary.editSoftlinkRelation(ForeignableOwner.getSystemOwner(), softlinkRelation);
            }

            editor.save(ForeignableOwner.getSystemOwner(), getContextId(), getResourceId(), resourceEditProperties,
                    resourceRelation.getCurrentResourceRelation(), relationPropertiesToSave,
                    getNameOfResourceOrResourceType(), getSoftlinkIdIfResource());

        } else {
            editor.savePropertiesForResourceType(getContextId(), getResourceTypeId(),
                    resourceEditProperties, resourceRelation.getCurrentResourceRelation(),
                    relationPropertiesToSave,
                    getNameOfResourceOrResourceType(),
                    typeRelationIdentifier);
        }
    }


    private String getSoftlinkIdIfResource() {
        return isCurrentFocusOnResource() ? ((ResourceEntity) resourceOrResourceType).getSoftlinkId() : null;
    }

    private String getNameOfResourceOrResourceType() {
        return resourceOrResourceType != null ? resourceOrResourceType.getName() : null;
    }

    public boolean isApplicationServer() {
        return isCurrentFocusOnResource()
                && ((ResourceEntity) resourceOrResourceType).getResourceType().isApplicationServerResourceType();
    }

    public boolean isApplication() {
        return isCurrentFocusOnResource()
                && ((ResourceEntity) resourceOrResourceType).getResourceType().isApplicationResourceType();
    }

    public boolean isNode() {
        return isCurrentFocusOnResource()
                && ((ResourceEntity) resourceOrResourceType).getResourceType().isNodeResourceType();
    }

    /**
     * @return true if current focus is of {@link ResourceTypeEntity}; If not yet set then return false as
     * default
     */
    private boolean isCurrentFocusOnResourceType() {
        return resourceOrResourceType != null && resourceOrResourceType instanceof ResourceTypeEntity;
    }

    /**
     * @return true if current focus is of {@link ResourceEntity}; If not yet set then return true as default
     */
    public boolean isCurrentFocusOnResource() {
        return resourceOrResourceType != null && resourceOrResourceType instanceof ResourceEntity;
    }

    private Integer getResourceTypeId() {
        if (isCurrentFocusOnResourceType()) {
            return resourceOrResourceType.getId();
        }
        return null;
    }

    private Integer getResourceId() {
        if (isCurrentFocusOnResource()) {
            return resourceOrResourceType.getId();
        }
        return null;
    }

    public boolean hasEditRelationNamePermission() {
        return isCurrentFocusOnResourceType() && permissionBoundary.hasPermission(Permission.RESOURCETYPE, currentContext,
                Action.UPDATE, null, (ResourceTypeEntity) resourceOrResourceType);
    }

    public boolean isLongValue(String value) {
        return (value != null && value.length() > 70);
    }

    public ForeignableAttributesDTO getForeignableToEdit(ResourceEditProperty property) {
        if (property != null) {
            return new ForeignableAttributesDTO(property.getFcOwner(), property.getFcExternalKey(), property.getFcExternalLink());
        }
        return new ForeignableAttributesDTO();
    }

    public List<ResourceEditProperty> getResourceEditProperties() {
        List<ResourceEditProperty> sortedList = new ArrayList<>(resourceEditProperties);
        Collections.sort(sortedList);
        return sortedList;
    }

    public List<ResourceEditProperty> getCurrentRelationProperties() {
        List<ResourceEditProperty> sortedList = new ArrayList<>(currentRelationProperties);
        Collections.sort(sortedList);
        return sortedList;
    }
}
