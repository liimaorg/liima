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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import ch.puzzle.itc.mobiliar.builders.ContextEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.PropertyDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.PropertyEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceRelationContextEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.auditview.control.AuditService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService.CopyMode;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;

/**
 */
public class CopyResourceDomainServiceTest {

    @InjectMocks
    CopyResourceDomainService copyResourceDomainService;
    ForeignableService foreignableService;

    private ContextEntityBuilder contextEntityBuilder = new ContextEntityBuilder();

    private ResourceEntity originResource;
    private ResourceEntity targetResource;
    ContextEntity globalContextMock;

    @Before
    public void setUp() {

        globalContextMock = contextEntityBuilder.mockContextEntity("GLOBAL", null, null);

        originResource = new ResourceEntityBuilder().buildApplicationEntity("appOriginResource", null, null, true);
        targetResource = new ResourceEntityBuilder().buildApplicationEntity("appTargetResource", null, null, true);

        copyResourceDomainService = new CopyResourceDomainService();
        foreignableService = mock(ForeignableService.class);
        copyResourceDomainService.foreignableService = foreignableService;
    }

    @Test
    public void copyResourceContexts_shouldCopyOnly_AmwOwned_PropertyDescriptors_In_Maia_PredecessorMode() throws ForeignableOwnerViolationException, AMWException {
        // given
        PropertyDescriptorEntity amwPropertyDesc = new PropertyDescriptorEntityBuilder().withId(1).withPropertyName("propertyName").build();
        PropertyDescriptorEntity maiaPropertyDesc = new PropertyDescriptorEntityBuilder().withId(2).withPropertyName("propertyNameMaia").withOwner(ForeignableOwner.MAIA).build();

        addPropertyDescriptor(originResource, amwPropertyDesc);
        addPropertyDescriptor(originResource, maiaPropertyDesc);

        CopyUnit copyUnit = new CopyUnit(originResource, targetResource, CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR, ForeignableOwner.MAIA);

        // when
        copyResourceDomainService.copyResourceContexts(copyUnit);

        // then
        assertEquals(1, targetResource.getContexts().iterator().next().getPropertyDescriptors().size());
        assertEquals("propertyName", targetResource.getContexts().iterator().next().getPropertyDescriptors().iterator().next().getPropertyName());
    }

    @Test
    public void copyResourceContexts_shouldCopy_Amw_PropertyValues() throws ForeignableOwnerViolationException, AMWException {
        // given
        PropertyDescriptorEntity amwPropertyDesc = new PropertyDescriptorEntityBuilder().withId(1).withPropertyName("propertyName").build();

        addPropertyDescriptor(originResource, amwPropertyDesc);
        addPropertyValue(originResource, amwPropertyDesc, "propertyValue");

        CopyUnit copyUnit = new CopyUnit(originResource, targetResource, CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR, ForeignableOwner.MAIA);

        // when
        copyResourceDomainService.copyResourceContexts(copyUnit);

        // then
        assertEquals(1, targetResource.getContexts().iterator().next().getPropertyDescriptors().size());
        assertEquals("propertyName", targetResource.getContexts().iterator().next().getPropertyDescriptors().iterator().next().getPropertyName());
        assertEquals(1, targetResource.getContexts().iterator().next().getProperties().size());
        assertEquals("propertyValue", targetResource.getContexts().iterator().next().getProperties().iterator().next().getValue());
    }

    @Test
    public void copyResourceContexts_shouldCopy_Amw_PropertyValuesOnMaiaProperties() throws ForeignableOwnerViolationException, AMWException {
        // given
        PropertyDescriptorEntity maiaPropertyDesc = new PropertyDescriptorEntityBuilder().withId(1).withPropertyName("propertyNameMaia").withOwner(ForeignableOwner.MAIA).build();

        addPropertyDescriptor(originResource, maiaPropertyDesc);
        addPropertyValue(originResource, maiaPropertyDesc, "propertyValueMaia");

        // Property with same name, was added on the target Resource by Maia
        PropertyDescriptorEntity maiaPropertyDescTarget = new PropertyDescriptorEntityBuilder().withId(2).withPropertyName("propertyNameMaia").withOwner(ForeignableOwner.MAIA).build();
        addPropertyDescriptor(targetResource, maiaPropertyDescTarget);

        CopyUnit copyUnit = new CopyUnit(originResource, targetResource, CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR, ForeignableOwner.MAIA);

        // when
        copyResourceDomainService.copyResourceContexts(copyUnit);

        // then
        assertEquals(1, targetResource.getContexts().iterator().next().getPropertyDescriptors().size());
        assertEquals("propertyNameMaia", targetResource.getContexts().iterator().next().getPropertyDescriptors().iterator().next().getPropertyName());
        assertEquals(1, targetResource.getContexts().iterator().next().getProperties().size());
        assertEquals("propertyValueMaia", targetResource.getContexts().iterator().next().getProperties().iterator().next().getValue());
    }

