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

package ch.puzzle.itc.mobiliar.presentation.templateEdit;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.shakedown.control.ShakedownStpService;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownStpEntity;
import ch.puzzle.itc.mobiliar.business.template.boundary.TemplateEditor;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.presentation.ViewBackingBean;
import ch.puzzle.itc.mobiliar.presentation.common.context.SessionContext;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import ch.puzzle.itc.mobiliar.presentation.util.TestingMode;
import ch.puzzle.itc.mobiliar.presentation.util.UserSettings;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@ViewBackingBean
public class EditTemplateView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    TemplateEditor templateEditor;

    @Inject
    UserSettings settings;

    @Inject
    ShakedownStpService stpService;

    @Inject
    PermissionService permissionService;

    @Inject
    @Getter
    private SessionContext sessionContext;

    @Getter
    TemplateDescriptorEntity template = new TemplateDescriptorEntity();

    @Getter
    TemplateDescriptorEntity compareTemplate;

    @Getter
    List<RevisionInformation> revisionInformations;

    @Getter
    RevisionInformation compareRevision;

    @Getter
    List<ShakedownStpEntity> stpList;

    @Getter
    ShakedownStpEntity selectedStp;

    @Getter
    @Setter
    Integer relationIdForTemplate;

    @Getter
    @Setter
    Integer resourceId;

    @Getter
    @Setter
    Integer resourceTypeId;

    @Getter
    @Setter
    boolean lineWrapping;

    @Getter
    private Integer contextIdViewParam;

    public void setContextIdViewParam(Integer contextIdViewParam) {
        this.contextIdViewParam = contextIdViewParam;
        sessionContext.setContextId(contextIdViewParam);
    }

    @Inject
    @TestingMode
    private Boolean testing;

    @TestingMode
    public void onChangedTestingMode(@Observes Boolean isTesting) {
        this.testing = isTesting;
    }

    /**
     * @return true if testing is true and initialized, otherwise false
     */
    public boolean isTesting() {
        return testing != null && testing;
    }

    @PostConstruct
    public void init() {
        if (settings.isTestingMode()) {
            loadStpList();
        }
    }

    private void loadStpList() {
        if (stpList == null) {
            stpList = stpService.getSTPs();
        }
        findSelectedStp();
    }

    private void findSelectedStp() {
        if (stpList != null && !StringUtils.isBlank(template.getName())) {
            for (ShakedownStpEntity stp : stpList) {
                if (stp.getStpName().equals(template.getName())) {
                    selectedStp = stp;
                    break;
                }
            }
        }
    }

    public Integer getSelectedStpId() {
        return selectedStp != null ? selectedStp.getId() : null;
    }

    public void setSelectedStpId(Integer selectedStpId) {
        if (stpList != null) {
            for (ShakedownStpEntity entity : stpList) {
                if (entity.getId().equals(selectedStpId)) {
                    selectedStp = entity;
                    return;
                }
            }
        }
        selectedStp = null;
    }

    public Integer getTemplateId() {
        return template != null ? template.getId() : null;
    }

    public boolean isCompareMode() {
        return compareTemplate != null;
    }

    public Integer getCompareRevisionId() {
        return compareRevision != null ? compareRevision.getRevision().intValue() : null;
    }

    public void setCompareRevisionId(Integer compareRevisionId) {
        if (compareRevisionId != null && compareRevisionId > 0) {
            for (RevisionInformation r : revisionInformations) {
                if (r.getRevision().intValue() == compareRevisionId) {
                    compareRevision = r;
                    compareTemplate = templateEditor.getTemplateByIdAndRevision(getTemplateId(),
                            compareRevisionId);
                    return;
                }
            }
        }
        compareTemplate = null;
        compareRevision = null;
    }

    /**
     * This is a viewParameter and is called by JSF!
     */
    public void setTemplateId(Integer templateId) {
        template = templateEditor.getTemplateById(templateId);
        revisionInformations = Lists.reverse(templateEditor.getTemplateRevisions(templateId));
        findSelectedStp();
    }

    public boolean isNewTemplate() {
        return template == null || template.getId() == null;
    }

    public List<Integer> getSelectedTargetPlatforms() {
        if (template == null) {
            return new ArrayList<>();
        }
        Set<ResourceGroupEntity> runtimes = template.getTargetPlatforms();
        List<Integer> result = new ArrayList<>();
        if (runtimes != null) {
            for (ResourceGroupEntity runtime : runtimes) {
                result.add(runtime.getId());
            }
        }
        return result;
    }

    public List<String> getSelectedTargetPlatformsOfCompareTemplateAsList() {
        if (compareTemplate == null) {
            return new ArrayList<>();
        }
        Set<ResourceGroupEntity> targetPlatforms = compareTemplate.getTargetPlatforms();
        List<String> result = new ArrayList<>();
        if (targetPlatforms != null) {
            for (ResourceGroupEntity t : targetPlatforms) {
                result.add(t.getName());
            }
        }
        Collections.sort(result);
        return result;

    }

    public void setSelectedTargetPlatforms(List<Integer> ids) {
        Set<ResourceGroupEntity> platforms = new HashSet<>();
        for (ResourceGroupEntity p : sessionContext.getTargetPlatforms()) {
            if (ids.contains(p.getId())) {
                platforms.add(p);
            }
        }
        if (template != null) {
            template.setTargetPlatforms(platforms);
        }
    }

    public void save() {
        boolean success = true;
        String errorMessage = "Was not able to save the template: ";
        try {
            setTemplateName();
            saveTemplate();
        } catch (ResourceNotFoundException | ResourceTypeNotFoundException e) {
            success = fail(errorMessage + e.getMessage());
        } catch (AMWException e) {
            success = fail(e);
        }
        if (success) {
            succeed();
        }
    }

    private void setTemplateName() throws AMWException {
        template.setTesting(settings.isTestingMode());
        if (template.isTesting()) {
            if (selectedStp != null) {
                template.setName(selectedStp.getStpName());
            } else {
                throwError("No STP-name selected!");
            }
        }
    }

    private void saveTemplate() throws AMWException {
        if (relationIdForTemplate != null) {
            templateEditor.saveTemplateForRelation(template, relationIdForTemplate,
                                                   resourceId != null);
        } else if (resourceId == null) {
            templateEditor.saveTemplateForResourceType(template, resourceTypeId, settings.isTestingMode());
        } else {
            templateEditor.saveTemplateForResource(template, resourceId, settings.isTestingMode());
        }
    }


    public boolean canModifyTemplates() {
        if (settings.isTestingMode()) {
            return permissionService.hasPermission(Permission.SHAKEDOWN_TEST_MODE);
        }
        Permission permission = getPermission();
        Action action = getAction();
        return permissionService.hasPermission(permission, action);
    }

    private Permission getPermission() {
        Permission permission;
        if (isEditResource()) {
            permission = Permission.RESOURCE_TEMPLATE;
        } else {
            permission = Permission.RESOURCETYPE_TEMPLATE;
        }
        return permission;
    }

    private Action getAction() {
        Action action;
        if (isNewTemplate()) {
            action = Action.CREATE;
        } else {
            action = Action.UPDATE;
        }
        return action;
    }

    private boolean canAdd() {
        return canModifyTemplates();
    }

    public boolean isRelation() {
        return relationIdForTemplate != null;
    }

    public boolean isEditResource() {
        return resourceId != null;
    }

    // helper methods for testing below
    void throwError(String msg) throws AMWException {
        throw new AMWException(msg);
    }

    void succeed() {
        GlobalMessageAppender.addSuccessMessage("Template successfully saved.");
        revisionInformations = Lists.reverse(templateEditor.getTemplateRevisions(template.getId()));
    }

    boolean fail(AMWException e) {
        GlobalMessageAppender.addErrorMessage(e);
        return false;
    }

    boolean fail(String errorMessage) {
        GlobalMessageAppender.addErrorMessage(errorMessage);
        return false;
    }
}
