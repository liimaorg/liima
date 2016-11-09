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

package ch.puzzle.itc.mobiliar.business.template.control;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.puzzle.itc.mobiliar.common.exception.AMWException;

public class FreemarkerSyntaxValidatorTest {

	FreemarkerSyntaxValidator validator;

	@Before
	public void setUp() throws Exception {
		validator = new FreemarkerSyntaxValidator();
	}

	@Test
	public void testValidateFreemarkerSyntax() throws AMWException {
		// given
		String template = "<#if consumedResTypes.Webservice[webServiceName]??>"
				+ "<#assign webService = consumedResTypes.Webservice[webServiceName] >"
				+ "<#else>"
				+ "<#stop webServiceName+\" not found in \"+.template_name+\" writeURL\" >"
				+ "</#if>";
		validator.validateFreemarkerSyntax(template);
	}

	@Test
	public void testValidateFreemarkerSyntaxIncompleteIf() {
		// given
		String template = "<#if"
				+ "<#assign webService = consumedResTypes.Webservice[webServiceName] ></#if>";
		try {
			validator.validateFreemarkerSyntax(template);
			Assert.fail("There should have been an exception...");
		}
		catch (AMWException e) {
			String expectedException = "Check if you have a valid #if-#elseif-#else structure.";
			Assert.assertTrue(e.getMessage().contains(expectedException));
		}
	}
	
	@Test
	public void testValidateFreemarkerSyntaxIncompleteArray() {
		// given
		String template = "<#assign webService = consumedResTypes.Webservice[webSer >";
		try {
			validator.validateFreemarkerSyntax(template);
			Assert.fail("There should have been an exception...");
		}
		catch (AMWException e) {
			Assert.assertTrue(e.getMessage().contains("Encountered \">\""));
			Assert.assertTrue(e.getMessage().contains("but was expecting one of"));
			Assert.assertTrue(e.getMessage().contains("\"]\""));
		}
	}

    @Test(expected = NullPointerException.class)
    public void testValidateFreemarkerSyntaxWithNull() throws AMWException {
        // given
        String template = null;

        // when
        validator.validateFreemarkerSyntax(template);
    }
	
	/**
	 * If there are no freemarker elements - this is valid as well
	 * @throws AMWException
	 */
	@Test
	public void testValidateFreemarkerNoFreemarkerContent() throws AMWException {
		// given
		String template = "hello world";
		validator.validateFreemarkerSyntax(template);		
	}

	@Test
	public void testValidateFreemarkerSyntaxIncorrectIf() {
		// given
		String template = "<#if hostName???>";
		try {
			validator.validateFreemarkerSyntax(template);
			Assert.fail("There should have been an exception...");
		}
		catch (AMWException e) {
			Assert.assertTrue(e.getMessage().contains("Encountered \">\""));
			Assert.assertTrue(e.getMessage().contains("but was expecting"));
			Assert.assertTrue(e.getMessage().contains("<ID>"));
		}
	}
	
	@Test
	public void testValidateFreemarkerSyntaxMacro() throws AMWException {
		// given
		String template = "<#macro writeURL webServiceName><#assign loadBalancer=consumedResTypes.JspLoadBalancer.jspLoadBalancer>${url}</#macro>";
		validator.validateFreemarkerSyntax(template);		
	}
	
	@Test
	public void testValidateFreemarkerSyntaxNoClosingTag() throws AMWException {
		// given
		String template = "<#macro writeURL webServiceName><#assign loadBalancer=consumedResTypes.JspLoadBalancer.jspLoadBalancer>${url}";
		try {
			validator.validateFreemarkerSyntax(template);
			Assert.fail("There should have been an exception...");
		}
		catch (AMWException e) {
			Assert.assertTrue(e.getMessage().contains("Unexpected end of file reached"));
            Assert.assertTrue(e.getMessage().contains("You have an unclosed"));
            Assert.assertTrue(e.getMessage().contains("#function"));
            Assert.assertTrue(e.getMessage().contains("#macro"));
		}
	}
	
	@Test
	public void testValidateFreemarkerSyntaxIncompletePlaceholder() throws AMWException {
		// given
		String template = "${url";
		try {
			validator.validateFreemarkerSyntax(template);
			Assert.fail("There should have been an exception...");
		}
		catch (AMWException e) {
			Assert.assertTrue(e.getMessage().contains("Unexpected end of file reached"));
		}
	}
	
	
	/**
	 * Although "something" doesn't exist, the syntax validation should pass just fine. 
	 * @throws AMWException
	 */
	@Test
	public void testValidateFreemarkerSyntaxInclude() throws AMWException {
		// given
		String template = "<#include something>";
		validator.validateFreemarkerSyntax(template);
	}
}
