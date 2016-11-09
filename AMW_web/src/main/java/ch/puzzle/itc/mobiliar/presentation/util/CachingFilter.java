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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

//from http://www.fransvanbuul.net/?p=201
@WebFilter(filterName = "CachingFilter", urlPatterns = { "/*" })
public class CachingFilter implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (allowNonValidatedCaching(httpRequest)) {
			HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest) {
				@Override
				public String getServletPath() {
					// maven.build.timestamp format:  yyyy-MM-dd HH:mm
					return httpRequest.getServletPath().replaceAll("\\/\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\/", "/");
				}
			};
			HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(httpResponse) {
				@Override
				public void setHeader(String name, String value) {
					if (!"ETag".equals(name)) {
						httpResponse.setHeader(name, value);
					}
				}
			};
			httpResponse.setHeader("Cache-Control", "max-age=2592000, public"); // 30 days
			httpResponse.setHeader("Pragma", "cache");
			chain.doFilter(requestWrapper, responseWrapper);
		} else {
			chain.doFilter(request, response);
		}
	}

	private boolean allowNonValidatedCaching(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		return uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".gif");
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}
}