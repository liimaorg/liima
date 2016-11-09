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

import java.util.List;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class AmwFunctionsModel implements TemplateHashModel {

	private List<AmwFunctionEntity> functions;
	private AmwResourceTemplateModel amwResourceTemplateModel;
	private AmwTemplateModel baseModelForContextSwitch;

	public AmwFunctionsModel(List<AmwFunctionEntity> functions, AmwResourceTemplateModel amwResourceTemplateModel, AmwTemplateModel baseModelForContextSwitch) {
		this.functions = functions;
		this.amwResourceTemplateModel = amwResourceTemplateModel;
		this.baseModelForContextSwitch = baseModelForContextSwitch;
	}

	@Override
	public TemplateModel get(String key) throws TemplateModelException {
		if (key == null) {
			throw new TemplateModelException(
					"the Functionname must be set, but null given.");
		}
		AmwFunctionEntity correctFunction = null;

		for (AmwFunctionEntity function : functions) {
			if (key.equals(function.getName())) {
				correctFunction = function;
			}
		}
		// TODO: add Exceptions to the Exceptionhandler
		if (correctFunction != null) {
			// context Wechsel
			AmwTemplateModel model = AmwTemplateModelHelper.getAmwTemplateModelContextSwitched(baseModelForContextSwitch, amwResourceTemplateModel);
			// return the given Function to be evaluated
			return new AmwFunctionModel(correctFunction, model);

		} else {
			throw new TemplateModelException(
					"No Function found with given function name: " + key);
		}
	}

	@Override
	public boolean isEmpty() throws TemplateModelException {
		if (functions != null && !functions.isEmpty()) {
			return true;
		}
		return false;
	}

}
