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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.TargetPlatformEntityBuilder;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * Helper class for copy functionality tests
 */
public class CopyResourceDomainServiceTestHelper {

    public static ResourceEntity mockOriginResource(){
        TargetPlatformEntityBuilder targetPlatformEntityBuilder = new TargetPlatformEntityBuilder();

        ResourceEntity originResource = new ResourceEntityBuilder().mockAppServerEntity("originResource", null, null,
                targetPlatformEntityBuilder.mockTargetPlatformEntity("EAP 6"));

        when(originResource.isDeletable()).thenReturn(true);
        Set<AmwFunctionEntity> originFunctions = new HashSet<>();
        originFunctions.add(new AmwFunctionEntityBuilder("origFct1", 1).forResource(originResource).build());
        originFunctions.add(new AmwFunctionEntityBuilder("origFct2", 2).forResource(originResource).build());
        when(originResource.getFunctions()).thenReturn(originFunctions);
        return originResource;
    }
}
