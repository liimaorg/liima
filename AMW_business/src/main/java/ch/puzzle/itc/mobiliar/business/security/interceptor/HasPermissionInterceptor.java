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

import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.ArrayList;
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
			if (permissionMethodAnnotation.permission() != null) {
				permissions.add(permissionMethodAnnotation.permission());
			}
			if (permissionMethodAnnotation.oneOfPermission() != null && permissionMethodAnnotation.oneOfPermission().length > 0) {
				for (Permission permission : permissionMethodAnnotation.oneOfPermission()) {
					permissions.add(permission);
				}
			}
		}
		return permissions;
	}

	private static List<Action> getRequiredAction(InvocationContext context) {
		HasPermission permissionMethodAnnotation = getMethodPermissionAnnotation(context);

		List<Action> actions = new ArrayList<>();
		if (permissionMethodAnnotation != null) {
			if (permissionMethodAnnotation.action() != null && !permissionMethodAnnotation.action().equals(Action.NULL)) {
				actions.add(permissionMethodAnnotation.action());
			}
			if (permissionMethodAnnotation.oneOfAction() != null && permissionMethodAnnotation.oneOfAction().length > 0) {
				for (Action action : permissionMethodAnnotation.oneOfAction()) {
					actions.add(action);
				}
			}
		}
		return actions;
	}

	private static HasPermission getMethodPermissionAnnotation(InvocationContext context) {
		return context.getMethod().getAnnotation(HasPermission.class);
	}

	@AroundInvoke
	public Object roleCall(InvocationContext context) throws Exception {
		List<Permission> permissions = getRequiredPermission(context);
		List<Action> actions = getRequiredAction(context);

		if (permissions.isEmpty()) {
			return context.proceed();
		} else {
			for (Permission permission : permissions) {
				if (actions.isEmpty()) {
					if (permissionService.hasPermissionAndAction(permission, null)) {
						return context.proceed();
					}
				} else {
					for (Action action : actions) {
						if (permissionService.hasPermissionAndAction(permission, action)) {
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
