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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.BaseTemplateProcessor;
import org.junit.Test;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class AmwTemplateModelTest {

	@Test
	public void shouldGenerate_thisProperties() throws IOException, TemplateException {
		// given
		
		Map<String, FreeMarkerProperty> thisProperties = new HashMap<>();
		thisProperties.put("name", new FreeMarkerProperty("namevalue", "name"));
		
		Map<String, FreeMarkerProperty> asProperties = new HashMap<>();
		asProperties.put("name", new FreeMarkerProperty("asName", "name"));
		
		AmwResourceTemplateModel asPropertiesModel = new AmwResourceTemplateModel();
		asPropertiesModel.setProperties(asProperties);
		
		Map<String, FreeMarkerProperty> nodeProperties = new HashMap<>();
		nodeProperties.put("name", new FreeMarkerProperty("nodeName", "name"));
		
		AmwResourceTemplateModel nodePropertiesModel = new AmwResourceTemplateModel();
		nodePropertiesModel.setProperties(nodeProperties);
		
		AmwTemplateModel modell = new AmwTemplateModel();
		AmwResourceTemplateModel unitResourceTemplateModel = new AmwResourceTemplateModel();
		unitResourceTemplateModel.setProperties(thisProperties);
		modell.setUnitResourceTemplateModel(unitResourceTemplateModel );
		modell.setAsProperties(asPropertiesModel);
		modell.setNodeProperties(nodePropertiesModel);
		
		// when
		String result = generate("${appServer.name} ${node.name} ${name}", modell);
		
		// then
		assertEquals("asName nodeName namevalue", result);

	}
	
	
	
	private String generate(String testTemplateContent, AmwTemplateModel data) throws IOException, TemplateException {
        Configuration cfg = BaseTemplateProcessor.getConfiguration(new AMWTemplateExceptionHandler());
        StringTemplateLoader loader = new StringTemplateLoader();

        String testTemplateName = "template";

        loader.putTemplate(testTemplateName,testTemplateContent);
        cfg.setTemplateLoader(loader);

        Writer fileContentWriter = new StringWriter();
        Template t = cfg.getTemplate(testTemplateName);
        // generate
        t.process(data, fileContentWriter);

        fileContentWriter.flush();
        return fileContentWriter.toString();
    }

}
