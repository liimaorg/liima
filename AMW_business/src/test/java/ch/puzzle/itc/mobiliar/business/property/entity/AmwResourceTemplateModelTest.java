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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.business.integration.entity.util.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.AmwModelPreprocessExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.BaseTemplateProcessor;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class AmwResourceTemplateModelTest {
	
	@Test
	public void testEvaluateFunction() throws Exception{
		// given
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
		thisProperties.put("foo", new FreeMarkerProperty("val1", "foo"));
		thisProperties.put("bar", new FreeMarkerProperty("val2", "bar"));

		AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
		resourceModel.setProperties(thisProperties);
		ArrayList<AmwFunctionEntity> functions = new ArrayList<AmwFunctionEntity>();
		AmwFunctionEntity function = new AmwFunctionEntity();
		function.setName("test");
		function.setImplementation("<#return (foo + bar)>");
		functions.add(function);
		resourceModel.setFunctions(functions);
		
		TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${amwfunction.test()}");
		
		// when
		String result = renderTemplate(templateDescriptorEntity, resourceModel);
		
		//then
		assertEquals("val1val2", result);
	}
	
	@Test
	public void testEvaluateFunction_in_function() throws Exception{
		// given
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
		thisProperties.put("foo", new FreeMarkerProperty("val1", "foo"));
		thisProperties.put("bar", new FreeMarkerProperty("val2", "bar"));

		AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
		resourceModel.setProperties(thisProperties);
		ArrayList<AmwFunctionEntity> functions = new ArrayList<AmwFunctionEntity>();
		AmwFunctionEntity function = new AmwFunctionEntity();
		function.setName("test");
		function.setImplementation("<#return (foo + bar + amwfunction.test2())>");
		functions.add(function);
		
		AmwFunctionEntity function2 = new AmwFunctionEntity();
		function2.setName("test2");
		function2.setImplementation("<#return (foo + bar)>");
		functions.add(function2);
		resourceModel.setFunctions(functions);
		
		TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${amwfunction.test()}");
		
		// when
		String result = renderTemplate(templateDescriptorEntity, resourceModel);
		
		//then
		assertEquals("val1val2val1val2", result);
	}
	
	@Test
	public void testEvaluateFunction_deep() throws Exception{
		// given
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
		thisProperties.put("foo", new FreeMarkerProperty("val1", "foo"));
		thisProperties.put("bar", new FreeMarkerProperty("val2", "bar"));
		
        Map<String, FreeMarkerProperty> adProperties = new HashMap<>();
		adProperties.put("foo", new FreeMarkerProperty("val11", "foo"));
		adProperties.put("bar", new FreeMarkerProperty("val22", "bar"));

		ArrayList<AmwFunctionEntity> functions = new ArrayList<AmwFunctionEntity>();
		AmwFunctionEntity function = new AmwFunctionEntity();
		function.setName("test");
		function.setImplementation("<#return (foo + bar)>");
		functions.add(function);
		
		AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
		resourceModel.setProperties(thisProperties);
		
        Map<String, Map<String, AmwResourceTemplateModel>> consumedResTypes = new HashMap<>();
        Map<String, AmwResourceTemplateModel> consumedPerType= new HashMap<>();
		AmwResourceTemplateModel ad = new AmwResourceTemplateModel();
		ad.setProperties(adProperties);
		ad.setFunctions(functions);
		consumedPerType.put("adintern", ad);
		consumedResTypes.put("ActiveDirectory", consumedPerType);
		resourceModel.setConsumedResTypes(consumedResTypes);
		
		TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${consumedResTypes.ActiveDirectory.adintern.amwfunction.test()}");
		
		// when
		String result = renderTemplate(templateDescriptorEntity, resourceModel);
		
		//then
		assertEquals("val11val22", result);
	}
	
	@Test
	public void testEvaluateFunction_fullTemplateModel() throws Exception{
		// given
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
		thisProperties.put("foo", new FreeMarkerProperty("val1", "foo"));
		thisProperties.put("bar", new FreeMarkerProperty("val2", "bar"));
		
        Map<String, FreeMarkerProperty> adProperties = new HashMap<>();
		adProperties.put("foo", new FreeMarkerProperty("val11", "foo"));
		adProperties.put("bar", new FreeMarkerProperty("val22", "bar"));

		ArrayList<AmwFunctionEntity> functions = new ArrayList<AmwFunctionEntity>();
		AmwFunctionEntity function = new AmwFunctionEntity();
		function.setName("test");
		function.setImplementation("<#return (foo + bar + ' ' + appServer.asName + ' ' + node.nodeName)>");
		functions.add(function);
		
		AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
		resourceModel.setProperties(thisProperties);
		
        Map<String, Map<String, AmwResourceTemplateModel>> consumedResTypes = new HashMap<>();
        Map<String, AmwResourceTemplateModel> consumedPerType= new HashMap<>();
		AmwResourceTemplateModel ad = new AmwResourceTemplateModel();
		ad.setProperties(adProperties);
		ad.setFunctions(functions);
		consumedPerType.put("adintern", ad );
		consumedResTypes.put("ActiveDirectory", consumedPerType);
		resourceModel.setConsumedResTypes(consumedResTypes);
		
		TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${appServer.asName} ${node.nodeName} ${consumedResTypes.ActiveDirectory.adintern.amwfunction.test()}");
		
		AmwTemplateModel model = new AmwTemplateModel();
		model.setAmwModelPreprocessExceptionHandler(new AmwModelPreprocessExceptionHandler());

        Map<String, FreeMarkerProperty> properties = new HashMap<>();
		properties.put("foo", new FreeMarkerProperty("val111", "foo"));
		properties.put("bar", new FreeMarkerProperty("val222", "bar"));
        resourceModel.setProperties(properties);
		
		AmwResourceTemplateModel asPropertiesModel = new AmwResourceTemplateModel();
        Map<String, FreeMarkerProperty> asProperties = new HashMap<>();
		asProperties.put("asName", new FreeMarkerProperty("asNameVal", "asName"));
		asPropertiesModel.setProperties(asProperties);
		model.setAsProperties(asPropertiesModel);
		
		AmwResourceTemplateModel nodePropertiesModel = new AmwResourceTemplateModel();
        Map<String, FreeMarkerProperty> nodeProperties = new HashMap<>();
		nodeProperties.put("nodeName", new FreeMarkerProperty("nodeNameVal", "nodeName"));
		nodePropertiesModel.setProperties(nodeProperties);
		model.setNodeProperties(nodePropertiesModel);
		
		model.setUnitResourceTemplateModel(resourceModel);
		model.populateBaseProperties();
		
		// when
		String result = renderTemplate(templateDescriptorEntity, model);
		
		//then
		assertEquals("asNameVal nodeNameVal val11val22 asNameVal nodeNameVal", result);
	}

    @Test
    public void testEvaluatePropertyInProperty() throws Exception{
        // given
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("val1", "foo"));
        thisProperties.put("bar", new FreeMarkerProperty("${foo}", "bar"));

        AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
        resourceModel.setProperties(thisProperties);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${bar}");

        AmwTemplateModel model = new AmwTemplateModel();
        model.setAmwModelPreprocessExceptionHandler(new AmwModelPreprocessExceptionHandler());
        model.setUnitResourceTemplateModel(resourceModel);

        // when
        resourceModel.preProcess(model);
        String result = renderTemplate(templateDescriptorEntity, resourceModel);

        //then
        assertEquals("val1", result);
    }
    
    @Test
    public void testAccessPropertiesAsHash() throws Exception{
        // given
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("val1", "foo"));
        thisProperties.put("bar", new FreeMarkerProperty("val2", "bar"));

        AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
        resourceModel.setProperties(thisProperties);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${amwproperties.bar}${amwproperties.foo}");

        AmwTemplateModel model = new AmwTemplateModel();
        model.setUnitResourceTemplateModel(resourceModel);

        // when
        resourceModel.preProcess(model);
        String result = renderTemplate(templateDescriptorEntity, resourceModel);

        //then
        assertEquals("val2val1", result);
    }
    
    @Test
    public void testAccessPropertiesAsHashListAccess() throws Exception{
        // given
        Map<String, FreeMarkerProperty> thisProperties = new LinkedHashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("val1", "foo"));
        thisProperties.put("bar", new FreeMarkerProperty("val2", "bar"));

        AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
        resourceModel.setProperties(thisProperties);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#list amwproperties?keys as key>${key}:${amwproperties[key]}</#list>");

        AmwTemplateModel model = new AmwTemplateModel();
        model.setUnitResourceTemplateModel(resourceModel);

        // when
        resourceModel.preProcess(model);
        String result = renderTemplate(templateDescriptorEntity, resourceModel);

        //then
        assertEquals("foo:val1bar:val2", result);
    }

    @Test
    public void testAccessToResourceType() throws Exception{
        // given

        ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().name("MyResourceType").build();
        ResourceEntity resource = new ResourceEntityBuilder().withType(resourceType).withName("MyResource").build();

        AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
        resourceModel.setResourceEntity(resource);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${resourceTypeName}");

        AmwTemplateModel model = new AmwTemplateModel();
        model.setUnitResourceTemplateModel(resourceModel);

        // when
        resourceModel.preProcess(model);
        String result = renderTemplate(templateDescriptorEntity, resourceModel);

        //then
        assertEquals("MyResourceType", result);
    }

    @Test
    public void testAccessToResourceType_Null() throws Exception{
        // given
        ResourceEntity resource = new ResourceEntityBuilder().withName("MyResource").build();

        AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
        resourceModel.setResourceEntity(resource);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${resourceTypeName}");

        AmwTemplateModel model = new AmwTemplateModel();
        model.setUnitResourceTemplateModel(resourceModel);

        // when
        resourceModel.preProcess(model);
        String result = renderTemplate(templateDescriptorEntity, resourceModel);

        //then
        assertEquals("", result);
    }

    @Test
    public void testEvaluatePropertyInPropertyOnConsumedResource() throws Exception{
        // given
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("val1", "foo"));
        thisProperties.put("bar", new FreeMarkerProperty("${foo}", "bar"));

        AmwResourceTemplateModel consumedResourceModel = new AmwResourceTemplateModel();
        consumedResourceModel.setProperties(thisProperties);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${conResource.bar}");

        AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();

        Map<String, AmwResourceTemplateModel> consumedResource = new LinkedHashMap<>();
        consumedResource.put("conResource", consumedResourceModel);

        Map<String, Map<String, AmwResourceTemplateModel>> consumedResTypes = new LinkedHashMap<>();
        consumedResTypes.put("ConType", consumedResource);

        resourceModel.setConsumedResTypes(consumedResTypes);


        AmwTemplateModel model = new AmwTemplateModel();
        model.setAmwModelPreprocessExceptionHandler(new AmwModelPreprocessExceptionHandler());
        model.setUnitResourceTemplateModel(resourceModel);

        // when
        resourceModel.preProcess(model);
        String result = renderTemplate(templateDescriptorEntity, resourceModel);

        //then
        assertEquals("val1", result);
    }

    @Test
    public void testEvaluateMikFunction() throws Exception{
        // given
        PropertyDescriptorEntity desc = new PropertyDescriptorEntity();
        desc.setPropertyName("mikProperty");
        desc.setMachineInterpretationKey("MIK1");
        FreeMarkerProperty mik = new FreeMarkerProperty(null, desc);

        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("val1", "foo"));
        thisProperties.put("bar", new FreeMarkerProperty("val2", "bar"));
        thisProperties.put("mikProperty", mik);

        AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
        resourceModel.setProperties(thisProperties);
        ArrayList<AmwFunctionEntity> functions = new ArrayList<AmwFunctionEntity>();
        AmwFunctionEntity function = new AmwFunctionEntity();
        function.addMik(new MikEntity("MIK1", function));
        function.setName("test");
        function.setImplementation("<#return (foo + bar)>");
        functions.add(function);
        resourceModel.setFunctions(functions);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${mikProperty}");

        AmwTemplateModel model = new AmwTemplateModel();
        model.setAmwModelPreprocessExceptionHandler(new AmwModelPreprocessExceptionHandler());
        model.setUnitResourceTemplateModel(resourceModel);

        // when
        resourceModel.preProcess(model);
        String result = renderTemplate(templateDescriptorEntity, resourceModel);

        //then
        assertEquals("val1val2", result);
    }

    @Test
    public void testEvaluateMikFunctionInAConsumedResource() throws Exception{
        // given
        PropertyDescriptorEntity desc = new PropertyDescriptorEntity();
        desc.setPropertyName("mikProperty");
        desc.setMachineInterpretationKey("MIK1");
        FreeMarkerProperty mik = new FreeMarkerProperty(null, desc);

        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("val1", "foo"));
        thisProperties.put("bar", new FreeMarkerProperty("val2", "bar"));
        thisProperties.put("mikProperty", mik);

        AmwResourceTemplateModel consumedResourceModel = new AmwResourceTemplateModel();
        consumedResourceModel.setProperties(thisProperties);
        ArrayList<AmwFunctionEntity> functions = new ArrayList<AmwFunctionEntity>();
        AmwFunctionEntity function = new AmwFunctionEntity();
        function.addMik(new MikEntity("MIK1", function));
        function.setName("test");
        function.setImplementation("<#return (foo + bar)>");
        functions.add(function);
        consumedResourceModel.setFunctions(functions);


        AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();

        Map<String, AmwResourceTemplateModel> consumedResource = new LinkedHashMap<>();
        consumedResource.put("mikResource", consumedResourceModel);

        Map<String, Map<String, AmwResourceTemplateModel>> consumedResTypes = new LinkedHashMap<>();
        consumedResTypes.put("MikType", consumedResource);

        resourceModel.setConsumedResTypes(consumedResTypes);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${mikResource.mikProperty}");

        AmwTemplateModel model = new AmwTemplateModel();
        model.setAmwModelPreprocessExceptionHandler(new AmwModelPreprocessExceptionHandler());
        model.setUnitResourceTemplateModel(resourceModel);

        // when
        resourceModel.preProcess(model);
        String result = renderTemplate(templateDescriptorEntity, resourceModel);

        //then
        assertEquals("val1val2", result);
    }
	
	private TemplateDescriptorEntity createTemplate(String templateContent) {
		TemplateDescriptorEntity templateDescriptorEntity = new TemplateDescriptorEntity();
		templateDescriptorEntity.setName("foo");
		templateDescriptorEntity.setFileContent(templateContent);
		templateDescriptorEntity.setTargetPath("foo");
		return templateDescriptorEntity;
	}
	
	private String renderTemplate(TemplateDescriptorEntity template, Object model ) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException{
		
		AMWTemplateExceptionHandler handler = new AMWTemplateExceptionHandler();

        Configuration cfg = BaseTemplateProcessor.getConfiguration(handler);
        StringTemplateLoader loader = new StringTemplateLoader();

        loader.putTemplate(template.getName(), template.getFileContent());
        cfg.setTemplateLoader(loader);

        String result = null;

        Writer fileContentWriter = new StringWriter();
        freemarker.template.Template fileContentTemplate = cfg.getTemplate(template.getName());
        fileContentTemplate.process(model, fileContentWriter);
        fileContentWriter.flush();
        result =  fileContentWriter.toString();
        cfg.clearTemplateCache();

        return result;
	}
}
