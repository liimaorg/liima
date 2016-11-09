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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.business.database.control.JpaSqlResultMapper;
import ch.puzzle.itc.mobiliar.business.database.control.QueryUtils;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.AbstractContext;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;

@Stateless
public class ResourceEditService {

	@Inject
	protected EntityManager entityManager;

	@Inject
	ResourceTypeProvider resourceTypeProvider;

	@Inject
	ContextDomainService contextDomainService;

	@Inject
	ResourceRepository resourceRepository;

	/**
	 * Load consumed or provided relations for edit
	 * 
	 * @param resourceId
	 * @param mode
	 *             CONSUMED or PROVIDED
	 * @return
	 */
	public List<ResourceEditRelation> loadResourceEditRelations(Integer resourceId,
			ResourceEditRelation.Mode mode) {
		StringBuilder sb = new StringBuilder();

		String resourceRelationTypeEntity = QueryUtils.getTable(ResourceRelationTypeEntity.class);
		String resourceEntity = QueryUtils.getTable(ResourceEntity.class);
        String resourceGroupEntity = QueryUtils.getTable(ResourceGroupEntity.class);
		String releaseEntity = QueryUtils.getTable(ReleaseEntity.class);
		String resourceTypeEntity = QueryUtils.getTable(ResourceTypeEntity.class);
		String resourceRelationEntity = null;
		if (ResourceEditRelation.Mode.CONSUMED == mode) {
			resourceRelationEntity = QueryUtils.getTable(ConsumedResourceRelationEntity.class);
		}
		else if (ResourceEditRelation.Mode.PROVIDED == mode) {
			resourceRelationEntity = QueryUtils.getTable(ProvidedResourceRelationEntity.class);
		}

		sb.append("SELECT distinct ");
		sb.append("resRel.ID resRelId, ");
		sb.append("resRel.SLAVERESOURCE_ID slaveId, ");
		sb.append("resRel.IDENTIFIER identifier, ");
		sb.append("resRel.FCOWNER resRelOwner, ");
        sb.append("resRel.FCEXTERNALKEY resRelExternalKey, ");
        sb.append("resRel.FCEXTERNALLINK resRelExternalLink, ");

        sb.append("slave.NAME slaveName, ");

        sb.append("slave.FCOWNER slaveOwner, ");
        sb.append("slaveGroup.FCEXTERNALKEY slaveExternalKey, ");
        sb.append("slaveGroup.FCEXTERNALLINK slaveExternalLink, ");
		sb.append("slave.RESOURCEGROUP_ID slaveGroupId, ");
		sb.append("slave.RELEASE_ID slaveReleaseId, ");
		sb.append("slaverelease.NAME slaveReleaseName, ");
		sb.append("resRelType.RESOURCETYPEB_ID slaveTypeId, ");
		sb.append("slaveType.NAME slaveTypeName, ");

        sb.append("master.FCOWNER masterOwner, ");
        sb.append("masterGroup.FCEXTERNALKEY masterExternalKey, ");
        sb.append("masterGroup.FCEXTERNALLINK masterExternalLink, ");

		sb.append("masterType.NAME masterTypeName, ");
		sb.append("resRelType.ID resRelTypeId, ");
		sb.append("resRelType.IDENTIFIER typeIdentifier, ");

		sb.append("'" + mode.name() + "' relationMode, ");
		sb.append("slaverelease.INSTALLATIONINPRODUCTION slaveReleaseDate ");

		sb.append("FROM " + resourceRelationTypeEntity + " resRelType ");
		sb.append("LEFT JOIN " + resourceRelationEntity
				+ " resRel ON resRelType.ID=resRel.RESOURCERELATIONTYPE_ID ");
		sb.append("LEFT JOIN " + resourceEntity + " master ON master.ID=resRel.MASTERRESOURCE_ID ");

        sb.append("LEFT JOIN " + resourceGroupEntity + " masterGroup ON masterGroup.ID=master.RESOURCEGROUP_ID ");

		sb.append("LEFT JOIN " + resourceEntity + " slave ON slave.ID=resRel.SLAVERESOURCE_ID ");

        sb.append("LEFT JOIN " + resourceGroupEntity + " slaveGroup ON slaveGroup.ID=slave.RESOURCEGROUP_ID ");

        sb.append("LEFT JOIN " + resourceTypeEntity
				+ " slaveType ON resRelType.RESOURCETYPEB_ID=slaveType.ID ");
		sb.append("LEFT JOIN " + resourceTypeEntity
				+ " masterType ON resRelType.RESOURCETYPEA_ID=masterType.ID ");
		sb.append("LEFT JOIN " + releaseEntity + " slaverelease ON slave.RELEASE_ID=slaverelease.ID ");
		sb.append("WHERE resRel.MASTERRESOURCE_ID=:resourceId ");

		Query query = entityManager.createNativeQuery(sb.toString());
		query.setParameter("resourceId", resourceId);

		return JpaSqlResultMapper.list(query, ResourceEditRelation.class);
	}

