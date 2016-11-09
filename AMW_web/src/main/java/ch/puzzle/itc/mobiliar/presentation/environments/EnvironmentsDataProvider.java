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

package ch.puzzle.itc.mobiliar.presentation.environments;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.common.util.ContextNames;
import ch.puzzle.itc.mobiliar.presentation.CompositeBackingBean;
import ch.puzzle.itc.mobiliar.presentation.common.ContextDataProvider;
import ch.puzzle.itc.mobiliar.presentation.security.SecurityDataProvider;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

@CompositeBackingBean
public class EnvironmentsDataProvider implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String newName;
	private ContextEntity currentContext;
	
	@Inject
	private EnvironmentsController controller;

	@Inject
	private ContextDataProvider context;
	
	@Inject
	private SecurityDataProvider roleDataProvider;


	private Map<Integer, String> currentContextTypeNames;

	private String getCurrentContextTypeName() {
		String contextTypeName = null;
		if (currentContextTypeNames == null) {
			currentContextTypeNames = new TreeMap<Integer, String>();
		}
		if (!currentContextTypeNames.containsKey(context.getContextId())) {
			currentContext = controller.loadContextWithType(context.getContextId());
			contextTypeName = currentContext.getContextType().getName();
			currentContextTypeNames.put(context.getContextId(), contextTypeName);
		} else {
			contextTypeName = currentContextTypeNames.get(context.getContextId());
		}

		return contextTypeName;

	}
	
	public ContextEntity getCurrentContext(){
		return currentContext;
	}

	public boolean getIsEnv() {
		ContextNames contextNames = ContextNames.valueOf(getCurrentContextTypeName());
		return contextNames != null && contextNames.name().equals(ContextNames.ENV.name());
	}

	public String getNameOfChildContext() {
		if (context.getContextDisplayName() != null) {
			ContextNames c = ContextNames.valueOf(getCurrentContextTypeName());
			if (c != null && c.getChildContext() != null) {
				return c.getChildContext().getDisplayName();
			}
		}
		return "sub context";
	}

	public String getContextTypeName() {
		ContextNames contextNames = ContextNames.valueOf(getCurrentContextTypeName());
		if (contextNames != null) {
			return contextNames.name();
		}
		return "Context";
	}

	public void save() {
		if (controller.doSave(context.getContextId(), context.getContextDisplayName())) {
			context.loadContexts();
		}
	}

	public void createContext() {
		Integer selectedRole = null;
		//Wenn ein 
		if(roleDataProvider.getRoleSelectedId() != null){
			selectedRole = roleDataProvider.getRoleSelectedId();
		} else {
			selectedRole = 0;
		}
		
		if (controller.doCreateContext(newName, context.getContextId(),selectedRole)) {
			context.loadContexts();
		}
		resetPopupFields();
	}


	public void removeContext() {
	
		if (controller.doRemoveContext(context.getContextId())) {			
			if (currentContextTypeNames!=null && currentContextTypeNames.containsKey(context.getContextId())) {
				currentContextTypeNames.remove(context.getContextId());
			}
			context.loadContexts();
			setToGlobal();				
		}
		resetPopupFields();
	}

	private void setToGlobal() {
		context.setContextId(context.getGlobalContextId());
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	private void resetPopupFields(){
		newName = null;
	}


}
