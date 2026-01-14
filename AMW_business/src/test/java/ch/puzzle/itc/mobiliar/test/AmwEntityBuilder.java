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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.*;

public class AmwEntityBuilder extends EntityBuilder {

	@Override
	protected void build() {
		buildInternal();
	}

	protected void buildInternal() {

		for (String typeName : EntityBuilderType.typeNames()) {
			buildResourceType(typeName);
		}

		for (EntityBuilderType type : EntityBuilderType.vals()) {
			buildResource(typeFor(type.type), type.name);
		}
		buildContextAndPlatform();

		buildConsumedRelation(resourceFor(AS), resourceFor(NODE1));
		buildConsumedRelation(resourceFor(AS), resourceFor(NODE2));
		buildConsumedRelation(resourceFor(AS), platform);
		buildConsumedRelation(resourceFor(AS), resourceFor(APP));
		buildConsumedRelation(resourceFor(APP), resourceFor(AD));
		buildConsumedRelation(resourceFor(APP), resourceFor(CERT));
		buildConsumedRelation(resourceFor(APP), resourceFor(DB2));
		buildConsumedRelation(resourceFor(APP), resourceFor(JBOSS7MANAGEMENT));
		buildConsumedRelation(resourceFor(APP), resourceFor(KEYSTORE));
		buildConsumedRelation(resourceFor(APP), resourceFor(MAIL));
		buildConsumedRelation(resourceFor(APP), resourceFor(CLUSTER));
		buildConsumedRelation(resourceFor(APP), resourceFor(TRUSTSTORE));

		// inheritance
		ResourceTypeEntity cert = buildResourceType("Certificate");
		buildTypeInheritance(cert, typeFor(KEYSTORE.type));
		buildTypeInheritance(cert, typeFor(TRUSTSTORE.type));

		as = resourceFor(AS.name);
		app = resourceFor(APP.name);
	}

}
