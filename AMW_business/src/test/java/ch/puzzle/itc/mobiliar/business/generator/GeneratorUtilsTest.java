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

package ch.puzzle.itc.mobiliar.business.generator;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorUtils;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.test.AmwEntityBuilder;
import ch.puzzle.itc.mobiliar.test.EntityBuilder;
import ch.puzzle.itc.mobiliar.test.GeneratorObjectFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.AD;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.APP;
import static org.junit.Assert.*;

public class GeneratorUtilsTest {

	GeneratorUtils utils = new GeneratorUtils();
	GeneratorObjectFactory f = new GeneratorObjectFactory();

	PropertyDescriptorEntity propertyDescriptor = new PropertyDescriptorEntity();
	ResourceEntity applicationServer = f.createApplicationServer();
	private int targetPlatformId;
	private ContextEntity context;
	private AMWTemplateExceptionHandler templateExceptionHandler;

	@Before
	public void setUp() throws Exception {
		// Add property descriptor to applicationServer-global context
		targetPlatformId = 1;
		context = f.createEnvironment();
		f.addPropertyDescriptorToGlobalResourceContext(propertyDescriptor, applicationServer);
		templateExceptionHandler = new AMWTemplateExceptionHandler();
	}
	
	@Test
	public void test(){
		assertTrue(Integer.valueOf(1).compareTo(Integer.valueOf(1))== 0);
	}

	@Test
	public void findGlobalPropertyForApplicationServer() {
		// Add property to global context
		PropertyEntity globalProperty = f.createAndAddPropertyToResource(propertyDescriptor, "global", f.getGlobalContext(),
				applicationServer);

		List<PropertyEntity> result = utils.getPropertyValues(applicationServer, f.getGlobalContext(), templateExceptionHandler);

		assertNotNull(result);
		assertEquals(result.size(), 1);
		assertNotNull(result.get(0));
		assertEquals(result.get(0).getDescriptor().getId(), propertyDescriptor.getId());
		assertEquals(result.get(0), globalProperty);
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void findEnvironmentPropertyForApplicationServer() {
		// Add property to environment
		PropertyEntity property = f.createAndAddPropertyToResource(propertyDescriptor, "environment", context, applicationServer);
		List<PropertyEntity> result = utils.getPropertyValues(applicationServer, context, templateExceptionHandler);

		assertNotNull(result);
		assertEquals(result.size(), 1);
		assertNotNull(result.get(0));
		assertEquals(result.get(0).getDescriptor().getId(), propertyDescriptor.getId());
		assertEquals(result.get(0), property);
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void findEnvironmentAndPropertyForApplicationServer() {
		// Add property to global context
		PropertyEntity globalProperty = f.createAndAddPropertyToResource(propertyDescriptor, "global", f.getGlobalContext(),
				applicationServer);

		// Add property to environment
		ContextEntity environment = f.createEnvironment();
		PropertyEntity property = f.createAndAddPropertyToResource(propertyDescriptor, "environment", environment, applicationServer);

		List<PropertyEntity> result = utils.getPropertyValues(applicationServer, environment, templateExceptionHandler);

		assertNotNull(result);
		assertEquals(result.size(), 1);
		assertNotNull(result.get(0));
		assertEquals(result.get(0).getDescriptor().getId(), propertyDescriptor.getId());
		assertEquals(result.get(0), property);
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testCollectTemplateDescriptors() {
		context.addTemplate(f.createTemplate("foo", "foo", targetPlatformId));
		Set<TemplateDescriptorEntity> collected = utils.collectTemplateDescriptors(context, null, targetPlatformId, false);
		assertEquals(1, collected.size());
	}
	
	@Test
	public void testCollectTestingTemplateDescriptors(){
		TemplateDescriptorEntity standardTempl = f.createTemplate("standardTempl", "bla", targetPlatformId);
		TemplateDescriptorEntity testingTempl = f.createTemplate("testingTempl", "testing", targetPlatformId);
		testingTempl.setTesting(true);
		
		TemplateDescriptorEntity otherPlatformStandardTempl = f.createTemplate("standardTempl", "bla", targetPlatformId+1);
		TemplateDescriptorEntity otherPlatformTestingTempl = f.createTemplate("testingTempl", "testing", targetPlatformId+1);
		otherPlatformTestingTempl.setTesting(true);
		
		context.addTemplate(standardTempl);
		context.addTemplate(testingTempl);
		context.addTemplate(otherPlatformTestingTempl);
		context.addTemplate(otherPlatformStandardTempl);
		
		Set<TemplateDescriptorEntity> collectedTesting = utils.collectTemplateDescriptors(context, null, targetPlatformId, true);
		Set<TemplateDescriptorEntity> collectedStandard = utils.collectTemplateDescriptors(context, null, targetPlatformId, false);
		
		//Ensure, only the templates of the given runtime are included and they are separated properly depending on their testing-mode.
		assertEquals(1, collectedTesting.size());
		assertEquals(1, collectedStandard.size());
		
		assertEquals(testingTempl, collectedTesting.iterator().next());
		assertEquals(standardTempl, collectedStandard.iterator().next());
	}
	

	@Test
	public void testGetTemplatesForResource() {
		ResourceEntity resource = f.createApplicationServer();
		TemplateDescriptorEntity template = f.createTemplate("foo", "foo", targetPlatformId);

		ResourceContextEntity resourceContext = f.createResourceContext(context, resource);
		resourceContext.addTemplate(template);

		resource.setContexts(new HashSet<ResourceContextEntity>());
		resource.getContexts().add(resourceContext);

		Set<TemplateDescriptorEntity> templates = utils.getTemplates(resource, context, null, targetPlatformId, false);
		assertEquals(1, templates.size());
	}

	@Test
	public void testGetTemplatesForRelation() {
		EntityBuilder builder = new AmwEntityBuilder();
		TemplateDescriptorEntity template = f.createTemplate("foo", "foo", targetPlatformId);

		ConsumedResourceRelationEntity resourceRelation = builder.relationFor(APP, AD);

		ResourceRelationContextEntity relationContext = f.createResourceRelationContext(33, context, resourceRelation);
		relationContext.addTemplate(template);

		resourceRelation.setContexts(new HashSet<ResourceRelationContextEntity>());
		resourceRelation.getContexts().add(relationContext);

		Set<TemplateDescriptorEntity> templates = utils.getTemplates(context, resourceRelation, null,
				targetPlatformId, false);
		assertEquals(1, templates.size());

	}
}
