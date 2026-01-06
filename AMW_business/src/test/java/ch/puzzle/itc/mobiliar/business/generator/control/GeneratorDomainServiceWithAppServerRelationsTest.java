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

package ch.puzzle.itc.mobiliar.business.generator.control;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class GeneratorDomainServiceWithAppServerRelationsTest {

    @Mock
    PermissionService permissionService;

    @Mock
    EnvironmentGenerationResult result;

    @InjectMocks
    GeneratorDomainServiceWithAppServerRelations service;

    @Test
    public void testDoNotOmitTemplateWithPermission() throws Exception {
        //given
        Mockito.doReturn(true).when(permissionService).hasPermission(any(Permission.class), any(
                  ContextEntity.class), any(Action.class), isNull(), isNull());

        //when
        service.omitTemplateForLackingPermissions(new ContextEntity(), new ResourceEntity(), result);

        //then
        Mockito.verify(result, Mockito.never()).omitAllTemplates();
    }

    @Test
    public void testOmitTemplateForLackingPermissions() throws Exception {
        //given
        Mockito.doReturn(false).when(permissionService).hasPermission(any(Permission.class), any(
                ContextEntity.class), any(Action.class), isNull(), isNull());

        //when
        service.omitTemplateForLackingPermissions(new ContextEntity(), new ResourceEntity(), result);

        //then
        Mockito.verify(result).omitAllTemplates();
    }
}