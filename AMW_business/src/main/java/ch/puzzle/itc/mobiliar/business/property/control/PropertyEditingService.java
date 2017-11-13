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

/*
 * To change this license header, choose License Headers in Project Properties. To change this template file,
 * choose Tools | Templates and open the template in the editor.
 */
package ch.puzzle.itc.mobiliar.business.property.control;

import ch.puzzle.itc.mobiliar.business.database.control.JpaSqlResultMapper;
import ch.puzzle.itc.mobiliar.business.database.control.QueryUtils;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextHierarchy;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty.Origin;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation.Mode;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceEditService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;

import javax.inject.Inject;
import javax.persistence.Query;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author oschmid
 */
public class PropertyEditingService {

	@Inject
	private Logger log;

	@Inject
	PropertyEditingQueries queries;

	@Inject
	ResourceEditService resourceEditService;

    @Inject
    ContextHierarchy contextHierarchy;

	/**
	 * loads the properties from the database in a single query and returns them as a transfer object
	 * 
	 * @param resourceId
	 *             - the resource id of the resource for which the properties should be loaded
	 * @param type
	 *             - the resource type of the resource for which the properties should be loaded
	 * @param currentContext
	 *             - the context for which the properties should be loaded
	 * @return a list of containers which contain the required property information
	 */
	public List<ResourceEditProperty> loadPropertiesForEditResource(Integer resourceId,
			ResourceTypeEntity type, ContextEntity currentContext) {
		Map<Integer, ResourceEditProperty> propMap = new HashMap<Integer, ResourceEditProperty>();

		List<Integer> contextList = contextHierarchy.getContextWithParentIds(currentContext);
		List<Integer> typeList = getTypeWithParentIds(null, type);

		Query query = queries.getPropertyValueForResource(resourceId, typeList, contextList);

		List<ResourceEditProperty> result = JpaSqlResultMapper.list(query, ResourceEditProperty.class);
		for (ResourceEditProperty prop : result) {
			// Since we have to load also the properties of the parent contexts (to display the replaced
			// values) we must find the property of the current context and set parents hierarchical
			propMap.put(prop.getDescriptorId(),
					findChildPropAndSetParent(prop, propMap.get(prop.getDescriptorId()), contextList));
		}
		return new ArrayList<ResourceEditProperty>(new TreeSet<ResourceEditProperty>(propMap.values()));

	}

    /**
	* loads the properties from the database in a single query and returns them as a transfer object
	*
	* @param resourceType
	*             - the resourcetype for which the properties should be loaded
	* @param currentContext
	*             - the context for which the properties should be loaded
	* @return a list of containers which contain the required property information
	*/
    public List<ResourceEditProperty> loadPropertiesForEditResourceType(ResourceTypeEntity resourceType, ContextEntity currentContext) {
	   Map<Integer, ResourceEditProperty> propMap = new HashMap<Integer, ResourceEditProperty>();

	   List<Integer> contextList = contextHierarchy.getContextWithParentIds(currentContext);
	   List<Integer> typeList = getTypeWithParentIds(null, resourceType);

	   Query query = queries.getPropertyValueForResourceType(typeList, contextList);

	   List<ResourceEditProperty> result = JpaSqlResultMapper.list(query, ResourceEditProperty.class);
	   for (ResourceEditProperty prop : result) {
		  // Since we have to load also the properties of the parent contexts (to display the replaced
		  // values) we must find the property of the current context and set parents hierarchical
		  propMap.put(prop.getDescriptorId(),
				  findChildPropAndSetParent(prop, propMap.get(prop.getDescriptorId()), contextList));
	   	  if(!resourceType.getId().equals(prop.getTypeId())){
			 prop.setDescriptorDefinedOnSuperResourceType(true);
		  }
		   if(!resourceType.getId().equals(prop.getPropertyValueTypeId())){
			   prop.setDefinedOnSuperResourceType(true);
		   }
	   }
	   return new ArrayList<ResourceEditProperty>(new TreeSet<ResourceEditProperty>(propMap.values()));
    }


