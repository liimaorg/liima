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

import ch.puzzle.itc.mobiliar.business.environment.entity.AbstractContext;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import org.apache.commons.lang.StringUtils;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * Builds {@link TemplateDescriptorEntity} for testing
 * 
 * @author cweber
 */
public class TemplateDescriptorEntityBuilder extends BaseEntityBuilder {

	public static final String FILE_CONTENT = "#test";
	public static final String TARGET_PATH = "amw_conf/configuration/standalone.xml";
	public static final String NAME = "standalone.xml";

	private String fileContent;
	private String targetPath;
	private String name;
	private AbstractContext ownerResource;
	private Set<ResourceGroupEntity> targetPlatforms;
	private boolean testing;
	private boolean relationTemplate;
	private Integer id;

	public TemplateDescriptorEntityBuilder withFileContent(String fileContent) {
		this.fileContent = fileContent;
		return this;
	}

	public TemplateDescriptorEntityBuilder withTargetPath(String targetPath) {
		this.targetPath = targetPath;
		return this;
	}

	public TemplateDescriptorEntityBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public TemplateDescriptorEntityBuilder withOwnerResource(AbstractContext ownerResource) {
		this.ownerResource = ownerResource;
		return this;
	}

	public TemplateDescriptorEntityBuilder withTargetPlatforms(Set<ResourceGroupEntity> targetPlatforms) {
		this.targetPlatforms = targetPlatforms;
		return this;
	}

	public TemplateDescriptorEntityBuilder withTesting(boolean testing) {
		this.testing = testing;
		return this;
	}

	public TemplateDescriptorEntityBuilder setId(Integer id) {
		this.id = id;
		return this;
	}

	public TemplateDescriptorEntityBuilder withRelationTemplate(boolean relationTemplate) {
		this.relationTemplate = relationTemplate;
		return this;
	}

	/**
	 * @return mocked TemplateDescriptorEntity
	 */
	public TemplateDescriptorEntity mock() {
		TemplateDescriptorEntity mock = Mockito.mock(TemplateDescriptorEntity.class);
		when(mock.getFileContent()).thenReturn(fileContent);
		when(mock.getId()).thenReturn(id);
		when(mock.getTargetPath()).thenReturn(targetPath);
		when(mock.getName()).thenReturn(name);
		when(mock.getOwnerResource()).thenReturn(ownerResource);
		when(mock.getTargetPlatforms()).thenReturn(targetPlatforms);
		when(mock.isTesting()).thenReturn(testing);
		when(mock.isRelationTemplate()).thenReturn(relationTemplate);

		return mock;
	}

	/**
	 * @return TemplateDescriptorEntity
	 */
	public TemplateDescriptorEntity build() {
		TemplateDescriptorEntity template = new TemplateDescriptorEntity();
		template.setFileContent(fileContent);
		template.setTargetPath(targetPath);
		template.setId(id);
		template.setName(name);
		template.setOwnerResource(ownerResource);
		template.setTargetPlatforms(targetPlatforms);
		template.setTesting(testing);
		template.setRelationTemplate(relationTemplate);

		return template;
	}

}
