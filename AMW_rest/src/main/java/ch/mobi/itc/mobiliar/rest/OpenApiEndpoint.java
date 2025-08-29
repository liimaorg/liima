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

package ch.mobi.itc.mobiliar.rest;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.Set;

/**
 * Custom OpenAPI endpoint allowing programmatic configuration instead of
 * relying on classpath YAML. Registered by {@link RESTApplication}.
 */
public class OpenApiEndpoint extends OpenApiResource {
    public OpenApiEndpoint() {
        OpenAPI openAPI = new OpenAPI().info(new Info()
                .title("Liima")
                .description(
                        "Liima allows you to manage the configurations of your Java EE applications on an unlimited" +
                        "number of different environments with various versions, including the automated deployment of those apps.")
                .license(new License().name("GNU Affero General Public License")
                        .url("https://www.gnu.org/licenses/agpl-3.0.en.html")));

        setOpenApiConfiguration(new SwaggerConfiguration()
                .openAPI(openAPI)
                .resourcePackages(Set.of("ch.mobi.itc.mobiliar.rest"))); // avoid including resources from imported packages
    }
}
