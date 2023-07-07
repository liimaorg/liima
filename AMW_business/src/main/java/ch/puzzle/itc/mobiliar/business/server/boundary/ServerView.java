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

package ch.puzzle.itc.mobiliar.business.server.boundary;

import java.util.*;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import ch.puzzle.itc.mobiliar.business.utils.JpaWildcardConverter;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigKey;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;


@Stateless
public class ServerView {
	@Inject
	private EntityManager entityManager;
	
	private static final String LOCAL_ENV = ConfigurationService.getProperty(ConfigKey.LOCAL_ENV);
	public static final String MULTI_MARKER = "multiple";
	
	public List<ServerTuple> getServers(String hostFilter, String appServerFilter, String runtimeFilter, String nodeFilter, String contextFilter, boolean merge) {
		List<ServerTuple> result;
		
		//convert wildcards
		hostFilter = JpaWildcardConverter.convertWildCards(hostFilter);
		appServerFilter = JpaWildcardConverter.convertWildCards(appServerFilter);
		runtimeFilter = JpaWildcardConverter.convertWildCards(runtimeFilter);
		nodeFilter = JpaWildcardConverter.convertWildCards(nodeFilter);		
		
		List<ServerTuple> nodeServers = this.getNodeServers(hostFilter, appServerFilter, runtimeFilter, nodeFilter, contextFilter);
		List<ServerTuple> asServers = this.getAppServers(hostFilter, appServerFilter, runtimeFilter, nodeFilter, contextFilter);
		nodeServers.addAll(new HashSet<>(asServers));

		result = nodeServers;
		if(merge) {
			result = new LinkedList<>(merge(result).values());
		}
		
		//sort
        Collections.sort(result, new Comparator<ServerTuple>() {
			@Override
			public int compare(ServerTuple o1, ServerTuple o2) {
				String name1 = o1.getAppServer()+o1.getEnvironment()+o1.getNode();
				String name2 = o2.getAppServer()+o2.getEnvironment()+o2.getNode();
				return name1.toLowerCase().compareTo(name2.toLowerCase());
			}
		});
		return result;
	}

	// filter duplicates and merge releases, runtime with same hostname
	private HashMap<String, ServerTuple> merge(List<ServerTuple> servers) {
        HashMap<String, ServerTuple> releaseFilteredServer = new HashMap<>();
        for(ServerTuple server : servers) {
            String code = server.getHost()+server.getAppServer()+server.getEnvironment()+server.getNode();
            if(releaseFilteredServer.containsKey(code)) {
                ServerTuple oldServer = releaseFilteredServer.get(code);
                
                if(!oldServer.getAppServerRelease().equals(server.getAppServerRelease())) {
                    server.setAppServerRelease(MULTI_MARKER);
                }
                if(!oldServer.getNodeRelease().equals(server.getNodeRelease())) {
                    server.setNodeRelease(MULTI_MARKER);
                }
                if(!oldServer.getRuntime().equals(server.getRuntime())) {
                    server.setRuntime(MULTI_MARKER);
                }
            }
            releaseFilteredServer.put(code, server);
        }
        return releaseFilteredServer;
	}
	
	// get hostName Properties directly on nodes
	public List<ServerTuple> getNodeServers(String hostFilter, String appServerFilter, String runtimeFilter, String nodeFilter, String contextFilter) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<ServerTuple> q = cb.createQuery(ServerTuple.class);

		//get Node ResType
		Root<ResourceEntity> node = q.from(ResourceEntity.class);
		Join<ResourceEntity, ResourceTypeEntity> nodeType = node.join("resourceType", JoinType.LEFT);
		Join<ResourceEntity, ReleaseEntity> nodeRelease = node.join("release", JoinType.LEFT);
		
		//get Props on node
		Join<ResourceEntity, ResourceContextEntity> resCtx = node.join("contexts", JoinType.LEFT);
		Join<ResourceContextEntity, ContextEntity> nodeCtx = resCtx.join("context", JoinType.LEFT);
		Join<ContextEntity, ContextEntity> nodeDomain = nodeCtx.join("parent", JoinType.LEFT);
		Join<ResourceContextEntity, PropertyEntity> nodeProp = resCtx.join("properties", JoinType.LEFT);
		Join<PropertyEntity, PropertyDescriptorEntity> nodePropType = nodeProp.join("descriptor", JoinType.LEFT);
		
