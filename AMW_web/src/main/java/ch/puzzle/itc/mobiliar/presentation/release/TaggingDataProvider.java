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

package ch.puzzle.itc.mobiliar.presentation.release;

import ch.puzzle.itc.mobiliar.business.configurationtag.control.TagConfigurationService;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import ch.puzzle.itc.mobiliar.presentation.util.NavigationUtils;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.event.Observes;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

@Named
@ViewScoped
public class TaggingDataProvider implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	TagConfigurationService tagConfigurationService;

	@Inject
	PermissionService permissions;

	@Inject
	PropertyEditor editor;

	@Getter
	@Setter
	private String tagLabel;

	@Getter
	@Setter
	private Date tagDate;

	@Getter
	private boolean canTagCurrentState;

	private Set<String> tagLabels;

	private ResourceEntity resource;

	public void onResourceChange(@Observes ResourceEntity resourceEntity) {
		this.resource = resourceEntity;
		canTagCurrentState = permissions.hasPermission(Permission.RESOURCE, null, Action.UPDATE, resourceEntity.getResourceGroup(), null);
		extractTagLabels(tagConfigurationService.loadTagLabelsForResource(resourceEntity));
	}

	public void onResourceTypeChange(@Observes ResourceTypeEntity resourceTypeEntity) {
		this.resource = null;
		tagLabels = Collections.emptySet();
	}

	public String tagConfiguration() {
		if (resource == null) {
			String message = "No resource selected.";
			GlobalMessageAppender.addErrorMessage(message);
		}
		else if (tagLabel == null) {
			String message = "No tag label defined.";
			GlobalMessageAppender.addErrorMessage(message);
		}
		else if (tagDate == null) {
			String message = "No tag date defined.";
			GlobalMessageAppender.addErrorMessage(message);
		}
		else {
			if (tagLabels.contains(tagLabel.trim())) {
				String message = "A label with the value '" + tagLabel
						+ "' already exists for this application.";
				GlobalMessageAppender.addErrorMessage(message);
			}
			else {
				tagConfigurationService.tagConfiguration(resource.getId(), tagLabel, tagDate);
				String message = "New tag '" + tagLabel + "' created.";
				GlobalMessageAppender.addSuccessMessage(message);
				return NavigationUtils.getRefreshOutcome();
			}
		}
		return NavigationUtils.getRefreshOutcome();
	}

	private void extractTagLabels(List<ResourceTagEntity> tags) {
		tagLabels = new LinkedHashSet<>();
		for (ResourceTagEntity r : tags) {
			tagLabels.add(r.getLabel());
		}
	}
}
