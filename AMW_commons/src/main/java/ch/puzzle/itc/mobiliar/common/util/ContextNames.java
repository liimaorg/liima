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

package ch.puzzle.itc.mobiliar.common.util;

import java.util.Comparator;

public enum ContextNames {
	ENV("Environment", null), DOMAIN("Domain", ENV), GLOBAL("Global", DOMAIN);
	
	private ContextNames childContext;
	private String displayName;
	
	private ContextNames(String displayName, ContextNames childContext){
		this.displayName = displayName;
		this.childContext = childContext;
	}
	
	public String getDisplayName(){
		return displayName;
	}

	public ContextNames getChildContext() {
		return childContext;
	}

     public boolean isChildContext(ContextNames context){
	    return isChildContextRec(this, context);
	}

     private boolean isChildContextRec(ContextNames parent, ContextNames context){
	    if(parent.getChildContext()==null){
		   return false;
	    } else if(parent.getChildContext()==context){
		   return true;
	    } else{
		   return isChildContextRec(parent.getChildContext(), context);
	    }
	}

     public final static Comparator<ContextNames> COMPARE_BY_HIERARCHY = new Comparator<ContextNames>() {
	    @Override public int compare(ContextNames o1, ContextNames o2) {
		  if(o1==null){
			 if(o2==null){
				return 0;
			 }
			 else{
				return -1;
			 }
		  }
		  else{
			 if(o2==null){
				return 1;
			 }
			 else{
				return o1.isChildContext(o2) ? 1 : -1;
			 }
		  }
	    }
	};

}