	/**
	 * Load relations from type for edit
	 * 
	 * @param type
	 * @return
	 */
	public List<ResourceEditRelation> loadResourceEditRelationsFromType(ResourceTypeEntity type) {
		StringBuilder sb = new StringBuilder();

		List<Integer> typeList = getTypeWithParentIds(null, type);
		String resourceRelationTypeEntity = QueryUtils.getTable(ResourceRelationTypeEntity.class);
		String resourceTypeEntity = QueryUtils.getTable(ResourceTypeEntity.class);

		sb.append("SELECT ");
		sb.append("cast(NULL as INT) resRelId, ");
		sb.append("cast(NULL as INT) slaveId, ");
		sb.append("cast(NULL as VARCHAR(5)) identifier, ");
		sb.append("cast(NULL as VARCHAR(5)) resRelOwner, ");
        sb.append("cast(NULL as VARCHAR(5)) resRelExternalKey, ");
        sb.append("cast(NULL as VARCHAR(5)) resRelExternalLink, ");

        sb.append("cast(NULL as VARCHAR(5)) slaveName, ");
		sb.append("cast(NULL as VARCHAR(5)) slaveOwner, ");
        sb.append("cast(NULL as VARCHAR(5)) slaveExternalKey, ");
        sb.append("cast(NULL as VARCHAR(5)) slaveExternalLink, ");


        sb.append("cast(NULL as INT) slaveGroupId, ");
		sb.append("cast(NULL as INT) slaveReleaseId, ");
		sb.append("cast(NULL as VARCHAR(5)) slaveReleaseName, ");
		sb.append("resRelType.RESOURCETYPEB_ID slaveTypeId, ");
		sb.append("slaveType.NAME slaveTypeName, ");
		sb.append("cast(NULL as VARCHAR(5)) masterOwner, ");
        sb.append("cast(NULL as VARCHAR(5)) masterExternalKey, ");
        sb.append("cast(NULL as VARCHAR(5)) masterExternalLink, ");

        sb.append("masterType.NAME masterTypeName, ");
		sb.append("resRelType.ID resRelTypeId, ");
		sb.append("resRelType.IDENTIFIER typeIdentifier, ");
		sb.append("cast('" + ResourceEditRelation.Mode.TYPE + "' as VARCHAR(5)) relationMode, ");
		sb.append("cast(NULL as DATE) slaveReleaseDate ");

		sb.append("FROM " + resourceRelationTypeEntity + " resRelType ");
		sb.append("LEFT JOIN " + resourceTypeEntity
				+ " slaveType ON resRelType.RESOURCETYPEB_ID=slaveType.ID ");
		sb.append("LEFT JOIN " + resourceTypeEntity
				+ " masterType ON resRelType.RESOURCETYPEA_ID=masterType.ID ");

		sb.append("WHERE resRelType.RESOURCETYPEA_ID IN (:typeList) ");

		Query query = entityManager.createNativeQuery(sb.toString());
		query.setParameter("typeList", typeList);

		return JpaSqlResultMapper.list(query, ResourceEditRelation.class);
	}

	/**
	 * Load resource relations for edit
	 * 
	 * @param resourceId
	 * @return list of ResourceEditRelation
	 */
	public Map<ResourceEditRelation.Mode, List<ResourceEditRelation>> loadResourceRelationsForEdit(
			Integer resourceId) {
		ResourceEntity resource = resourceRepository
				.loadWithResourceGroupAndRelatedResourcesForId(resourceId);
		Map<ResourceEditRelation.Mode, List<ResourceEditRelation>> resultMap = new HashMap<ResourceEditRelation.Mode, List<ResourceEditRelation>>();
		resultMap.put(ResourceEditRelation.Mode.CONSUMED,
				loadResourceEditRelations(resourceId, ResourceEditRelation.Mode.CONSUMED));
		resultMap.put(ResourceEditRelation.Mode.PROVIDED,
				loadResourceEditRelations(resourceId, ResourceEditRelation.Mode.PROVIDED));
		resultMap.put(ResourceEditRelation.Mode.TYPE,
				loadResourceEditRelationsFromType(resource.getResourceType()));
		return resultMap;
	}

