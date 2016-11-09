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

package ch.puzzle.itc.mobiliar.business.environment.control;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class bundles all functionalities, which are required to investigate the hierarchy property of contexts.
 */
public class ContextHierarchy {

    /**
     * Builds a list of context ids from given context and his parent contexts.<br>
     * List starts always with the Global context
     *
     * @param context
     * @return a list with contextIds in ascending priority
     */
    public List<Integer> getContextWithParentIds(ContextEntity context) {
        List<Integer> result = getContextWithParentIdsRec(new ArrayList<Integer>(), context);
        // order has to be swapped at end of recursion
        Collections.reverse(result);
        return result;
    }

    private List<Integer> getContextWithParentIdsRec(List<Integer> result, ContextEntity context) {
        if (context != null) {
            result.add(context.getId());
            return getContextWithParentIdsRec(result, context.getParent());
        }
        return result;
    }
}
