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

package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import lombok.Value;

import java.util.List;

/**
 * Groups the relations of a resource for display on the edit screen, mirroring the JSF ResourceRelationModel:
 * - runtime:    consumed relations with slave type RUNTIME
 * - consumed:   consumed relations excluding APPLICATION and RUNTIME
 * - provided:   provided relations
 * - unresolved: type-level relations that have no concrete resource instance yet
 */
@Value
public class GroupedRelations {

    /**
     * Holds the best-matching release for a relation group plus all available releases for that group.
     */
    @Value
    public static class RelationGroup {
        ResourceEditRelation best;
        List<ResourceEditRelation> availableReleases;
    }

    List<RelationGroup> runtime;
    List<RelationGroup> consumed;
    List<RelationGroup> provided;
    List<ResourceEditRelation> unresolved;
}
