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

package ch.puzzle.itc.mobiliar.business.function.entity;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.TargetPlatformEntityBuilder;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.entity.MikEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class AmwFunctionEntityTest {

    private static final String RESOURCE_TYPE_NAME = "resourceTypeName";
    private static final String RESOURCE_NAME = "resourceName";

    private ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().buildResourceTypeEntity(RESOURCE_TYPE_NAME, null, false);
    private ResourceEntity resource =  new ResourceEntityBuilder().withName(RESOURCE_NAME).build();

    private TargetPlatformEntityBuilder targetPlatformEntityBuilder = new TargetPlatformEntityBuilder();
    private AmwFunctionEntity entity;

    @Test
    public void getMiksWhenNotInitializedShouldNotBeNull(){
        // given
        entity = new AmwFunctionEntityBuilder("name", 1).build();

        // when
        Set<String> miks = entity.getMikNames();

        // then
        assertNotNull(miks);
        assertTrue(miks.isEmpty());
    }

    @Test
    public void isDefinedOnResourceWhenResourceIsSetShouldReturnTrue(){
        // given
        entity = new AmwFunctionEntityBuilder("name", 1).forResource(resource).build();
        assertNotNull(entity.getResource());

        // when
        boolean definedOnResource = entity.isDefinedOnResource();

        // then
        assertTrue(definedOnResource);
    }

    @Test
    public void isDefinedOnResourceWhenNoResourceIsSetShouldReturnFalse(){
        // given
        entity = new AmwFunctionEntityBuilder("name", 1).build();
        assertNull(entity.getResource());

        // when
        boolean definedOnResource = entity.isDefinedOnResource();

        // then
        assertFalse(definedOnResource);
    }

    @Test
    public void isDefinedOnResourceTypeWhenResourceTypeIsSetShouldReturnTrue(){
        // given
        entity = new AmwFunctionEntityBuilder("name", 1).forResourceType(resourceType).build();
        assertNotNull(entity.getResourceType());

        // when
        boolean definedOnResourceType = entity.isDefinedOnResourceType();

        // then
        assertTrue(definedOnResourceType);
    }

    @Test
    public void isDefinedOnResourceTypeWhenNoResourceTypeIsSetShouldReturnFalse(){
        // given
        entity = new AmwFunctionEntityBuilder("name", 1).build();
        assertNull(entity.getResourceType());

        // when
        boolean definedOnResourceType = entity.isDefinedOnResourceType();

        // then
        assertFalse(definedOnResourceType);
    }

    @Test
    public void setResourceWhenResourceTypeIsSetShouldSetResourceAndResetTypeToNull(){
        // given
        entity = new AmwFunctionEntityBuilder("name", 1).forResourceType(resourceType).build();
        assertNotNull(entity.getResourceType());
        assertNull(entity.getResource());

        // when
        entity.setResource(resource);

        // then
        assertNotNull(entity.getResource());
        assertNull(entity.getResourceType());
    }

    @Test
    public void setResourceTypeWhenResourceIsSetShouldSetResourceTypeAndResetResourceToNull(){
        // given
        entity = new AmwFunctionEntityBuilder("name", 1).forResource(resource).build();
        assertNotNull(entity.getResource());
        assertNull(entity.getResourceType());

        // when
        entity.setResourceType(resourceType);

        // then
        assertNotNull(entity.getResourceType());
        assertNull(entity.getResource());
    }

    @Test
    public void setNameShouldTrimName(){
        // given
        String name = "bla";

        entity = new AmwFunctionEntity();

        // when
        entity.setName("   " + name + "   ");

        // then
        assertEquals(name, entity.getName());
    }

    @Test
    public void setNameShouldNotSetEmptyName(){
        // given
        String name = "";

        entity = new AmwFunctionEntity();

        // when
        entity.setName(name);

        // then
        assertNull(entity.getName());
    }

    @Test
    public void setNameShouldNotSetTrimmedEmptyName(){
        // given
        String name = "       ";

        entity = new AmwFunctionEntity();

        // when
        entity.setName(name);

        // then
        assertNull(entity.getName());
    }


    @Test
    public void isOverwrittenBySubTypeOrResourceFunctionWhenResourceTypeFunctionIsOverwrittenByResourceFunctionShouldReturnTrue(){
        // given
        AmwFunctionEntity function1 = new AmwFunctionEntityBuilder("name", 2).forResourceType(resourceType).build();
        AmwFunctionEntity function2 = new AmwFunctionEntityBuilder("name2", 3).forResource(resource).build();

        function2.overwrite(function1);

        // when
        boolean isOverwritten = function1.isOverwrittenBySubTypeOrResourceFunction();

        // then
        assertTrue(isOverwritten);
    }

    @Test
    public void isOverwrittenBySubTypeOrResourceFunctionWhenResourceTypeFunctionIsOverwrittenByResourceTypeFunctionShouldReturnTrue(){
        // given
        AmwFunctionEntity function1 = new AmwFunctionEntityBuilder("name", 2).forResourceType(resourceType).build();
        AmwFunctionEntity function2 = new AmwFunctionEntityBuilder("name2", 3).forResourceType(resourceType).build();

//        function1.overrideWith(function2);
        function2.overwrite(function1);

        // when
        boolean isOverwritten = function1.isOverwrittenBySubTypeOrResourceFunction();

        // then
        assertTrue(isOverwritten);
    }

    @Test
    public void isOverwrittenBySubTypeOrResourceFunctionWhenNotOverwrittenShouldReturnFalse(){
        // given
        AmwFunctionEntity function1 = new AmwFunctionEntityBuilder("name", 2).forResourceType(resourceType).build();


        // when
        boolean isOverwritten = function1.isOverwrittenBySubTypeOrResourceFunction();

        // then
        assertFalse(isOverwritten);
    }

    @Test
    public void isOverridingResourceTypeFunctionWhenResourceFunctionIsOverwritingResourceTypeFunctionShouldReturnTrue(){
        // given
        AmwFunctionEntity function1 = new AmwFunctionEntityBuilder("name", 2).forResourceType(resourceType).build();
        AmwFunctionEntity function2 = new AmwFunctionEntityBuilder("name2", 3).forResource(resource).build();

//        function1.overrideWith(function2);
        function2.overwrite(function1);

        // when
        boolean isOverwritten = function2.isOverwritingResourceTypeFunction();

        // then
        assertTrue(isOverwritten);
    }

    @Test
    public void isOverridingResourceTypeFunctionWhenResourceTypeFunctionIsOverwritingResourceTypeFunctionShouldReturnTrue(){
        // given
        AmwFunctionEntity function1 = new AmwFunctionEntityBuilder("name", 2).forResourceType(resourceType).build();
        AmwFunctionEntity function2 = new AmwFunctionEntityBuilder("name2", 3).forResourceType(resourceType).build();

//        function1.overrideWith(function2);
        function2.overwrite(function1);

        // when
        boolean isOverwritten = function2.isOverwritingResourceTypeFunction();

        // then
        assertTrue(isOverwritten);
    }

    @Test
    public void isOverridingResourceTypeFunctionWhenNotOverridingShouldReturnFalse(){
        // given
        AmwFunctionEntity function2 = new AmwFunctionEntityBuilder("name2", 3).forResourceType(resourceType).build();

        // when
        boolean isOverwritten = function2.isOverwritingResourceTypeFunction();

        // then
        assertFalse(isOverwritten);
    }


    @Test
    public void getOverwrittenFunctionResourceTypeNameWhenOverwritingShouldReturnResourceTypeName(){
        // given
        AmwFunctionEntity function1 = new AmwFunctionEntityBuilder("name1", 2).forResourceType(resourceType).build();
        AmwFunctionEntity function2 = new AmwFunctionEntityBuilder("name2", 3).forResourceType(resourceType).build();

//        function1.overrideWith(function2);
        function2.overwrite(function1);

        // when
        String parentResourceTypeName = function2.getOverwrittenFunctionResourceTypeName();

        // then
        assertEquals(RESOURCE_TYPE_NAME, parentResourceTypeName);
    }

    @Test
    public void getOverwrittenFunctionResourceTypeNameWhenNotOverwritingShouldReturnEmptyString(){
        // given
        AmwFunctionEntity function2 = new AmwFunctionEntityBuilder("name2", 3).forResourceType(resourceType).build();

        // when
        String parentResourceTypeName = function2.getOverwrittenFunctionResourceTypeName();

        // then
        assertTrue(parentResourceTypeName.isEmpty());
    }


    @Test
    public void getOverwritingFunctionSubResourceTypeOrResourceNameWhenNotOverwrittenShouldReturnEmptyString(){
        // given
        AmwFunctionEntity function2 = new AmwFunctionEntityBuilder("name2", 3).forResourceType(resourceType).build();

        // when
        String childResourceOrTypeName = function2.getOverwrittenFunctionResourceTypeName();

        // then
        assertTrue(childResourceOrTypeName.isEmpty());
    }

    @Test
    public void getOverwritingFunctionSubResourceTypeOrResourceNameWhenOverwrittenByResourceTypeFunctionShouldReturnResourceTypeName(){
        // given
        AmwFunctionEntity parent = new AmwFunctionEntityBuilder("name1", 2).forResourceType(resourceType).build();
        AmwFunctionEntity child = new AmwFunctionEntityBuilder("name2", 3).forResource(resource).build();
        child.overwrite(parent);
        assertTrue(parent.isOverwrittenBySubTypeOrResourceFunction());
        assertTrue(child.isOverwritingResourceTypeFunction());

        // when
        String childResourceOrTypeName = child.getOverwrittenFunctionResourceTypeName();

        // then
        assertEquals(RESOURCE_TYPE_NAME, childResourceOrTypeName);
    }

    @Test
    public void overrideWithShouldSetParentAndChild(){
        // given
        AmwFunctionEntity parent = new AmwFunctionEntityBuilder("name1", 2).forResourceType(resourceType).build();
        AmwFunctionEntity child = new AmwFunctionEntityBuilder("name2", 3).forResource(resource).build();

        assertFalse(parent.isOverwrittenBySubTypeOrResourceFunction());
        assertFalse(child.isOverwritingResourceTypeFunction());

        // when
        child.overwrite(parent);

        // then
        assertTrue(parent.isOverwrittenBySubTypeOrResourceFunction());
        assertTrue(child.isOverwritingResourceTypeFunction());
    }


    @Test
    public void resetOverridingWhenOverwrittenShouldResetParentAndChild(){
        // given
        AmwFunctionEntity parent = new AmwFunctionEntityBuilder("name1", 2).forResourceType(resourceType).build();
        AmwFunctionEntity child = new AmwFunctionEntityBuilder("name2", 3).forResource(resource).build();
        child.overwrite(parent);
        assertTrue(parent.isOverwrittenBySubTypeOrResourceFunction());
        assertTrue(child.isOverwritingResourceTypeFunction());


        // when
        child.resetOverwriting();

        // then
        assertFalse(parent.isOverwrittenBySubTypeOrResourceFunction());
        assertFalse(child.isOverwritingResourceTypeFunction());
    }

    @Test
    public void resetOverridingWhenResetOnNotOverwrittenShouldBeIgnored(){
        // given
        AmwFunctionEntity parent = new AmwFunctionEntityBuilder("name1", 2).forResourceType(resourceType).build();
        AmwFunctionEntity child = new AmwFunctionEntityBuilder("name2", 3).forResource(resource).build();
        assertFalse(parent.isOverwrittenBySubTypeOrResourceFunction());
        assertFalse(child.isOverwritingResourceTypeFunction());


        // when
        child.resetOverwriting();

        // then
        assertFalse(parent.isOverwrittenBySubTypeOrResourceFunction());
        assertFalse(child.isOverwritingResourceTypeFunction());
    }

    @Test
    public void shouldDecorateFunctionWithFreemarker(){
        // given
        AmwFunctionEntity function = new AmwFunctionEntityBuilder("name", Integer.valueOf(1)).withImplementation("impl").build();

        // when
        String result = function.getDecoratedImplementation();

        // then
        assertEquals("<#function name >impl</#function>", result);
    }

    @Test
    public void shouldDecorateFunctionWithFreemarker_nullImplementation(){
        // given
        AmwFunctionEntity function = new AmwFunctionEntityBuilder("name", Integer.valueOf(1)).withImplementation(null).build();

        // when
        String result = function.getDecoratedImplementation();

        // then
        assertEquals("<#function name ></#function>",result);
    }

    @Test
    public void shouldDecorateFunctionWithFreemarker_emptyImplementation(){
        // given
        AmwFunctionEntity function = new AmwFunctionEntityBuilder("name", Integer.valueOf(1)).withImplementation("").build();

        // when
        String result = function.getDecoratedImplementation();

        // then
        assertEquals("<#function name ></#function>",result);
    }

    @Test
    public void shouldGetCopyFromOrigin() throws AMWException{
        test_copyAmwFunctionEntity(CopyResourceDomainService.CopyMode.COPY);
        test_copyAmwFunctionEntity(CopyResourceDomainService.CopyMode.RELEASE);
    }

    public void test_copyAmwFunctionEntity(CopyResourceDomainService.CopyMode mode) throws AMWException{
        // given
        ResourceEntity originResource = new ResourceEntityBuilder().mockAppServerEntity("originResource", null, null,
                targetPlatformEntityBuilder.mockTargetPlatformEntity("EAP 6"));
        when(originResource.isDeletable()).thenReturn(true);
        AmwFunctionEntity originParent = new AmwFunctionEntityBuilder("fct1", 1).withImplementation("fooBar")
                .with(new MikEntity("mik1", null), new MikEntity("mik2", null))
                .forResourceType(originResource.getResourceType()).build();
        AmwFunctionEntity origin = new AmwFunctionEntityBuilder("fct1", 1).withImplementation("foo")
                .with(new MikEntity("mik1", null), new MikEntity("mik2", null))
                .forResource(originResource).withOverwrittenParent(originParent).build();

        Set<AmwFunctionEntity> originFunctions = new HashSet<>();
        originFunctions.add(new AmwFunctionEntityBuilder("origFct1", 1).forResource(originResource).build());
        originFunctions.add(new AmwFunctionEntityBuilder("origFct2", 2).forResource(originResource).build());
        originFunctions.add(origin);
        when(originResource.getFunctions()).thenReturn(originFunctions);

        ResourceEntity targetResource = new ResourceEntityBuilder().buildAppServerEntity("targetResource", null, null, true);

        // when
        AmwFunctionEntity copy = origin.getCopy(null, new CopyUnit(originResource, targetResource, mode, ForeignableOwner.AMW));

        // then
        assertNotNull(copy);
        assertEquals(targetResource, copy.getResource());
        assertNull(copy.getResourceType());
        assertEquals(origin.getName(), copy.getName());
        assertEquals(origin.getImplementation(), copy.getImplementation());

        assertEquals(origin.getMikNames(), copy.getMikNames());
        assertEquals(origin.getOverwrittenParent(), copy.getOverwrittenParent());
        assertTrue(origin.getOverwrittenParent().getOverwritingChildFunction().contains(copy));
    }


}