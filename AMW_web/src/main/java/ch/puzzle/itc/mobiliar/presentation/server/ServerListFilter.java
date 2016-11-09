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

package ch.puzzle.itc.mobiliar.presentation.server;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

import org.apache.commons.lang.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Holds the filter criteria for the severList screen in the session
 */
@Getter
@Setter
@SessionScoped
public class ServerListFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    
	private String host;
	private String appServer;
	private String runtime;
	private String node;
	private String environment;
	boolean emptySearch = false;
			
	public void reset() {
		host = null;
		appServer = null;
		runtime = null;
		node = null;
		environment = null;
	}
	
	public boolean isEmpty() {
		return StringUtils.isEmpty(host) && StringUtils.isEmpty(appServer) && StringUtils.isEmpty(runtime) && StringUtils.isEmpty(node)
				&& StringUtils.isEmpty(environment);
	}
}
