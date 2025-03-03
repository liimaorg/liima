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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.utils.JpaWildcardConverter;
import ch.puzzle.itc.mobiliar.business.utils.database.DatabaseUtil;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.common.util.Tuple;

public class ApplistScreenDomainServiceQueries {

    @Inject
    private EntityManager entityManager;

    @Inject
    private DatabaseUtil dbUtil;

    Tuple<List<ResourceEntity>, Long> getAppServersWithApps(String nameFilter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Predicate p;
        boolean nameFilterIsEmpty = nameFilter == null || nameFilter.trim().isEmpty();

        // Filter and retrieve results
        CriteriaQuery<ResourceEntity> q = cb.createQuery(ResourceEntity.class);
        Root<ResourceEntity> appServer = q.from(ResourceEntity.class);
        Join<ResourceEntity, ResourceTypeEntity> appServerType = appServer.join("resourceType", JoinType.LEFT);
        SetJoin<ResourceEntity, ConsumedResourceRelationEntity> relation = appServer.joinSet("consumedMasterRelations", JoinType.LEFT);
        Join<ConsumedResourceRelationEntity, ResourceEntity> app = relation.join("slaveResource", JoinType.LEFT);

        p = cb.and(cb.equal(appServerType.<String>get("name"), DefaultResourceTypeDefinition.APPLICATIONSERVER.name()));

        if (!nameFilterIsEmpty) {
            String nameFilterLower = JpaWildcardConverter.convertWildCards(nameFilter).toLowerCase();
            p = cb.and(
                    p,
                    cb.or(
                            cb.like(cb.lower(appServer.<String>get("name")), nameFilterLower, JpaWildcardConverter.ESCAPE_CHARACTER),
                            cb.like(cb.lower(app.<String>get("name")), nameFilterLower, JpaWildcardConverter.ESCAPE_CHARACTER)));
        }
        q.where(p);

        q.distinct(true);

        Expression<String> name = appServer.get("name");
        // workaround for H2 incompatibility, see https://github.com/h2database/h2database/issues/408
        if (!dbUtil.isH2()) {
            name = cb.lower(name);
        }

        q.orderBy(cb.asc(appServer.get("deletable")), cb.asc(name));

        TypedQuery<ResourceEntity> query = entityManager.createQuery(q);

        // Count all values after filtering
        Long count = (long) query.getResultList().size();

        return  new Tuple<>(query.getResultList(), count);
    }

    private Long getTotalCount(CriteriaBuilder cb) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ResourceEntity> appServer = countQuery.from(ResourceEntity.class);
        Join<ResourceEntity, ResourceTypeEntity> appServerType = appServer.join("resourceType", JoinType.LEFT);
        SetJoin<ResourceEntity, ConsumedResourceRelationEntity> relation = appServer.joinSet("consumedMasterRelations", JoinType.LEFT);
        Join<ConsumedResourceRelationEntity, ResourceEntity> app = relation.join("slaveResource", JoinType.LEFT);
        countQuery.select(cb.countDistinct(appServer.get("id")));
        countQuery.where(cb.equal(appServerType.<String>get("name"), DefaultResourceTypeDefinition.APPLICATIONSERVER.name()));
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();
        return totalCount;
    }

}
