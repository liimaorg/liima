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

import java.io.IOException;
import java.util.Objects;

import javax.ejb.Stateless;

import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;

@Stateless
public class FreemarkerSyntaxValidator {

	/**
	 * Validates if the given freemarker context is syntactically correct.
	 * 
	 * @param freemarkerContent
	 * @throws AMWException
	 *              if the template can not be successfully validate. The error message distincts between
	 *              parsing exceptions and other (unexpected) potential issues.
	 */
	public void validateFreemarkerSyntax(String freemarkerContent) throws AMWException {
        Objects.requireNonNull(freemarkerContent, "freemarker content must not be null");
		Configuration c = new Configuration();
		c.setStrictSyntaxMode(true);
		c.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
		String templateName = "validation";

		StringTemplateLoader stringLoader = new StringTemplateLoader();
		stringLoader.putTemplate(templateName, freemarkerContent);
		c.setTemplateLoader(stringLoader);
		try {
			c.getTemplate(templateName);
		}
		catch (ParseException e) {
			// Validation failed! - was not able to parse the template!
			throw new AMWException("The template is syntactically incorrect: " + e.getMessage(), e);
		}
		catch (IOException e) {
			// Something else went wrong
			throw new AMWException("The template can not be validated: " + e.getMessage(), e);
		}
	}

}
