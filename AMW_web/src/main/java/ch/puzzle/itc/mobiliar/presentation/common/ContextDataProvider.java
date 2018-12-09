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

package ch.puzzle.itc.mobiliar.presentation.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.common.util.ContextNames;

@SessionScoped
@Named
public class ContextDataProvider implements Serializable {


    private static final long serialVersionUID = 1L;

    @Inject
    private ContextDomainService domainService;

    @Inject
    private CommonDomainService commonService;

    @Inject
    ContextLocator contextLocator;

    /**
     * Initial so soll der Globale Context selektiert werden!
     */
    private String contextDisplayName = ContextNames.GLOBAL.getDisplayName();
    private String contextNameAlias;
    private Integer contextId;
    private List<ContextEntity> contexts = null;
    private Integer globalContextId;
    private List<ResourceGroupEntity> targetPlatforms;

    public String getUUID(){
        return UUID.randomUUID().toString();
    }

    public Integer getGlobalContextId() {
        return globalContextId;
    }

    public boolean isCurrentContext(Integer id){
        if(id==null || id.equals(0)) {
            id = globalContextId;
        }
        return ((contextId!=null && contextId.equals(id)) || (contextId==null && id.equals(globalContextId)));
    }


    @PostConstruct
    public void load(){
        ContextEntity c = domainService.getGlobalResourceContextEntity();
        globalContextId = c.getId();
        loadContexts();
        loadTargetPlatforms();
    }

    public List<ContextEntity> getEnvironments(){
        List<ContextEntity> env = new ArrayList<>();
        for(ContextEntity c : contexts){
            if(c.getContextType().getName().equals(ContextNames.ENV.name())){
                env.add(c);
            }
        }
        return env;
    }

    public List<ContextEntity> getChildrenForContext(Integer id){
        if(id==null || id.equals(0)) {
            id=globalContextId;
        }
        List<ContextEntity> result = new ArrayList<>();
        for(ContextEntity c : contexts){
            if(c.getParent() != null && c.getParent().getId().equals(id)) {
                result.add(c);
            }
        }
        return result;
    }

    public List<ResourceGroupEntity> getTargetPlatforms() {
        return targetPlatforms;
    }

    public boolean getIsGlobal() {
        return contextId == null || contextId.equals(globalContextId);
    }

    public Integer getContextId() {
        return contextId == null ? getGlobalContextId() : contextId;
    }

    public ContextEntity getCurrentContext(){
        if(contexts!=null){
            ContextEntity global = null;
            for(ContextEntity c : contexts){
                if(c.getId().equals(contextId)) {
                    return c;
                } else if(c.getId().equals(globalContextId)) {
                    global = c;
                }
            }
            return global;
        }
        return null;
    }

    public void setContextId(Integer contextId) {
        if(contextId == null || contextId.equals(0)) {
            contextId = globalContextId;
        }

        if (!contextId.equals(this.contextId)) {
            this.contextId = contextId;
            ContextEntity current = getCurrentContext();
            this.contextDisplayName = current.getName();
            this.contextNameAlias = current.getNameAlias();
        }

    }

    public String getContextDisplayName() {
        return this.contextDisplayName;
    }

    public String getContextNameAlias() {
        return this.contextNameAlias;
    }

    public void setContextDisplayName(String contextName) {
        this.contextDisplayName = contextName;
    }

    public void setContextNameAlias(String contextNameAlias) {
        this.contextNameAlias = contextNameAlias;
    }

    public void loadContexts() {
        contexts = Collections.unmodifiableList(contextLocator.getAllEnvironments());
    }

    public void loadTargetPlatforms() {
        targetPlatforms = Collections.unmodifiableList(commonService.getRuntimeResourceGroups());
    }

}
