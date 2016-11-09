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

package ch.puzzle.itc.mobiliar.business.foreignable.control;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.Foreignable;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;

@RunWith(MockitoJUnitRunner.class)
public class ForeignableServiceTest {

    @Mock
    private EntityManager entityManagerMock;

    @Mock
    PermissionService permissionServiceMock;

    @InjectMocks
    private ForeignableService foreignableService;

    @Before
    public void setUp(){
        // by default not super role
        when(permissionServiceMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);
    }


    @Test
    public void isForeignableModifiableByOwnerWhenSameOwnerShouldReturnTrue(){
        // given
        ForeignableOwner editingOwner = ForeignableOwner.AMW;
        ForeignableOwner foreignableOwner = ForeignableOwner.AMW;
        Foreignable<ResourceEntity> foreignable = new ResourceEntityBuilder().withOwner(foreignableOwner).build();

        // when
        boolean isModifiable = foreignableService.isForeignableModifiableByOwner(editingOwner, foreignable);

        // then
        assertTrue(isModifiable);
    }

    @Test
    public void isForeignableModifiableByOwnerWhenDifferentOwnerShouldReturnFalse(){
        // given
        ForeignableOwner editingOwner = ForeignableOwner.AMW;
        ForeignableOwner foreignableOwner = ForeignableOwner.MAIA;
        Foreignable<ResourceEntity> foreignable = new ResourceEntityBuilder().withOwner(foreignableOwner).build();

        // when
        boolean isModifiable = foreignableService.isForeignableModifiableByOwner(editingOwner, foreignable);

        // then
        assertFalse(isModifiable);
    }

    @Test
    public void isForeignableModifiableByOwnerWhenDifferentOwnerButHasChuckNorrisRoleShouldReturnTrue(){
        // given
        ForeignableOwner editingOwner = ForeignableOwner.AMW;
        ForeignableOwner foreignableOwner = ForeignableOwner.MAIA;
        Foreignable<ResourceEntity> foreignable = new ResourceEntityBuilder().withOwner(foreignableOwner).build();
        when(permissionServiceMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(true);

        // when
        boolean isModifiable = foreignableService.isForeignableModifiableByOwner(editingOwner, foreignable);

        // then
        assertTrue(isModifiable);
    }

    @Test
    public void verifyDeletableByOwnerWhenSameOwnerShouldNotThrowException() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        ForeignableOwner foreignableOwner = ForeignableOwner.AMW;
        Foreignable<ResourceEntity> foreignable = new ResourceEntityBuilder().withOwner(foreignableOwner).build();

        // when
        foreignableService.verifyDeletableByOwner(deletingOwner, foreignable);

        // then
        assertTrue(true);
    }

    @Test
    public void verifyDeletableByOwnerWhenDifferentOwnerButHasChuckNorrisRoleShouldNotThrowException() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.MAIA;
        ForeignableOwner foreignableOwner = ForeignableOwner.AMW;
        Foreignable<ResourceEntity> foreignable = new ResourceEntityBuilder().withOwner(foreignableOwner).build();
        when(permissionServiceMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(true);

        // when
        foreignableService.verifyDeletableByOwner(deletingOwner, foreignable);

        // then
        assertTrue(true);
    }

    @Test(expected = ForeignableOwnerViolationException.class)
    public void verifyDeletableByOwnerWhenDifferentOwnerShouldThrowException() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.MAIA;
        ForeignableOwner foreignableOwner = ForeignableOwner.AMW;
        Foreignable<ResourceEntity> foreignable = new ResourceEntityBuilder().withOwner(foreignableOwner).build();

        // when
        foreignableService.verifyDeletableByOwner(deletingOwner, foreignable);
    }


    @Test
    public void verifyEditableByOwnernWhenSameOwnerShouldNotThrowException2() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        ForeignableOwner foreignableOwner = ForeignableOwner.AMW;
        int unchangedForeignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withId(1).build().foreignableFieldHashCode();
        Foreignable<ResourceEntity> changedForeignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withName("changed foreignable field").withId(1).build();

        // when
        foreignableService.verifyEditableByOwner(deletingOwner, unchangedForeignable, changedForeignable);

        // then
        assertTrue(true);
    }


    @Test
    public void verifyEditableByOwnernWhenCreatingNewObjectShouldNotThrowException2() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner editingOwner = ForeignableOwner.MAIA;
        ForeignableOwner foreignableOwner = ForeignableOwner.AMW;
        int unchangedForeignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withId(null).build().foreignableFieldHashCode();
        Foreignable<ResourceEntity> changedForeignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withName("changed foreignable field").withId(null).build();

        // when
        foreignableService.verifyEditableByOwner(editingOwner, unchangedForeignable, changedForeignable);

        // then
        assertTrue(true);
    }


    @Test
    public void verifyEditableByOwnernWhenNoForeignableFieldsChangedShouldNotThrowException2() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.MAIA;
        ForeignableOwner foreignableOwner = ForeignableOwner.AMW;
        int unchangedForeignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withId(1).build().foreignableFieldHashCode();
        Set<AmwFunctionEntity> functions = new HashSet<>();
        functions.add(new AmwFunctionEntity());
        Foreignable<ResourceEntity> changedForeignable = new ResourceEntityBuilder().withFunctions(functions).withOwner(foreignableOwner).withId(1).build();

        // when
        foreignableService.verifyEditableByOwner(deletingOwner, unchangedForeignable, changedForeignable);

        // then
        assertTrue(true);
    }

    @Test(expected = ForeignableOwnerViolationException.class)
    public void verifyEditableByOwnernWhenDifferentOwnerIsEditingForeignableFieldOfExistingForeignableObjectShouldThrowException() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner editableOwner = ForeignableOwner.MAIA;
        ForeignableOwner foreignableOwner = ForeignableOwner.AMW;
        Foreignable<ResourceEntity> foreignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withId(1).build();
        Foreignable<ResourceEntity> changedForeignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withName("changed foreignable field").withId(1).build();

        // when
        foreignableService.verifyEditableByOwner(editableOwner,changedForeignable.foreignableFieldHashCode(), foreignable);
    }

    @Test(expected = ForeignableOwnerViolationException.class)
    public void verifyEditableByOwnernWhenDifferentOwnerIsEditingForeignableFieldOfExistingForeignableObjectShouldThrowException2() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner editableOwner = ForeignableOwner.MAIA;
        ForeignableOwner foreignableOwner = ForeignableOwner.AMW;
        int unchangedForeignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withId(1).build().foreignableFieldHashCode();
        Foreignable<ResourceEntity> changedForeignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withName("changed foreignable field").withId(1).build();

        // when
        foreignableService.verifyEditableByOwner(editableOwner, unchangedForeignable, changedForeignable);
    }



    @Test
    public void verifyEditableByOwnernWhenDifferentOwnerIsEditingForeignableFieldOfExistingForeignableObjectButHasChuckNorrisRoleShouldNotThrowException2() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.MAIA;
        ForeignableOwner foreignableOwner = ForeignableOwner.AMW;
        int unchangedForeignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withId(1).build().foreignableFieldHashCode();

        Foreignable<ResourceEntity> changedForeignable = new ResourceEntityBuilder().withOwner(foreignableOwner).withName("changed foreignable field").withId(1).build();
        when(permissionServiceMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(true);

        // when
        foreignableService.verifyEditableByOwner(deletingOwner, unchangedForeignable, changedForeignable);

        // then
        assertTrue(true);
    }



}