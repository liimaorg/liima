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

package ch.puzzle.itc.mobiliar.business.security.control;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.PermissionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RestrictionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RoleEntity;
import ch.puzzle.itc.mobiliar.business.utils.BaseRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Stateless
public class RestrictionRepository extends BaseRepository<RestrictionEntity> {

    @Inject
    EntityManager entityManager;

    /**
     * Persists a new RestrictionEntity and returns its id
     *
     * @param role
     * @param permission
     * @param context
     * @param action
     * @return Id of the newly created RestrictionEntity
     */
    public Integer create(RoleEntity role, PermissionEntity permission, ContextEntity context, Action action) {
        RestrictionEntity restriction = new RestrictionEntity();
        restriction.setRole(role);
        restriction.setPermission(permission);
        restriction.setContext(context);
        restriction.setAction(action);
        entityManager.persist(restriction);
        entityManager.flush();
        return restriction.getId();
    }

}
