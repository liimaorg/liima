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
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;
import org.apache.commons.lang.StringUtils;

import javax.persistence.EntityManager;
import java.util.Date;

import static ch.puzzle.itc.mobiliar.test.EntityBuilderType.*;

public class PersistingEntityBuilder extends AmwEntityBuilder {

	private EntityManager entityManager;
	public ResourceEntity ad;
	public ResourceEntity ws;

	public PersistingEntityBuilder(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	protected void build() {
		// dont build amw by default
	}

	public PersistingEntityBuilder buildAmw() {
		super.buildInternal();
		return this;
	}

	@Override
	protected void setId(Identifiable resource, Integer id) {
		entityManager.persist(resource);
		entityManager.merge(resource);
	}

	@Override
	public ResourceEntity buildResource(ResourceTypeEntity type, String name) {
		return flushAndRefresh(super.buildResource(type, name));
	}

	private ResourceEntity flushAndRefresh(ResourceEntity resource) {
		entityManager.flush();
		entityManager.refresh(resource);
		return resource;
	}

	@Override
	public ConsumedResourceRelationEntity buildConsumedRelation(ResourceEntity master, ResourceEntity slave, ForeignableOwner changingOwner) {
		return flushAndRefresh(super.buildConsumedRelation(master, slave, ForeignableOwner.AMW), master, slave);
	}

	@Override
	public ProvidedResourceRelationEntity buildProvidedRelation(ResourceEntity master, ResourceEntity slave, ForeignableOwner changingOwner) {
		return flushAndRefresh(super.buildProvidedRelation(master, slave, ForeignableOwner.AMW), master, slave);
	}

	private <T extends AbstractResourceRelationEntity> T flushAndRefresh(T relation, ResourceEntity master, ResourceEntity slave) {
		entityManager.flush();
		entityManager.refresh(master);
		entityManager.refresh(slave);
		return relation;
	}

	public PersistingEntityBuilder buildSimple() {
	   	buildResource(buildResourceType(RUNTIME.type), RUNTIME.name);
		buildContextAndPlatform();

		ResourceTypeEntity asType = buildResourceType(AS.type);
		as = buildResource(asType, AS.name);

		ResourceTypeEntity appType = buildResourceType(APP.type);
		app = buildResource(appType, APP.name);

		buildConsumedRelation(as, app, ForeignableOwner.AMW);

		addResourceProperty(context, as, "testProp", "testValue");

		return this;
	}

	public int getResourceCount(Class<?> clz) {
		return ((Long) entityManager.createQuery("select count(*) from " + clz.getSimpleName()).getSingleResult()).intValue();
	}


	////////////////////

	/**
	 * @param type
	 * @param name
	 * @param group
	 *             optional - new group will be created by default
	 * @param rel
	 *             optional - new release will be created by default
	 * @return
	 */
	public static ResourceEntity buildResourceEntityWithRelease(EntityManager em, ResourceTypeEntity type, String name, ResourceGroupEntity group, ReleaseEntity rel) {
		ResourceEntity r = null;
		if (group == null) {
			r = ResourceFactory.createNewResource(name);
		} else {
			r = ResourceFactory.createNewResource(group);
		}
		r.setResourceType(type);
		if (rel == null) {
			rel = PersistingEntityBuilder.buildReleaseEntity(em, null, null);
		}
		r.setRelease(rel);
		em.persist(r);
		return r;
	}

	/**
	 * @param name
	 *             optional
	 * @param installationDate
	 *             optional - current date will be set as default
	 * @return
	 */
	public static ReleaseEntity buildReleaseEntity(EntityManager em, String name, Date installationDate) {
		ReleaseEntity rel = new ReleaseEntity();
		if (installationDate != null) {
			rel.setInstallationInProductionAt(installationDate);
		} else {
			rel.setInstallationInProductionAt(new Date());
		}

		if (!StringUtils.isEmpty(name)) {
			rel.setName(name);
		} else {
			rel.setName(rel.getInstallationInProductionAt().toString());
		}
		em.persist(rel);
		return rel;
	}

	public static ResourceTypeEntity buildResourceType(EntityManager em, String name){
		ResourceTypeEntity type = new ResourceTypeEntity();
		type.setName(name);
		em.persist(type);
		return type;
	}

}
