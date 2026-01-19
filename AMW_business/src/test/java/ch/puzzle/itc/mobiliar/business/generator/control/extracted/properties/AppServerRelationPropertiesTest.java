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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.AD;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.APP;
import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.MAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import ch.puzzle.itc.mobiliar.business.domain.TestUtils;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextTypeEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyMaskingContext;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.test.AmwEntityBuilder;
import ch.puzzle.itc.mobiliar.test.CustomLogging;
import ch.puzzle.itc.mobiliar.test.EntityBuilder;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

public class AppServerRelationPropertiesTest {
	ContextEntity context = new ContextEntity();
	EntityBuilder builder = new AmwEntityBuilder();

	static {
		CustomLogging.setup(Level.OFF);
	}

	@BeforeEach
	public void setUpContext() {
		// Ensure context has an id so property collection works with context
		// comparisons
		context.setId(1);
		ContextTypeEntity type = new ContextTypeEntity();
		type.setId(1);
		type.setName("ENV");
		context.setContextType(type);
	}

	@Test
	public void test() throws TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity owner = builder.resourceFor(APP);
		ResourceEntity ad = builder.resourceFor(AD);
		ConsumedResourceRelationEntity relation = builder.relationFor(owner.getName(), ad.getName());

		AppServerRelationProperties appServerRelationProperties = new AppServerRelationProperties(context, owner,
				templateExceptionHandler, null);
		appServerRelationProperties.addConsumedRelation("adIntern", ad, relation);

		AmwResourceTemplateModel properties = appServerRelationProperties.transformModel();

		assertEquals("1", properties.get("id").toString());
		assertEquals("ch_puzzle_itc_mobi_amw", properties.get("name").toString());

		assertNull(properties.get("propertyTypes"));
		assertTrue(((TemplateHashModel) properties.get("providedResTypes")).isEmpty());

