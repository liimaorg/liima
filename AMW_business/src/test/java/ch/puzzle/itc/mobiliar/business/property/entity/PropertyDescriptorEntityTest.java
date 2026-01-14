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

package ch.puzzle.itc.mobiliar.business.property.entity;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.builders.PropertyDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.PropertyEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;

public class PropertyDescriptorEntityTest {
	

    private PropertyDescriptorEntity propertyDescriptorEntity;
    private PropertyDescriptorEntityBuilder propDescBuilder = new PropertyDescriptorEntityBuilder();
    private PropertyEntityBuilder propBuilder = new PropertyEntityBuilder();

    @BeforeEach
    public void setUp(){
        propertyDescriptorEntity = new PropertyDescriptorEntity();
    }

    @Test
    public void propertyNamesShallNotHaveLeadingSpaces(){
        // when
        propertyDescriptorEntity = new PropertyDescriptorEntity();
        propertyDescriptorEntity.setPropertyName(" Test");

        // then
        assertNotNull(propertyDescriptorEntity.getPropertyName());
        assertEquals("Test",propertyDescriptorEntity.getPropertyName());
    }

    @Test
    public void propertyNamesShallNotHaveTrailingSpaces(){
        // when
        propertyDescriptorEntity = new PropertyDescriptorEntity();
        propertyDescriptorEntity.setPropertyName("Test ");

        // then
        assertNotNull(propertyDescriptorEntity.getPropertyName());
        assertEquals("Test",propertyDescriptorEntity.getPropertyName());
    }

    @Test
    public void addPropertyTagOnEmptyTagsShouldSetNewTag(){
        // given
        String tagName = "tag1";
        PropertyTagEntity tag1 = createTagWithName(tagName);
        assertTrue(propertyDescriptorEntity.getPropertyTags().isEmpty());

        // when
        propertyDescriptorEntity.addPropertyTag(tag1);

        // then
        assertEquals(1, propertyDescriptorEntity.getPropertyTags().size());
    }

    @Test
    public void addPropertyTagWhenTagAlreadyExistsShouldNotAddTag(){
        // given
        String tagName = "tag1";
        PropertyTagEntity tag1 = createTagWithName(tagName);
        propertyDescriptorEntity.addPropertyTag(tag1);
        assertFalse(propertyDescriptorEntity.getPropertyTags().isEmpty());

        // when
        propertyDescriptorEntity.addPropertyTag(tag1);

        // then
        assertEquals(1, propertyDescriptorEntity.getPropertyTags().size());
    }

    @Test
    public void addPropertyTagWhenTagNotYetExistsShouldAddTag(){
        // given
        String tagName = "tag1";
        PropertyTagEntity tag1 = createTagWithName(tagName);
        propertyDescriptorEntity.addPropertyTag(tag1);
        assertFalse(propertyDescriptorEntity.getPropertyTags().isEmpty());

        // when
        propertyDescriptorEntity.addPropertyTag(createTagWithName("otherTagName"));

        // then
        assertEquals(2, propertyDescriptorEntity.getPropertyTags().size());
    }

    @Test
    public void getValidationLogic_shouldReturnValidationLogicFromDescriptor(){
        // given
        PropertyTypeEntity type = new PropertyTypeEntity();
        type.setId(1);
        String typeRegex = "*";
        type.setValidationRegex(typeRegex);
        propertyDescriptorEntity.setPropertyTypeEntity(type);
        String descRegex = "[0-9]*";
        propertyDescriptorEntity.setValidationLogic(descRegex);

        // when
        String validationLogic = propertyDescriptorEntity.getValidationLogic();

        // then
        assertEquals(descRegex, validationLogic);
    }

    @Test
    public void getValidationLogic_shouldReturnValidationLogicFromType(){
        // given
        PropertyTypeEntity type = new PropertyTypeEntity();
        type.setId(1);
        String typeRegex = "*";
        type.setValidationRegex(typeRegex);
        propertyDescriptorEntity.setPropertyTypeEntity(type);

        // when
        String validationLogic = propertyDescriptorEntity.getValidationLogic();

        // then
        assertEquals(typeRegex, validationLogic);
    }

    @Test
    public void getValidationLogic_shouldReturnDefaultValidationLogic(){
        // when
        String validationLogic = propertyDescriptorEntity.getValidationLogic();

        // then
        assertEquals(PropertyDescriptorEntity.DEFAULTVALIDATIONEXPRESSION, validationLogic);
    }

    private PropertyTagEntity createTagWithName(String name){
        PropertyTagEntity tag = new PropertyTagEntity();
        tag.setName(name);
        return tag;
    }

    @Test
    public void copyPropertyDescriptorEntity_should_create_target_if_null() throws AMWException{
        // given
        String origName = "foo";
        PropertyDescriptorEntity origin = createOriginPropDesc(origName);

        // when
        PropertyDescriptorEntity copy = origin.getCopy(null, new CopyUnit(null, new ResourceEntityBuilder().withName("targetResource").build(), CopyResourceDomainService.CopyMode.COPY));
        PropertyDescriptorEntity release = origin.getCopy(null, new CopyUnit(null, new ResourceEntityBuilder().withName("targetResource").build(), CopyResourceDomainService.CopyMode.RELEASE));

        // then
        assertPropDescCopy(copy, origin, null, null);
        assertPropDescCopy(release, origin, null, null);
    }

