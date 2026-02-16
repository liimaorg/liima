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

import ch.puzzle.itc.mobiliar.business.generator.control.TemplateUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;


/**
 * Read only entity for the resource edit screen to display properties
 * All fields except @propertyDescriptorOrigin references to property values or origin of property value
 * 
 * @author cweber
 */
public class ResourceEditProperty implements Comparable<ResourceEditProperty> {

    public enum Origin {
        INSTANCE, RELATION, TYPE, TYPE_REL
    }

    @Getter
    @Setter
    private String technicalKey;

    private String propertyValue;

    @Getter
    @Setter
    private String displayName;

    @Setter
    private String propertyComment;
    @Getter
    @Setter
    private boolean isNullable;
    @Getter
    @Setter
    private boolean isOptional;
    @Getter
    @Setter
    private boolean isEncrypted;

    /**
     * Kardinalität uf de Properties definiert, öb das Systemeigeni Properties si. Das bedütet: We dert öppis anders als NULL definiert isch (wie z.B. Maven-Properties) de het das e spezielli bedütig. Im Bispilfall dass si über e Deployscreen chöi überschribe wärde. D Kardinalität het uswirkige druf i welere Reihefolg si ufem Editscreen dargstellt wärde u öb si vom Benutzer chöi glöscht wärde oder nid (hei si e kardinalität chöi si nid glöscht wärde).
     */
    @Getter
    @Setter
    private Integer cardinalityProperty;
    @Setter
    private String validationLogic;
    private String typeValidationRegex;
    @Getter
    private String mik;
    @Getter
    @Setter
    private Integer propContextId;
    @Getter
    @Setter
    private Integer typeContextId;
    @Getter
    @Setter
    private Integer descriptorId;
    @Getter
    @Setter
    private String propContName;
    @Getter
    @Setter
    private String typeContName;
    @Getter
    @Setter
    private String typeName;
    @Getter
    private Integer typeId;
    @Getter
    private Integer propertyValueTypeId;
    @Getter
    private Integer masterTypeId;

    @Getter
    private Integer propertyId;
    @Getter
    private Origin origin;
    @Getter
    private String originalValue;
    private String originalPropertyComment; // from descriptor
    @Getter
    @Setter
    private boolean reset;
    private String resourceName;

    @Getter
    /** returns true if propertyValue isEncrypted == true AND has already been decrypted **/
    private boolean decrypted;

    @Getter
    private String exampleValue;

    @Getter
    private String defaultValue;

    @Getter
    private Origin propertyDescriptorOrigin;

    @Getter
    @Setter
    private ResourceEditProperty parent;

    protected static final String DEFAULTVALIDATIONEXPRESSION = ".*";
    public static final String UNDECRYPTED = "*******";

    public String getPropertyDisplayName() {
        if (this.displayName == null || this.displayName.isEmpty()) {
            return this.technicalKey;
        }
        return displayName;
    }
    public String getPropertyDefaultValue(){
        return defaultValue;
    }

    public String getPropertyValue() {
        return isEncrypted && propertyValue != null && !propertyValue.isEmpty() ? UNDECRYPTED
                : propertyValue;
    }

    public String getDecryptedPropertyValue() {
        return isEncrypted && !decrypted && propertyValue != null && !propertyValue.isEmpty() ? UNDECRYPTED
                : propertyValue;
    }

    public boolean isPropertyValueSet() {
        return propertyValue != null && !propertyValue.isEmpty();
    }

    /**
     * @return the unobfuscated value - if the value is encrypted, this method returns the encrypted string
     *         instead of "****"
     */
    public String getUnobfuscatedValue() {
        return decrypted ? TemplateUtils.encrypt(propertyValue) : propertyValue;
    }

    /**
     * Defines if the property is loaded for a resource or a relation
     */
    @Getter
    private Origin loadedFor;