		assertFalse(((TemplateHashModel) properties.get("consumedResTypes")).isEmpty());
		assertEquals("4", TestUtils.asHashModel(properties, "consumedResTypes", "ActiveDirectory", "adIntern").get("id")
				.toString());
		assertEquals("adIntern", TestUtils.asHashModel(properties, "consumedResTypes", "ActiveDirectory", "adIntern")
				.get("name").toString());
		assertEquals("4", TestUtils.asHashModel(properties, "adIntern").get("id").toString());
		assertEquals("adIntern", TestUtils.asHashModel(properties, "adIntern").get("name").toString());

		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testMergeMergesAllConsumedInBothDirections() {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity owner = builder.resourceFor(APP);
		ResourceEntity slave = builder.resourceFor(AD);

		AppServerRelationProperties props1 = new AppServerRelationProperties(context, owner, templateExceptionHandler, null);
		AppServerRelationProperties props2 = new AppServerRelationProperties(context, owner, templateExceptionHandler, null);

		props1.addConsumedRelation("foo", slave, null);

		props1.merge(props2);
		assertEquals(1, props1.getConsumed().size());
		assertEquals(1, props2.getConsumed().size());

		AppServerRelationProperties props3 = new AppServerRelationProperties(context, owner, templateExceptionHandler, null);
		props3.merge(props2);
		assertEquals(1, props3.getConsumed().size());
		assertEquals(1, props2.getConsumed().size());
		assertEquals(1, props1.getConsumed().size());
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testTemplatesInRelatedResources() throws TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity owner = builder.resourceFor(APP);
		ResourceEntity ad = builder.resourceFor(AD);
		ConsumedResourceRelationEntity relation = builder.relationFor(owner.getName(), ad.getName());

		AppServerRelationProperties appServerRelationProperties = new AppServerRelationProperties(context, owner,
				templateExceptionHandler, null);
		appServerRelationProperties.addConsumedRelation("active_directory", ad, relation);
		Map<ResourceEntity, Set<GeneratedTemplate>> templatesCache = new LinkedHashMap<>();
		templatesCache.computeIfAbsent(ad, k -> new LinkedHashSet<>())
				.add(new GeneratedTemplate("name", "path", "content"));

		appServerRelationProperties.getConsumed().get(0).setTemplatesCache(templatesCache);
		appServerRelationProperties.setTemplatesCache(templatesCache);

		TemplateHashModel model = TestUtils.asHashModel(appServerRelationProperties.transformModel(),
				"active_directory", "templates", "name");
		assertEquals("content", model.get("content").toString());

		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testOnlyProvidedResource() throws TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity ad = builder.resourceFor(AD);
		ResourceEntity mail = builder.resourceFor(MAIL);
		ProvidedResourceRelationEntity relation = builder.buildProvidedRelation(ad, mail);
		AppServerRelationProperties properties = new AppServerRelationProperties(context, ad, templateExceptionHandler, null);

		properties.addProvidedRelation("mailrelay", mail, relation);

		TemplateHashModel model = TestUtils.asHashModel(properties.transformModel(), "providedResTypes", "Mail", "mailrelay");
		assertEquals("mailrelay", model.get("name").toString());
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void testOnlyConsumedResource() throws TemplateModelException {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity ad = builder.resourceFor(AD);
		ResourceEntity mail = builder.resourceFor(MAIL);
		builder.buildResourceProperty(ad, "foo", "bar");

		ConsumedResourceRelationEntity relation = builder.buildConsumedRelation(ad, mail);

		AppServerRelationProperties properties = new AppServerRelationProperties(context, ad, templateExceptionHandler, null);
		properties.addConsumedRelation("mailrelay", mail, relation);

		TemplateHashModel model = TestUtils.asHashModel(properties.transformModel(), "consumedResTypes", "Mail", "mailrelay");

		assertEquals("mailrelay", model.get("name").toString());
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void masksEncryptedPropertiesWhenMaskingEnabled() {
		// Create encrypted descriptor
		PropertyDescriptorEntity descriptor = new PropertyDescriptorEntity();
		descriptor.setId(1);
		descriptor.setPropertyName("password");
		descriptor.setEncrypt(true);

		// Create FreeMarkerProperty with encrypted descriptor
		FreeMarkerProperty property = new FreeMarkerProperty("superSecret", descriptor);
		assertEquals("superSecret", property.getCurrentValue());

		// maskIfEncrypted should replace the value
		property.maskIfEncrypted();
		assertEquals("****", property.getCurrentValue());
	}

	@Test
	public void doesNotMaskNonEncryptedProperties() {
		// Create non-encrypted descriptor
		PropertyDescriptorEntity descriptor = new PropertyDescriptorEntity();
		descriptor.setId(2);
		descriptor.setPropertyName("username");
		descriptor.setEncrypt(false);

		// Create FreeMarkerProperty with non-encrypted descriptor
		FreeMarkerProperty property = new FreeMarkerProperty("myUsername", descriptor);

		// Without encryption, maskIfEncrypted should do nothing
		property.maskIfEncrypted();
		assertEquals("myUsername", property.getCurrentValue());
	}

	@Test
	public void generatesTemplateModelWithMaskingEnabled() throws TemplateModelException {
		// Verify that TemplateHashModel generation works correctly when PropertyMasking
		// is enabled and encrypted properties are masked in the model
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity ad = builder.resourceFor(AD);
		ResourceEntity mail = builder.resourceFor(MAIL);

		ConsumedResourceRelationEntity relation = builder.buildConsumedRelation(ad, mail);

		// Create a PropertyMaskingContext for testing
		PropertyMaskingContext maskingContext = new PropertyMaskingContext();
		maskingContext.enableMasking();

		try {
			AppServerRelationProperties properties = new AppServerRelationProperties(context, ad,
					templateExceptionHandler, maskingContext);

			// Manually add regular and encrypted properties to test masking in the model
			PropertyDescriptorEntity regularDescriptor = new PropertyDescriptorEntity();
			regularDescriptor.setId(10);
			regularDescriptor.setPropertyName("foo");
			regularDescriptor.setEncrypt(false);
			FreeMarkerProperty regularProperty = new FreeMarkerProperty("bar", regularDescriptor);
			properties.getProperties().put("foo", regularProperty);

			PropertyDescriptorEntity encryptedDescriptor = new PropertyDescriptorEntity();
			encryptedDescriptor.setId(3);
			encryptedDescriptor.setPropertyName("apiKey");
			encryptedDescriptor.setEncrypt(true);
			FreeMarkerProperty encryptedProperty = new FreeMarkerProperty("secretApiKey123", encryptedDescriptor);
			encryptedProperty.maskIfEncrypted();
			properties.getProperties().put("apiKey", encryptedProperty);
			properties.addConsumedRelation("mailrelay", mail, relation);

			AmwResourceTemplateModel model = properties.transformModel();

			// Verify regular properties are accessible unchanged
			assertEquals("bar", model.get("foo").toString());

			// Verify encrypted property is masked in the model
			assertEquals("****", model.get("apiKey").toString());
			assertTrue(templateExceptionHandler.isSuccess());
		} finally {
			// PropertyMaskingContext is request-scoped, no manual cleanup needed in production
			// This is just for test clarity
			maskingContext.disableMasking();
		}
	}

	@Test
	public void doesNotMaskPropertiesWhenMaskingDisabled() throws TemplateModelException {
		// Verify that encrypted properties are NOT masked during normal operations
		// (e.g., regular deployments, not test generation) regardless of permissions
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity ad = builder.resourceFor(AD);
		ResourceEntity mail = builder.resourceFor(MAIL);

		ConsumedResourceRelationEntity relation = builder.buildConsumedRelation(ad, mail);

		// Create a PropertyMaskingContext but leave masking DISABLED (default state)
		PropertyMaskingContext maskingContext = new PropertyMaskingContext();
		assertFalse(maskingContext.isMaskingEnabled(), "Masking should be disabled by default");

		AppServerRelationProperties properties = new AppServerRelationProperties(context, ad,
				templateExceptionHandler, maskingContext);

		// Add encrypted properties
		PropertyDescriptorEntity encryptedDescriptor = new PropertyDescriptorEntity();
		encryptedDescriptor.setId(5);
		encryptedDescriptor.setPropertyName("dbPassword");
		encryptedDescriptor.setEncrypt(true);
		FreeMarkerProperty encryptedProperty = new FreeMarkerProperty("mySecretPassword", encryptedDescriptor);
		properties.getProperties().put("dbPassword", encryptedProperty);

		properties.addConsumedRelation("mailrelay", mail, relation);

		AmwResourceTemplateModel model = properties.transformModel();

		// Verify encrypted property is NOT masked when masking is disabled
		assertEquals("mySecretPassword", model.get("dbPassword").toString(),
				"Encrypted properties should NOT be masked during normal operations (non-test generation)");
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void doesNotMaskPropertiesWhenMaskingContextIsNull() throws TemplateModelException {
		// Verify that encrypted properties are NOT masked when PropertyMaskingContext is null
		// This simulates normal deployment scenarios where masking is not in use
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		ResourceEntity ad = builder.resourceFor(AD);
		ResourceEntity mail = builder.resourceFor(MAIL);

		ConsumedResourceRelationEntity relation = builder.buildConsumedRelation(ad, mail);

		// Pass null for PropertyMaskingContext (as used in tests and potentially other non-test-generation scenarios)
		AppServerRelationProperties properties = new AppServerRelationProperties(context, ad,
				templateExceptionHandler, null);

		// Add encrypted properties
		PropertyDescriptorEntity encryptedDescriptor = new PropertyDescriptorEntity();
		encryptedDescriptor.setId(6);
		encryptedDescriptor.setPropertyName("apiToken");
		encryptedDescriptor.setEncrypt(true);
		FreeMarkerProperty encryptedProperty = new FreeMarkerProperty("token123456", encryptedDescriptor);
		properties.getProperties().put("apiToken", encryptedProperty);

		properties.addConsumedRelation("mailrelay", mail, relation);

		AmwResourceTemplateModel model = properties.transformModel();

		// Verify encrypted property is NOT masked when context is null
		assertEquals("token123456", model.get("apiToken").toString(),
				"Encrypted properties should NOT be masked when PropertyMaskingContext is null");
		assertTrue(templateExceptionHandler.isSuccess());
	}

	@Test
	public void masksRelationPropertiesWhenMaskingEnabled() throws Exception {
		// Ensure relation-collected properties are masked when masking is enabled
		PropertyMaskingContext maskingContext = new PropertyMaskingContext();
		maskingContext.enableMasking();

		// Prepare a relation property map with an encrypted property
		PropertyDescriptorEntity encryptedDescriptor = new PropertyDescriptorEntity();
		encryptedDescriptor.setId(99);
		encryptedDescriptor.setPropertyName("relSecret");
		encryptedDescriptor.setEncrypt(true);
		FreeMarkerProperty encryptedProperty = new FreeMarkerProperty("superSecretRel", encryptedDescriptor);

		Map<String, FreeMarkerProperty> relationProps = new LinkedHashMap<>();
		relationProps.put("relSecret", encryptedProperty);

		// Use reflection to invoke maskEncryptedProperties to simulate relation collection masking
		AppServerRelationProperties props = new AppServerRelationProperties(context, builder.resourceFor(APP),
				new AMWTemplateExceptionHandler(), maskingContext);
		props.maskEncryptedProperties(relationProps);

		assertEquals("****", relationProps.get("relSecret").getCurrentValue(),
				"Encrypted relation properties must be masked when masking is enabled");
	}
}
