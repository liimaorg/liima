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

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NavigationUtils {

	public static boolean isParameterSet(String key, Object value) {
		Object existing = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
		return existing != null && existing.equals(value);
	}

     public static void serverSideReload(){
	    serverSideRedirect(((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getQueryString());
	}

	public static void serverSideRedirect(String... paramKeyValues) {
		Map<String, String> params = new HashMap<>();
		for(String keyValue : paramKeyValues){
			String[] split = keyValue.split("=");
			if(split.length==2){
				params.put(split[0], split[1]);
			}
		}
		serverSideRedirect(params);
	}

	public static void serverSideRedirect(Map<String, String> paramKeyValues) {
		StringBuilder sb = new StringBuilder();
		for(String paramKey : paramKeyValues.keySet()){
		     if(sb.length()>0){
			    sb.append('&');
			}
			sb.append(paramKey);
			sb.append('=');
			sb.append(paramKeyValues.get(paramKey));
		}
		serverSideRedirect(sb.toString());
	}

    public static void serverSideRedirect(String queryString) {
	   String pageUrl = FacesContext.getCurrentInstance().getApplication().getViewHandler().getResourceURL(FacesContext.getCurrentInstance(), FacesContext.getCurrentInstance().getViewRoot()
			   .getViewId());
	   try {
		  FacesContext.getCurrentInstance().getExternalContext().redirect(pageUrl + '?' + queryString);
	   }
	   catch (IOException e) {
		  GlobalMessageAppender.addErrorMessage("Redirection to "+pageUrl+" was not successful!");
	   }
    }

	public static String getRefreshOutcome() {
		return FacesContext.getCurrentInstance().getViewRoot().getViewId() + "?faces-redirect=true";
	}

	public static String getRefreshOutcomeWithAdditionalParam(String param) {
		return FacesContext.getCurrentInstance().getViewRoot().getViewId() + "?faces-redirect=true&"
				+ param;
	}

	public static String getRefreshOutcomeWithAdditionalParams(String[] params) {
		StringBuilder sb = new StringBuilder(FacesContext.getCurrentInstance().getViewRoot().getViewId());
		sb.append("?faces-redirect=true");
		for (String param : params) {
			sb.append("&").append(param);
		}
		return sb.toString();
	}

	public static String getRefreshOutcomeWithResource(Integer resourceId) {
		return FacesContext.getCurrentInstance().getViewRoot().getViewId() + "?faces-redirect=true&id="
				+ resourceId;
	}
}
