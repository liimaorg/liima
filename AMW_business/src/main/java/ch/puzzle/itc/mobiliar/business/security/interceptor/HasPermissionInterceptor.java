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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Interceptor
@HasPermission
public class HasPermissionInterceptor implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Logger log;

	@Inject
	PermissionService permissionService;

	private static List<Permission> getRequiredPermission(InvocationContext context) {
		HasPermission permissionMethodAnnotation = getMethodPermissionAnnotation(context);

		List<Permission> permissions = new ArrayList<>();
		if (permissionMethodAnnotation != null) {
			if (!permissionMethodAnnotation.permission().equals(Permission.DEFAULT)) {
				permissions.add(permissionMethodAnnotation.permission());
			}
			if (permissionMethodAnnotation.oneOfPermission().length > 0) {
				Collections.addAll(permissions, permissionMethodAnnotation.oneOfPermission());
			}
		}
		return permissions;
	}

	private static List<Action> getRequiredAction(InvocationContext context) {
		HasPermission permissionMethodAnnotation = getMethodPermissionAnnotation(context);

		List<Action> actions = new ArrayList<>();
		if (permissionMethodAnnotation != null) {
			if (!permissionMethodAnnotation.action().equals(Action.NULL)) {
				actions.add(permissionMethodAnnotation.action());
			}
			if (permissionMethodAnnotation.oneOfAction().length > 0) {
				Collections.addAll(actions, permissionMethodAnnotation.oneOfAction());
			}
		}
		return actions;
	}

	private static boolean hasResourceSpecific(InvocationContext context) {
		HasPermission permissionMethodAnnotation = getMethodPermissionAnnotation(context);
		return permissionMethodAnnotation != null && permissionMethodAnnotation.resourceSpecific();
	}

	private static HasPermission getMethodPermissionAnnotation(InvocationContext context) {
		return context.getMethod().getAnnotation(HasPermission.class);
	}

	@AroundInvoke
	public Object roleCall(InvocationContext context) throws Exception {
		List<Permission> permissions = getRequiredPermission(context);
		List<Action> actions = getRequiredAction(context);
		boolean resourceSpecific = hasResourceSpecific(context);
		ResourceEntity resource = null;

		if (permissions.isEmpty()) {
			return context.proceed();
		} else {
			if (resourceSpecific && context.getParameters().length > 0) {
				for (Object o : context.getParameters()) {
					if (o instanceof ResourceEntity) {
						resource = (ResourceEntity) o;
						break;
					}
				}
			}
			for (Permission permission : permissions) {
				if (actions.isEmpty()) {
					if (permissionService.hasPermission(permission, null, null, resource)) {
						return context.proceed();
					}
				} else {
					for (Action action : actions) {
						if (permissionService.hasPermission(permission, null, action, resource)) {
							return context.proceed();
						}
					}
				}
			}
		}

		permissionService.throwNotAuthorizedException(null);
		return null;
	}
}
