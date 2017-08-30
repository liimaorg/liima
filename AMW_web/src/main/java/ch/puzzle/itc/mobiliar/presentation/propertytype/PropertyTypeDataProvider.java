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

package ch.puzzle.itc.mobiliar.presentation.propertytype;

import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyTagEditor;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import ch.puzzle.itc.mobiliar.presentation.CompositeBackingBean;
import ch.puzzle.itc.mobiliar.presentation.settings.SettingsDataProvider;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
@CompositeBackingBean
public class PropertyTypeDataProvider implements Serializable {

	@PostConstruct
	public void initView() {
		reloadPropertyTypes();
	}

	@Inject
	private PropertyTypeController controller;

    @Inject
    PropertyTagEditor propertyTagEditor;

    @Inject
    SettingsDataProvider settingsDataProviderProvider;
	
	private List<PropertyTypeEntity> propertyTypes;
	
	private PropertyTypeEntity selectedPropertyType;

	// edit propType
	@Getter
	private Integer selectedPropertyTypeId;
	@Getter
	@Setter
	private String selectedPropertyTypeName;
	@Getter
	@Setter
	private String selectedPropertyTypeRegex;
	@Getter
	@Setter
	private boolean selectedEncrypted;
	@Setter
	private String selectedPropTypeTagsString;

	// add PropType
	@Getter
	@Setter
	private String newPropertyTypeName;
	@Getter
	@Setter
	private String newValidationRegex;
	@Getter
	@Setter
	private boolean newEncrypted;
	@Setter
	@Getter
	private String newPropTypeTagsString;

    private List<PropertyTagEntity> globalPropertyTags;

	public PropertyTypeEntity getSelectedPropertyType() {
		return selectedPropertyType;
	}

	public void setSelectedPropertyType(PropertyTypeEntity propertyType) {
		this.selectedPropertyType = propertyType;
		setSelectedPropertyTypeName(propertyType.getPropertyTypeName());
		setSelectedPropertyTypeRegex(propertyType.getValidationRegex());
		setSelectedEncrypted(propertyType.isEncrypt());
	}
	
	public List<PropertyTypeEntity> getAllPropertyTypes() {
		return propertyTypes;
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
    public String getSelectedPropTypeTagsString() {
        return propertyTagEditor.getTagsAsCommaSeparatedString(selectedPropertyType.getPropertyTags());
    }

	public String createByPropertyTypeEntity() {
		if (controller.doCreateByPropertyType(getNewPropertyTypeName(), getNewValidationRegex(), isNewEncrypted(), getNewPropTypeTagsString())) {
			reloadPropertyTypes();
			selectPropertyTypeByName(getNewPropertyTypeName());
			clearCreatePropertyTypePopup();
		}
		return buildSettingsPropertiesUrl();
	}

	public void save() {
		if (controller.doSave(getSelectedPropertyType().getId(), getSelectedPropertyTypeName(), getSelectedPropertyTypeRegex(), isSelectedEncrypted(), selectedPropTypeTagsString)) {
			reloadPropertyTypes();
		} else {
			setSelectedPropertyTypeName(getSelectedPropertyType().getPropertyTypeName());
			setSelectedPropertyTypeRegex(getSelectedPropertyType().getValidationRegex());
		}
	}

	public String remove() {
		if (controller.doRemovePropertyType(getSelectedPropertyType())) {
			this.selectedPropertyTypeId = null;
			reloadPropertyTypes();
			clearCreatePropertyTypePopup();
		}
		return buildSettingsPropertiesUrl();
	}

	public void clearCreatePropertyTypePopup() {
		newPropertyTypeName = null;
		newValidationRegex = null;
		newEncrypted = false;
		newPropTypeTagsString = null;
	}

	private void reloadPropertyTypes() {
		propertyTypes = controller.loadPropertyTypes();
        if(selectedPropertyTypeId != null) {
            setSelectedPropertyTypeId(selectedPropertyTypeId);
        } else {
			String propId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("propId");
			if (propId != null) {
				setSelectedPropertyTypeId(Integer.valueOf(propId));
			} else {
				selectFirstPropertytype();
			}
		}
        globalPropertyTags = propertyTagEditor.getAllGlobalPropertyTags();
	}

	private void selectPropertyTypeByName(String propertyName) {
		if (!getAllPropertyTypes().isEmpty() && propertyName != null) {
			for (PropertyTypeEntity propertyType : getAllPropertyTypes()) {
				if (propertyName.equals(propertyType.getPropertyTypeName())) {
					setSelectedPropertyTypeId(propertyType.getId());
					return;
				}
			}
		}
		// falls name null ist, so soll erstes element selektiert werden
		selectFirstPropertytype();
	}

	private void selectFirstPropertytype() {
		if (!getAllPropertyTypes().isEmpty()) {
            PropertyTypeEntity propertyType = getAllPropertyTypes().get(0);
			setSelectedPropertyTypeId(propertyType.getId());
		}
	}

	public void setSelectedPropertyTypeId(Integer selectedPropertyTypeId) {
		this.selectedPropertyTypeId = selectedPropertyTypeId;
		setSelectedPropertyType(getPropertyTypeById(selectedPropertyTypeId));
	}
	
	private PropertyTypeEntity getPropertyTypeById(Integer propertyType){
		for(PropertyTypeEntity t : propertyTypes){
			if(t.getId().equals(propertyType)) {
				return t;
			}
		}
		return null;
	}

	private String buildSettingsPropertiesUrl() {
		return FacesContext.getCurrentInstance().getViewRoot().getViewId() + "?faces-redirect=true&mode=props";
	}

}
