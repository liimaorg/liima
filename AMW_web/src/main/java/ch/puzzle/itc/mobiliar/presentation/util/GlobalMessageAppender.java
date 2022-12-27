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

import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.apache.commons.text.StringEscapeUtils;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import java.util.List;

/**
 * @author oschmid
 *
 */
public class GlobalMessageAppender {

	/**
	 * Show Success message
	 * 
	 * @param message
	 */
	public static void addSuccessMessage(String message) {
		// WICHTIG: SEVERITY_INFO wird als Success verwendet
		setMessage(message, FacesMessage.SEVERITY_INFO);
	}
	
	/**
	 * Adds the exception message to the display.
	 * @param exception
	 */
	public static void addErrorMessage(Exception exception){
		addErrorMessage(exception.getMessage());
		//TODO log!
	}
	
	/**
	 * Show Error message and keep it also during a Redirect HTTP 302
	 * 
	 * @param message
	 */
	public static void addErrorMessage(String message) {
		setMessage(message, FacesMessage.SEVERITY_ERROR);
	}
	
	/**
	 * Fehlermeldung anzeigen
	 */
	public static void addErrorMessage(AMWException exception) {
		setMessage(exception.getMessage(), FacesMessage.SEVERITY_ERROR);
	}
	
	/**
	 * 
	 * @param keepForRedirect true if the messages should be kept during a Http 302 Redirect
	 */
	public static void setKeepMessagesforCurrentInstance(boolean keepForRedirect){
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(keepForRedirect);
	}
	
	private static void setMessage(String message, Severity severity){
		String htmlMessage = "";

		FacesMessage facesMessage = null;

		List<FacesMessage> messages = FacesContext.getCurrentInstance()
				.getMessageList();
		for (FacesMessage m : messages) {
			if (m.getSeverity().equals(severity)) {
				facesMessage = m;
				htmlMessage += m.getDetail();
				break;
			}
		}

		htmlMessage += "<li>" + StringEscapeUtils.escapeHtml4(message) + "</li>";

		if (facesMessage == null) {
			facesMessage = new FacesMessage(severity, htmlMessage, htmlMessage);
			FacesContext.getCurrentInstance().addMessage(null, facesMessage);
			// FIXME: use setKeepMessagesforCurrentInstance methode instead outside where the message is added 
			setKeepMessagesforCurrentInstance(true);
		} else {
			facesMessage.setDetail(htmlMessage);
			facesMessage.setSummary(htmlMessage);
		}
	}

}
