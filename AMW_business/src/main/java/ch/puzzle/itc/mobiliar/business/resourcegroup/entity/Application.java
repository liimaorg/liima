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

package ch.puzzle.itc.mobiliar.business.resourcegroup.entity;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

public class Application extends Resource {

    public Application(ResourceTypeProvider resourceTypeProvider, ContextEntity globalContext) {
        super(resourceTypeProvider, DefaultResourceTypeDefinition.APPLICATION, globalContext);
    }


    public static Application createByResource(ResourceEntity r, ResourceTypeProvider resourceTypeProvider, ContextEntity globalContext) {
        Application app = new Application(resourceTypeProvider, globalContext);

        if (r == null) {
            // create by system owner
            r = ResourceFactory.createNewResourceForOwner(ForeignableOwner.getSystemOwner());
        }

        app.wrap(r);

        return app;
    }

}