    /**
     * Constructor must have the same number of arguments as the result of the SQL query and the result types
     * must match. (see loadPropertyDescriptorsForResource.sql / loadPropertyDescriptorsForResourceType.sql)
     *
     * @param technicalKey technicalKey of the property (from {@link PropertyDescriptorEntity#propertyName})
     * @param displayName the display name of the property (from {@link PropertyDescriptorEntity#displayName})
     * @param propertyValue the value in the given context (from {@link PropertyEntity#value})
     * @param exampleValue the example name of the property (from {@link PropertyDescriptorEntity#exampleValue})
     * @param defaultValue the default name of the property (from {@link PropertyDescriptorEntity#defaultValue})
     * @param propertyComment the comment defined for the property description (from {@link PropertyDescriptorEntity#propertyComment})
     * @param isNullable if the property is nullable (from {@link PropertyDescriptorEntity#isNullable()})
     * @param isOptional if the property is optional (from {@link PropertyDescriptorEntity#isOptional()})
     * @param isEncrypted if the property is encrypted (from {@link PropertyDescriptorEntity#isEncrypt()})
     * @param cardinalityProperty the cardinality of the property (from {@link PropertyDescriptorEntity#cardinalityProperty})
     * @param validationLogic the validation logic a property has to fulfill (if any) (from {@link PropertyDescriptorEntity#validationLogic})
     * @param machineInterpretationKey the machine interpretation key, if present, is used to compute the actual value of a property (@ generation time) (from {@link PropertyDescriptorEntity#machineInterpretationKey})
     * @param propContextId the id of the context where the property is defined or null if defined on a resource type (from {@link ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity#name#id})
     * @param typeContextId the id of the type-context where the property is defined or null if defined on a resource (from {@link ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity#name#id })
     * @param descriptorId the id of the property descriptor (from {@link PropertyDescriptorEntity#id})
     * @param propContName the name of the context where the property is defined (from {@link ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity#name})
     * @param typeContName the name of the type-context where the property is defined (from {@link ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity#name})
     * @param typeId the id of the resource type on which the propertydescriptor is defined (from {@link ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity#id})
     * @param propertyValueTypeId the id of the resource type on which the propertyValue is defined (from {@link ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity#id})
     * @param masterTypeId
     * @param typeName the name of the resource type on which the property is defined or null if the property is defined on the resource (from {@link ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity#name})
     * @param validationRegex the validation regex if any (from {@link PropertyTypeEntity#validationRegex})
     * @param propertyId the id of the property value (from {@link PropertyEntity#id})
     * @param origin constant to define if the property is set ({@link ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty.Origin})
     * @param loadedFor constant to define if the result is loaded for ({@link ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty.Origin})
     * @param resourceName
     * @param propertyDescriptorOrigin constant to define if the property descriptors origin is defined on  resource context -> 'instance' or resource type -> 'type' ({@link ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty.Origin})
     */
    public ResourceEditProperty(String technicalKey, String displayName, String propertyValue, String exampleValue, String defaultValue,
                                String propertyComment, boolean isNullable, boolean isOptional, boolean isEncrypted, Integer cardinalityProperty,
            String validationLogic, String machineInterpretationKey, Integer propContextId, Integer typeContextId,
            Integer descriptorId, String propContName, String typeContName, Integer typeId, Integer propertyValueTypeId,
            Integer masterTypeId, String typeName, String validationRegex, Integer propertyId,
            String origin, String loadedFor, String resourceName, String propertyDescriptorOrigin) {
        this.technicalKey = technicalKey;
        this.displayName = displayName;
        this.propertyValue = propertyValue;
        this.propertyComment = propertyComment;
        this.exampleValue = exampleValue;
        this.defaultValue = defaultValue;

        /*try {
            this.propertyValue = QueryUtils.clobToString(propertyValue);
            this.propertyComment = QueryUtils.clobToString(propertyComment);
            this.exampleValue = QueryUtils.clobToString(exampleValue);
            this.defaultValue = QueryUtils.clobToString(defaultValue);
        }
        catch (Exception e) {
            // do nothing
        }*/
        this.originalValue = this.propertyValue;
        this.originalPropertyComment = this.propertyComment;
        this.isNullable = isNullable;
        this.isOptional = isOptional;
        this.isEncrypted = isEncrypted;
        this.cardinalityProperty = cardinalityProperty;
        this.validationLogic = validationLogic;
        this.mik = machineInterpretationKey;
        this.propContextId = propContextId;
        this.typeContextId = typeContextId;
        this.descriptorId = descriptorId;
        this.propContName = propContName;
        this.typeContName = typeContName;
        this.typeId = typeId;
        this.propertyValueTypeId = propertyValueTypeId;
        this.masterTypeId = masterTypeId;
        this.typeName = typeName;
        this.typeValidationRegex = validationRegex;
        this.propertyId = propertyId;
        this.origin = origin != null ? Origin.valueOf(origin.toUpperCase()) : null;
        this.loadedFor = loadedFor != null ? Origin.valueOf(loadedFor.toUpperCase()) : null;
        this.resourceName = resourceName;
        this.reset = false;
        this.propertyDescriptorOrigin = propertyDescriptorOrigin != null ? Origin.valueOf(propertyDescriptorOrigin.toUpperCase()) : null;
    }

