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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.builders.ResourceEditPropertyBuilder;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;

/**
 * Purpose of this test is to guarantee that the correct arguments get set within the constructor - since the constructor is used within the native queries
 */
public class ResourceEditPropertyTest {


	private ResourceEditProperty resourceEditProperty;

	@Test
    public void constructorCardinalityArgumentShouldSetCardinality(){
        // given
        Integer cardinality = 999;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withCardinality(cardinality).build();
        
        // then
       assertEquals(cardinality, resourceEditProperty.getCardinalityProperty());
    }

    @Test
    public void constructorPropertyContextTypeIdArgumentShouldSetTypeContextId(){
        // given
        Integer propertyContextTypeId = 88;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withPropertyContextTypeId(propertyContextTypeId).build();

        // then
        assertEquals(propertyContextTypeId, resourceEditProperty.getTypeContextId());
    }

    @Test
    public void constructorPropertyIdArgumentShouldSetPropertyId(){
        // given
        Integer propertyId = 22;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withPropertyId(propertyId).build();

        // then
        assertEquals(propertyId, resourceEditProperty.getPropertyId());
    }

    @Test
    public void constructorTechnicalKeyArgumentShouldSetTechnicalKey(){
        // given
        String technicalKey = "technicalKey";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withTechnicalKey(technicalKey).build();

        // then
        assertEquals(technicalKey, resourceEditProperty.getTechnicalKey());
    }

    @Test
    public void constructorDisplayNameArgumentShouldSetDisplayName(){
        // given
        String displayName = "displayName";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withDisplayName(displayName).build();

        // then
        assertEquals(displayName, resourceEditProperty.getDisplayName());
    }

    @Test
    public void constructorValueArgumentShouldSetPropertyValue(){
        // given
        String propertyValue = "propertyValue";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(propertyValue).build();

        // then
        assertEquals(propertyValue, resourceEditProperty.getPropertyValue());
    }

    @Test
    public void constructorCommentArgumentShouldSetPropertyComment(){
        // given
        String propertyComment = "propertyComment";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withComment(propertyComment).build();

        // then
        assertEquals(propertyComment, resourceEditProperty.getPropertyComment());
    }

    @Test
    public void constructorIsNullableArgumentShouldSetIsNullable(){
        // given
        boolean isNullable = true;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withIsNullable(isNullable).build();

        // then
        assertEquals(isNullable, resourceEditProperty.isNullable());
    }

    @Test
    public void constructorIsEncryptedArgumentShouldSetIsEncrypted(){
        // given
        boolean isEncrypted = true;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withIsEncrypted(isEncrypted).build();

        // then
        assertEquals(isEncrypted, resourceEditProperty.isEncrypted());
    }

    @Test
    public void constructorValidationLogicArgumentShouldSetValidationLogic(){
        // given
        String validationLogic = "validationLogic";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withValidationLogic(validationLogic).build();

        // then
        assertEquals(validationLogic, resourceEditProperty.getValidationLogic());
    }

    @Test
    public void constructorPropContNameArgumentShouldSetPropContName(){
        // given
        String propContName = "propContName";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withPropContName(propContName).build();

        // then
        assertEquals(propContName, resourceEditProperty.getPropContName());
    }

    @Test
    public void constructorTypeContNameArgumentShouldSetTypeContName(){
        // given
        String typeContName = "typeContName";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withPropTypeContName(typeContName).build();

        // then
        assertEquals(typeContName, resourceEditProperty.getTypeContName());
    }

    @Test
    public void constructorMasterResourceTypeNameArgumentShouldSetTypeName(){
        // given
        String typeName = "typeName";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withMasterResTypeName(typeName).build();

        // then
        assertEquals(typeName, resourceEditProperty.getTypeName());
    }



    @Test
    public void constructorOriginArgumentShouldSetOrigin(){
        // given
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.INSTANCE;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withOrigin(origin).build();

        // then
        assertEquals(origin, resourceEditProperty.getOrigin());
    }



    @Test
    public void constructorPropertyContextIdArgumentShouldSetPropContextId(){
        // given
        Integer propContextId = 22;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withPropContextId(propContextId).build();

        // then
        assertEquals(propContextId, resourceEditProperty.getPropContextId());
    }

    @Test
    public void constructorDescriptorIdArgumentShouldSetDescriptorId(){
        // given
        Integer descriptorId = 22;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withDescriptorId(descriptorId).build();

        // then
        assertEquals(descriptorId, resourceEditProperty.getDescriptorId());
    }

    @Test
    public void constructorTypeIdArgumentShouldSetTypeId(){
        // given
        Integer typeId = 22;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withTypeId(typeId).build();

        // then
        assertEquals(typeId, resourceEditProperty.getTypeId());
    }


