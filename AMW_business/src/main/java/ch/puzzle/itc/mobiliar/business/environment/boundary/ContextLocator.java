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

package ch.puzzle.itc.mobiliar.business.environment.boundary;

import ch.puzzle.itc.mobiliar.business.auditview.control.AuditService;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextRepository;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.control.RestrictionRepository;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.util.ContextNames;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class ContextLocator {

	@Inject
	ContextRepository contextRepository;

	@Inject
	PermissionService permissionService;

	@Inject
	RestrictionRepository restrictionRepository;

	@Inject
	AuditService auditService;

	public ContextEntity getContextByName(String name) {
		return contextRepository.getContextByName(name);
	}

	public ContextEntity getContextById(Integer id) {
		return contextRepository.find(id);
	}

	public List<ContextEntity> getAllEnvironments() {
		return contextRepository.getEnvironments();
	}

	public List<ContextEntity> getAllDeletedEnvironments() {
		List<Object> results = auditService.getAllDeletedEntities(ContextEntity.class);
		List<ContextEntity> deletedContexts = new ArrayList<>();
		for (Object deletedContext : results) {
			deletedContexts.add((ContextEntity) deletedContext);
		}
		return deletedContexts;
	}

	@HasPermission(permission = Permission.REMOVE_ENV_OR_DOM)
	public String deleteContext(Integer contextId) throws AMWException {
		String contextName;
		ContextEntity context = contextRepository.find(contextId);
		contextName = context.getName();
		if (!context.getContextType().getName().equals(ContextNames.GLOBAL.name())){
			context.getContextType().getContexts().remove(context);
			if (context.getParent() != null) {
				context.getParent().getChildren().remove(context);
			}
			if (context.getChildren() != null) {
				for (ContextEntity c : context.getChildren()) {
					restrictionRepository.deleteAllWithContext(c);
				}
			}
			restrictionRepository.deleteAllWithContext(context);
			permissionService.reloadCache();
			contextRepository.remove(context);
		} else {
			throw new AMWException("Es wurde versucht den Kontext \"Global\" (id: "+contextId+" zu löschen.");
		}
		return contextName;
	}

}
