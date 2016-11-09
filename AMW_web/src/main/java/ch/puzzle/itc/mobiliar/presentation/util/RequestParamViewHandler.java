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

import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

//http://www.ninthavenue.com.au/preserving-jsf-request-parameters-and-rest-urls
public class RequestParamViewHandler extends ViewHandlerWrapper {

	private ViewHandler wrapped;
	
	public RequestParamViewHandler(ViewHandler wrapped) {
        this.wrapped = wrapped;
    }
	
	@Override
	public ViewHandler getWrapped() {
		return wrapped;
	}
	
	/** 
	 * We always post back to the original Request URL, not the viewID
	 * since we sometimes encode state in the Request URL such as object id,
	 * page number, etc.
	 */
	@Override
	public String getActionURL(FacesContext faces, String viewID) {
	    HttpServletRequest request = (HttpServletRequest) 
	            faces.getExternalContext().getRequest();

	    // remaining on the same view keeps URL state 
	    String requestViewID = request.getRequestURI().substring(
	            request.getContextPath().length());
	    if (requestViewID.equals(viewID)) {

	        // keep RESTful URLs and query strings
	        String action = (String) request.getAttribute(
	                RequestDispatcher.FORWARD_REQUEST_URI);
	        if (action == null) {
	            action = request.getRequestURI();
	        }
	        if (request.getQueryString() != null) {
	            return action + "?" + request.getQueryString();
	        } else {
	            return action;
	        }
	    } else {
	        // moving to a new view drops old URL state 
	        return super.getActionURL(faces, viewID);
	    }
	}



}
