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

package ch.puzzle.itc.mobiliar.business.security.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;

@HasPermission
@Interceptor
public class HasPermissionInterceptor {

	@Inject
	private Logger log;

	@Inject
	PermissionService permissionService;

	private static List<Permission> getRequiredPermission(InvocationContext context,
			HasPermission permissionMethodAnnotation) {
		List<Permission> permissions = new ArrayList<>();
		if (!permissionMethodAnnotation.permission().equals(Permission.DEFAULT)) {
			permissions.add(permissionMethodAnnotation.permission());
		}
		if (permissionMethodAnnotation.oneOfPermission().length > 0) {
			Collections.addAll(permissions, permissionMethodAnnotation.oneOfPermission());
		}
		return permissions;
	}

	private static List<Action> getRequiredAction(InvocationContext context, HasPermission permissionMethodAnnotation) {
		List<Action> actions = new ArrayList<>();
		if (!permissionMethodAnnotation.action().equals(Action.NULL)) {
			actions.add(permissionMethodAnnotation.action());
		}
		if (permissionMethodAnnotation.oneOfAction().length > 0) {
			Collections.addAll(actions, permissionMethodAnnotation.oneOfAction());
		}
		return actions;
	}

	private static boolean hasResourceSpecific(InvocationContext context, HasPermission permissionMethodAnnotation) {
		return permissionMethodAnnotation.resourceSpecific();
	}

	@AroundInvoke
	public Object roleCall(InvocationContext context) throws Exception {
		HasPermission permissionMethodAnnotation = context.getMethod().getAnnotation(HasPermission.class);
		List<Permission> permissions = getRequiredPermission(context, permissionMethodAnnotation);
		List<Action> actions = getRequiredAction(context, permissionMethodAnnotation);
		boolean resourceSpecific = hasResourceSpecific(context, permissionMethodAnnotation);
		ResourceGroupEntity resourceGroup = null;

		if (resourceSpecific && context.getParameters().length > 0) {
			for (Object o : context.getParameters()) {
				if (o instanceof ResourceEntity) {
					ResourceEntity resource = (ResourceEntity) o;
					resourceGroup = resource.getResourceGroup();
					break;
				}
			}
		}
		for (Permission permission : permissions) {
			if (actions.isEmpty()) {
				if (permissionService.hasPermission(permission, null, null, resourceGroup, null)) {
					return context.proceed();
				}
			} else {
				for (Action action : actions) {
					if (permissionService.hasPermission(permission, null, action, resourceGroup, null)) {
						return context.proceed();
					}
				}
			}
		}
		log.warning(String.format(
				"User %s doesn't have permissions to call method %s. Required permissions: %s actions: %s resourceSpecific: %s resourceGroup: %s",
				permissionService.getCurrentUserName(), context.getMethod().getName(), permissions, actions,
				resourceSpecific, resourceGroup));
		permissionService.throwNotAuthorizedException(null);
		return null;
	}
}