		//get AppServer
		Join<ResourceEntity, ConsumedResourceRelationEntity> nodeResRel = node.join("consumedSlaveRelations", JoinType.LEFT);
		Join<ConsumedResourceRelationEntity, ResourceEntity> appServer = nodeResRel.join("masterResource", JoinType.LEFT);
		Join<ResourceEntity, ReleaseEntity> asRelease = appServer.join("release", JoinType.LEFT);
		Join<ResourceEntity, ResourceTypeEntity> asType = appServer.join("resourceType", JoinType.LEFT);
		
		//get Runtime of as
		Join<ResourceEntity, ConsumedResourceRelationEntity> asResRel = appServer.join("consumedMasterRelations", JoinType.LEFT);
		Join<ConsumedResourceRelationEntity, ResourceEntity> asRuntime = asResRel.join("slaveResource", JoinType.LEFT);
		Join<ResourceEntity, ResourceTypeEntity> runtimeType = asRuntime.join("resourceType", JoinType.LEFT);
		
		q.select(cb.construct(
				ServerTuple.class, 
				nodeProp.get("value"),
				appServer.get("name"),
				appServer.get("id"),
				asRelease.get("name"),
				asRuntime.get("name"),
				node.get("name"),
				node.get("id"),
				nodeRelease.get("name"),
				nodeDomain.get("name"),
				nodeDomain.get("id"),
				nodeCtx.get("name"),
				nodeCtx.get("id"),
				cb.literal(1)   // true
				)
			);
		
		Predicate p = cb.and(
				cb.equal(nodeType.get("name"), DefaultResourceTypeDefinition.NODE.name()),
				cb.or(
						cb.equal(asType.get("name"), DefaultResourceTypeDefinition.APPLICATIONSERVER.name()),
						cb.isNull(asType.get("name")) //nodes without appServer
					),
				cb.or(
						cb.equal(runtimeType.get("name"), DefaultResourceTypeDefinition.RUNTIME.name()),
						cb.isNull(runtimeType.get("name"))
					),
				cb.isNotNull(nodeProp.get("value")), 
				cb.equal(nodePropType.get("propertyName"), "hostName")
						
			);
		
		p = addFilters(p, cb, hostFilter, appServerFilter, runtimeFilter, nodeFilter, contextFilter,
				nodeProp.<String>get("value"), appServer.<String>get("name"),
				asRuntime.<String>get("name"), node.<String>get("name"), nodeCtx.<String>get("name"));
		
		q.where(p);
		
		TypedQuery<ServerTuple> query = entityManager.createQuery(q);
		List<ServerTuple> servers = query.getResultList();
		