    @Test
    public void copyConsumedMasterRelations_shouldOnlyCopyAmwOwnedReleations() throws ForeignableOwnerViolationException, AMWException {
        // given

        ResourceEntity slave = new ResourceEntityBuilder().buildApplicationEntity("database", null, null, true);
        slave.setOwner(ForeignableOwner.getSystemOwner());

        ResourceEntity slave2 = new ResourceEntityBuilder().buildApplicationEntity("cpi", null, null, true);
        slave2.setOwner(ForeignableOwner.MAIA);

        originResource.addConsumedResourceRelation(slave, mock(ResourceRelationTypeEntity.class), null, ForeignableOwner.getSystemOwner());
        originResource.addConsumedResourceRelation(slave2, mock(ResourceRelationTypeEntity.class), null, ForeignableOwner.MAIA);


        CopyUnit copyUnit = new CopyUnit(originResource, targetResource, CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR, ForeignableOwner.MAIA);

        // when
        copyResourceDomainService.copyConsumedMasterRelations(copyUnit);
        // then
        assertEquals(1, targetResource.getConsumedMasterRelations().size());
        assertEquals("database", targetResource.getConsumedMasterRelations().iterator().next().getSlaveResource().getName());

    }

    @Test
    public void copyProvidedMasterRelations_shouldOnlyCopyAmwOwnedReleations() throws ForeignableOwnerViolationException, AMWException {
        // given

        ResourceEntity provider = new ResourceEntityBuilder().buildApplicationEntity("amwws", null, null, true);
        provider.setOwner(ForeignableOwner.getSystemOwner());
        provider.setId(Integer.valueOf(1));

        ResourceEntity provider2 = new ResourceEntityBuilder().buildApplicationEntity("ppi", null, null, true);
        provider2.setOwner(ForeignableOwner.MAIA);
        provider2.setId(Integer.valueOf(2));

        originResource.addProvidedResourceRelation(provider, mock(ResourceRelationTypeEntity.class), ForeignableOwner.getSystemOwner());
        originResource.addProvidedResourceRelation(provider2, mock(ResourceRelationTypeEntity.class), ForeignableOwner.MAIA);

        CopyUnit copyUnit = new CopyUnit(originResource, targetResource, CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR, ForeignableOwner.MAIA);

        // when
        copyResourceDomainService.copyProvidedMasterRelations(copyUnit);
        // then
        assertEquals(1, targetResource.getProvidedMasterRelations().size());
        assertEquals("amwws", targetResource.getProvidedMasterRelations().iterator().next().getSlaveResource().getName());
    }

    @Test
    public void shouldCopySlaveRelPropWithDescriptor() throws AMWException, ForeignableOwnerViolationException {
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
        CopyUnit copyUnit = new CopyUnit(slaveResource, targetRes, CopyMode.RELEASE, ForeignableOwner.AMW);

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
        copyUnit = new CopyUnit(masterResource, targetRes, CopyMode.RELEASE, ForeignableOwner.AMW);

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

    private void addPropertyDescriptor(ResourceEntity resource, PropertyDescriptorEntity propertyDescriptor) {
        ResourceContextEntity context = resource.getOrCreateContext(globalContextMock);
        context.addPropertyDescriptor(propertyDescriptor);
    }

    private void addPropertyValue(ResourceEntity resource, PropertyDescriptorEntity propertyDescriptor, String value) {
        ResourceContextEntity context = resource.getOrCreateContext(globalContextMock);
        PropertyEntity propertyEntity = new PropertyEntityBuilder().buildPropertyEntity(value, propertyDescriptor);
        context.addProperty(propertyEntity);
    }
}
