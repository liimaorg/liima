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
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.logging.Logger;

@Interceptor
@HasPermission
public class HasPermissionInterceptor implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Logger log;

	@Inject
	PermissionService permissionService;

	@AroundInvoke
	public Object roleCall(InvocationContext context) throws Exception {
		HasPermission hasAnnotation = context.getMethod().getAnnotation(HasPermission.class);

		if (hasAnnotation == null) {
			return context.proceed();
		}
		if (hasAnnotation.permission() != null
				&& permissionService.hasPermission(hasAnnotation.permission().name())) {
			return context.proceed();
		}
		if (hasAnnotation.oneOfPermission() != null && hasAnnotation.oneOfPermission().length > 0) {
			Permission[] permissions = hasAnnotation.oneOfPermission();
			for (int i = 0; i < permissions.length; i++) {
				if (permissionService.hasPermission(permissions[i].name())) {
					return context.proceed();
				}
			}
		}
		permissionService.throwNotAuthorizedException(null);
		return null;
	}
}
