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

package ch.puzzle.itc.mobiliar.presentation.generator;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.EnvironmentGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorDomainServiceWithAppServerRelations;
import ch.puzzle.itc.mobiliar.business.generator.control.NodeGenerationResult;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.CheckedNotAuthorizedException;
import ch.puzzle.itc.mobiliar.presentation.ViewBackingBean;
import ch.puzzle.itc.mobiliar.presentation.common.context.SessionContext;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ViewBackingBean
public class GenerationTestController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    SessionContext sessionContext;

    @Inject
    GeneratorDomainServiceWithAppServerRelations generatorDomainServiceWithAppServerRelations;

    @Inject
    PropertyEditor propertyEditor;

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    ReleaseMgmtService releaseMgmtService;

    @Inject
    PermissionBoundary permissionBoundary;

    @Getter
    ResourceEntity currentResource;

    ComparedGenerationResult compareResult;

    ReleaseEntity compareRelease;

    @Getter
    EnvironmentGenerationResult generationResult;

    @Getter
    @Setter
    Integer releaseId;

    @Getter
    @Setter
    List<ReleaseEntity> releases;

    Map<ReleaseEntity, Date> releaseWithLastSuccessfulDeploymentDate;

    @Getter
    Date compareDate;

    @Getter
    private Integer resourceIdFromParam;

    @Getter
    @Setter
    private Integer relationIdViewParam;

    @Getter
    private Integer contextIdViewParam;

    @Getter
    @Setter
    private Integer resourceTypeId;

    public void setContextIdViewParam(Integer contextIdViewParam) {
        this.contextIdViewParam = contextIdViewParam;
        // initialize context
        sessionContext.setContextId(contextIdViewParam);
    }

    public void setResourceIdFromParam(Integer resourceIdFromParam) {
        this.resourceIdFromParam = resourceIdFromParam;
        currentResource = resourceLocator.getResourceWithGroupAndRelatedResources(resourceIdFromParam);
    }

    public ContextEntity getCurrentContext() {
        return sessionContext.getCurrentContext();
    }


    public ComparedGenerationResult getCompareResult() {
        return compareResult;
    }

    public void setToLatestDeployment() {
        setCompareDate(getLatestDeploymentDate());
    }


    Date getLatestDeploymentDate() {
        return compareRelease != null ? releaseWithLastSuccessfulDeploymentDate
                .get(compareRelease) : releaseWithLastSuccessfulDeploymentDate
                .get(findReleaseById(releaseId));
    }

    public void setCompareDate(Date compareDate) {
        this.compareDate = compareDate;
        loadCompareRelease();
    }

    public Integer getCompareReleaseId() {
        return compareRelease != null ? compareRelease.getId() : null;
    }

    public void setCompareReleaseId(Integer compareReleaseId) {
        compareRelease = findReleaseById(compareReleaseId);
        loadCompareRelease();
    }

    ReleaseEntity findReleaseById(Integer releaseId) {
        if (releaseId != null) {
            for (ReleaseEntity release : releases) {
                if (release.getId().equals(releaseId)) {
                    return release;
                }
            }
        }
        return null;
    }

    public boolean hasLatestDeployment() {
        return getLatestDeploymentDate() != null;
    }

    public boolean hasCompareRelease() {
        return compareRelease != null;
    }


    void loadCompareRelease() {
        if (compareRelease == null && compareDate == null) {
            compareResult = new ComparedGenerationResult(generationResult);
        } else {
            compareResult = new ComparedGenerationResult(generationResult, generate(
                    compareRelease == null ? null : compareRelease.getId(), compareDate));
        }
    }

    /**
     * gets called from the view
     */
    // TODO remove call from init event listener from view!
    public void init() {
        if (currentResource != null) {
            if (generationResult == null) {

                loadReleases();
                generationResult = generate(releaseId, compareDate);
                compareResult = new ComparedGenerationResult(generationResult);
            }
        } else {
            GlobalMessageAppender.addErrorMessage("Could not test generate because no resource is set!");
        }
    }

    private void loadReleases() {
        ResourceGroupEntity resourceGroupEntity = currentResource.getResourceGroup();
        releaseWithLastSuccessfulDeploymentDate = releaseMgmtService.getDeployableReleasesForResourceGroupWithLatestDeploymentDate(resourceGroupEntity, sessionContext.getCurrentContext());
        releases = new ArrayList<>(releaseWithLastSuccessfulDeploymentDate.keySet());

        if (releaseId == null) {
            releaseId = currentResource.getRelease().getId();
        }
    }

    private EnvironmentGenerationResult generate(Integer releaseId, Date stateDate) {
        EnvironmentGenerationResult result = null;

        // generate
        try {
            result = generatorDomainServiceWithAppServerRelations.generateApplicationServerForTest(
                    sessionContext.getContextId(), resourceIdFromParam, releaseId,
                    stateDate);
        } catch (IOException e) {
            String errorMessage = "Could not generate Application Server.";
            GlobalMessageAppender.addErrorMessage(errorMessage);
        } catch (CheckedNotAuthorizedException e) {
            String errorMessage = "You do not have the permission to see the templates in this environment.";
            GlobalMessageAppender.addErrorMessage(errorMessage);
            return new EnvironmentGenerationResult();
        } catch (AMWException e) {
            String errorMessage = "Could not generate Application Server: " + e.getMessage();
            GlobalMessageAppender.addErrorMessage(errorMessage);
            return new EnvironmentGenerationResult();
        }

        if (result == null || result.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("Generation failed");
            if (result != null && result.getEnvironmentException() != null && StringUtils.isNotEmpty(result.getEnvironmentException().getMessage())) {
                errorMessage.append(": ").append(result.getEnvironmentException().getMessage());
            }
            GlobalMessageAppender.addErrorMessage(errorMessage.toString());
        } else {
            GlobalMessageAppender.addSuccessMessage("Generation successful for " + result.getGenerationContext().getContext().getName());
        }
        if (result != null) {
            for (NodeGenerationResult nodeGenerationResult : result.getNodeGenerationResults()) {
                if (nodeGenerationResult.getTestGenerationNodeInfo() != null) {
                    GlobalMessageAppender.addSuccessMessage(nodeGenerationResult.getTestGenerationNodeInfo());
                }
            }
        }
        // don not keep messages
        GlobalMessageAppender.setKeepMessagesforCurrentInstance(false);

        return result;
    }

    public boolean hasPermissionToGoBackToResListView() {
        return permissionBoundary.hasPermission(Permission.BACK_TO_RES_LIST);
    }


}
