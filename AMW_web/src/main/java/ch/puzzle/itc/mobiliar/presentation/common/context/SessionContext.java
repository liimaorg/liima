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

package ch.puzzle.itc.mobiliar.presentation.common.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import lombok.Getter;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.presentation.CompositeBackingBean;
import ch.puzzle.itc.mobiliar.presentation.Selected;

@CompositeBackingBean
public class SessionContext implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ContextDomainService domainService;

    @Inject
    private ContextLocator contextLocator;

    @Inject
    private CommonDomainService commonService;

    @Inject
    private Event<ContextEntity> contextEntityEvent;

    private ContextEntity currentContext;

    private ContextEntity globalContext;

    @Produces
    @Selected
    public ContextEntity getCurrentContext() {
        return currentContext;
    }

    @Getter
    private List<ResourceGroupEntity> targetPlatforms;
    private Integer contextId;
    private List<ContextEntity> contexts = null;

    @PostConstruct
    public void load() {
        globalContext = domainService.getGlobalResourceContextEntity();

        initializeCurrentContext();

        loadContexts();
        loadTargetPlatforms();
    }

    private void initializeCurrentContext() {
        if (currentContext == null) {
            currentContext = globalContext;
        }
    }

    private void loadContexts() {
        contexts = Collections.unmodifiableList(contextLocator.getAllEnvironments());
    }

    private void loadTargetPlatforms() {
        targetPlatforms = Collections.unmodifiableList(commonService.getRuntimeResourceGroups());
    }

    public Integer getGlobalContextId() {
        return globalContext != null ? globalContext.getId() : null;
    }

    public boolean isCurrentContext(Integer id) {
        if (id == null || id.equals(0)) {
            id = getGlobalContextId();
        }
        return ((contextId != null && contextId.equals(id)) || (contextId == null && id
                .equals(getGlobalContextId())));
    }

    public void setContextId(Integer contextId) {
        if (contextId != null && !contextId.equals(this.contextId)) {
            this.contextId = contextId;
            currentContext = findCurrentContext();
            contextEntityEvent.fire(currentContext);
        }
    }

    public List<ContextEntity> getChildrenForContext(Integer id) {
        if (id == null || id.equals(0)) {
            id = getGlobalContextId();
        }
        List<ContextEntity> result = new ArrayList<>();
        for (ContextEntity c : contexts) {
            if (c.getParent() != null && c.getParent().getId().equals(id)) {
                result.add(c);
            }
        }
        return result;
    }

    public boolean getIsGlobal() {
        return contextId == null || contextId.equals(getGlobalContextId());
    }

    public Integer getContextId() {
        return contextId == null ? getGlobalContextId() : contextId;
    }

    private ContextEntity findCurrentContext() {
        if (contexts != null) {
            ContextEntity global = null;
            for (ContextEntity c : contexts) {
                if (c.getId().equals(contextId)) {
                    return c;
                } else if (c.getId().equals(getGlobalContextId())) {
                    global = c;
                }
            }
            return global;
        }
        return null;
    }

    public boolean isEnvironment() {
        return currentContext != null && currentContext.isEnvironment();
    }

}
