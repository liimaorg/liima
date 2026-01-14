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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import ch.puzzle.itc.mobiliar.builders.ContextEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.PropertyDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.PropertyEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceRelationContextEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService.CopyMode;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;

/**
 */
public class CopyResourceDomainServiceTest {

    @InjectMocks
    CopyResourceDomainService copyResourceDomainService;

    private ContextEntityBuilder contextEntityBuilder = new ContextEntityBuilder();

    ContextEntity globalContextMock;

    @BeforeEach
    public void setUp() {
        globalContextMock = contextEntityBuilder.mockContextEntity("GLOBAL", null, null);
        copyResourceDomainService = new CopyResourceDomainService();
    }

    @Test
    public void shouldCopySlaveRelPropWithDescriptor() throws AMWException {
        // given
        // app (master) -> ws (slave)
        // create descriptor and property on slave
        ResourceContextEntity slaveResCtx = new ResourceContextEntity();
        slaveResCtx.setContext(globalContextMock);

        PropertyEntity slaveProp = new PropertyEntityBuilder().buildPropertyEntity("abc", null);
        Set<PropertyEntity> slaveProps = new HashSet<>();
        slaveProps.add(slaveProp);
        PropertyDescriptorEntity slavePropDes = new PropertyDescriptorEntityBuilder().withPropertyName("propDesc").withProperties(slaveProps).build();
        slaveResCtx.addPropertyDescriptor(slavePropDes);
        slaveResCtx.addProperty(slaveProp);

        ResourceEntity slaveResource = new ResourceEntityBuilder().withName("ws1").withTypeOfName("ws").withContexts(Collections.singleton(slaveResCtx)).build();
        ResourceEntity masterResource = new ResourceEntityBuilder().withName("app1").withTypeOfName("app").withContexts(Collections.singleton(slaveResCtx)).build();

        // add a resource relation prop, overwriting prop on origin
        PropertyEntity masterRelProp = new PropertyEntityBuilder().buildPropertyEntity("xyz", slavePropDes);
        ResourceRelationContextEntity masterResRelCtx = new ResourceRelationContextEntityBuilder().mockResourceRelationContextEntity(globalContextMock);
        when(masterResRelCtx.getProperties()).thenReturn(Collections.singleton(masterRelProp));

        AbstractResourceRelationEntity slaveResRel = new ResourceRelationEntityBuilder().buildConsumedResRelEntity(masterResource, slaveResource, "foo", 2);
        slaveResRel.setContexts(Collections.singleton(masterResRelCtx));

        ResourceEntity targetRes = new ResourceEntityBuilder().withName("ws1Copy").build();
        CopyUnit copyUnit = new CopyUnit(slaveResource, targetRes, CopyMode.RELEASE);

        // when copy slave
        copyResourceDomainService.doCopy(copyUnit);

        // then
        // target should have new descriptor
        PropertyDescriptorEntity targetDesc = targetRes.getContexts().iterator().next().getPropertyDescriptors().iterator().next();
        assertNotEquals(System.identityHashCode(slavePropDes), System.identityHashCode(targetDesc));

        // relation and prop on relation should be copied
        Set<ConsumedResourceRelationEntity> targetConsResRels = targetRes.getConsumedSlaveRelations();
        assertEquals(1, targetConsResRels.size());
        ConsumedResourceRelationEntity  targetConsResRel = targetConsResRels.iterator().next();
        Set<PropertyEntity> targetRelProps = targetConsResRel.getContexts().iterator().next().getProperties();
        assertEquals(1, targetRelProps.size());
        PropertyEntity targetRelProp = targetRelProps.iterator().next();
        assertNotEquals(System.identityHashCode(masterRelProp), System.identityHashCode(targetRelProp));
        assertEquals(masterRelProp.getValue(), targetRelProp.getValue());

        // relation prop should use copied descriptor
        assertEquals(System.identityHashCode(targetRelProp.getDescriptor()), System.identityHashCode(targetDesc));

        // give
        targetRes = new ResourceEntityBuilder().withName("app1Copy").build();
        copyUnit = new CopyUnit(masterResource, targetRes, CopyMode.RELEASE);

        // when copy master
        copyResourceDomainService.doCopy(copyUnit);

        // then
        // relation and prop on relation should be copied
        targetConsResRels = targetRes.getConsumedMasterRelations();
        assertEquals(1, targetConsResRels.size());
        targetConsResRel = targetConsResRels.iterator().next();
        targetRelProps = targetConsResRel.getContexts().iterator().next().getProperties();
        assertEquals(1, targetRelProps.size());
        targetRelProp = targetRelProps.iterator().next();
        assertNotEquals(System.identityHashCode(masterRelProp), System.identityHashCode(targetRelProp));
        assertEquals(masterRelProp.getValue(), targetRelProp.getValue());

        // relation prop should use descriptor of slave
        assertEquals(System.identityHashCode(targetRelProp.getDescriptor()), System.identityHashCode(slavePropDes));
    }

}
