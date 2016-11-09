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

package ch.puzzle.itc.mobiliar.business.environment.entity;

import java.util.*;

public abstract class HasContexts<T extends ContextDependency<?>> {

	public abstract Integer getId();
	/**
	 * @param context
	 * @return
	 */
	public List<T> getContextsByLowestContext(ContextEntity context) {
		List<T> result = new ArrayList<T>();
		if (getContexts() != null && context != null) {
			addToContextList(result, context);
		}
		return result;
	}
		
	private void addToContextList(List<T> list, ContextEntity context) {
		if (context != null) {
			T c = getContext(context);
			if (c != null) {
				list.add(c);
			}
			if (context.getParent() != null) {
				addToContextList(list, context.getParent());
			}
		}
	}
	
	//TODO: queries reduzieren
	protected T getContext(ContextEntity context) {
		if (getContexts() != null) {
			for (T c : getContexts()) {  //lädt alle context, obwohl nur einer gebraucht wird. Wird lazy von der DB geladen.
				if (c != null && c.getContext() != null && c.getContext().getId().equals(context.getId())) {
					return c;
				}
			}
		}
		return null;
	}	
	
	/**
	 * returns e given Context if available or creates the context
	 * 
	 * @param context
	 * @return
	 */
	public T getOrCreateContext(ContextEntity context){
		T c = getContext(context);
		if(c==null){
			c = createContext();
			c.setContext(context);
			addContext(c);
		}
		return c;
	}
	
	public abstract T createContext();
	
	public abstract Set<T> getContexts();
	
	public abstract void setContexts(Set<T> contexts);
	
	public void addContext(T context) {
		if (getContexts() == null) {
			setContexts(new HashSet<T>());
		}
		getContexts().add(context);
	}

    /**
     * Replaces getContexts call but ensures that if list not yet initialized then empty list will be returned
     */
    public Set<T> getNullSaveContexts(){
        if (getContexts() == null) {
            return Collections.unmodifiableSet(new HashSet<T>());
        }
        return getContexts();
    }
}
