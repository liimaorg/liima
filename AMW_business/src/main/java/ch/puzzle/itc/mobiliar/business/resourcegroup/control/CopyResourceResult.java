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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;

/**
 * Exceptions for the {@link CopyResourceDomainService}
 *
 * @author cweber
 */
public class CopyResourceResult {

    private List<String> exceptions = new ArrayList<>();

    // for testing
    private Set<CopyFailure> exceptionEnums = new HashSet<>();

    @Getter
    private Set<CopyInfo> changedResourceParameters = new HashSet<>();
    @Getter
    private Map<Integer, CopyResult> changedTemplates = new HashMap<>();
    @Getter
    private Map<Integer, String> skippedConsumedRelations = new HashMap<>();
    @Getter
    private Map<Integer, String> skippedProvidedRelations = new HashMap<>();
    @Getter
    @Setter
    private String targetResourceName;

    @Getter
    @Setter
    private ResourceEntity targetResource;

    public class CopyResult {
        @Getter
        private String name;
        @Getter
        private Set<CopyInfo> copyInfos = new HashSet<>();

        public CopyResult(String name, CopyInfo info) {
            this.name = name;
            copyInfos.add(info);
        }

        public void addInfo(CopyInfo info) {
            copyInfos.add(info);
        }
    }

    public enum CopyFailure {
        RESOURCETYPE_DIFF
    }

    public enum CopyInfo {
        FILECONTENT_CHANGED("filecontent"),
        TARGETPLATFORM_ADDED("target platform added"),
        TARGETPLATFORM_CHANGED("target platform changed"),
        TARGETPATH_CHANGED("targetpath"),
        DELETABLE_CHANGED("deletable");

        @Getter
        public String info;

        CopyInfo(String info) {
            this.info = info;
        }

        @Override
        public String toString() {
            return info;
        }
    }

    public enum CopyTarget {
        RESOURCE("Resource"), CONSUMED_REL("Consumed Relation"), PROVIDED_REL("Provide Relation"), TEMPLATE("Template");

        public String typeName;

        CopyTarget(String typeName) {
            this.typeName = typeName;
        }
    }

    public CopyResourceResult(String targetResourceName) {
        this.targetResourceName = targetResourceName;
    }

    public void addCopyResultError(CopyFailure error, CopyTarget target, String targetName, String newValue, String oldValue) {
        String message = createErrorMessage(error, target, targetName, newValue, oldValue);
        exceptions.add(message);
        exceptionEnums.add(error);
    }

    public List<String> getCopyResultInfosAsHTMLStrings() {
        List<String> infos = new ArrayList<>();

        // changed resource parameters
        if (!changedResourceParameters.isEmpty()) {
            List<String> changedRes = new ArrayList<>();
            for (CopyInfo info : changedResourceParameters) {
                changedRes.add(info.toString());
            }
            infos.add(getResultInfosAsHtml("Changed parameters on " + targetResourceName, changedRes));
        }

        // changed templates
        if (!changedTemplates.isEmpty()) {
            List<String> changedTempl = new ArrayList<>();
            for (CopyResult result : changedTemplates.values()) {
                changedTempl.add(result.getName() + ": " + StringUtils.join(result.getCopyInfos(), ","));
            }
            infos.add(getResultInfosAsHtml("Changed templates", changedTempl));
        }

        // skipped consumed relations
        if (!skippedConsumedRelations.isEmpty()) {
            infos.add(getResultInfosAsHtml("Skipped consumed relations", new ArrayList<>(skippedConsumedRelations.values())));
        }

        // skipped provided relations
        if (!skippedProvidedRelations.isEmpty()) {
            infos.add(getResultInfosAsHtml("Skipped provided relations", new ArrayList<>(skippedProvidedRelations.values())));
        }
        return infos;
    }

    private String getResultInfosAsHtml(String title, List<String> infos) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(StringEscapeUtils.escapeHtml4(title)).append("</b>");
        sb.append("<ul>");
        for (String info : infos) {
            sb.append("<li>").append(StringEscapeUtils.escapeHtml4(info)).append("</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private String createErrorMessage(CopyFailure info, CopyTarget target, String targetName, String newValue, String oldValue) {
        String prefix = getPrefix(target, targetName);
        switch (info) {
            case RESOURCETYPE_DIFF:
                return prefix + "Attempt to copy from resource with type " + oldValue + " to resource with type " + newValue;
            default:
                return prefix + info.name();
        }
    }

    private String getPrefix(CopyTarget target, String targetName) {
        switch (target) {
            case RESOURCE:
                return target.typeName + " '" + targetResourceName + "': ";
            case TEMPLATE:
                return target.typeName + " '" + targetName + "': ";
            case CONSUMED_REL:
                return target.typeName + " to " + targetName;
            default:
                return "";
        }
    }

    public boolean isSuccess() {
        return exceptions.isEmpty();
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public Set<CopyFailure> getExceptionEnums() {
        return exceptionEnums;
    }

    public Integer getTargetResourceId() {
        if (targetResource != null) {
            return targetResource.getId();
        }
        return null;
    }

    public void addTemplateChange(Integer templateId, String templateName, CopyInfo info) {
        if (changedTemplates.containsKey(templateId)) {
            changedTemplates.get(templateId).addInfo(info);
        } else {
            changedTemplates.put(templateId, new CopyResult(templateName, info));
        }
    }

    public void addChangedResourceParam(CopyInfo info) {
        changedResourceParameters.add(info);
    }

    public void addSkippedConsumedRelation(Integer id, String masterName, String slaveName, String identifier, String masterType, String slaveType) {
        skippedConsumedRelations.put(id, masterName + " (" + masterType + ") " + "-> " + slaveName + " (" + slaveType + ") "
                + (!StringUtils.isEmpty(identifier) ? " [" + identifier + "]" : ""));
    }

    public void addSkippedProvidedRelation(Integer id, String masterName, String slaveName, String identifier, String masterType, String slaveType) {
        skippedProvidedRelations.put(id, masterName + " (" + masterType + ") " + "-> " + slaveName + " (" + slaveType + ") "
                + (!StringUtils.isEmpty(identifier) ? " [" + identifier + "]" : ""));
    }

}
