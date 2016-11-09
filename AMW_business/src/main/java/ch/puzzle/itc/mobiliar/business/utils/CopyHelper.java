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

package ch.puzzle.itc.mobiliar.business.utils;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.Foreignable;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

import java.util.*;

/**
 * Helper class for copy functionality
 */
public class CopyHelper {

    public static final Object[][] VALID_MODE_OWNER_COMBINATIONS = new Object[][]{{CopyResourceDomainService.CopyMode.COPY, ForeignableOwner.AMW}, {CopyResourceDomainService.CopyMode.RELEASE,
            ForeignableOwner.AMW}, {CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR, ForeignableOwner.MAIA}};

    public static void copyForeignable(Foreignable target, Foreignable origin, CopyUnit copyUnit) {
        if (copyUnit.getMode() == CopyResourceDomainService.CopyMode.COPY) {
            // set owner when target is new created
            if (((Identifiable)target).getId() == null || target.getOwner().isSameOwner(copyUnit.getActingOwner())) {
                target.setOwner(copyUnit.getActingOwner());
                // clear key and link, they are no longer valid for copied entity
                target.setExternalKey(null);
                target.setExternalLink(null);
            }
        }
        else if (copyUnit.getMode() == CopyResourceDomainService.CopyMode.RELEASE) {
            // TODO verify owner when implementing releasing
            if(target instanceof ResourceEntity){
                // only resource changes owner when released
                target.setOwner(copyUnit.getActingOwner());
            }else{
                target.setOwner(origin.getOwner());
            }
            target.setExternalLink(origin.getExternalLink());
            target.setExternalKey(origin.getExternalKey());
        }
    }

    public static boolean equalsWithNullCheck(Object obj1, Object obj2) {
        return obj1 == null ? obj2 == null : obj1.equals(obj2);
    }

    public static Map<CopyResourceDomainService.CopyMode, Set<ForeignableOwner>> getValidModeOwnerCombinationsMap(){
        Map<CopyResourceDomainService.CopyMode, Set<ForeignableOwner>> combinations = new HashMap<>();

        for (int i = 0; i < VALID_MODE_OWNER_COMBINATIONS.length; i++) {
            CopyResourceDomainService.CopyMode mode = (CopyResourceDomainService.CopyMode) VALID_MODE_OWNER_COMBINATIONS[i][0];
            ForeignableOwner owner = (ForeignableOwner) VALID_MODE_OWNER_COMBINATIONS[i][1];
            if(combinations.get(mode) == null){
                combinations.put(mode, new HashSet<ForeignableOwner>());
            }
            combinations.get(mode).add(owner);
        }

        return combinations;
    }
}

