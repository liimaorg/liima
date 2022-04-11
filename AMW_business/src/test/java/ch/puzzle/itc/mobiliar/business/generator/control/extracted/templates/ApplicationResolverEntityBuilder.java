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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.ApplicationWithVersionEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.test.PersistingEntityBuilder;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.HashSet;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.*;

public class ApplicationResolverEntityBuilder extends PersistingEntityBuilder {

	public ResourceEntity as2;
	public ResourceEntity app2;
	public ResourceEntity node1;
	public ResourceEntity node2;
	public ConsumedResourceRelationEntity relation;
	public GenerationOptions options;

	public ApplicationResolverEntityBuilder(EntityManager entityManager) {
		super(entityManager);
	}

	public ApplicationResolverEntityBuilder buildScenario() {

	   	buildResource(buildResourceType(RUNTIME.type), RUNTIME.name);
		buildContextAndPlatform();

		as = buildResource(buildResourceType(AS.type), "as");
		app = buildResource(buildResourceType(APP.type), "app");

		buildConsumedRelation(as, app, ForeignableOwner.AMW);

		ws = buildResource(buildResourceType(WS.type), "ws");

		buildResourceType(NODE1.type);
		buildResourceType(NODE2.type);
		as2 = buildResource(AS, "as2");
		app2 = buildResource(APP, "app2");

		node1 = buildResource(NODE1, "node1");
		node2 = buildResource(NODE2, "node2");

		buildConsumedRelation(as, node1, ForeignableOwner.AMW);
		relation = buildConsumedRelation(app, ws, ForeignableOwner.AMW);

		buildConsumedRelation(as2, node1, ForeignableOwner.AMW);
		buildConsumedRelation(as2, app2, ForeignableOwner.AMW);
		buildProvidedRelation(app2, ws, ForeignableOwner.AMW);

		DeploymentEntity d = new DeploymentEntity();
		d.setApplicationsWithVersion(new HashSet<ApplicationWithVersionEntity>());
		d.setRuntime(platform);
		GenerationContext generationContext = new GenerationContext(context, null, d , new Date(),
				GenerationModus.SIMULATE, new ResourceDependencyResolverService());
		generationContext.setNode(node1);
		options = new GenerationOptions(generationContext);
		return this;
	}

	@Override
	public ResourceEntity buildResource(ResourceTypeEntity type, String name) {
		ResourceEntity resource = super.buildResource(type, name);
		buildResourceProperty(resource, "label", name);
		return resource;
	}
}
