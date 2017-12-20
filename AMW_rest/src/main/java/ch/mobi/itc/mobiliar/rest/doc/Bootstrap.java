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

package ch.mobi.itc.mobiliar.rest.doc;

import io.swagger.models.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@WebServlet(name = "swagger", loadOnStartup = 1)
public class Bootstrap extends HttpServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {

		Info info = new Info()
				.title("Liima")
				.license(new License()
						.name("GNU Affero General Public License")
						.url("https://www.gnu.org/licenses/agpl-3.0.en.html"));

		ServletContext context = config.getServletContext();
		Swagger swagger = new Swagger().info(info);
		swagger.setBasePath(context.getContextPath()+"/resources");
		swagger.externalDocs(new ExternalDocs("Find out more about Swagger", "http://swagger.io"));

		context.setAttribute("swagger", swagger);
	}

}
