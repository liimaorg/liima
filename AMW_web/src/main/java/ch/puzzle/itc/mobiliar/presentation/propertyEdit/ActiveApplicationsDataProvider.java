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

import ch.puzzle.itc.mobiliar.business.resourceactivation.boundary.ResourceActivationService;
import ch.puzzle.itc.mobiliar.business.resourceactivation.entity.ResourceActivationEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.presentation.resourceRelation.ResourceRelationModel;
import ch.puzzle.itc.mobiliar.presentation.resourceRelation.events.ChangeSelectedRelationEvent;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named
@ViewScoped
public class ActiveApplicationsDataProvider implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	ResourceRelationModel model;

	@Inject
	PermissionService permissions;

	@Inject
	ResourceActivationService resourceActivationService;

	private List<ResourceActivationEntity> resourceActivationEntries;

	ContextEntity currentContext;
	ResourceEditRelation currentRelation;

	@Getter
	boolean canExcludeApps;

	@Getter
	@Setter
	private List<Integer> activeApplications;

	@PostConstruct
	public void init() {
		canExcludeApps = permissions.hasPermission(Permission.RESOURCE);
	}

	public void onChangedContext(@Observes ContextEntity contextEntity) {
		this.currentContext = contextEntity;
		loadInactiveApplications();
	}

	public void onChangedRelation(@Observes ChangeSelectedRelationEvent relation) {
		this.currentRelation = relation.getRelation();
		loadInactiveApplications();
	}

	public void save() {
		if (this.activeApplications != null) {
			List<Integer> inactiveApplications = getApplicationIds();
			inactiveApplications.removeAll(activeApplications);
			resourceActivationService.activateDeactivateResources(model.getCurrentResourceRelationId(),
					currentContext.getId(), inactiveApplications, this.activeApplications);
		}
	}

	void loadInactiveApplications() {
		if (isApplicationServerToNodeInstanceRelation() && currentRelation !=null && currentContext != null) {
			try {
				resourceActivationEntries = resourceActivationService.loadResourceActivations(
						currentRelation.getResRelId(), currentContext.getId());
			}
			catch (ResourceNotFoundException e) {
				GlobalMessageAppender.addErrorMessage(e);
			}
			populateActiveApplications();
		}
		else {
			resourceActivationEntries = Collections.emptyList();
			activeApplications = null;
		}
	}

	List<Integer> getApplicationIds() {
		List<Integer> applicationIds = new ArrayList<>();
		if (model.getConsumedApplications() != null) {
			for (ResourceEditRelation app : model.getConsumedApplications()) {
				applicationIds.add(app.getSlaveGroupId());
			}
		}
		return applicationIds;
	}

	void populateActiveApplications() {
		List<Integer> inactiveIds = new ArrayList<>();
		for (ResourceActivationEntity resourceActivationEntry : resourceActivationEntries) {
			if (!resourceActivationEntry.isActive()) {
				inactiveIds.add(resourceActivationEntry.getResourceGroup().getId());
			}
		}
		List<Integer> numericActiveApplications = new ArrayList<>(getApplicationIds());
		numericActiveApplications.removeAll(inactiveIds);
		activeApplications = numericActiveApplications;
	}

	/**
	 * @return true, if the current relation is an instance relation between an application server and a
	 *         node. false otherwise.
	 */
	boolean isApplicationServerToNodeInstanceRelation() {
		if (currentRelation != null && !currentRelation.isResourceTypeRelation()) {
			if (DefaultResourceTypeDefinition.APPLICATIONSERVER.name().equals(
					currentRelation.getMasterTypeName())) {
				if (DefaultResourceTypeDefinition.NODE.name().equals(currentRelation.getSlaveTypeName())) {
					return true;
				}
			}
		}
		return false;
	}
}