	public List<ResourceEditProperty> loadPropertiesForEditRelation(Mode relationTyp,
			Integer resourceRelationId, Integer relatedResourceId, ResourceTypeEntity masterResourceType,
			ResourceTypeEntity slaveResourceType, ContextEntity currentContext) {

		Map<Integer, ResourceEditProperty> propMap = new HashMap<Integer, ResourceEditProperty>();
		List<Integer> contextList = contextHierarchy.getContextWithParentIds(currentContext);
		List<Integer> masterResourceTypeList = getTypeWithParentIds(null, masterResourceType);
		List<Integer> slaveResourceTypeList = getTypeWithParentIds(null, slaveResourceType);
		Query query;
		switch (relationTyp) {
		case CONSUMED:
			query = queries.getPropertyValueForConsumedRelationQuery(resourceRelationId,
					relatedResourceId, masterResourceTypeList, slaveResourceTypeList, contextList);
			break;
		case PROVIDED:
			query = queries.getPropertyValueForProvidedRelationQuery(resourceRelationId,
					relatedResourceId, masterResourceTypeList, slaveResourceTypeList, contextList);
			break;
		default:
			query = null;
		}
		List<ResourceEditProperty> result = JpaSqlResultMapper.list(query, ResourceEditProperty.class);
		for (ResourceEditProperty prop : result) {
			// Since we have to load also the properties of the parent contexts (to display the replaced
			// values) we must find the property of the current context and set parents hierarchical
			propMap.put(prop.getDescriptorId(),
					findChildPropAndSetParent(prop, propMap.get(prop.getDescriptorId()), contextList));
		}
		return new ArrayList<ResourceEditProperty>(new TreeSet<ResourceEditProperty>(propMap.values()));
	}




    public List<ResourceEditProperty> loadPropertiesForEditResourceTypeRelation(ResourceTypeEntity masterResourceType,
		    ResourceTypeEntity slaveResourceType, ContextEntity currentContext) {
	   Map<Integer, ResourceEditProperty> propMap = new HashMap<>();
	   List<Integer> contextList = contextHierarchy.getContextWithParentIds(currentContext);
	   List<Integer> masterResourceTypeList = getTypeWithParentIds(null, masterResourceType);
	   List<Integer> slaveResourceTypeList = getTypeWithParentIds(null, slaveResourceType);
	   Query query = queries.getPropertyValueForResourceTypeRelationQuery(masterResourceTypeList, slaveResourceTypeList, contextList);
	   List<ResourceEditProperty> result = JpaSqlResultMapper.list(query, ResourceEditProperty.class);
	   for (ResourceEditProperty prop : result) {
		  if(!slaveResourceType.getId().equals(prop.getPropertyValueTypeId()) || !masterResourceType.getId().equals(prop.getMasterTypeId())){
			 prop.setDefinedOnSuperResourceType(true);
		  }
		  // Since we have to load also the properties of the parent contexts (to display the replaced
		  // values) we must find the property of the current context and set parents hierarchical
		  propMap.put(prop.getDescriptorId(),
				  findChildPropAndSetParent(prop, propMap.get(prop.getDescriptorId()), contextList));
	   }
	   return new ArrayList<ResourceEditProperty>(new TreeSet<ResourceEditProperty>(propMap.values()));
    }


