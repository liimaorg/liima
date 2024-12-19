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

package ch.puzzle.itc.mobiliar.presentation.resourcesedit;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceTypeLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.NamedIdentifiable;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.util.ApplicationServerContainer;
import ch.puzzle.itc.mobiliar.common.util.ConfigKey;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.presentation.ViewBackingBean;
import ch.puzzle.itc.mobiliar.presentation.common.context.SessionContext;
import ch.puzzle.itc.mobiliar.presentation.resourceRelation.events.ChangeSelectedRelationEvent;
import ch.puzzle.itc.mobiliar.presentation.resourcesedit.events.SelectedRelationId;
import ch.puzzle.itc.mobiliar.presentation.util.UserSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFilterTypes.*;

@ViewBackingBean
public class EditResourceView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    ResourceTypeLocator resourceTypeLocator;

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    PermissionBoundary permissionBoundary;

    @Inject
    Event<ResourceTypeEntity> resourceTypeEvent;

    @Inject
    Event<ResourceEntity> resourceEvent;

    @Inject
    @SelectedRelationId
    Event<Integer> relationIdViewParamEvent;

    @Inject
    UserSettings userSettingsSessionScoped;

    @Inject
    SessionContext sessionContext;

    @Getter
    private Integer resourceIdFromParam;

    @Getter
    private Integer resourceTypeIdFromParam;

    /**
     * The currently edited resource
     */
    @Getter
    ResourceEntity resource;

    @Getter
    private boolean canEditResourceType;

    @Getter
    private boolean canGenerateTestConfiguration;

    private Integer relationIdViewParam;

    ResourceTypeEntity resourceType;

    ResourceEntity relativeApplicationServer;

    @Getter
    private Integer contextIdViewParam;

    @Getter
    private boolean canShowDeploymentLink;

    @Getter
    private boolean canDelegatePermissions;

    public void setContextIdViewParam(Integer contextIdViewParam) {
        this.contextIdViewParam = contextIdViewParam;
        // initialize context
        sessionContext.setContextId(contextIdViewParam);
    }

    public NamedIdentifiable getAsNamedIdentifiable() {
        return isEditResource() ? resource : resourceType;
    }

    public ResourceTypeEntity getResourceType() {
        if (resource != null) {
            return resource.getResourceType();
        }
        return resourceType;
    }

    /**
     * Takes a relation id which is either an id of a resource relation or a resource type relation.
     *
     * @see ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation#getUniqueIdentifier()
     */
    public void setRelationIdViewParam(Integer relationIdViewParam) {
        this.relationIdViewParam = relationIdViewParam;
        relationIdViewParamEvent.fire(relationIdViewParam);
    }

    /**
     * To be called by JSF (by viewParameter)
     */
    public void setResourceIdFromParam(Integer resourceIdFromParam) {
        permissionBoundary.checkPermissionAndFireException(Permission.RESOURCE, Action.READ, "edit resources");
        this.resourceIdFromParam = resourceIdFromParam;
        loadResource(false);
    }

    /**
     * Forces a reload
     */
    public void forceReload() {
        loadResource(true);
    }

    private void loadResource(boolean forced) {
        if (forced || resource == null || !resource.getId().equals(resourceIdFromParam)) {
            resource = resourceLocator.getResourceWithGroupAndRelatedResources(resourceIdFromParam);
            if (resource.getResourceType().isApplicationResourceType()) {
                relativeApplicationServer = resourceLocator.getApplicationServerForApplication(resource);
                this.canGenerateTestConfiguration = permissionBoundary.hasPermission(Permission.RESOURCE_TEST_GENERATION, sessionContext.getCurrentContext(), Action.READ, relativeApplicationServer, null);
            } else {
                relativeApplicationServer = null;
                this.canGenerateTestConfiguration = permissionBoundary.hasPermission(Permission.RESOURCE_TEST_GENERATION, sessionContext.getCurrentContext(), Action.READ, resource, null);
            }
            resourceType = null;

            this.canEditResourceType = permissionBoundary.hasPermission(Permission.RESOURCETYPE, Action.READ);
            this.canDelegatePermissions = permissionBoundary.canDelegatePermissionsForThisResource(resource, sessionContext.getCurrentContext());

            canShowDeploymentLink = isResourceOrTypeDeployable(resource.getResourceType());

            resourceEvent.fire(resource);
        }
    }

    /**
     * To be called by JSF (by viewParameter)
     *
     * @param resourceTypeIdFromParam
     */
    public void setResourceTypeIdFromParam(Integer resourceTypeIdFromParam) {
        permissionBoundary.checkPermissionAndFireException(Permission.RESOURCETYPE, Action.READ, "edit resource types");
        this.resourceTypeIdFromParam = resourceTypeIdFromParam;
        // Only execute if resource has not been set...
        if (resourceType == null || !resourceType.getId().equals(resourceTypeIdFromParam)) {
            resource = null;
            resourceType = resourceTypeLocator.getResourceType(resourceTypeIdFromParam);

            // this just disables "Go to > Resource type" in dropdown
            this.canEditResourceType = false;
            this.canGenerateTestConfiguration = false;
            this.canDelegatePermissions = false;

            canShowDeploymentLink = isResourceOrTypeDeployable(resourceType);

            resourceTypeEvent.fire(resourceType);
        }
    }

    /**
     * @return true if we are currently editing a resource - false if we're editing a resource type.
     */
    public boolean isEditResource() {
        return this.resource != null;
    }

    /**
     * A null-safe convenience method to get the id of the currently edited resource
     *
     * @return
     */
    public Integer getResourceId() {
        return resource != null ? resource.getId() : null;
    }

    public Integer getReleaseId() {
        return (resource != null && resource.getRelease() != null) ? resource.getRelease().getId() : null;
    }

    public Integer getResourceTypeId() {
        return getResourceType() != null ? getResourceType().getId() : null;
    }

    public String getNameLabel() {
        if (isEditResource()) {
            return getCapitalizedResourceTypeName();
        }
        return "Resourcetype";
    }

    public String getCapitalizedResourceTypeName() {
        return StringUtils.capitalize(getResourceType().getName().toLowerCase());
    }

    /**
     * @return true if a newer release of the current resource exists. False, if this resource is the
     * newest release.
     * @throws NullPointerException if no resource is selected.
     */
    public boolean hasNewerRelease() {
        return resource != null
                && !resource.getRelease().equals(resource.getResourceGroup().getNewestRelease());
    }

    /**
     * @param identifiable
     * @return true if the resourceGroupEntity exists in the release of the current resource, false if the
     * resource is not available for the current release
     */
    public boolean existsForThisRelease(NamedIdentifiable identifiable) {
        if (isEditResource() && identifiable instanceof ResourceGroupEntity) {
            ReleaseEntity firstRelease = ((ResourceGroupEntity) identifiable).getFirstRelease();
            return firstRelease != null && resource.getRelease().compareTo(firstRelease) >= 0;
        }
        return true;
    }

    public String getDisplayName() {
        return resource != null ? resource.getName() : getResourceType().getName();
    }

    public Integer getRelativeApplicationServerId() {
        return relativeApplicationServer != null && !ApplicationServerContainer.APPSERVERCONTAINER.getDisplayName().equals(relativeApplicationServer.getName()) ? relativeApplicationServer.getId() : null;
    }

    public boolean canSaveChanges() {
        // Resource (instance)
        if (isEditResource()) {
           return permissionBoundary.hasPermission(Permission.RESOURCE, sessionContext.getCurrentContext(),
                    Action.UPDATE, resource, getResourceType());
        }
        // ResourceType
        return permissionBoundary.hasPermission(Permission.RESOURCETYPE, sessionContext.getCurrentContext(),
                    Action.UPDATE, null, getResourceType());
    }

    public boolean hasAddPropertyPermission() {
        if (isEditResource()) {
            if (contextIdViewParam == null) {
                return permissionBoundary.hasPermissionToEditPropertiesByResourceAndContext(resourceIdFromParam, sessionContext.getCurrentContext().getId());
            }
            return permissionBoundary.hasPermissionToEditPropertiesByResourceAndContext(resourceIdFromParam, contextIdViewParam);
        } else {
            if (contextIdViewParam == null) {
                return permissionBoundary.hasPermissionToEditPropertiesByResourceTypeAndContext(resourceTypeIdFromParam, sessionContext.getCurrentContext().getId());
            }
            return permissionBoundary.hasPermissionToEditPropertiesByResourceTypeAndContext(resourceTypeIdFromParam, contextIdViewParam);
        }
    }

    public boolean canGoBackToResourceList() {
        return permissionBoundary.hasPermission(Permission.BACK_TO_RES_LIST);
    }

    public ContextEntity getCurrentContext() {
        return sessionContext.getCurrentContext();
    }

    public boolean isGlobalContext() {
        return sessionContext.getIsGlobal();
    }

    private boolean isResourceOrTypeDeployable(ResourceTypeEntity resourceTypeEntity) {
        return permissionBoundary.hasPermissionToDeploy() &&
                (resourceTypeEntity.isApplicationResourceType() ||
                        resourceTypeEntity.isApplicationServerResourceType());
    }

    public void onChangedRelation(@Observes ChangeSelectedRelationEvent relationEvent) {
        if (relationEvent != null && relationEvent.getRelation() != null) {
            ResourceEditRelation relation = relationEvent.getRelation();
            if (relation.isResourceTypeRelation()) {
                Integer resRelTypeId = relation.getResRelTypeId();
                if (resRelTypeId != null && !resRelTypeId.equals(relationIdViewParam)) {
                    relationIdViewParam = relation.getResRelTypeId();
                }
            } else {
                Integer resRelId = relation.getResRelId();
                if (resRelId != null && !resRelId.equals(relationIdViewParam)) {
                    relationIdViewParam = relation.getResRelId();
                }
            }
        }
    }

    public Integer getRelationIdViewParam() {
        return relationIdViewParam;
    }

    public boolean isTestGenerationAvailable() {
        if (isEditResource() && resource.getResourceType().isApplicationServerResourceType()) {
            return true;
        }
        return getRelativeApplicationServerId() != null;
    }

    public String getApplicationName() {
        if (isEditResource() && resource.getResourceType().isApplicationResourceType()) {
            return resource.getName();
        }
        return null;
    }

    public String getApplicationServerName() {
        if (isEditResource()) {
            if (resource.getResourceType().isApplicationServerResourceType()){
                return resource.getName();
            }
            if (resource.getResourceType().isApplicationResourceType()) {
                return relativeApplicationServer != null ? relativeApplicationServer.getName() : null;
            }
        }
        return null;
    }

    public String getEnvironmentName() {
        if (sessionContext.getCurrentContext().isEnvironment()){
            return sessionContext.getCurrentContext().getName();
        }
        return null;
    }

    @SneakyThrows
    public String getDeploymentLinkAngular() {
        List<DeploymentLinkAngular> deploymentLinkAngular = Stream.of(
                        new DeploymentLinkAngular(APPLICATION_NAME.getFilterDisplayName(), getApplicationName()),
                        new DeploymentLinkAngular(APPSERVER_NAME.getFilterDisplayName(), getApplicationServerName()),
                        new DeploymentLinkAngular(ENVIRONMENT_NAME.getFilterDisplayName(), getEnvironmentName())

                ).filter(f -> StringUtils.isNotEmpty(f.getVal()))
                .collect(Collectors.toList());
        return new ObjectMapper().writeValueAsString(deploymentLinkAngular);
    }

    public String getUserName() {
        return permissionBoundary.getUserName();
    }

    private static class DeploymentLinkAngular {
        @Getter
        final String name, val;

        public DeploymentLinkAngular(String name, String val) {
            this.name = name;
            this.val = val;
        }
    }
}
