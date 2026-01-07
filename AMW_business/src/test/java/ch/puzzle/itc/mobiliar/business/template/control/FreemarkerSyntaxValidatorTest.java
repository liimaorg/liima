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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.common.exception.AMWException;

public class FreemarkerSyntaxValidatorTest {

    FreemarkerSyntaxValidator validator;

    @BeforeEach
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

        // when/then
        AMWException e = assertThrows(AMWException.class, () -> {
            validator.validateFreemarkerSyntax(template);
        });
        assertThat(e.getMessage(), containsString("Encountered \"</#if>\", but was expecting one of these patterns:"));
    }

    @Test
    public void testValidateFreemarkerSyntaxIncompleteArray() {
        // given
        String template = "<#assign webService = consumedResTypes.Webservice[webSer >";

        // when/then
        AMWException e = assertThrows(AMWException.class, () -> {
            validator.validateFreemarkerSyntax(template);
        });
        assertThat(e.getMessage(), containsString("Encountered \">\""));
        assertThat(e.getMessage(), containsString("but was expecting one of"));
        assertThat(e.getMessage(), containsString("\"]\""));
    }

    @Test
    public void testValidateFreemarkerSyntaxWithNull() throws AMWException {

        // given
        String template = null;

        // when
        assertThrows(NullPointerException.class, () -> {
            validator.validateFreemarkerSyntax(template);
        });
    }

    /**
     * If there are no freemarker elements - this is valid as well
     * 
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

        // when/then
        AMWException e = assertThrows(AMWException.class, () -> {
            validator.validateFreemarkerSyntax(template);
        });
        assertThat(e.getMessage(), containsString("Encountered \">\""));
        assertThat(e.getMessage(), containsString("but was expecting"));
        assertThat(e.getMessage(), containsString("<ID>"));
    }

    @Test
    public void testValidateFreemarkerSyntaxMacro() throws AMWException {
        // given
        String template = "<#macro writeURL webServiceName><#assign loadBalancer=consumedResTypes.JspLoadBalancer.jspLoadBalancer>${url}</#macro>";
        validator.validateFreemarkerSyntax(template);
    }

    @Test
    public void testValidateFreemarkerSyntaxNoClosingTag() {
        // given
        String template = "<#macro writeURL webServiceName><#assign loadBalancer=consumedResTypes.JspLoadBalancer.jspLoadBalancer>${url}";

        // when/then
        AMWException e = assertThrows(AMWException.class, () -> {
            validator.validateFreemarkerSyntax(template);
        });
        assertThat(e.getMessage(), containsString("Unexpected end of file reached"));
        assertThat(e.getMessage(), containsString("You have an unclosed"));
        assertThat(e.getMessage(), containsString("#function"));
        assertThat(e.getMessage(), containsString("#macro"));
    }

    @Test
    public void testValidateFreemarkerSyntaxIncompletePlaceholder() {
        // given
        String template = "${url";

        // when/then
        AMWException e = assertThrows(AMWException.class, () -> {
            validator.validateFreemarkerSyntax(template);
        });

        assertThat(e.getMessage(), containsString("Unexpected end of file reached"));
    }

    /**
     * Although "something" doesn't exist, the syntax validation should pass just
     * fine.
     * 
     * @throws AMWException
     */
    @Test
    public void testValidateFreemarkerSyntaxInclude() throws AMWException {
        // given
        String template = "<#include something>";
        validator.validateFreemarkerSyntax(template);
    }
}
