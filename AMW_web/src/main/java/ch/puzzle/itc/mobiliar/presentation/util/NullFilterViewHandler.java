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

package ch.puzzle.itc.mobiliar.presentation.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;

/**
 * 
 * ViewHandlerWrapper that removes null values from view parameters.
 * Used to make the GUI compatible with JSF 1.2, see https://java.net/jira/browse/JAVASERVERFACES-3154.
 * The removed parameters are logged on fine.
 *
 */
public class NullFilterViewHandler extends ViewHandlerWrapper {

	private ViewHandler parent;
	private final static Logger LOGGER = Logger.getLogger(NullFilterViewHandler.class.getName());

	public NullFilterViewHandler(ViewHandler handler) {
		this.parent = handler;
	}

	@Override
	public ViewHandler getWrapped() {
		return parent;
	}

	@Override
	public String getBookmarkableURL(FacesContext context, String viewId, Map<String, List<String>> parameters, boolean includeViewParams) {
		return super.getBookmarkableURL(context, viewId, filterNullParams(viewId, parameters), includeViewParams);
	}

	@Override
	public String getRedirectURL(FacesContext context, String viewId, Map<String, List<String>> parameters, boolean includeViewParams) {
		return super.getRedirectURL(context, viewId, filterNullParams(viewId, parameters), includeViewParams);
	}

	private Map<String, List<String>> filterNullParams(String viewId, Map<String, List<String>> parameters) {
		if(parameters == null) {
			return null;
		}
		Map<String, List<String>> result = new HashMap<>();

		for (String param : parameters.keySet()) {
			for (String paramVale : parameters.get(param)) {
				if (paramVale == null) {
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.fine(viewId + " " + param +": removed null value");
					}
					continue;
				}
				
				if(!result.containsValue(param)) {
					result.put(param, new ArrayList<String>());
				}
				result.get(param).add(paramVale);
			}
		}

		return result;
	}
}
