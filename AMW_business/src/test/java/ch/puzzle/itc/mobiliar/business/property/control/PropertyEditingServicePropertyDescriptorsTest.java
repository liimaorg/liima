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

/*
 * To change this license header, choose License Headers in Project Properties. To change this template file,
 * choose Tools | Templates and open the template in the editor.
 */
package ch.puzzle.itc.mobiliar.business.property.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import ch.puzzle.itc.mobiliar.business.environment.control.ContextHierarchy;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.utils.database.DatabaseUtil;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.util.ContextNames;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;

/**
 * @author oschmid
 */
@ExtendWith({MockitoExtension.class, PersistenceTestExtension.class})
public class PropertyEditingServicePropertyDescriptorsTest {

    @Spy
    @PersistenceContext
    EntityManager entityManager;

    @InjectMocks
    PropertyEditingQueries queries;

    @InjectMocks
    PropertyEditingService service;

    @Mock
    DatabaseUtil dbUtil;

    ContextHierarchy contextHierarchy = new ContextHierarchy();
    private ProvidedResourceRelationEntity providedResourceRelationEntity;

    PropertyDescriptorEntity propertyDescriptorEntity;
    PropertyDescriptorEntity controlPropertyDescriptorEntity;

    ResourceEntity master;
    ResourceEntity slave;
    ResourceEntity provided;

    ResourceTypeEntity masterType;
    ResourceTypeEntity slaveType;

    ResourceRelationTypeEntity resourceRelationTypeEntity;
    ConsumedResourceRelationEntity consumedResourceRelationEntity;

    ContextEntity contextEntity;

    @BeforeEach
    public void setUp() throws ElementAlreadyExistsException {
        service.queries = queries;
        service.contextHierarchy = contextHierarchy;
        propertyDescriptorEntity = new PropertyDescriptorEntity();
        propertyDescriptorEntity.setPropertyName("foo");
        entityManager.persist(propertyDescriptorEntity);
        controlPropertyDescriptorEntity = new PropertyDescriptorEntity();
        controlPropertyDescriptorEntity.setPropertyName("bar");
        masterType = new ResourceTypeEntity();
        masterType.setName("masterType");
        master = ResourceFactory.createNewResource("master");
        master.setResourceType(masterType);
        slaveType = new ResourceTypeEntity();
        slaveType.setName("slaveType");
        slave = ResourceFactory.createNewResource("slave");
        slave.setResourceType(slaveType);
        provided = ResourceFactory.createNewResource("provided");
        provided.setResourceType(slaveType);
        resourceRelationTypeEntity = new ResourceRelationTypeEntity();
        resourceRelationTypeEntity.setResourceTypes(masterType, slaveType);
        entityManager.persist(resourceRelationTypeEntity);
        consumedResourceRelationEntity = master
                  .addConsumedResourceRelation(slave, resourceRelationTypeEntity, null, ForeignableOwner.AMW);
        providedResourceRelationEntity = master.addProvidedResourceRelation(provided, resourceRelationTypeEntity, ForeignableOwner.AMW);        ;
        contextEntity = new ContextEntity();
        contextEntity.setName(ContextNames.GLOBAL.name());
        entityManager.persist(contextEntity);
        entityManager.persist(masterType);
        entityManager.persist(master);
        entityManager.persist(slaveType);
        entityManager.persist(slave);
        entityManager.persist(provided);
        entityManager.flush();
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Ensures that - when not adding any additional properties to the relation - only the control property is returned
     */
    @Test
    public void shouldReturnPropertyDescriptorOnResourceOnly() {
        //given
        ResourceContextEntity globalResourceContext = slave.getOrCreateContext(contextEntity);
        globalResourceContext.addPropertyDescriptor(controlPropertyDescriptorEntity);
        entityManager.flush();

        //when
        Query result = queries
                  .getPropertyValueForConsumedRelationQuery(consumedResourceRelationEntity.getId(),
                            slave.getId(),
                            Arrays.asList(masterType.getId()),
                            Arrays.asList(slaveType.getId()),
                            Arrays.asList(contextEntity.getId()));
        List<String> propertyNames = getPropertyNames(result.getResultList());

        //then
        assertEquals(1, propertyNames.size());
        assertTrue(propertyNames.contains(controlPropertyDescriptorEntity.getPropertyName()));
    }

    /**
     * Add a property to the relation and ensure that it's part of the result
     */
    @Test
    public void shouldReturnPropertyDescriptorOnProvidedResourceRelation() {
        //given
        ResourceContextEntity globalResourceContext = provided.getOrCreateContext(contextEntity);
        globalResourceContext.addPropertyDescriptor(controlPropertyDescriptorEntity);
        ResourceRelationContextEntity globalRelationContext = providedResourceRelationEntity
                  .getOrCreateContext(
                            contextEntity);
        globalRelationContext.addPropertyDescriptor(propertyDescriptorEntity);
        entityManager.flush();
        //when
        Query result = queries
                  .getPropertyValueForProvidedRelationQuery(providedResourceRelationEntity.getId(),
                            provided.getId(),
                            Arrays.asList(masterType.getId()),
                            Arrays.asList(slaveType.getId()),
                            Arrays.asList(contextEntity.getId()));
        List<String> propertyNames = getPropertyNames(result.getResultList());
        //then
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains(controlPropertyDescriptorEntity.getPropertyName()));
        assertTrue(propertyNames.contains(propertyDescriptorEntity.getPropertyName()));
    }