    /**
     * Attention! This is a security sensitive method since it decrypts previously encrypted passwords...
     * Don't use it if you don't know exactly what you're doing!
     */
    public void decrypt() {
        if (isEncrypted) {
            if (propertyValue != null) {
                propertyValue = TemplateUtils.decrypt(propertyValue);
            }
            if (originalValue != null) {
                originalValue = TemplateUtils.decrypt(originalValue);
            }
            this.decrypted = true;
        }
    }

    public void setDecryptedPropertyValue(String value) {
        propertyValue = value;
    }

    public void setDecryptedOriginalValue(String value) {
        originalValue = value;
    }

    public void setPropertyValue(String value) {
        String newValue = value != null ? value.trim() : value;
        // We don't set the value if it matches the undecrypted value
        if (StringUtils.isNotEmpty(newValue) && !UNDECRYPTED.equals(value)) {
            if (isEncrypted && !decrypted) {
                newValue = TemplateUtils.encrypt(newValue);
            }
            propertyValue = newValue;
        }
    }


    public String getOriginOfValue(Integer contextId, String relationIdentifier) {
        if (!isDefinedInContext(contextId)) {
            if (getTypeName() != null) {
                return getResourceDescriptionForContextOwner(getTypeName(), relationIdentifier) + " ("
                        + getTypeContName() + ")";
            }
            else if (getPropContName() != null) {
                return getResourceDescriptionForContextOwner(null, relationIdentifier) + " ("
                        + getPropContName() + ")";
            }
        }
        else if (getReplacedValue() != null) {
            if (parent.getTypeName() != null) {
                return getResourceDescriptionForContextOwner(parent.getTypeName(), relationIdentifier)
                        + " (" + parent.getTypeContName() + ")";
            }
            else {
                return getResourceDescriptionForContextOwner(null, relationIdentifier) + " ("
                        + parent.getPropContName() + ")";
            }
        }
        return null;
    }

    private String getResourceDescriptionForContextOwner(String typeName, String relationIdentifier) {
        switch (origin) {
        case INSTANCE:
        case TYPE:
            if (typeName != null) {
                return "resource type \"" + typeName + "\"";
            }
            return "resource \"" + resourceName + "\"";
        case RELATION:
        case TYPE_REL:
            if (typeName != null) {
                return "resource type relation \"" + typeName + "\"";
            }
            return "resource relation \"" + relationIdentifier + "\"";
        default:
            return null;
        }
    }

    public String getReplacedValue() {
        if (propertyValue != null && parent != null && parent.getPropertyValue() != null) {
            return isEncrypted && !decrypted ? UNDECRYPTED : parent.getPropertyValue();
        }
        return null;
    }

    public boolean isDefinedInContext(Integer contextId) {
        if (definedOnSuperResourceType) {
            return false;
        }
        switch (loadedFor) {
        case INSTANCE:
        case RELATION:
            return this.propContextId != null && this.propContextId.equals(contextId)
                    && origin == loadedFor;
        case TYPE:
        case TYPE_REL:
            return this.typeContextId != null && this.typeContextId.equals(contextId)
                    && origin == loadedFor;

        }
        return false;
    }

    public String getValidationLogic() {
        if (typeValidationRegex != null) {
            // wert aus dem propertytype
            return typeValidationRegex;
        }
        return validationLogic == null || validationLogic.trim().isEmpty() ? DEFAULTVALIDATIONEXPRESSION
                : validationLogic;
    }

