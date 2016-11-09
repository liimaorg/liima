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

import java.io.IOException;
import java.util.List;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.BaseTemplateProcessor;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class AmwFunctionModel implements TemplateMethodModelEx {

	private AmwFunctionEntity function;
	private AmwTemplateModel model;

	public AmwFunctionModel(AmwFunctionEntity function, AmwTemplateModel model) {
		this.function = function;
		this.model = model;
	}

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		// evaluate Function
		BaseTemplateProcessor processor = new BaseTemplateProcessor();
		// TODO: add Exceptions to the Exceptionhandler
		try {
			return new SimpleScalar(processor.evaluateAmwFunction(function, model, model.getAmwModelPreprocessExceptionHandler()));
		} catch (IOException | TemplateException e) {
			throw new TemplateModelException("Error evaluating function: "
					+ function.getName(), e);
		}
	}
}