    /**
     * Add a property to the relation and ensure that it's part of the result
     */
    @Test
    public void shouldReturnPropertyDescriptorOnConsumedResourceRelation() {
        //given
        ResourceContextEntity globalResourceContext = slave.getOrCreateContext(contextEntity);
        globalResourceContext.addPropertyDescriptor(controlPropertyDescriptorEntity);
        ResourceRelationContextEntity globalRelationContext = consumedResourceRelationEntity
                  .getOrCreateContext(
                            contextEntity);
        globalRelationContext.addPropertyDescriptor(propertyDescriptorEntity);
        entityManager.flush();
        //when
        Query result = queries
                  .getPropertyValueForConsumedRelationQuery(consumedResourceRelationEntity.getId(),
                            slave.getId(),
                            Arrays.asList(masterType.getId()),
                            Arrays.asList(slaveType.getId()),
                            Arrays.asList(contextEntity.getId()));
        List<String> propertyNames = getPropertyNames(result.getResultList());
        //then
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains(controlPropertyDescriptorEntity.getPropertyName()));
        assertTrue(propertyNames.contains(propertyDescriptorEntity.getPropertyName()));
    }

    /**
     * Add a property to the type relation and ensure that it's part of the result
     */
    @Test
    public void shouldReturnPropertyDescriptorOnResourceTypeRelationForConsumedResourceRelation() {
        //given

        ResourceTypeContextEntity globalResourceTypeContext = slaveType.getOrCreateContext(contextEntity);
        ResourceRelationTypeContextEntity globalResourceRelationTypeContext = resourceRelationTypeEntity.getOrCreateContext(
                  contextEntity);
        globalResourceTypeContext.addPropertyDescriptor(controlPropertyDescriptorEntity);
        globalResourceRelationTypeContext.addPropertyDescriptor(propertyDescriptorEntity);
        entityManager.flush();

        //when
        Query result = queries
                  .getPropertyValueForConsumedRelationQuery(consumedResourceRelationEntity.getId(),
                            slave.getId(),
                            Arrays.asList(masterType.getId()),
                            Arrays.asList(slaveType.getId()),
                            Arrays.asList(contextEntity.getId()));
        List<String> propertyNames = getPropertyNames(result.getResultList());

        //then
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains(controlPropertyDescriptorEntity.getPropertyName()));
        assertTrue(propertyNames.contains(propertyDescriptorEntity.getPropertyName()));
    }


    /**
     * Add a property to the type relation and ensure that it's part of the result
     */
    @Test
    public void shouldReturnPropertyDescriptorOnResourceTypeRelationForProvidedResourceRelation() {
        //given

        ResourceTypeContextEntity globalResourceTypeContext = slaveType.getOrCreateContext(contextEntity);
        ResourceRelationTypeContextEntity globalResourceRelationTypeContext = resourceRelationTypeEntity.getOrCreateContext(
                  contextEntity);
        globalResourceTypeContext.addPropertyDescriptor(controlPropertyDescriptorEntity);
        globalResourceRelationTypeContext.addPropertyDescriptor(propertyDescriptorEntity);
        entityManager.flush();

        //when
        Query result = queries
                  .getPropertyValueForProvidedRelationQuery(providedResourceRelationEntity.getId(),
                            provided.getId(),
                            Arrays.asList(masterType.getId()),
                            Arrays.asList(slaveType.getId()),
                            Arrays.asList(contextEntity.getId()));
        List<String> propertyNames = getPropertyNames(result.getResultList());

        //then
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains(controlPropertyDescriptorEntity.getPropertyName()));
        assertTrue(propertyNames.contains(propertyDescriptorEntity.getPropertyName()));
    }



    /**
     * Ensures that - when not adding any additional properties to the relation - only the control property is returned
     */
    @Test
    public void shouldReturnPropertyDescriptorOnResourceTypeOnly() {
        //given
        ResourceTypeContextEntity globalResourceTypeContext = slaveType.getOrCreateContext(contextEntity);
               globalResourceTypeContext.addPropertyDescriptor(controlPropertyDescriptorEntity);
        entityManager.flush();

        //when
        Query result = queries.getPropertyValueForResourceTypeRelationQuery(Arrays.asList(masterType.getId()),
                  Arrays.asList(slaveType.getId()), Arrays.asList(contextEntity.getId()));
        List<String> propertyNames = getPropertyNames(result.getResultList());

        //then
        assertEquals(1, propertyNames.size());
        assertTrue(propertyNames.contains(controlPropertyDescriptorEntity.getPropertyName()));
    }

    /**
     * Add a property to the type relation and ensure that it's part of the result
     */
    @Test
    public void shouldReturnPropertyDescriptorOnResourceTypeRelation() {
        //given

        ResourceTypeContextEntity globalResourceTypeContext = slaveType.getOrCreateContext(contextEntity);
        ResourceRelationTypeContextEntity globalResourceRelationTypeContext = resourceRelationTypeEntity.getOrCreateContext(
                  contextEntity);
        globalResourceTypeContext.addPropertyDescriptor(controlPropertyDescriptorEntity);
        globalResourceRelationTypeContext.addPropertyDescriptor(propertyDescriptorEntity);
        entityManager.flush();

        //when
        Query result = queries.getPropertyValueForResourceTypeRelationQuery(Arrays.asList(masterType.getId()),
                  Arrays.asList(slaveType.getId()), Arrays.asList(contextEntity.getId()));
        List<String> propertyNames = getPropertyNames(result.getResultList());

        //then
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains(controlPropertyDescriptorEntity.getPropertyName()));
        assertTrue(propertyNames.contains(propertyDescriptorEntity.getPropertyName()));
    }


    private List<String> getPropertyNames(List<Object[]> resultList) {
        List<String> foundProperties = new ArrayList<>();
        for (Object[] results : resultList) {
            foundProperties.add((String) results[0]);
        }
        return foundProperties;
    }
}
