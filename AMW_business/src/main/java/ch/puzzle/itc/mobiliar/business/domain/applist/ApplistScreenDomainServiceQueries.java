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

package ch.puzzle.itc.mobiliar.business.domain.applist;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.utils.JpaWildcardConverter;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

public class ApplistScreenDomainServiceQueries {
	
	@Inject
	private EntityManager entityManager;

	/**
	 * alphabetic sorted
	 * 
	 * @param input
	 * @return
	 */
	//unused code
	Query applicationServersForSuggestBox(String input) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> q = cb.createQuery(String.class);
		Root<ResourceEntity> r = q.from(ResourceEntity.class);
		Join<ResourceEntity, ResourceTypeEntity> resType = r.join("resourceType");
		Predicate appServerNamePred = cb.like(resType.<String> get("name"),
				DefaultResourceTypeDefinition.APPLICATIONSERVER.name());
		input = input + "%";
		q.where(cb.and(appServerNamePred, cb.like(r.<String> get("name"), input)));
		q.select(r.<String> get("name"));
		q.orderBy(cb.asc(r.get("name")));

		return entityManager.createQuery(q);
	}

	Query doFetchRelatedElementsForSpecificAppServers(List<ResourceGroupEntity> appservers) {
		return entityManager
				.createQuery(
						"from ResourceGroupEntity rg left join rg.resources r left join fetch r.consumedMasterRelations rel left join fetch rel.slaveResource consRes left join fetch consRes.resourceTags where rg in (:appservers)")
				.setParameter("appservers", appservers);
	}

	/**
	 * alphabetic sorted
	 * 
	 * @param nameFilter
	 * @return
	 */
	
	//fetch appServer(id, name, release, type, group), app(id, name, relation name, release, type, group)
	//group by group -> as -> group -> app
	//oder as -> app
	List<ResourceEntity> doFetchApplicationServersWithApplications(String nameFilter, List<Integer> myAmwIds, Integer maxResult) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		Predicate p;
		boolean nameFilterIsEmpty = nameFilter == null || nameFilter.trim().isEmpty();

		CriteriaQuery<ResourceEntity> q = cb.createQuery(ResourceEntity.class);
		Root<ResourceEntity> appServer = q.from(ResourceEntity.class);

		Join<ResourceEntity, ResourceTypeEntity> appServerType = appServer.join("resourceType", JoinType.LEFT);
		SetJoin<ResourceEntity, ConsumedResourceRelationEntity> relation = appServer.joinSet("consumedMasterRelations", JoinType.LEFT);
		Join<ConsumedResourceRelationEntity, ResourceEntity> app = relation.join("slaveResource", JoinType.LEFT);
		
		p = cb.and(cb.equal(appServerType.<String> get("name"), DefaultResourceTypeDefinition.APPLICATIONSERVER.name()));
		
		if (!nameFilterIsEmpty) {
			nameFilter = JpaWildcardConverter.convertWildCards(nameFilter);
			p = cb.and(
					p,
					cb.or(
							cb.like(appServer.<String> get("name"), nameFilter, JpaWildcardConverter.ESCAPE_CHARACTER),
							cb.like(app.<String> get("name"), nameFilter, JpaWildcardConverter.ESCAPE_CHARACTER)));
		}
		if (myAmwIds != null) {
			p = cb.and(p,
			cb.or(
					appServer.get("resourceGroup").get("id").in(myAmwIds),
					app.get("resourceGroup").get("id").in(myAmwIds)));
		}
		
		q.where(p);
		
		q.distinct(true);
		
		q.orderBy(cb.asc(appServer.get("deletable")), cb.asc(appServer.get("name")));
		TypedQuery<ResourceEntity> query = entityManager.createQuery(q);
		if(maxResult != null) {
			query.setMaxResults(maxResult);
		}
		
		return query.getResultList();
	}
	
}