		return servers;
	}
	
	// get hostName Properties on the relation between appServers and Nodes
	public List<ServerTuple> getAppServers(String hostFilter, String appServerFilter, String runtimeFilter, String nodeFilter, String contextFilter) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<ServerTuple> q = cb.createQuery(ServerTuple.class);

		//get Node ResType
		Root<ResourceEntity> node = q.from(ResourceEntity.class);
		Join<ResourceEntity, ResourceTypeEntity> nodeType = node.join("resourceType", JoinType.LEFT);
		Join<ResourceEntity, ReleaseEntity> nodeRelease = node.join("release", JoinType.LEFT);

		//get AppServer
		Join<ResourceEntity, ConsumedResourceRelationEntity> nodeResRel = node.join("consumedSlaveRelations", JoinType.LEFT);
		Join<ConsumedResourceRelationEntity, ResourceEntity> appServer = nodeResRel.join("masterResource", JoinType.LEFT);
		Join<ResourceEntity, ReleaseEntity> asRelease = appServer.join("release", JoinType.LEFT);
		Join<ResourceEntity, ResourceTypeEntity> asType = appServer.join("resourceType", JoinType.LEFT);
		
		//get Runtime of as
		Join<ResourceEntity, ConsumedResourceRelationEntity> asResRel = appServer.join("consumedMasterRelations", JoinType.LEFT);
		Join<ConsumedResourceRelationEntity, ResourceEntity> asRuntime = asResRel.join("slaveResource", JoinType.LEFT);
		Join<ResourceEntity, ResourceTypeEntity> runtimeType = asRuntime.join("resourceType", JoinType.LEFT);
		
		//get Props between as and node
		Join<ConsumedResourceRelationEntity, ResourceRelationContextEntity> resRelCtx = nodeResRel.join("contexts", JoinType.LEFT);
		Join<ResourceRelationContextEntity, ContextEntity> asCtx = resRelCtx.join("context", JoinType.LEFT);
		Join<ContextEntity, ContextEntity> asDomain = asCtx.join("parent", JoinType.LEFT);

		Join<ResourceRelationContextEntity, PropertyEntity> asProp = resRelCtx.join("properties", JoinType.LEFT);
		//here an on clause should be added, so we don't get hostnames that are divined directly on the node multiple times (and descriptor.propertyName = 'hostName')
		//on support was added in jpa 2.1 which is part of JEE 7
		Join<PropertyEntity, PropertyDescriptorEntity> asPropType = asProp.join("descriptor", JoinType.LEFT);		

		q.select(cb.construct(
				ServerTuple.class, 
				asProp.get("value"),
				appServer.get("name"),
				appServer.get("id"),
				asRelease.get("name"),
				asRuntime.get("name"),
				node.get("name"),
				node.get("id"),
				nodeRelease.get("name"),
				asDomain.get("name"),
				asDomain.get("id"),
				asCtx.get("name"),
				asCtx.get("id"),
				cb.literal(0)  //false
				)
			);

		Predicate p = cb.and(
					cb.equal(nodeType.get("name"), DefaultResourceTypeDefinition.NODE.name()),
					cb.equal(asType.get("name"), DefaultResourceTypeDefinition.APPLICATIONSERVER.name()),
					cb.or(
							cb.equal(runtimeType.get("name"), DefaultResourceTypeDefinition.RUNTIME.name()),
							cb.isNull(runtimeType.get("name"))
						),
					cb.isNotNull(asProp.get("value")), 
					cb.equal(asPropType.get("propertyName"), "hostName")
				);


		p = addFilters(p, cb, hostFilter, appServerFilter, runtimeFilter, nodeFilter, contextFilter,
				asProp.<String>get("value"), appServer.<String>get("name"),
				asRuntime.<String>get("name"), node.<String>get("name"), asCtx.<String>get("name"));
		
		q.where(p);
		
		TypedQuery<ServerTuple> query = entityManager.createQuery(q);
		List<ServerTuple> servers = query.getResultList();

		return servers;		
	}
	
	private Predicate addFilters(Predicate p, CriteriaBuilder cb, String hostFilter, String appServerFilter, String runtimeFilter, String nodeFilter, String contextFilter,
			Path<String> hostPath, Path<String> appServerPath, Path<String> runtimePath, Path<String> nodePath, Path<String> contextPath) {
		
		if(!StringUtils.isEmpty(hostFilter)) {
			p = cb.and(p, cb.like(cb.lower(hostPath), hostFilter.toLowerCase(), JpaWildcardConverter.ESCAPE_CHARACTER));
		}
		if(!StringUtils.isEmpty(nodeFilter)) {
			p = cb.and(p, cb.like(cb.lower(nodePath), nodeFilter.toLowerCase(), JpaWildcardConverter.ESCAPE_CHARACTER));
		}
		if(!StringUtils.isEmpty(appServerFilter)) {
			p = cb.and(p, cb.like(cb.lower(appServerPath), appServerFilter.toLowerCase(), JpaWildcardConverter.ESCAPE_CHARACTER));
		}
		if(!StringUtils.isEmpty(runtimeFilter)) {
			p = cb.and(p, cb.like(cb.lower(runtimePath), runtimeFilter.toLowerCase(), JpaWildcardConverter.ESCAPE_CHARACTER));
		}
		if(!StringUtils.isEmpty(contextFilter)) {
			p = cb.and(p, cb.equal(cb.lower(contextPath), contextFilter.toLowerCase()));
		}
		p = cb.and(p, cb.notEqual(contextPath, LOCAL_ENV));
		
		return p;
	}
	

}