    public String getClassNameForPropertyInputField(Integer contextId) {
        String result = isDefinedInContext(contextId) ? "currentContext" : "upperContext";

        // validate Value (always ok if mik or default value is present)
        if(!isNullable && !isOptional && noValueSet() && StringUtils.isEmpty(getMik()) && StringUtils.isEmpty(getDefaultValue())) {
            result += " fieldNoValueValidationError";
        }
        // validate Regex
        //if(((isNullable || isOptional) && noValueSet() && StringUtils.isEmpty(getDefaultValue())) || !StringUtils.isEmpty(getMik())){
        if((isNullable || isOptional) && noValueSet() && StringUtils.isEmpty(getDefaultValue()) && !StringUtils.isEmpty(getMik())){
            // regex is not validated
        }else{
            if(hasRegexValidationErrors()){
                result += " fieldValidationError";
            }
        }
        // add mik-style
        if (!StringUtils.isEmpty(getMik())) {
            result += " dynaProp";
        }

        return result;
    }

    private boolean hasRegexValidationErrors() {
        String value = getPropertyValue();
        if(StringUtils.isEmpty(getPropertyValue())){
            value = getDefaultValue();
        }

        try {
            return value != null && !value.matches(getValidationLogic());
        }
        catch (Exception e) {
            // TODO: SEHR WICHTIG!!!!!! Entweder anzeigen, dass ein validationfehler passiert ist
            // oder regexp bei der Erfassung auf gültigkeit prüfen!!!
            return true;
        }
    }

    private boolean noValueSet() {
        return StringUtils.isEmpty(getPropertyValue()) && StringUtils.isEmpty(getDefaultValue());
    }

    public boolean isNullableValidationError() {
        return (StringUtils.isEmpty(getPropertyValue()) && StringUtils.isEmpty(getDefaultValue()) && !isNullable());
    }

    public boolean hasChanged() {
        boolean isDecrypted = propertyValue != null && propertyValue.equals(UNDECRYPTED);
        boolean isEqualToOriginalValue = (originalValue == null && propertyValue == null)
                || (originalValue != null && originalValue.equals(propertyValue));
        boolean isEqualPropertyComment = (originalPropertyComment == null && propertyComment == null)
                || (originalPropertyComment != null && originalPropertyComment.equals(propertyComment));

        if (isDecrypted || isReset()) {
            return false;
        }
        else {
            return !isEqualToOriginalValue || !isEqualPropertyComment;
        }
    }

    @Override
    public int compareTo(ResourceEditProperty o) {

        Integer cardinality = o.getCardinalityProperty();
        if (cardinality == null) {
            cardinality = Integer.MAX_VALUE;
        }
        Integer myCardinality = getCardinalityProperty();
        if (myCardinality == null) {
            myCardinality = Integer.MAX_VALUE;
        }

        if (!myCardinality.equals(cardinality)) {
            return myCardinality - cardinality;
        }

        // compare displayed name such that the displayed list is sorted
        if (o.getPropertyDisplayName() != null && getPropertyDisplayName() != null) {
            return getPropertyDisplayName().compareToIgnoreCase(o.getPropertyDisplayName());
        }

        return 0;
    }

    @Override
    public String toString() {
        return "ResourceEditProperty [displayName=" + displayName + ", technicalKey=" + technicalKey
                + ", propertyValue=" + propertyValue + (typeName != null ? " type=" + typeName : "")
                + "]";
    }

    public String getPropertyComment() {
        return propertyComment != null && propertyComment.trim().isEmpty() ? null : propertyComment;
    }

    public boolean isRelationProperty() {
        return loadedFor != null && loadedFor == Origin.RELATION || loadedFor == Origin.TYPE_REL;
    }

    /**
     * @return true if the property value is defined on an instance, false if it is defined on a type
     */
    public boolean isDefinedOnInstance() {
        return getPropContextId() != null;
    }

    /**
     * @return the context id. If the property is defined on the instance, this method returns the
     *         {@link #propContextId}, otherwise {@link #typeContextId}.
     */
    public int getTypeOrInstanceContextId() {
        return isDefinedOnInstance() ? getPropContextId() : getTypeContextId();
    }

    /**
     * Allows the loading context to evaluate (based on the typeId), if the propertyvalue is actually defined on
     * this type or on one of its super types
     */
    @Setter
    @Getter
    private boolean definedOnSuperResourceType;

    /**
     * Allows the loading context to evaluate (based on the typeId), if the propertydexcriptor is actually defined on
     * this type or on one of its super types
     */
    @Setter
    @Getter
    private boolean descriptorDefinedOnSuperResourceType;

}
