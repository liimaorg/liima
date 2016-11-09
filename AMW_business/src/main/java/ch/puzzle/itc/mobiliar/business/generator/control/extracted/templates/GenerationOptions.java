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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import java.util.List;
import java.util.Map;

import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.BasePropertyCollector;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;

import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import com.google.common.collect.Maps;

/**
 * Provides a context for common objects used during generation.
 */
public class GenerationOptions{

	private final GenerationContext context;
	private final Map<Integer, ApplicationWithVersion> versions;
	private Map<String, AmwResourceTemplateModel> applications;
	private Map<String, FreeMarkerProperty> contextProperties;
	private Map<String, GeneratedTemplate> templateFiles;

	public GenerationOptions(GenerationContext context) {
		
		this.context = context;
		this.versions = Maps.newHashMap();
		populateVersions(context.getApplicationsWithVersion());
		populateProperties();
	}

	private void populateProperties() {
		templateFiles = Maps.newLinkedHashMap();
		this.applications = Maps.newLinkedHashMap();
		this.contextProperties = Maps.newTreeMap();
		contextProperties.putAll(new BasePropertyCollector().propertiesForContext(context.getContext()));
	}

	private void populateVersions(List<ApplicationWithVersion> versions) {
		for (ApplicationWithVersion applicationWithVersion : versions) {
			this.versions.put(applicationWithVersion.getApplicationId(), applicationWithVersion);
		}
	}

	public Map<Integer, ApplicationWithVersion> getVersions() {
		return versions;
	}

	public Map<String, GeneratedTemplate> getTemplateFiles() {
		return templateFiles;
	}

	public Map<String, FreeMarkerProperty> getContextProperties() {
		return contextProperties;
	}

	public GenerationContext getContext(){
		return context;
	}

	public Map<String, AmwResourceTemplateModel> getApplications() {
		return applications;
	}
}
