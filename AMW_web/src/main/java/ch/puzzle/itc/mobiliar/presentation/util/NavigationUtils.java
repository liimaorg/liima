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

public class NavigationUtils {

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

    public static String getRefreshOutcomeWithRelation(Integer relationId) {
        if (relationId != null) {
            return FacesContext.getCurrentInstance().getViewRoot().getViewId()
                    + "?faces-redirect=true&rel=" + relationId;
        }
        return getRefreshOutcome();
    }
}