    /**
	 * Decides which of the two is the child and sets the other one as parent<br>
	 * The priorities are:
	 * <ol>
	 * <li>relation property before resource property</li>
	 * <li>instance property before type property</li>
	 * <li>child context before parent context</li>
	 * </ol>
	 * 
	 * @param candidate
	 *             not yet evaluated, no parent set
	 * @param existing
	 *             already evaluated, maybe a parent is set
	 * @param contextList
	 *             list with context ids in the correct hierarchical order, starting with the highest context
	 *             (global)
	 * @return the resourceEditProperty identified as child
	 */
	protected ResourceEditProperty findChildPropAndSetParent(ResourceEditProperty candidate,
			ResourceEditProperty existing, List<Integer> contextList) {
		ResourceEditProperty bestChildMatch;

		// 1. find best matching child
		if ((existing == null || (existing.getOrigin() == Origin.INSTANCE || existing.getOrigin() == Origin.TYPE))
				&& (candidate.getOrigin() == Origin.RELATION || candidate.getOrigin() == Origin.TYPE_REL)) {
			bestChildMatch = candidate;
		}
		else if (existing != null && (existing.getOrigin() == Origin.RELATION || existing.getOrigin() == Origin.TYPE_REL)
				&& (candidate.getOrigin() == Origin.INSTANCE || candidate.getOrigin()==Origin.TYPE)) {
			bestChildMatch = existing;
		}
		else {
			
			//If we have no existing element, the candidate is obviously the best one...
			if(existing==null){
				bestChildMatch = candidate;
			}
			//If only one of the two elements is defined on the instance, we take this one as the best match. 
			else if(existing.isDefinedOnInstance() != candidate.isDefinedOnInstance()){
				bestChildMatch = existing.isDefinedOnInstance() ? existing : candidate;
			}
			//Otherwise, we have to compare the properties by their context.
			else {		
				int candidatePrio = contextList.indexOf(candidate.getTypeOrInstanceContextId());
				int existingPrio = contextList.indexOf(existing.getTypeOrInstanceContextId());
				bestChildMatch = candidatePrio > existingPrio ? candidate : existing;
			}
		}

		// 2. set parent and return child
		if (bestChildMatch.equals(candidate)) {
			// priority of candidate is higher than priority of existing
			// set existing as parent of candidate and return candidate as child
			candidate.setParent(existing);
			return candidate;
		}
		else {
			// priority of existing is higher than priority of candidate

			// find best matching parent
			ResourceEditProperty bestParentMatch = findChildPropAndSetParent(candidate,
					existing.getParent(), contextList);
			existing.setParent(bestParentMatch);

			// return existing as child
			return existing;
		}
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

    /**
     *
     * @param resource
     * @param property
     * @param relevantContexts
     * @return a Map containing all properties which override the value of its parent context.
     * <ul>
     *     <li>Map.key = context Name</li>
     *     <li>Map.value = context of the value</li>
     *  </ul>
     */
    public Map<String, String> getPropertyOverviewForResource(ResourceEntity resource, ResourceEditProperty property, List<ContextEntity> relevantContexts) {
        if (relevantContexts.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        List<Integer> contextIds = buildRelevantContextIdsList(relevantContexts);
        Query query = queries.getPropertyOverviewForResourceQuery(property.getTechnicalKey(), resource.getId(), contextIds);
        return getDifferingProperties(property, query);
    }

    /**
     *
     * @param relationId
     * @param property
     * @param relevantContexts
     * @return a Map containing all properties which override the value of its parent context.
     * <ul>
     *     <li>Map.key = context Name</li>
     *     <li>Map.value = context of the value</li>
     *  </ul>
     */
    public Map<String, String> getPropertyOverviewForRelation(int relationId, ResourceEditProperty property, List<ContextEntity> relevantContexts) {
        if (relevantContexts.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        List<Integer> relevantContextIds = buildRelevantContextIdsList(relevantContexts);
        // TODO query for provided resource
        Query query = queries.getPropertyOverviewForRelatedResourceQuery(property.getTechnicalKey(), relationId, relevantContextIds);
        return getDifferingProperties(property, query);
    }

    private Map<String, String> getDifferingProperties(ResourceEditProperty property, Query query) {
        HashMap<String, String> differingProps = new HashMap<>();
        List resultList = query.getResultList();
        for (Object o : resultList) {
            Map.Entry<String, String> entry = createEntryForOverridenProperty(o, property.getPropertyId());
            differingProps.put(entry.getKey(), entry.getValue());
        }
        return differingProps;
    }

    private List<Integer> buildRelevantContextIdsList(List<ContextEntity> contextList) {
        List<Integer> relevantContexts = new ArrayList<>();
        for (ContextEntity contextEntity : contextList) {
            relevantContexts.add(contextEntity.getId());
            if (!contextEntity.getChildren().isEmpty()) {
                for (ContextEntity child: contextEntity.getChildren()) {
                    relevantContexts.add(child.getId());
                }
            }
        }
        return relevantContexts;
    }

	/**
	 * @param resultSetEntry
	 * @param propertyId
	 * @return {@link Map.Entry where the key is the context/environment as string and the value it's value}
	 */
    private Map.Entry<String, String> createEntryForOverridenProperty(Object resultSetEntry, Integer propertyId) {
        Object[] tuple = (Object[]) resultSetEntry;
        String contextName = String.valueOf(tuple[0]);
        String valueForContext;
        try {
            valueForContext = QueryUtils.clobToString((Clob)tuple[1]);
        } catch (SQLException|IOException e) {
            valueForContext = "ERROR: failed to parse the CLOB field TAMW_PROPERTY.valueForContext";
            log.log(Level.WARNING, "ERROR: failed to parse the CLOB field TAMW_PROPERTY.valueForContext of property with id " + propertyId);
        }
        return new AbstractMap.SimpleEntry(contextName, valueForContext);
    }
}
