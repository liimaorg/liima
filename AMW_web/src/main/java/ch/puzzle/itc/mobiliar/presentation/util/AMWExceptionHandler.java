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

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.http.HttpServletRequest;

import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;

public class AMWExceptionHandler extends ExceptionHandlerWrapper {
	
	private ExceptionHandler wrapped;
	
	private Logger log = Logger.getLogger(AMWExceptionHandler.class.getSimpleName());

	public AMWExceptionHandler(ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return wrapped;
	}

	@Override
	public void handle() throws FacesException {
		Iterator i = getUnhandledExceptionQueuedEvents().iterator();
		while (i.hasNext()) {
			ExceptionQueuedEvent event = (ExceptionQueuedEvent) i.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
			Throwable t = context.getException();
			try {
				if (t instanceof ViewExpiredException) {
			    	FacesContext fc = FacesContext.getCurrentInstance();
			    	HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
					log.info("ViewExpired on "+ request.getRequestURI()+"?"+ request.getQueryString()+" for user "+request.getRemoteUser()+", reloading");

			    	GlobalMessageAppender.addErrorMessage("Could not send request because the view expired.");
			    	GlobalMessageAppender.addErrorMessage("Automatically reloaded the page, data may have been lost!");
			    	try {
			    		// reload the page
						fc.getExternalContext().redirect(request.getRequestURI()+"?"+ request.getQueryString());
					} catch (IOException e) {
						e.printStackTrace();
					}
			    	fc.renderResponse();
				}
				else if(t instanceof RuntimeException){
					log.log(Level.SEVERE, "An Error occured", t);
			    	AMWRuntimeException ex = findAMWRuntimeException(t);
			    	Throwable exception = ex==null ? t : ex;
			    	FacesContext fc = FacesContext.getCurrentInstance();
			    	fc.getExternalContext().getSessionMap().put(ErrorPage.EXCEPTIONMESSAGE_PARAM, exception.getMessage());
			    	fc.getExternalContext().getSessionMap().put(ErrorPage.STACKTRACE_PARAM, t.getStackTrace());
			    	fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "error?faces-redirect=true");
			    	fc.renderResponse();
				}
				else{
					// log error
					GlobalMessageAppender.addErrorMessage("Technical error: "+t.getMessage());
					log.log(Level.WARNING, "An Error occured", t);
				}
			}
			finally {
				i.remove();
			}
		}
		getWrapped().handle();
	}

     public AMWRuntimeException findAMWRuntimeException(Throwable e){
	    if(e.getCause()==null){
		   return null;
	    }
	    if(e.getCause() instanceof AMWRuntimeException){
		   return (AMWRuntimeException)e.getCause();
	    }
	    else{
		   return findAMWRuntimeException(e.getCause());
	    }
	}
}