	/**
	 * Load resource relationtypes for edit
	 *
	 * @param type
	 * @return list of ResourceEditRelation
	 */
	public Map<ResourceEditRelation.Mode, List<ResourceEditRelation>> loadResourceRelationTypesForEdit(
			ResourceTypeEntity type) {
		Map<ResourceEditRelation.Mode, List<ResourceEditRelation>> resultMap = new HashMap<ResourceEditRelation.Mode, List<ResourceEditRelation>>();
		resultMap.put(ResourceEditRelation.Mode.TYPE, loadResourceEditRelationsFromType(type));
		return resultMap;
	}


	private void loadContext(Set<? extends AbstractContext> ctx, Class<?> clazz) {
		if (ctx != null && ctx.size() > 0) {
			List<Integer> ctxIds = new ArrayList<Integer>();
			for (AbstractContext c : ctx) {
				ctxIds.add(c.getId());
			}
			entityManager
					.createQuery(
							"select ctx from "
									+ clazz.getSimpleName()
									+ " ctx "
									+ "left join fetch ctx.properties as properties "
									+ "left join fetch ctx.propertyDescriptors as propertyDescriptors "
									+ "left join fetch propertyDescriptors.propertyTypeEntity "
									+ "left join fetch ctx.templates as templates "
									+ "left join fetch templates.targetPlatforms where ctx.id IN (:ids)")
					.setParameter("ids", ctxIds).getResultList();
		}
	}


	/**
	 * @param resourceId
	 * @param withConsumedResources
	 * @return the resource entity or null if it does not exist
	 */
	public ResourceEntity loadResourceEntityForEdit(Integer resourceId, boolean withConsumedResources) {
		return loadResourceEntityForEdit(resourceId, null, null, withConsumedResources);
	}

	/**
	 * @param resourceId
	 * @param resourceGroupId
	 * @param releaseId
	 * @param withConsumedResources
	 * @return the resource entity
	 */
	@Deprecated
	// move to resourcerepository
	private ResourceEntity loadResourceEntityForEdit(Integer resourceId, Integer resourceGroupId,
			Integer releaseId, boolean withConsumedResources) {

		TypedQuery<ResourceEntity> query = entityManager
				.createQuery(
						// The resource
						"select res from ResourceEntity res "
								+ "left join fetch res.contexts as resContexts left join fetch resContexts.context as resContext left join fetch resContext.contextType "
								+ "left join fetch res.resourceTags "
								+ (withConsumedResources ? ("left join fetch res.consumedMasterRelations ")
										: "")
								+
								// The resource type
								"left join fetch res.resourceType as resType left join fetch resType.contexts as resTypeContexts left join fetch resTypeContexts.context as resTypeContext left join fetch resTypeContext.contextType "
								+

								// The parent resource type
								"left join fetch resType.parentResourceType as parentResType "
								+ "left join fetch parentResType.contexts as parentResTypeContexts left join fetch parentResTypeContexts.context as parentResTypeContext left join fetch parentResTypeContext.contextType "

								// where
								+ (resourceId != null ? "where res.id=:resId"
										: "where res.resourceGroup.id=:resourceGroupId and res.release.id=:releaseId"),
						ResourceEntity.class);
		if (resourceId != null) {
			query.setParameter("resId", resourceId);
		}
		else {
			query.setParameter("resourceGroupId", resourceGroupId).setParameter("releaseId", releaseId);
		}

		ResourceEntity res = query.getSingleResult();
		loadContext(res.getContexts(), ResourceContextEntity.class);
		loadContext(res.getResourceType().getContexts(), ResourceTypeContextEntity.class);
		if (res.getResourceType().getParentResourceType() != null) {
			loadContext(res.getResourceType().getParentResourceType().getContexts(),
					ResourceTypeContextEntity.class);
		}
		if (withConsumedResources) {
			for (ConsumedResourceRelationEntity rel : res.getConsumedMasterRelations()) {
				loadContext(rel.getContexts(), ResourceRelationContextEntity.class);
			}
		}
		return entityManager.find(ResourceEntity.class, res.getId());
	}


	protected List<Integer> getTypeWithParentIds(List<Integer> result, ResourceTypeEntity type) {
		if (result == null) {
			result = new ArrayList<Integer>();
		}
		if (type != null) {
			result.add(type.getId());
			return getTypeWithParentIds(result, type.getParentResourceType());
		}
		// order has to be swapped at end of recursion
		Collections.reverse(result);
		return result;
	}
}
