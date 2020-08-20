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

package ch.puzzle.itc.mobiliar.builders;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;

public class PropertyDescriptorEntityBuilder extends BaseEntityBuilder {
	private final static String VALIDATION_LOGIC = "^true$|^TRUE$|^false$|^FALSE$";
	private final static String COMMENT = "lorem ipsum dolor sit amet";

	private Integer id;
	private boolean encrypt;
	private String propertyName;
	private boolean nullable;
	private boolean testing;
	private String validationLogic;
	private String propertyComment;
	private Integer cardinalityProperty;
	private PropertyTypeEntity propertyTypeEntity;
	private Set<PropertyEntity> properties = new HashSet<>();
	private String defaultValue;
	private String exampleValue;
	private String machineInterpretationKey;
	private boolean optional;
	private List<PropertyTagEntity> propertyTags;
	private String displayName;
    private String fcExternalLink;
    private String fcExternalKey;

    private ForeignableOwner owner = ForeignableOwner.getSystemOwner(); // default value is amw

	/**
	 * @param properties
	 *             - optional, default empty set
	 * @param name
	 *             - optional, default name will be created
	 * @param comment
	 *             - optional, default {@value #COMMENT}
     *
     * @param encrypted
     * @param nullable
     * @param readOnly
     * @param testing
     * @param propType
     * @param defaultValue
     * @param exampleValue
     * @param machineInterpretationKey
     * @param optional

	 * @return
	 */
	public PropertyDescriptorEntity mockPropertyDescriptorEntity(Set<PropertyEntity> properties, String name, String comment, boolean encrypted, boolean nullable,
			boolean readOnly, boolean testing, PropertyTypeEntity propType, String defaultValue, String exampleValue, String machineInterpretationKey, boolean optional, String displayName) {
		PropertyDescriptorEntity mock = Mockito.mock(PropertyDescriptorEntity.class);
		when(mock.getId()).thenReturn(1);
		when(mock.getProperties()).thenReturn(properties != null ? properties : new HashSet<PropertyEntity>());
		int nextId = getNextId();
		when(mock.getId()).thenReturn(nextId);
		when(mock.getPropertyName()).thenReturn(!StringUtils.isEmpty(name) ? name : "propDesc" + nextId);
		when(mock.getPropertyComment()).thenReturn(!StringUtils.isEmpty(comment) ? comment : COMMENT);
		when(mock.isEncrypt()).thenReturn(encrypted);
		when(mock.isNullable()).thenReturn(nullable);
		when(mock.isTesting()).thenReturn(testing);
		when(mock.getValidationLogic()).thenReturn(VALIDATION_LOGIC);
		when(mock.getPropertyTypeEntity()).thenReturn(propType);
        when(mock.getDefaultValue()).thenReturn(defaultValue);
        when(mock.getExampleValue()).thenReturn(exampleValue);
        when(mock.getMachineInterpretationKey()).thenReturn(machineInterpretationKey);
        when(mock.isOptional()).thenReturn(optional);
        when(mock.getDisplayName()).thenReturn(displayName);
        when(mock.getOwner()).thenReturn(owner);


		return mock;
	}

	/**
	 * @param properties
	 *             - optional, default empty set
	 * @param name
	 *             - optional, default name will be created
	 * @param comment
	 *             - optional, default {@value #COMMENT}
	 * @param encrypted
	 * @param nullable
	 * @param readOnly
	 * @param testing
     * @param propType
     * @param defaultValue
     * @param exampleValue
     * @param machineInterpretationKey
     * @param optional
     *
	 * @return
	 */
	public PropertyDescriptorEntity buildPropertyDescriptorEntity(Set<PropertyEntity> properties, String name, String comment, boolean encrypted, boolean nullable,
			boolean readOnly, boolean testing, PropertyTypeEntity propType, String defaultValue, String exampleValue, String machineInterpretationKey, boolean optional, String displayName) {
		PropertyDescriptorEntity propertyDescriptorEntity = new PropertyDescriptorEntity();
		propertyDescriptorEntity.setProperties(properties);
		int nextId = getNextId();
		propertyDescriptorEntity.setId(nextId);
		propertyDescriptorEntity.setPropertyName(!StringUtils.isEmpty(name) ? name : "propDesc" + nextId);
		propertyDescriptorEntity.setPropertyComment(!StringUtils.isEmpty(comment) ? comment : COMMENT);
		propertyDescriptorEntity.setEncrypt(encrypted);
		propertyDescriptorEntity.setNullable(nullable);
		propertyDescriptorEntity.setTesting(testing);
		propertyDescriptorEntity.setValidationLogic(VALIDATION_LOGIC);
		propertyDescriptorEntity.setPropertyTypeEntity(propType);
        propertyDescriptorEntity.setDefaultValue(defaultValue);
        propertyDescriptorEntity.setExampleValue(exampleValue);
        propertyDescriptorEntity.setMachineInterpretationKey(machineInterpretationKey);
        propertyDescriptorEntity.setOptional(optional);
        propertyDescriptorEntity.setDisplayName(displayName);

		return propertyDescriptorEntity;
	}