    @Test
    public void constructorMasterResTypeIdArgumentShouldSetMasterTypeId(){
        // given
        Integer masterTypeId = 22;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withMasterResTypeId(masterTypeId).build();

        // then
        assertEquals(masterTypeId, resourceEditProperty.getMasterTypeId());
    }


    @Test
    public void constructorLoadedForInstanceArgumentShouldMarkAsNotRelation(){
        // given
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.INSTANCE;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).build();

        // then
        assertFalse(resourceEditProperty.isRelationProperty());
    }

    @Test
    public void constructorLoadedForTypeArgumentShouldMarkAsNotRelation(){
        // given
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.TYPE;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).build();

        // then
        assertFalse(resourceEditProperty.isRelationProperty());
    }

    @Test
    public void constructorLoadedForRelationArgumentShouldMarkAsRelation(){
        // given
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.RELATION;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).build();

        // then
        assertTrue(resourceEditProperty.isRelationProperty());
    }

    @Test
    public void constructorLoadedForTypeRelationArgumentShouldMarkAsRelation(){
        // given
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.TYPE_REL;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).build();

        // then
        assertTrue(resourceEditProperty.isRelationProperty());
    }

    @Test
    public void constructorExampleValueArgumentShouldSetExampleValue(){
        // given
        String exampleValue = "exampleValue";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withExampleValue(exampleValue).build();

        // then
        assertEquals(exampleValue, resourceEditProperty.getExampleValue());
    }

    @Test
    public void constructorDefaultValueArgumentShouldSetDefaultValue(){
        // given
        String defaultValue = "defaultValue";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withDefaultValue(defaultValue).build();

        // then
        assertEquals(defaultValue, resourceEditProperty.getDefaultValue());
    }


    @Test
    public void constructorDescriptorOriginInstanceArgumentShouldSetDescriptorOriginValue(){
        // given
        ResourceEditProperty.Origin descriptorOrigin = ResourceEditProperty.Origin.INSTANCE;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withDescriptorOrigin(descriptorOrigin).build();

        // then
        assertEquals(descriptorOrigin, resourceEditProperty.getPropertyDescriptorOrigin());
    }

    @Test
    public void constructorFcOwnerInstanceArgumentShouldSetDescriptorFcOwnerValue(){
        // given
        ForeignableOwner fcOwner = ForeignableOwner.AMW;

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withFcOwner(fcOwner).build();

        // then
        assertEquals(fcOwner, resourceEditProperty.getFcOwner());
    }

    @Test
    public void constructorFcExternalLinkInstanceArgumentShouldSetDescriptorFcExternalLinkValue(){
        // given
        String externalLink = "externalLink";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withFcExternalLink(externalLink).build();

        // then
        assertEquals(externalLink, resourceEditProperty.getFcExternalLink());
    }

    @Test
    public void constructorFcExternalKeyInstanceArgumentShouldSetDescriptorFcExternalKeyValue(){
        // given
        String externalKey = "externalKey";

        // when
        resourceEditProperty = new ResourceEditPropertyBuilder().withFcExternalKey(externalKey).build();

        // then
        assertEquals(externalKey, resourceEditProperty.getFcExternalKey());
    }

    @Test
    public void isDefinedInContextWhereDefinedInSuperResTypeShouldReturnFalse(){
        // given
        Integer contextId = 1;
        resourceEditProperty = new ResourceEditPropertyBuilder().build();
        resourceEditProperty.setDefinedOnSuperResourceType(true);

        // when
        boolean definedInContext = resourceEditProperty.isDefinedInContext(contextId);


        // then
        assertFalse(definedInContext);
    }

    @Test
    public void isDefinedInContextWhereDefinedForInstanceAndPropertyContextShouldReturnFalse(){
        // given
        Integer propertyContextId = 1;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.INSTANCE;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withPropContextId(propertyContextId).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        boolean definedInContext = resourceEditProperty.isDefinedInContext(propertyContextId);

        // then
        assertFalse(definedInContext);
    }

    @Test
    public void isDefinedInContextWhereDefinedForTypeAndPropertyContextShouldReturnFalse(){
        // given
        Integer propertyContextId = 1;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.TYPE;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withPropContextId(propertyContextId).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        boolean definedInContext = resourceEditProperty.isDefinedInContext(propertyContextId);

        // then
        assertFalse(definedInContext);
    }

    @Test
    public void isDefinedInContextWhereDefinedForRelationAndContextIdMatchesPropertyContextShouldReturnTrue(){
        // given
        Integer propertyContextId = 1;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.RELATION;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.RELATION;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropContextId(propertyContextId).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        boolean definedInContext = resourceEditProperty.isDefinedInContext(propertyContextId);

        // then
        assertTrue(definedInContext);
    }

    @Test
    public void isDefinedInContextWhereDefinedForRelationAndContextIdNotMatchesPropertyContextShouldReturnFalse(){
        // given
        Integer propertyContextId = 1;
        Integer otherContextId = 555;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.RELATION;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.RELATION;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropContextId(propertyContextId).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        boolean definedInContext = resourceEditProperty.isDefinedInContext(otherContextId);

        // then
        assertFalse(definedInContext);
    }

    @Test
    public void isDefinedInContextWhereDefinedForRelationAndContextIdMatchesPropertyContextButDifferentOriginShouldReturnFalse(){
        // given
        Integer propertyContextId = 1;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.RELATION;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.INSTANCE;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropContextId(propertyContextId).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        boolean definedInContext = resourceEditProperty.isDefinedInContext(propertyContextId);

        // then
        assertFalse(definedInContext);
    }

    @Test
    public void isDefinedInContextWhereDefinedForTypeRelationAndContextIdMatchesPropertyContextShouldReturnTrue(){
        // given
        Integer propertyTypeContextId = 1;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.TYPE_REL;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.TYPE_REL;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropertyContextTypeId(propertyTypeContextId).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        boolean definedInContext = resourceEditProperty.isDefinedInContext(propertyTypeContextId);

        // then
        assertTrue(definedInContext);
    }

    @Test
    public void isDefinedInContextWhereDefinedForTypeRelationAndContextIdNotMatchesPropertyContextShouldReturnFalse(){
        // given
        Integer propertyContextId = 1;
        Integer otherContextId = 555;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.TYPE_REL;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.TYPE_REL;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropertyContextTypeId(propertyContextId).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        boolean definedInContext = resourceEditProperty.isDefinedInContext(otherContextId);

        // then
        assertFalse(definedInContext);
    }


    @Test
    public void isDefinedInContextWhereDefinedForTypeRelationAndContextIdMatchesPropertyContextButDifferentOriginShouldReturnFalse(){
        // given
        Integer propertyContextId = 1;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.INSTANCE;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.INSTANCE;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropertyContextTypeId(propertyContextId).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        boolean definedInContext = resourceEditProperty.isDefinedInContext(propertyContextId);

        // then
        assertFalse(definedInContext);
    }

    @Test
    public void getOriginOfValueWhenNotDefinedInContextWithoutTypeNameAndPropContNameShouldReturnNull(){
        // given
        Integer contextId = 1;
        String relationIdentifier = "relationIdentifier";

        String masterResourceTypeName = null;
        String propContName = null;
        String typeContName = "typeContName";
        Integer propertyContextId = 1;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.TYPE;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.INSTANCE;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropContName(propContName).withPropContextId(propertyContextId).withMasterResTypeName(masterResourceTypeName).withPropTypeContName(typeContName).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        String originOfValue = resourceEditProperty.getOriginOfValue(contextId, relationIdentifier);

        // then
        assertNull(originOfValue);
    }

    @Test
    public void getOriginOfValueWhenNotDefinedInContextWithTypeNameForInstanceShouldReturnResourceTypeNameAndTypeContextName(){
        // given
        Integer contextId = 1;
        String relationIdentifier = "relationIdentifier";

        String masterResourceTypeName = "masterResourceTypeName";
        String typeContName = "typeContName";
        Integer propertyContextId = 1;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.INSTANCE;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.TYPE;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropContextId(propertyContextId).withMasterResTypeName(masterResourceTypeName).withPropTypeContName(typeContName).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        String originOfValue = resourceEditProperty.getOriginOfValue(contextId, relationIdentifier);

        // then
        assertEquals("resource type \"masterResourceTypeName\" (typeContName)", originOfValue);
    }


    @Test
    public void getOriginOfValueWhenNotDefinedInContextWithTypeNameForRelationShouldReturnResourceTypeRelationAndTypeContextName(){
        // given
        Integer contextId = 1;
        String relationIdentifier = "relationIdentifier";

        String masterResourceTypeName = "masterResourceTypeName";
        String typeContName = "typeContName";
        Integer propertyContextId = 1;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.RELATION;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.TYPE_REL;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropContextId(propertyContextId).withMasterResTypeName(masterResourceTypeName).withPropTypeContName(typeContName).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        String originOfValue = resourceEditProperty.getOriginOfValue(contextId, relationIdentifier);

        // then
        assertEquals("resource type relation \"masterResourceTypeName\" (typeContName)", originOfValue);
    }


    @Test
    public void getOriginOfValueWhenNotDefinedInContextWithoutTypeNameWithPropContNameForInstanceShouldReturnResourceNameAndContextName(){
        // given
        Integer contextId = 1;
        String relationIdentifier = "relationIdentifier";

        String masterResourceTypeName = null;
        String propContName = "propContName";
        String typeContName = "typeContName";
        String resourceName = "resourceName";
        Integer propertyContextId = 1;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.TYPE;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.INSTANCE;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropContName(propContName).withPropContextId(propertyContextId).withMasterResTypeName(masterResourceTypeName).withPropTypeContName(typeContName).withResourceName(resourceName).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        String originOfValue = resourceEditProperty.getOriginOfValue(contextId, relationIdentifier);

        // then
        assertEquals("resource \"resourceName\" (propContName)", originOfValue);
    }

    @Test
    public void getOriginOfValueWhenNotDefinedInContextWithoutTypeNameWithPropContNameForRelationShouldReturnResourceRelationAndContextName(){
        // given
        Integer contextId = 1;
        String relationIdentifier = "relationIdentifier";

        String masterResourceTypeName = null;
        String propContName = "propContName";
        String typeContName = "typeContName";
        String resourceName = "resourceName";
        Integer propertyContextId = 1;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.RELATION;
        ResourceEditProperty.Origin loadedFor = ResourceEditProperty.Origin.TYPE_REL;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropContName(propContName).withPropContextId(propertyContextId).withMasterResTypeName(masterResourceTypeName).withPropTypeContName(typeContName).withResourceName(resourceName).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        String originOfValue = resourceEditProperty.getOriginOfValue(contextId, relationIdentifier);

        // then
        assertEquals("resource relation \"relationIdentifier\" (propContName)", originOfValue);
    }

    @Test
    public void getOriginOfValueWhenDefinedInContextWithoutParentShouldReturnNull(){
        // given
        Integer contextId = 1;
        String relationIdentifier = "relationIdentifier";

        String masterResourceTypeName = "masterResourceTypeName";
        String typeContName = "typeContName";
        Integer propertyContextId = contextId;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.INSTANCE;
        ResourceEditProperty.Origin loadedFor = origin;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(loadedFor).withOrigin(origin).withPropContextId(propertyContextId).withMasterResTypeName(masterResourceTypeName).withPropTypeContName(typeContName).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        // when
        String originOfValue = resourceEditProperty.getOriginOfValue(contextId, relationIdentifier);

        // then
        assertNull(originOfValue);
    }


    @Test
    public void getOriginOfValueWhenDefinedInContextAndHasReplacedValueFromParentWithoutTypeForInstanceShouldReturnResourceNameAndParentContextName(){
        // given
        Integer contextId = 1;
        String relationIdentifier = "relationIdentifier";

        String resourceName = "resourceName";
        String masterResourceTypeName = "masterResourceTypeName";
        String typeContName = "typeContName";
        String value = "value";
        String parentResourceValue = "parentResourceValue";
        String parentPropertyContextName = "parentPropertyContextName";
        Integer propertyContextId = contextId;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.INSTANCE;
        ResourceEditProperty.Origin loadedFor = origin;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).withResourceName(resourceName).withLoadedFor(loadedFor).withOrigin(origin).withPropContextId(propertyContextId).withMasterResTypeName(masterResourceTypeName).withPropTypeContName(typeContName).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        resourceEditProperty.setParent(new ResourceEditPropertyBuilder().withPropContName(parentPropertyContextName).withValue(parentResourceValue).build());

        // when
        String originOfValue = resourceEditProperty.getOriginOfValue(contextId, relationIdentifier);

        // then
        assertEquals("resource \"resourceName\" (parentPropertyContextName)", originOfValue);

    }

    @Test
    public void getOriginOfValueWhenDefinedInContextAndHasReplacedValueFromParentWithoutTypeForRelationShouldReturnResourceRelationNameAndParentContextName(){
        // given
        Integer contextId = 1;
        String relationIdentifier = "relationIdentifier";

        String resourceName = "resourceName";
        String masterResourceTypeName = "masterResourceTypeName";
        String typeContName = "typeContName";
        String value = "value";
        String parentResourceValue = "parentResourceValue";
        String parentPropertyContextName = "parentPropertyContextName";
        Integer propertyContextId = contextId;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.RELATION;
        ResourceEditProperty.Origin loadedFor = origin;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).withResourceName(resourceName).withLoadedFor(loadedFor).withOrigin(origin).withPropContextId(propertyContextId).withMasterResTypeName(masterResourceTypeName).withPropTypeContName(typeContName).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        resourceEditProperty.setParent(new ResourceEditPropertyBuilder().withPropContName(parentPropertyContextName).withValue(parentResourceValue).build());

        // when
        String originOfValue = resourceEditProperty.getOriginOfValue(contextId, relationIdentifier);

        // then
        assertEquals("resource relation \"relationIdentifier\" (parentPropertyContextName)", originOfValue);

    }

    @Test
    public void getOriginOfValueWhenDefinedInContextAndHasReplacedValueFromParentWithTypeForInstanceShouldReturnResourceTypeOfParentAndParentTypeContextName(){
        // given
        Integer contextId = 1;
        String relationIdentifier = "relationIdentifier";

        String resourceName = "resourceName";
        String masterResourceTypeName = "masterResourceTypeName";
        String typeContName = "typeContName";
        String value = "value";
        String parentResourceValue = "parentResourceValue";
        String parentPropertyContextName = "parentPropertyContextName";
        String parentMasterResourceTypeName = "parentMasterResourceTypeName";
        String parentTypeContName = "parentTypeContName";
        Integer propertyContextId = contextId;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.INSTANCE;
        ResourceEditProperty.Origin loadedFor = origin;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).withResourceName(resourceName).withLoadedFor(loadedFor).withOrigin(origin).withPropContextId(propertyContextId).withMasterResTypeName(masterResourceTypeName).withPropTypeContName(typeContName).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        resourceEditProperty.setParent(new ResourceEditPropertyBuilder().withPropTypeContName(parentTypeContName).withMasterResTypeName(parentMasterResourceTypeName).withPropContName(parentPropertyContextName).withValue(parentResourceValue).build());

        // when
        String originOfValue = resourceEditProperty.getOriginOfValue(contextId, relationIdentifier);

        // then
        assertEquals("resource type \"parentMasterResourceTypeName\" (parentTypeContName)", originOfValue);
    }

    @Test
    public void getOriginOfValueWhenDefinedInContextAndHasReplacedValueFromParentWithTypeForRelationShouldReturnResourceTypeRelationOfParentAndParentTypeContextName(){
        // given
        Integer contextId = 1;
        String relationIdentifier = "relationIdentifier";

        String resourceName = "resourceName";
        String masterResourceTypeName = "masterResourceTypeName";
        String typeContName = "typeContName";
        String value = "value";
        String parentResourceValue = "parentResourceValue";
        String parentPropertyContextName = "parentPropertyContextName";
        String parentMasterResourceTypeName = "parentMasterResourceTypeName";
        String parentTypeContName = "parentTypeContName";
        Integer propertyContextId = contextId;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.RELATION;
        ResourceEditProperty.Origin loadedFor = origin;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).withResourceName(resourceName).withLoadedFor(loadedFor).withOrigin(origin).withPropContextId(propertyContextId).withMasterResTypeName(masterResourceTypeName).withPropTypeContName(typeContName).build();
        resourceEditProperty.setDefinedOnSuperResourceType(false);

        resourceEditProperty.setParent(new ResourceEditPropertyBuilder().withPropTypeContName(parentTypeContName).withMasterResTypeName(parentMasterResourceTypeName).withPropContName(parentPropertyContextName).withValue(parentResourceValue).build());

        // when
        String originOfValue = resourceEditProperty.getOriginOfValue(contextId, relationIdentifier);

        // then
        assertEquals("resource type relation \"parentMasterResourceTypeName\" (parentTypeContName)", originOfValue);
    }


    @Test
    public void getPropertyDisplayNameWhenDisplayNameIsSetAndNotEmptyShouldReturnDisplayName(){
        //given
        String displayName = "notEmptyDisplayName";
        String technicalKey = "technicalKey";
        resourceEditProperty = new ResourceEditPropertyBuilder().withDisplayName(displayName).withTechnicalKey(technicalKey).build();

        //when
        String propertyDisplayName = resourceEditProperty.getPropertyDisplayName();

        //then
        assertEquals(displayName, propertyDisplayName);
    }

    @Test
    public void getPropertyDisplayNameWhenDisplayNameIsSetButEmptyShouldReturnTechnicalKey(){
        //given
        String displayName = "";
        String technicalKey = "technicalKey";
        resourceEditProperty = new ResourceEditPropertyBuilder().withDisplayName(displayName).withTechnicalKey(technicalKey).build();

        //when
        String propertyDisplayName = resourceEditProperty.getPropertyDisplayName();

        //then
        assertEquals(technicalKey, propertyDisplayName);
    }

    @Test
    public void getPropertyDisplayNameWhenDisplayNameIsNotSetShouldReturnTechnicalKey(){
        //given
        String displayName = null;
        String technicalKey = "technicalKey";
        resourceEditProperty = new ResourceEditPropertyBuilder().withDisplayName(displayName).withTechnicalKey(technicalKey).build();

        //when
        String propertyDisplayName = resourceEditProperty.getPropertyDisplayName();

        //then
        assertEquals(technicalKey, propertyDisplayName);
    }


    @Test
    public void getDecryptedPropertyValueWhenIsEncryptedAndValueNotEmptyShouldReturnUndecrypdedString(){
        //given
        String propertyValue = "notEmptyPropertyValue";
        boolean isEncrypted = true;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(propertyValue).withIsEncrypted(isEncrypted).build();

        //when
        String decryptedPropertyValue = resourceEditProperty.getDecryptedPropertyValue();

        //then
        assertEquals(ResourceEditProperty.UNDECRYPTED, decryptedPropertyValue);
    }

    @Test
    public void getDecryptedPropertyValueWhenIsNotEncryptedAndValueNotEmptyShouldReturnPropertyValue(){
        //given
        String propertyValue = "notEmptyPropertyValue";
        boolean isEncrypted = false;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(propertyValue).withIsEncrypted(isEncrypted).build();

        //when
        String decryptedPropertyValue = resourceEditProperty.getDecryptedPropertyValue();

        //then
        assertEquals(propertyValue, decryptedPropertyValue);
    }

    @Test
    public void getDecryptedPropertyValueWhenIsNotEncryptedButValueNullShouldReturnNull(){
        //given
        boolean isEncrypted = false;
        resourceEditProperty = new ResourceEditPropertyBuilder().withIsEncrypted(isEncrypted).build();

        //when
        String decryptedPropertyValue = resourceEditProperty.getDecryptedPropertyValue();

        //then
        assertNull(decryptedPropertyValue);
    }

    @Test
    public void isPropertyValueSetWhenNoValueIsSetShouldReturnFalse(){
        //given
        resourceEditProperty = new ResourceEditPropertyBuilder().build();

        //when
        boolean isPropertyValueSet = resourceEditProperty.isPropertyValueSet();

        //then
        assertFalse(isPropertyValueSet);
    }

    @Test
    public void isPropertyValueSetWhenValueIsSetShouldReturnTrue(){
        //given
        String propertyValue = "notEmptyPropertyValue";
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(propertyValue).build();

        //when
        boolean isPropertyValueSet = resourceEditProperty.isPropertyValueSet();

        //then
        assertTrue(isPropertyValueSet);
    }

    @Test
    public void decryptWhenIsEncryptionIsOffShouldNotEncryptValue(){
        //given
        boolean isEncrypted = false;
        String propertyValue = "propertyValue";
        resourceEditProperty = new ResourceEditPropertyBuilder().withIsEncrypted(isEncrypted).withValue(propertyValue).build();

        //when
         resourceEditProperty.decrypt();

        //then
        assertEquals(propertyValue, resourceEditProperty.getPropertyValue());
    }


    @Test
    public void getValidationLogicShouldReturnValidationLogic(){
        //given
        String validationLogic = "validationLogic";
        resourceEditProperty = new ResourceEditPropertyBuilder().withValidationLogic(validationLogic).build();

        //when
        String resultValidationLogic = resourceEditProperty.getValidationLogic();

        //then
        assertEquals(validationLogic, resultValidationLogic);
    }

    @Test
    public void getValidationLogicShouldReturnDefaultValidationLogicWhenValidationLogicNotSet(){
        //given
        resourceEditProperty = new ResourceEditPropertyBuilder().build();

        //when
        String resultValidationLogic = resourceEditProperty.getValidationLogic();

        //then
        assertEquals(ResourceEditProperty.DEFAULTVALIDATIONEXPRESSION, resultValidationLogic);
    }


    @Test
    public void isNullableValidationErrorWhenPropertyIsNullableShouldReturnFalse(){
        //given
        String value = "value";
        boolean isNullable = true;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).withIsNullable(isNullable).build();

        //when
        boolean isNullableValidationError = resourceEditProperty.isNullableValidationError();

        //then
        assertFalse(isNullableValidationError);
    }

    @Test
    public void isNullableValidationErrorWhenPropertyIsNullableAndValueIsNullShouldReturnFalse(){
        //given
        boolean isNullable = true;
        resourceEditProperty = new ResourceEditPropertyBuilder().withIsNullable(isNullable).build();

        //when
        boolean isNullableValidationError = resourceEditProperty.isNullableValidationError();

        //then
        assertFalse(isNullableValidationError);
    }

    @Test
    public void isNullableValidationErrorWhenPropertyIsNotNullableAndValueIsNullShouldReturnTrue(){
        //given
        boolean isNullable = false;
        resourceEditProperty = new ResourceEditPropertyBuilder().withIsNullable(isNullable).build();

        //when
        boolean isNullableValidationError = resourceEditProperty.isNullableValidationError();

        //then
        assertTrue(isNullableValidationError);
    }

    @Test
    public void isNullableValidationErrorWhenPropertyIsNotNullableAndValueNotNullShouldReturnFalse(){
        //given
        String value = "value";
        boolean isNullable = false;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).withIsNullable(isNullable).build();

        //when
        boolean isNullableValidationError = resourceEditProperty.isNullableValidationError();

        //then
        assertFalse(isNullableValidationError);
    }


    @Test
    public void getClassNameForPropertyInputFieldWhenIsInContextWithoutErrorShouldReturnCurrentContextClass(){
        //given
        Integer contextId = 1;
        boolean isNullable = true;
        ResourceEditProperty.Origin instance = ResourceEditProperty.Origin.INSTANCE;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(instance).withOrigin(instance).withPropContextId(contextId).withIsNullable(isNullable).build();

        //when
        String classNameForPropertyInputField = resourceEditProperty.getClassNameForPropertyInputField(contextId);

        //then
        assertEquals("currentContext", classNameForPropertyInputField);
    }

    @Test
    public void getClassNameForPropertyInputFieldWhenIsNotInContextWithoutErrorShouldReturnUpperContextClass(){
        //given
        Integer contextId = 1;
        boolean isNullable = true;
        ResourceEditProperty.Origin instance = ResourceEditProperty.Origin.INSTANCE;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.RELATION;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(instance).withOrigin(origin).withPropContextId(contextId).withIsNullable(isNullable).build();

        //when
        String classNameForPropertyInputField = resourceEditProperty.getClassNameForPropertyInputField(contextId);

        //then
        assertEquals("upperContext", classNameForPropertyInputField);
    }



    @Test
    public void getClassNameForPropertyInputFieldWhenIsInContextWithNullableValidationErrorShouldReturnCurrentContextAndValidationErrorClass(){
        //given
        Integer contextId = 1;
        boolean isNullable = false;
        ResourceEditProperty.Origin instance = ResourceEditProperty.Origin.INSTANCE;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(instance).withOrigin(instance).withPropContextId(contextId).withIsNullable(isNullable).build();

        //when
        String classNameForPropertyInputField = resourceEditProperty.getClassNameForPropertyInputField(contextId);

        //then
        assertEquals("currentContext fieldNoValueValidationError", classNameForPropertyInputField);
    }

    @Test
    public void getClassNameForPropertyInputFieldWhenIsNotInContextWithNullableValidationErrorShouldReturnUpperContextAndValidationErrorClass(){
        //given
        Integer contextId = 1;
        boolean isNullable = false;
        ResourceEditProperty.Origin instance = ResourceEditProperty.Origin.INSTANCE;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.RELATION;
        resourceEditProperty = new ResourceEditPropertyBuilder().withLoadedFor(instance).withOrigin(origin).withPropContextId(contextId).withIsNullable(isNullable).build();

        //when
        String classNameForPropertyInputField = resourceEditProperty.getClassNameForPropertyInputField(contextId);

        //then
        assertEquals("upperContext fieldNoValueValidationError", classNameForPropertyInputField);
    }

    @Test
    public void getClassNameForPropertyInputFieldWhenIsInContextWithRegexValidationErrorShouldReturnCurrentContextAndValidationErrorClass(){
        //given
        Integer contextId = 1;
        String value = "value";
        boolean isNullable = true;
        ResourceEditProperty.Origin instance = ResourceEditProperty.Origin.INSTANCE;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).withValidationRegex("[A]").withLoadedFor(instance).withOrigin(instance).withPropContextId(contextId).withIsNullable(isNullable).build();

        //when
        String classNameForPropertyInputField = resourceEditProperty.getClassNameForPropertyInputField(contextId);

        //then
        assertEquals("currentContext fieldValidationError", classNameForPropertyInputField);
    }

    @Test
    public void getClassNameForPropertyInputFieldWhenIsNotInContextWithRegexValidationErrorShouldReturnCurrentContextAndValidationErrorClass(){
        //given
        Integer contextId = 1;
        String value = "value";
        boolean isNullable = true;
        ResourceEditProperty.Origin instance = ResourceEditProperty.Origin.INSTANCE;
        ResourceEditProperty.Origin origin = ResourceEditProperty.Origin.RELATION;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).withValidationRegex("[A]").withLoadedFor(instance).withOrigin(origin).withPropContextId(contextId).withIsNullable(isNullable).build();

        //when
        String classNameForPropertyInputField = resourceEditProperty.getClassNameForPropertyInputField(contextId);

        //then
        assertEquals("upperContext fieldValidationError", classNameForPropertyInputField);
    }


    @Test
    public void hasChangedWhenPropertyIsUnchangedShouldReturnFalse(){
        //given
        String value = "value";

        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).build();

        //when
        boolean hasChanged = resourceEditProperty.hasChanged();

        //then
        assertFalse(hasChanged);
    }

    @Test
    public void hasChangedWhenPropertyValueChangedShouldReturnTrue(){
        //given
        String value = "value";

        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).build();
        resourceEditProperty.setDecryptedPropertyValue("otherValue");

        //when
        boolean hasChanged = resourceEditProperty.hasChanged();

        //then
        assertTrue(hasChanged);
    }

    @Test
    public void hasChangedWhenPropertyCommentChangedShouldReturnTrue(){
        //given
        String value = "value";

        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).build();
        resourceEditProperty.setPropertyComment("otherComment");

        //when
        boolean hasChanged = resourceEditProperty.hasChanged();

        //then
        assertTrue(hasChanged);
    }

    @Test
    public void hasChangedWhenPropertyCommentChangedButValueIsUndecryptedShouldReturnFalse(){
        //given
        String decryptedValue = ResourceEditProperty.UNDECRYPTED;

        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(decryptedValue).build();
        resourceEditProperty.setPropertyComment("otherComment");

        //when
        boolean hasChanged = resourceEditProperty.hasChanged();

        //then
        assertFalse(hasChanged);
    }

    @Test
    public void hasChangedWhenPropertyValueChangedToUndecryptedShouldReturnFalse(){
        //given
        String decryptedValue = ResourceEditProperty.UNDECRYPTED;

        resourceEditProperty = new ResourceEditPropertyBuilder().withValue("originalValue").build();
        resourceEditProperty.setDecryptedPropertyValue(decryptedValue);

        //when
        boolean hasChanged = resourceEditProperty.hasChanged();

        //then
        assertFalse(hasChanged);
    }

    @Test
    public void hasChangedWhenPropertyCommentChangedButResetShouldReturnFalse(){
        //given
        String value = "value";

        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(value).build();
        resourceEditProperty.setPropertyComment("otherComment");
        resourceEditProperty.setReset(true);

        //when
        boolean hasChanged = resourceEditProperty.hasChanged();

        //then
        assertFalse(hasChanged);
    }

    @Test
    public void compareToWhenCardinalityAndTechnicalKeysAreNullShouldReturnEqual(){
        // given
        ResourceEditProperty other = new ResourceEditPropertyBuilder().build();
        resourceEditProperty = new ResourceEditPropertyBuilder().build();

        // when
        int compareTo = resourceEditProperty.compareTo(other);

        // then
        assertEquals(0, compareTo);
    }

    @Test
    public void compareToWhenOtherCardinalityIsSetShouldReturnMore(){
        // given
        int cardinality = 10;
        ResourceEditProperty other = new ResourceEditPropertyBuilder().withCardinality(cardinality).build();
        resourceEditProperty = new ResourceEditPropertyBuilder().build();

        // when
        int compareTo = resourceEditProperty.compareTo(other);

        // then
        assertEquals(Integer.MAX_VALUE-cardinality, compareTo);
    }

    @Test
    public void compareToWhenThisCardinalityIsSetShouldReturnLower(){
        // given
        int cardinality = 10;
        ResourceEditProperty other = new ResourceEditPropertyBuilder().build();
        resourceEditProperty = new ResourceEditPropertyBuilder().withCardinality(cardinality).build();

        // when
        int compareTo = resourceEditProperty.compareTo(other);

        // then
        assertEquals(cardinality-Integer.MAX_VALUE, compareTo);
    }

    @Test
    public void compareToWhenOtherTechnicalKeyIsSetShouldReturnEqual(){
        // given
        String technicalKey = "technicalKey";
        ResourceEditProperty other = new ResourceEditPropertyBuilder().withTechnicalKey(technicalKey).build();
        resourceEditProperty = new ResourceEditPropertyBuilder().build();

        // when
        int compareTo = resourceEditProperty.compareTo(other);

        // then
        assertEquals(0, compareTo);
    }

    @Test
    public void compareToWhenThisTechnicalKeyIsSetShouldReturnEqual(){
        // given
        String technicalKey = "technicalKey";
        ResourceEditProperty other = new ResourceEditPropertyBuilder().build();
        resourceEditProperty = new ResourceEditPropertyBuilder().withTechnicalKey(technicalKey).build();

        // when
        int compareTo = resourceEditProperty.compareTo(other);

        // then
        assertEquals(0, compareTo);
    }

    @Test
    public void compareToWhenTechnicalKeysAreSetDifferentlyIsSetShouldReturnNotEqual(){
        // given
        ResourceEditProperty other = new ResourceEditPropertyBuilder().withTechnicalKey("otherTechnicalKey").build();
        resourceEditProperty = new ResourceEditPropertyBuilder().withTechnicalKey("thisTechnicalKey").build();

        // when
        int compareTo = resourceEditProperty.compareTo(other);

        // then
        assertTrue(compareTo != 0);
    }

    @Test
    public void setPropertyValueWithValidValueShouldSetNewValue(){
        // given
        String originalValue = "originalValue";
        String newValue = "newValue";
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(originalValue).withIsEncrypted(false).build();

        // when
        resourceEditProperty.setPropertyValue(newValue);

        // then
        assertEquals(newValue, resourceEditProperty.getPropertyValue());
    }

    @Test
    public void setPropertyValueWithUndecryptedValueShouldNotSetNewValue(){
        // given
        String originalValue = "originalValue";
        String newValue = ResourceEditProperty.UNDECRYPTED;
        resourceEditProperty = new ResourceEditPropertyBuilder().withValue(originalValue).withIsEncrypted(false).build();

        // when
        resourceEditProperty.setPropertyValue(newValue);

        // then
        assertEquals(originalValue , resourceEditProperty.getPropertyValue());
    }


    // TODO default und example value erfassen

}
