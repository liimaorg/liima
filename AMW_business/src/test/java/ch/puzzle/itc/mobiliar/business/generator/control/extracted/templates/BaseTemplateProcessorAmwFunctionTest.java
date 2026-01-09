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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntityBuilder;
import ch.puzzle.itc.mobiliar.business.generator.control.AmwModelPreprocessExceptionHandler;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwTemplateModel;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;

import java.util.HashMap;

import freemarker.template.TemplateException;


public class BaseTemplateProcessorAmwFunctionTest {

    BaseTemplateProcessor processor = new BaseTemplateProcessor();

    public static final String FUNCTION_INT_RESULT_TEMPLATE_FILENAME = "src/test/resources/test-data/business/generator/control/extracted/templates/amwFunction/function_int_result.ftl";
    public static final String FUNCTION_STRING_RESULT_TEMPLATE_FILENAME = "src/test/resources/test-data/business/generator/control/extracted/templates/amwFunction/function_string_result.ftl";
    public static final String FUNCTION_FREEMARKER_PROPERTY_FILE_FILENAME = "src/test/resources/test-data/business/generator/control/extracted/templates/amwFunction/function_create_propertyfile_property.ftl";
    
    private AmwModelPreprocessExceptionHandler amwModelPreprocessExceptionHandler;
    
    @BeforeEach
    public void setUp(){
    	amwModelPreprocessExceptionHandler = new AmwModelPreprocessExceptionHandler();
    }

    @Test
    public void shouldReturnIntFunction() throws IOException, TemplateException {
    	// given
        String content = new String(Files.readAllBytes(Paths.get(FUNCTION_INT_RESULT_TEMPLATE_FILENAME)), StandardCharsets.UTF_8);
		AmwFunctionEntity amwFunction = createAmwFunction("function", content);

        PropertyDescriptorEntity des1 = new PropertyDescriptorEntity();
        des1.setPropertyName("foo");

        PropertyDescriptorEntity des2 = new PropertyDescriptorEntity();
        des2.setPropertyName("bar");

        Map<String, FreeMarkerProperty> dataProperties = new HashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("1", des1));
        dataProperties.put("bar", new FreeMarkerProperty("2",des2));

        Map<String, Object> thisProperties = new HashMap<>();
        thisProperties.put("this", dataProperties);

        AmwTemplateModel model = new AmwTemplateModel();
        AmwResourceTemplateModel resourceTemplateModel = new AmwResourceTemplateModel();
        resourceTemplateModel.setProperties(dataProperties);
        model.setUnitResourceTemplateModel(resourceTemplateModel);
        
        // when
        String result = processor.evaluateAmwFunction(amwFunction, model, amwModelPreprocessExceptionHandler);
        
        // then
        assertEquals("3", result);
    }

    @Test
    public void shouldReturnStringFunction() throws IOException, TemplateException {
        // given
        String content = new String(Files.readAllBytes(Paths.get(FUNCTION_STRING_RESULT_TEMPLATE_FILENAME)), StandardCharsets.UTF_8);
        AmwFunctionEntity amwFunction = createAmwFunction("function", content);

        PropertyDescriptorEntity des1 = new PropertyDescriptorEntity();
        des1.setPropertyName("foo");

        PropertyDescriptorEntity des2 = new PropertyDescriptorEntity();
        des2.setPropertyName("bar");

        Map<String, FreeMarkerProperty> dataProperties = new HashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("1", des1));
        dataProperties.put("bar", new FreeMarkerProperty("2",des2));

        AmwTemplateModel model = new AmwTemplateModel();
        AmwResourceTemplateModel resourceTemplateModel = new AmwResourceTemplateModel();
        resourceTemplateModel.setProperties(dataProperties);
        model.setUnitResourceTemplateModel(resourceTemplateModel);
        
        // when
        
        String result = processor.evaluateAmwFunction(amwFunction, model, amwModelPreprocessExceptionHandler);

        // then
        assertEquals("test12", result);
    }

    @Test
    public void shouldReturnA_PropertyFile() throws  IOException, TemplateException {
        // given
        String content = new String(Files.readAllBytes(Paths.get(FUNCTION_FREEMARKER_PROPERTY_FILE_FILENAME)), StandardCharsets.UTF_8);
        AmwFunctionEntity amwFunction = createAmwFunction("function", content);

        PropertyDescriptorEntity des1 = new PropertyDescriptorEntity();
        des1.setPropertyName("foo");

        PropertyDescriptorEntity des2 = new PropertyDescriptorEntity();
        des2.setPropertyName("bar");

        Map<String, FreeMarkerProperty> dataProperties = new java.util.LinkedHashMap<>();
        dataProperties.put("foo", new FreeMarkerProperty("value1", des1));
        dataProperties.put("bar", new FreeMarkerProperty("value2",des2));


        AmwTemplateModel model = new AmwTemplateModel();
        AmwResourceTemplateModel resourceTemplateModel = new AmwResourceTemplateModel();
        resourceTemplateModel.setProperties(dataProperties);
        model.setAsProperties(resourceTemplateModel);

        // when
        String result = processor.evaluateAmwFunction(amwFunction, model, amwModelPreprocessExceptionHandler);

        // then
        assertEquals("foo=value1\n" +
                "bar=value2\n", result);

    }
	
	private AmwFunctionEntity createAmwFunction(String name, String content){
        AmwFunctionEntity amwFunctionEntity = new AmwFunctionEntityBuilder(name, Integer.valueOf(1)).withImplementation(content).build();
		return amwFunctionEntity;
	}
}