	public PropertyDescriptorEntity build(){
		PropertyDescriptorEntity entity = new PropertyDescriptorEntity();

		entity.setId(this.id);
		entity.setPropertyName(!StringUtils.isEmpty(this.propertyName) ? this.propertyName : "propDesc" + this.id);
		entity.setPropertyComment(!StringUtils.isEmpty(this.propertyComment) ? propertyComment : COMMENT);
		entity.setEncrypt(this.encrypt);
		entity.setNullable(this.nullable);
		entity.setTesting(this.testing);
		entity.setValidationLogic(!StringUtils.isEmpty(this.validationLogic) ? this.validationLogic : VALIDATION_LOGIC);
		entity.setPropertyTypeEntity(this.propertyTypeEntity);
		entity.setDefaultValue(this.defaultValue);
		entity.setExampleValue(this.exampleValue);
		entity.setMachineInterpretationKey(this.machineInterpretationKey);
		entity.setOptional(this.optional);
		entity.setDisplayName(this.displayName);
		entity.setPropertyTags(this.propertyTags);
		entity.setCardinalityProperty(this.cardinalityProperty);
		if(this.properties != null) {
			for (PropertyEntity property : this.properties) {
				property.setDescriptor(entity);
			}
		}
		entity.setProperties(this.properties);
        entity.setOwner(owner);
        entity.setExternalKey(fcExternalKey);
        entity.setExternalLink(fcExternalLink);

		return entity;
	}

	public PropertyDescriptorEntityBuilder withId(int id){
		this.id = id;
		return this;
	}

	public PropertyDescriptorEntityBuilder withGeneratedId(){
		this.id = getNextId();
		return this;
	}

	public PropertyDescriptorEntityBuilder withPropertyName(String propertyName){
		this.propertyName = propertyName;
		return this;
	}

	public PropertyDescriptorEntityBuilder withProperties(Set<PropertyEntity> properties){
		this.properties = properties;
		return this;
	}

	public PropertyDescriptorEntityBuilder withPropertyComment(String comment){
		this.propertyComment = comment;
		return this;
	}

    public PropertyDescriptorEntityBuilder withCardinalityProperty(Integer cardinalityProperty){
        this.cardinalityProperty = cardinalityProperty;
        return this;
    }

	public PropertyDescriptorEntityBuilder withDefaultValue(String defaultValue){
		this.defaultValue = defaultValue;
		return this;
	}

	public PropertyDescriptorEntityBuilder withExampleValue(String exampleValue){
		this.exampleValue = exampleValue;
		return this;
	}

	public PropertyDescriptorEntityBuilder withDisplayName(String displayName){
		this.displayName = displayName;
		return this;
	}

    public PropertyDescriptorEntityBuilder withOwner(ForeignableOwner owner){
        this.owner = owner;
        return this;
    }

	public PropertyDescriptorEntityBuilder withPropertyType(PropertyTypeEntity type){
		this.propertyTypeEntity = type;
		return this;
	}

	public PropertyDescriptorEntityBuilder withMik(String mik){
		this.machineInterpretationKey = mik;
		return this;
	}

    public PropertyDescriptorEntityBuilder withFcExternalLink(String fcExternalLink){
        this.fcExternalLink = fcExternalLink;
        return this;
    }

    public PropertyDescriptorEntityBuilder withValidationLogic(String validationLogic){
        this.validationLogic = validationLogic;
        return this;
    }

    public PropertyDescriptorEntityBuilder withFcExternalKey(String fcExternalKey){
        this.fcExternalKey = fcExternalKey;
        return this;
    }

    public PropertyDescriptorEntityBuilder isOptional(boolean isOptional){
        this.optional = isOptional;
        return this;
    }

    public PropertyDescriptorEntityBuilder isEncrypted(boolean encrypt){
        this.encrypt = encrypt;
        return this;
    }

    public PropertyDescriptorEntityBuilder isNullable(boolean nullable){
        this.nullable = nullable;
        return this;
    }

    public PropertyDescriptorEntityBuilder isTesting(boolean testing){
        this.testing = testing;
        return this;
    }

	public PropertyDescriptorEntityBuilder withTags(PropertyTagEntity... tags){
		propertyTags = new ArrayList<>();
		for (PropertyTagEntity tag : tags) {
			propertyTags.add(tag);
		}
		return this;
	}

	public PropertyTypeEntity mockPropertyTypeEntity(String typeName){
		PropertyTypeEntity mock = Mockito.mock(PropertyTypeEntity.class);
		when(mock.getId()).thenReturn(getNextId());
		when(mock.getPropertyTypeName()).thenReturn(typeName);
		return mock;
	}

	public PropertyTypeEntity buildPropertyTypeEntity(String typeName) {
		PropertyTypeEntity propType = new PropertyTypeEntity();
		propType.setId(getNextId());
		propType.setPropertyTypeName(typeName);
		return propType;
	}
}
