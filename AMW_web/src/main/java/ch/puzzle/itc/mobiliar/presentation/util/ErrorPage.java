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

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@RequestScoped
@Named
public class ErrorPage {
    public final static String EXCEPTIONMESSAGE_PARAM = "exceptionMessage";
    public final static String STACKTRACE_PARAM = "stacktrace";
    public String getMessage(){
        Object exceptionMessage =  FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(EXCEPTIONMESSAGE_PARAM);
        return exceptionMessage!=null ? String.valueOf(exceptionMessage) : null;

    }

    public String getStacktrace(){
        StackTraceElement[] stackTrace =  (StackTraceElement[])FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(STACKTRACE_PARAM);
        StringBuilder sb = new StringBuilder();
        if(stackTrace!=null) {
            for (StackTraceElement trace : stackTrace) {
                sb.append(trace.toString() + "<br/>");
            }
        }
        return sb.toString();

    }

}
