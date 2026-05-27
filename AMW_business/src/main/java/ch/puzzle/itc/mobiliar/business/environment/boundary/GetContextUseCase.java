/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2026 by Puzzle ITC
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

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

import java.util.List;

public interface GetContextUseCase {

    /**
     * Returns a context by its ID.
     *
     * @param id the context ID
     * @return the context entity
     * @throws NotFoundException if the context is not found
     */
    ContextEntity getById(Integer id) throws NotFoundException;

    /**
     * Returns child contexts of the given context.
     *
     * @param contextId the parent context ID
     * @return list of child context entities
     */
    List<ContextEntity> getChildren(Integer contextId);
}
