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

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import ch.mobi.itc.mobiliar.rest.deployments.DeploymentsRest;
import ch.mobi.itc.mobiliar.rest.environments.EnvironmentsRest;
import ch.mobi.itc.mobiliar.rest.exceptions.IllegalArgumentExceptionMapper;
import ch.mobi.itc.mobiliar.rest.exceptions.IllegalStateExceptionMapper;
import ch.mobi.itc.mobiliar.rest.exceptions.NoResultExceptionMapper;
import ch.mobi.itc.mobiliar.rest.exceptions.NotAuthorizedExceptionMapper;
import ch.mobi.itc.mobiliar.rest.permissions.RestrictionsRest;
import ch.mobi.itc.mobiliar.rest.resources.HostNamesRest;
import ch.mobi.itc.mobiliar.rest.resources.ResourcePropertiesRest;
import ch.mobi.itc.mobiliar.rest.resources.ResourceRelationPropertiesRest;
import ch.mobi.itc.mobiliar.rest.resources.ResourceRelationTemplatesRest;
import ch.mobi.itc.mobiliar.rest.resources.ResourceRelationsRest;
import ch.mobi.itc.mobiliar.rest.resources.ResourceTemplatesRest;
import ch.mobi.itc.mobiliar.rest.resources.ResourcesRest;
import ch.mobi.itc.mobiliar.rest.resources.ServerTupleCSVBodyWriter;

@ApplicationPath("/resources/")
public class RESTApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        addRestResourceClasses(resources);
        return resources;
    }
    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(ResourcesRest.class);
        resources.add(ResourcePropertiesRest.class);
        resources.add(ResourceTemplatesRest.class);
        resources.add(ResourceRelationsRest.class);
        resources.add(ResourceRelationPropertiesRest.class);
        resources.add(ResourceRelationTemplatesRest.class);
        resources.add(HostNamesRest.class);
        resources.add(DeploymentsRest.class);
        resources.add(EnvironmentsRest.class);
        resources.add(RestrictionsRest.class);
        resources.add(NoResultExceptionMapper.class);
        resources.add(IllegalStateExceptionMapper.class);
        resources.add(NotAuthorizedExceptionMapper.class);
        resources.add(IllegalArgumentExceptionMapper.class);
        resources.add(ServerTupleCSVBodyWriter.class);
    }
}
