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

package ch.puzzle.itc.mobiliar.maiafederationservice.entity;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.ApplicationID;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.Message;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.ProcessingState;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

import java.util.LinkedList;
import java.util.List;


public class ResourceHelper {

    private ProcessingState processingState;

    private List<Message> messages = new LinkedList<>();

    private ApplicationID appId;

    private String appName;

    private String appLink;

    // true if the app has been created during this import (needed for predecessor business logic)
    private boolean nouveau;

    private List<ResourceEntity> resources = new LinkedList<>();

    public ProcessingState getProcessingState() {
        return processingState;
    }

    public void setProcessingState(ProcessingState processingState) {
        this.processingState = processingState;
    }

    public void addResource(ResourceEntity resource) {
        this.resources.add(resource);
    }

    public void addResources(List<ResourceEntity> resources) { this.resources.addAll(resources); }

    public List<Message> getMessages() {
        return this.messages;
    }

    public String getAppName() {
        return this.appName;
    }

    public List<ResourceEntity> getResources() {
        return this.resources;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void addMessages(List<Message> messages) { this.messages.addAll(messages); }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    public boolean isNouveau() {
        return nouveau;
    }

    public void setNouveau(boolean nouveau) {
        this.nouveau = nouveau;
    }

    public ApplicationID getAppId() {
        if (appId == null) {
            this.appId = new ApplicationID();
            this.appId.setName(appName);
        }
        return appId;
    }

    public void setAppId(ApplicationID appId) {
        this.appId = appId;
    }

    public String getAppLink() {
        return appLink;
    }

    public void setAppLink(String appLink) {
        this.appLink = appLink;
    }


}