    @Test
    public void copyPropertyDescriptorEntity_existing() throws AMWException{
        // given
        String origName = "foo";
        PropertyDescriptorEntity origin = createOriginPropDesc(origName);

        String targetComment = "targetComment";
        String targetDefaultValue = "targetDefaultValue";
        String targetExampleValue = "targetExampleValue";
        String targetMik = "targetMik";
        String targetDisplayName = "targetDisplayName";
        String targetValue = "targetValue";
        PropertyTagEntity targetTag = new PropertyTagEntity();
        targetTag.setName("targetTag");
        PropertyEntity targetProp = propBuilder.buildPropertyEntity(targetValue, null);
        PropertyTypeEntity targetType = propDescBuilder.mockPropertyTypeEntity("type2");
        PropertyDescriptorEntity copyTarget = propDescBuilder.withGeneratedId().withPropertyName(origName)
                .withProperties(Collections.singleton(targetProp)).withPropertyComment(targetComment)
                .withDefaultValue(targetDefaultValue).withMik(targetMik)
                .withExampleValue(targetExampleValue).withPropertyType(targetType)
                .withDisplayName(targetDisplayName).withTags(targetTag).build();
        PropertyDescriptorEntity releaseTarget = propDescBuilder.withGeneratedId().withPropertyName(origName)
                .withProperties(Collections.singleton(targetProp)).withPropertyComment(targetComment)
                .withDefaultValue(targetDefaultValue).withMik(targetMik)
                .withExampleValue(targetExampleValue).withPropertyType(targetType)
                .withDisplayName(targetDisplayName).withTags(targetTag).build();

        Set<PropertyEntity> copyTargetProps = Collections.unmodifiableSet(copyTarget.getProperties());
        Set<PropertyEntity> releaseTargetProps = Collections.unmodifiableSet(releaseTarget.getProperties());

        // when
        PropertyDescriptorEntity copy = origin.getCopy(copyTarget, new CopyUnit(null, new ResourceEntityBuilder().withName("targetResource").build(), CopyResourceDomainService.CopyMode.COPY));
        PropertyDescriptorEntity release = origin.getCopy(releaseTarget, new CopyUnit(null, new ResourceEntityBuilder().withName("targetResource").build(), CopyResourceDomainService.CopyMode.RELEASE));

        // then
        assertPropDescCopy(copy, origin, copyTarget, copyTargetProps);
        assertPropDescCopy(release, origin, releaseTarget, releaseTargetProps);
    }

    private PropertyDescriptorEntity createOriginPropDesc(String name){
        String origComment = "origComment";
        String origDefaultValue = "origDefaultVal";
        String origExampleValue = "origExampleVal";
        String origMik = "origMik";
        String origDisplayName = "origDisplayName";
        String origValue = "origValue";
        PropertyTagEntity origTag = new PropertyTagEntity();
        origTag.setName("origTag");
        PropertyEntity origProp = propBuilder.buildPropertyEntity(origValue, null);
        PropertyTypeEntity origType = propDescBuilder.mockPropertyTypeEntity("type1");
        PropertyDescriptorEntity origin = propDescBuilder.withGeneratedId().withPropertyName(name)
                .withProperties(Collections.singleton(origProp)).withPropertyComment(origComment)
                .withDefaultValue(origDefaultValue).withMik(origMik).withExampleValue(origExampleValue)
                .withPropertyType(origType).withDisplayName(origDisplayName)
                .withTags(origTag).build();
        return  origin;
    }

    private void assertPropDescCopy(PropertyDescriptorEntity copy, PropertyDescriptorEntity origin,
                                    PropertyDescriptorEntity target, Set<PropertyEntity> existingTargetProperties) {
        assertNotNull(copy);
        // properties from origin will be added in copyPropertyEntity()
        if (target == null) {
            assertTrue( copy.getProperties().isEmpty());
        }
        else {
            assertEquals(existingTargetProperties, copy.getProperties());
        }
        assertEquals(origin.isEncrypt(), copy.isEncrypt());
        assertEquals(origin.getPropertyName(), copy.getPropertyName());
        assertEquals(origin.isNullable(), copy.isNullable());
        assertEquals(origin.getValidationLogic(), copy.getValidationLogic());
        assertEquals(origin.getPropertyComment(), copy.getPropertyComment());
        assertEquals(origin.getCardinalityProperty(), copy.getCardinalityProperty());
        assertEquals(origin.getPropertyTypeEntity(), copy.getPropertyTypeEntity());
        assertEquals(origin.getDefaultValue(), copy.getDefaultValue());
        assertEquals(origin.getExampleValue(), copy.getExampleValue());
        assertEquals(origin.getMachineInterpretationKey(), copy.getMachineInterpretationKey());
        assertEquals(origin.isOptional(), copy.isOptional());
    }

}
