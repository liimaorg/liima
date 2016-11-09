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

package ch.puzzle.itc.mobiliar.test;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.*;

public class SimpleEntityBuilder extends EntityBuilder {

    public ResourceEntity ad;
    public ResourceEntity ws;

    private int resourceGroupIds = 1000;

    @Override
    protected void build() {
	   buildResource(buildResourceType(RUNTIME.type), RUNTIME.name);
	   buildResourceType(NODE1.type);
	   buildContextAndPlatform();

	   as = buildResource(buildResourceType(AS.type), "as");
	   app = buildResource(buildResourceType(APP.type), "app");

	   buildConsumedRelation(as, app, ForeignableOwner.AMW);

	   // two additional resources
	   ad = buildResource(buildResourceType(AD.type), "ad");
	   ws = buildResource(buildResourceType(WS.type), "ws");

    }

    /**
	* Add a label property to each resource that is built
	*/
    @Override
    public ResourceEntity buildResource(ResourceTypeEntity type, String name) {
	   ResourceEntity resource = super.buildResource(type, name);
	   buildResourceProperty(resource, "label", name);
	   if (resource.getResourceGroup().getId() == null) {
		  resource.getResourceGroup().setId(resourceGroupIds++);
	   }
	   return resource;
    }

}
