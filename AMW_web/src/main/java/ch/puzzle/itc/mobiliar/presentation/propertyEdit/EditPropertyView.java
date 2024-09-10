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

package ch.puzzle.itc.mobiliar.presentation.propertyEdit;

import ch.puzzle.itc.mobiliar.business.foreignable.boundary.ForeignableBoundary;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableAttributesDTO;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyTagEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.PropertyDescriptorNotDeletableException;
import ch.puzzle.itc.mobiliar.presentation.ViewBackingBean;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import ch.puzzle.itc.mobiliar.presentation.util.TestingMode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;

@ViewBackingBean
public class EditPropertyView implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	PropertyEditor propertyEditor;

    @Inject
    PropertyTagEditor propertyTagEditor;

	@Inject
    PermissionBoundary permissionBoundary;

	@Inject
	ForeignableBoundary foreignableBoundary;

	// next two values are only needed for the back button
	@Getter
	@Setter
	private Integer relationId;

	@Getter
	private Integer resourceTypeIdFromParam;

	@Getter
	private Integer resourceIdFromParam;

	private Integer propertyDescriptorId;

	private PropertyDescriptorEntity propertyDescriptor;

    @Getter
    List<PropertyTypeEntity> propertyTypes;

	private PropertyTypeEntity customPropertyType;

	private static final String CUSTOMPROPERTYTYPE_NAME = "Custom";

	private boolean canEditProperties;

	private boolean canDecryptProperties;

    private List<PropertyTagEntity> globalPropertyTags;

    private int propertyDescriptorHashBeforeModification = 0;

	@Setter
	private String propertyTagsString;

	@Inject
	@TestingMode
	private Boolean testing;

	@Getter
	private boolean showForce;

	@TestingMode
	public void onChangedTestingMode(@Observes Boolean isTesting) {
		this.testing = isTesting;
	}

	@PostConstruct
	public void init() {
		customPropertyType = new PropertyTypeEntity();
		customPropertyType.setPropertyTypeName(CUSTOMPROPERTYTYPE_NAME);
		customPropertyType.setId(0);
		propertyTypes = propertyEditor.getPropertyTypes();
        globalPropertyTags = propertyTagEditor.getAllGlobalPropertyTags();
		propertyTypes.add(0, customPropertyType);
	}

	public void setResourceTypeIdFromParam(Integer resourceTypeIdFromParam) {
		this.resourceTypeIdFromParam = resourceTypeIdFromParam;
		this.resourceIdFromParam = null;
		// no context - check already done by PropertyEditDataProvider (onContext/ResourceChanged) => editableProperties
		canEditProperties = permissionBoundary.hasPermissionToEditPropertiesByResourceType(resourceTypeIdFromParam);
		canDecryptProperties = permissionBoundary.canToggleDecryptionOfResourceType(resourceTypeIdFromParam);

	}

	public void setResourceIdFromParam(Integer resourceIdFromParam) {
		this.resourceIdFromParam = resourceIdFromParam;
		this.resourceTypeIdFromParam = null;
		// no context - check already done by PropertyEditDataProvider (onContext/ResourceChanged) => editableProperties
		canEditProperties = permissionBoundary.hasPermissionToEditPropertiesByResource(resourceIdFromParam);
		canDecryptProperties = permissionBoundary.canToggleDecryptionOfResource(resourceIdFromParam);
	}

    /**
     * @return the global Tags as comma separated String
     */
    public String getGlobalTagsString(){
        return propertyTagEditor.getTagsAsList(globalPropertyTags);
    }

    /**
     * @return the PropertyTags as comma seprated list
     */
	public String getPropertyTagsString() {
		if(!StringUtils.isEmpty(propertyTagsString)){
			return propertyTagsString;
		}else {
        	return propertyTagEditor.getTagsAsCommaSeparatedString(propertyDescriptor.getPropertyTags());
		}	
	}

	public void setPropertyDescriptorIdFromParam(Integer propertyDescriptorIdFromParam) {
		if (propertyDescriptorId == null) {
			// edit an existing
			propertyDescriptorId = propertyDescriptorIdFromParam;
			loadPropertyDescriptor();
			// init tags
			propertyTagsString = getPropertyTagsString();
		}
	}

	public Integer getPropertyDescriptorIdFromParam() {
		return propertyDescriptorId;
	}

	public PropertyDescriptorEntity getPropertyDescriptor() {
		if (propertyDescriptor == null) {
			// create a new one
			propertyDescriptor = new PropertyDescriptorEntity();

            propertyDescriptorHashBeforeModification = propertyDescriptor.foreignableFieldHashCode();
		}
		return propertyDescriptor;
	}

	public boolean isLongDefaultValue() {
		return isLongValue(propertyDescriptor.getDefaultValue());
	}

	public boolean isLongExampleValue() {
		return isLongValue(propertyDescriptor.getExampleValue());
	}

	private boolean isLongValue(String value) {
		return (value != null && value.length() > 70);
	}

	public boolean canEditProperties() {
		return this.canEditProperties;
	}

	public boolean canDecryptProperties() {
		return this.canDecryptProperties;
	}

	@Setter
	Integer propertyTypeId;

	public Integer getPropertyTypeId() {
		return propertyDescriptor != null && propertyDescriptor.getPropertyTypeEntity() != null ? propertyDescriptor
				.getPropertyTypeEntity().getId() : null;
	}

	public void assignPropertyTypeId() {
		if (propertyTypes != null) {
			if (propertyTypeId == 0) {
				// Custom type
				propertyDescriptor.setPropertyTypeEntity(null);
				propertyDescriptor.setValidationLogic(null);
				return;
			}
			for (PropertyTypeEntity t : propertyTypes) {
				if (t.getId() != null && t.getId().equals(propertyTypeId)) {
					propertyDescriptor.setPropertyTypeEntity(t);
					propertyDescriptor.setEncrypt(t.isEncrypt());
					propertyDescriptor.setValidationLogic(t.getValidationRegex());

					// tags
					StringBuffer newTagsSb = new StringBuffer();
					LinkedHashSet<String> existingTags = new LinkedHashSet<>();

					if(!StringUtils.isEmpty(propertyTagsString)){
						newTagsSb.append(propertyTagsString);
						String[] tags = propertyTagsString.split(",");
						for (String tag : tags) {
							existingTags.add(tag);
						}
					}
					for (PropertyTagEntity tag : t.getPropertyTags()) {
						if(!existingTags.contains(tag.getName())){
							newTagsSb.append(tag.getName()).append(",");
						}
					}
					propertyTagsString = newTagsSb.toString();
				}
			}
		}
	}

	public void save() {
		try {

			savePropertyDescriptor();

			propertyDescriptorId = propertyDescriptor.getId();

			GlobalMessageAppender.addSuccessMessage("Changes for propertydescriptor instance successfully saved");
			
			loadPropertyDescriptor();
		}
		catch (AMWException e) {
			GlobalMessageAppender.addErrorMessage(e.getMessage());
		} catch (ForeignableOwnerViolationException e) {
			GlobalMessageAppender.addErrorMessage(buildErrorMessage(e, "edit", propertyDescriptor.getPropertyDescriptorDisplayName()));
		}
	}
    
    private String buildErrorMessage(ForeignableOwnerViolationException exception, String action, String name){
       return "Owner "+exception.getViolatingOwner()+" not allowed to "+action+ " " +name;
    }

	private void savePropertyDescriptor() throws AMWException, ForeignableOwnerViolationException {
		if (isEditResource()) {
			propertyDescriptor = propertyEditor.savePropertyDescriptorForResource(
					ForeignableOwner.getSystemOwner(), resourceIdFromParam, propertyDescriptor, propertyDescriptorHashBeforeModification,
					propertyTagsString);
		} else {
			propertyDescriptor = propertyEditor.savePropertyDescriptorForResourceType(
					ForeignableOwner.getSystemOwner(), resourceTypeIdFromParam, propertyDescriptor, propertyDescriptorHashBeforeModification,
					propertyTagsString);
		}
	}

    private boolean isEditResource(){
        return resourceIdFromParam != null;
    }

	public String delete() {
		return deletePropDesc(false);
	}

	public String forceDelete() {
		return deletePropDesc(true);
	}

	private String deletePropDesc(boolean forceDelete) {
		if (propertyDescriptor != null && propertyDescriptor.getId() != null) {
			showForce = false;
			try {
				if (isEditResource()) {
					propertyEditor.deletePropertyDescriptorForResource(ForeignableOwner.getSystemOwner(), resourceIdFromParam, propertyDescriptor, forceDelete);
				}
				else {
					propertyEditor.deletePropertyDescriptorForResourceType(ForeignableOwner.getSystemOwner(), resourceTypeIdFromParam, propertyDescriptor, forceDelete);
				}
				GlobalMessageAppender.addSuccessMessage(propertyDescriptor.getPropertyDescriptorDisplayName() + " was successfully deleted");
				propertyDescriptor = null;
				return "editResourceView?faces-redirect=true&includeViewParams=true";

			}
			catch (PropertyDescriptorNotDeletableException e) {
				showForce = true;
				String additionalInfo = "If you force the deletion, all those property values will be deleted as well";
				String errorMessage = String.format("%s <br> %s", e.getMessage(), additionalInfo);
				GlobalMessageAppender.addErrorMessage(errorMessage);
			}
			catch (AMWException e) {
				GlobalMessageAppender.addErrorMessage(e.getMessage());
			} catch (ForeignableOwnerViolationException e) {
				GlobalMessageAppender.addErrorMessage(buildErrorMessage(e, "delete", propertyDescriptor.getPropertyDescriptorDisplayName()));
			}
		}
		else {
			GlobalMessageAppender.addErrorMessage("Nothing to delete");
		}
		return null;
	}

	private void loadPropertyDescriptor() {
		if (propertyDescriptorId != null) {
			propertyDescriptor = propertyEditor.getPropertyDescriptor(propertyDescriptorId);
            propertyDescriptorHashBeforeModification = propertyDescriptor.foreignableFieldHashCode();
		}
	}

	public boolean isSameEncrypted(){
		if(propertyDescriptor.getPropertyTypeEntity() != null){
			return propertyDescriptor.isEncrypt() == propertyDescriptor.getPropertyTypeEntity().isEncrypt();
		}
		return true;
	}

	public boolean isSameValidationLogic(){
		if(propertyDescriptor.getPropertyTypeEntity() != null){
			return propertyDescriptor.getValidationLogic().equals(propertyDescriptor.getPropertyTypeEntity().getValidationRegex());
		}
		return true;
	}
	
	public boolean isNewDescriptorMode(){
		return propertyDescriptor == null || propertyDescriptor.getId() == null;
	}

	public boolean canManageForeignProperty(){
		return foreignableBoundary.isModifiableByOwner(ForeignableOwner.getSystemOwner(), getPropertyDescriptor());
	}

    public ForeignableAttributesDTO getForeignableToEdit(){
        if (propertyDescriptor != null){
            return new ForeignableAttributesDTO(propertyDescriptor.getOwner(), propertyDescriptor.getExternalKey(), propertyDescriptor.getExternalLink());
        }
        return new ForeignableAttributesDTO();
    }

}
