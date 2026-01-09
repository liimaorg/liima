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

import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.generator.control.GenerationUnitGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.container.DeploymentProperties;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwTemplateModel;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

import java.util.HashMap;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BaseTemplateProcessorTest {

    BaseTemplateProcessor processor = new BaseTemplateProcessor();

    public static final String FUNCTION_INT_RESULT_TEMPLATE_FILENAME = "src/test/resources/test-data/business/generator/control/extracted/templates/function_int_result.ftl";
    public static final String FUNCTION_HASH_RESULT_TEMPLATE_FILENAME = "src/test/resources/test-data/business/generator/control/extracted/templates/function_hash_result.ftl";
    public static final String FUNCTION_LIST_RESULT_TEMPLATE_FILENAME = "src/test/resources/test-data/business/generator/control/extracted/templates/function_list_result.ftl";
    public static final String FUNCTION_FREEMARKER_PROPERTY_FILE_FILENAME = "src/test/resources/test-data/business/generator/control/extracted/templates/function_create_propertyfile_property.ftl";
    public static final String FUNCTION_FREEMARKER_PROPERTY_FILE_TAG_FILENAME = "src/test/resources/test-data/business/generator/control/extracted/templates/function_create_propertyfile_tag_property.ftl";
    public static final String FUNCTION_FREEMARKER_MACRO_FILENAME = "src/test/resources/test-data/business/generator/control/extracted/templates/macro_concat_result.ftl";

    PropertyDescriptorEntity des1;
    PropertyDescriptorEntity des2;
    PropertyDescriptorEntity des3;

    @BeforeEach
    public void setUp(){
        des1 = new PropertyDescriptorEntity();
        des1.setPropertyName("foo");

        des2 = new PropertyDescriptorEntity();
        des2.setPropertyName("bar");

        des3 = new PropertyDescriptorEntity();
        des3.setPropertyName("bar2");

    }

    @Test
	public void test() throws IOException {
		// given
		Set<TemplateDescriptorEntity> templates = new HashSet<>();
		Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("bar",des1));

		TemplateDescriptorEntity templateDescriptorEntity = createTemplate("asdf ${foo}");

		templates.add(templateDescriptorEntity);

		GenerationUnit unit = new GenerationUnit(null, null, templates, null);

		// when
		GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

		//then
		GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
		assertEquals("asdf bar", generatedTemplate.getContent());
		assertTrue(result.isSuccess());
	}

    @Test
    public void testPropertynotfound() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("bar",des1));

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${asdf}");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

        //then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("", generatedTemplate.getContent());
        assertFalse(result.isSuccess());
    }

    @Test
    public void testPropertynotfound_descriptor() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("bar",des1));

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("${asdf._descriptor.encrypt}");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

        //then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("", generatedTemplate.getContent());
        assertFalse(result.isSuccess());
    }

    @Test
    public void testFreemarkerFunction_has_content() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", null);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#if foo?has_content>has_content</#if>");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

        //then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }
    @Test
    public void testFreemarkerFunction_length_onAMW_Property() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();

        thisProperties.put("foo", new FreeMarkerProperty("",des1));

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#if ((foo)?length > 0)>has_content</#if>");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

        //then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void testFreemarkerFunction_has_content_onAMW_Property() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();

        thisProperties.put("foo", new FreeMarkerProperty("",des1));

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#if ((foo.currentValue)?has_content)>has_content</#if>");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

        //then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void testFreemarkerFunction_has_content_on_nonexisting_AMW_Property() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#if ((foo.currentValue)?has_content)>has_content</#if>");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(null, null));

        //then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }


    @Test
    public void testFreemarkerTemplate_NoParse() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#noparse>${test}</#noparse>");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

        //then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("${test}", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void shouldFormatNumberCorrectly() throws IOException {
    	// given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("1000000", des1));

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("asdf ${foo}");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("asdf 1000000", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void emptyString() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("", des1));

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("asdf ${foo}");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("asdf ", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }


    @Test
    public void shouldReplaceInnerProperty() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("1000000", des1));
        thisProperties.put("bar", new FreeMarkerProperty("${foo}", des2));

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("asdf ${bar}");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("asdf 1000000", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void shouldReplaceInnerInnerProperty() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();

        Map<String, FreeMarkerProperty> dataProperties = new HashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("1000000", des1));
        dataProperties.put("bar", new FreeMarkerProperty("${foo}", des2));
        dataProperties.put("bar2", new FreeMarkerProperty("${bar}", des3));

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("asdf ${bar2}");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(dataProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("asdf 1000000", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }
    
    
    @Test
    public void shouldUseGlobalFunction() throws IOException {
    	// given
    	String content = "<#function add x y>"
    			+ "		<#return (x + y)>"
    			+ "</#function>";
    	
		GlobalFunctionEntity globalFunction = createGlobalFunctionTemplateEntity("function1", content);

        Set<TemplateDescriptorEntity> templates = new HashSet<>();

        Map<String, FreeMarkerProperty> dataProperties = new HashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("1", des1));
        dataProperties.put("bar", new FreeMarkerProperty("2", des2));
        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#include \"function1\">asdf ${add(foo?number,bar?number)}");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);
        List<GlobalFunctionEntity> globalFunctionTemplates = new ArrayList<>();
        
		globalFunctionTemplates.add(globalFunction);
		unit.setGlobalFunctionTemplates(globalFunctionTemplates);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(dataProperties, null));
        
        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("asdf 3", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void shouldUseGlobalFunction_Access_model() throws IOException {
        // given
        String content = "<#function add>"
                + "		<#return foo?number + bar?number>"
                + "</#function>";

        GlobalFunctionEntity globalFunction = createGlobalFunctionTemplateEntity("function1", content);

        Set<TemplateDescriptorEntity> templates = new HashSet<>();

        Map<String, FreeMarkerProperty> dataProperties = new HashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("1", des1));
        dataProperties.put("bar", new FreeMarkerProperty("2", des2));

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#include \"function1\">asdf ${add()}");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);
        List<GlobalFunctionEntity> globalFunctionTemplates = new ArrayList<>();

        globalFunctionTemplates.add(globalFunction);
        unit.setGlobalFunctionTemplates(globalFunctionTemplates);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(dataProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("asdf 3", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }


    @Test
    public void shouldReturnIntFunction() throws IOException {
        // given
        String content = new String(Files.readAllBytes(Paths.get(FUNCTION_INT_RESULT_TEMPLATE_FILENAME)), StandardCharsets.UTF_8);
        GlobalFunctionEntity globalFunction = createGlobalFunctionTemplateEntity("function", content);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#include \"function\">value=${add()}");

        Map<String, FreeMarkerProperty> dataProperties = new HashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("1", des1));
        dataProperties.put("bar", new FreeMarkerProperty("2", des2));

        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);
        List<GlobalFunctionEntity> globalFunctionTemplates = new ArrayList<>();

        globalFunctionTemplates.add(globalFunction);
        unit.setGlobalFunctionTemplates(globalFunctionTemplates);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(dataProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("value=3", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void shouldReturnListFunction() throws IOException {
        // given
        String content = new String(Files.readAllBytes(Paths.get(FUNCTION_LIST_RESULT_TEMPLATE_FILENAME)), StandardCharsets.UTF_8);
        GlobalFunctionEntity globalFunction = createGlobalFunctionTemplateEntity("function", content);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#include \"function\">value=<#list getList() as entry>${entry}</#list>");

        Map<String, FreeMarkerProperty> dataProperties = new HashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("value1", des1));
        dataProperties.put("bar", new FreeMarkerProperty("value2", des2));


        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);
        List<GlobalFunctionEntity> globalFunctionTemplates = new ArrayList<>();

        globalFunctionTemplates.add(globalFunction);
        unit.setGlobalFunctionTemplates(globalFunctionTemplates);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(dataProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("value=value1value2", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void shouldReturnList_with_freemarker_PropertiesFunction() throws IOException {
        // given
        String content = new String(Files.readAllBytes(Paths.get(FUNCTION_LIST_RESULT_TEMPLATE_FILENAME)), StandardCharsets.UTF_8);
        GlobalFunctionEntity globalFunction = createGlobalFunctionTemplateEntity("function", content);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#include \"function\"><#list getList() as entry>${entry._descriptor.technicalKey}=${entry}\n</#list>");

        Map<String, FreeMarkerProperty> dataProperties = new HashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("fmp1",des1));
        dataProperties.put("bar", new FreeMarkerProperty("fmp2",des2));

        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);
        List<GlobalFunctionEntity> globalFunctionTemplates = new ArrayList<>();

        globalFunctionTemplates.add(globalFunction);
        unit.setGlobalFunctionTemplates(globalFunctionTemplates);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(dataProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("foo=fmp1\nbar=fmp2\n", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void shouldReturnHashFunction() throws IOException {
        // given
        String content = new String(Files.readAllBytes(Paths.get(FUNCTION_HASH_RESULT_TEMPLATE_FILENAME)), StandardCharsets.UTF_8);
        GlobalFunctionEntity globalFunction = createGlobalFunctionTemplateEntity("function", content);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#include \"function\">foo=${getHash().foo}\nbar=${getHash().bar}");

        Map<String, FreeMarkerProperty> dataProperties = new HashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("value1",des1));
        dataProperties.put("bar", new FreeMarkerProperty("value2",des2));


        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);
        List<GlobalFunctionEntity> globalFunctionTemplates = new ArrayList<>();

        globalFunctionTemplates.add(globalFunction);
        unit.setGlobalFunctionTemplates(globalFunctionTemplates);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(dataProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("foo=value1\nbar=value2", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void shouldReturnA_PropertyFile() throws IOException {
        // given
        String content = new String(Files.readAllBytes(Paths.get(FUNCTION_FREEMARKER_PROPERTY_FILE_FILENAME)), StandardCharsets.UTF_8);
        GlobalFunctionEntity globalFunction = createGlobalFunctionTemplateEntity("function", content);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#include \"function\">${getThisPropertiesFileString()}");

        Map<String, FreeMarkerProperty> dataProperties = new java.util.LinkedHashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("value1", des1));
        dataProperties.put("bar", new FreeMarkerProperty("value2",des2));


        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);
        List<GlobalFunctionEntity> globalFunctionTemplates = new ArrayList<>();

        globalFunctionTemplates.add(globalFunction);
        unit.setGlobalFunctionTemplates(globalFunctionTemplates);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(dataProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("foo=value1\nbar=value2\n", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void shouldReturnA_PropertyFileForProperties_with_tag() throws IOException {
        // given
        String content = new String(Files.readAllBytes(Paths.get(FUNCTION_FREEMARKER_PROPERTY_FILE_TAG_FILENAME)), StandardCharsets.UTF_8);
        GlobalFunctionEntity globalFunction = createGlobalFunctionTemplateEntity("function", content);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#include \"function\">${getThisPropertiesFileString_with_Tag_tag1()}");

        PropertyTagEntity tag = new PropertyTagEntity();
        tag.setName("tag1");
        des1.addPropertyTag(tag);

        PropertyTagEntity tag2 = new PropertyTagEntity();
        tag2.setName("tag2");
        des2.addPropertyTag(tag2);

        Map<String, FreeMarkerProperty> dataProperties = new java.util.LinkedHashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("value1", des1));
        dataProperties.put("bar", new FreeMarkerProperty("value2",des2));


        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);
        List<GlobalFunctionEntity> globalFunctionTemplates = new ArrayList<>();

        globalFunctionTemplates.add(globalFunction);
        unit.setGlobalFunctionTemplates(globalFunctionTemplates);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(dataProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("foo=value1\n", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void shouldConcatByMacro() throws IOException {
        // given
        String content = new String(Files.readAllBytes(Paths.get(FUNCTION_FREEMARKER_MACRO_FILENAME)), StandardCharsets.UTF_8);
        GlobalFunctionEntity globalFunction = createGlobalFunctionTemplateEntity("function", content);

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#include \"function\"><@printConcat/>");

        Map<String, FreeMarkerProperty> thisProperties = new java.util.LinkedHashMap<>();
        thisProperties.put("foo", new FreeMarkerProperty("value1", des1));
        thisProperties.put("bar", new FreeMarkerProperty("value2",des2));

        Set<TemplateDescriptorEntity> templates = new HashSet<>();
        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);
        List<GlobalFunctionEntity> globalFunctionTemplates = new ArrayList<>();

        globalFunctionTemplates.add(globalFunction);
        unit.setGlobalFunctionTemplates(globalFunctionTemplates);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(thisProperties, null));

        // then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertEquals("value1value2", generatedTemplate.getContent());
        assertTrue(result.isSuccess());
    }

    @Test
    public void shouldFailCallingNewBuiltIn() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#assign ex = \"freemarker.template.utility.Execute\"?new()>${ex(\"id\")}");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(null, null));

        //then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertFalse(result.isSuccess());
        assertTrue(generatedTemplate.getErrorMessages().size() > 0);
    }

    @Test
    public void shouldFailCallingApiBuiltIn() throws IOException {
        // given
        Set<TemplateDescriptorEntity> templates = new HashSet<>();

        TemplateDescriptorEntity templateDescriptorEntity = createTemplate("<#assign uri=object?api.class.getResource(\"/\").toURI()>${uri}");

        templates.add(templateDescriptorEntity);

        GenerationUnit unit = new GenerationUnit(null, null, templates, null);

        // when
        GenerationUnitGenerationResult result = processor.generateResourceTemplates(unit, getAmwTemplateModel(null, null));

        //then
        GeneratedTemplate generatedTemplate = result.getGeneratedTemplates().get(0);
        assertFalse(result.isSuccess());
        assertTrue(generatedTemplate.getErrorMessages().size() > 0);
    }

	private TemplateDescriptorEntity createTemplate(String templateContent) {
		TemplateDescriptorEntity templateDescriptorEntity = new TemplateDescriptorEntity();
		templateDescriptorEntity.setName("foo");
		templateDescriptorEntity.setFileContent(templateContent);
		templateDescriptorEntity.setTargetPath("foo");
		return templateDescriptorEntity;
	}
	
	private GlobalFunctionEntity createGlobalFunctionTemplateEntity(String name, String content){
		GlobalFunctionEntity globalFunctionTemplate = new GlobalFunctionEntity();
		globalFunctionTemplate.setName(name);
		globalFunctionTemplate.setContent(content);
		return globalFunctionTemplate;
	}


    private AmwTemplateModel getAmwTemplateModel(Map<String, FreeMarkerProperty> thisProperties, Map<String, FreeMarkerProperty> asProperties) {
        Map<String, FreeMarkerProperty> contextProperties = new HashMap<>();

        AmwTemplateModel model = new AmwTemplateModel();
        model.setAsProperties(new AmwResourceTemplateModel());
        model.setNodeProperties(new AmwResourceTemplateModel());
        model.setContextProperties(contextProperties);
        model.setDeploymentProperties(new DeploymentProperties());
        model.setRuntimeProperties(new AmwResourceTemplateModel());
        AmwResourceTemplateModel resourceModel = new AmwResourceTemplateModel();
        resourceModel.setProperties(thisProperties);
        model.setUnitResourceTemplateModel(resourceModel);
        if(asProperties != null) {
            AmwResourceTemplateModel asModel = new AmwResourceTemplateModel();
            asModel.setProperties(asProperties);
            model.setAsProperties(asModel);
        }

        model.populateBaseProperties();

        return model;
    }
}
