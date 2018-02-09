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

package ch.puzzle.itc.mobiliar.business.generator.control;

import ch.puzzle.itc.mobiliar.business.environment.entity.AbstractContext;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

import java.util.*;

public class GeneratorUtils {

	/*************************************** START RELATED RESOURCES **********************************************/

	public Set<TemplateDescriptorEntity> getTemplates(ContextEntity context,
			AbstractResourceRelationEntity resourceRelation, Set<TemplateDescriptorEntity> result,
			Integer platform, boolean testing) {
		if (result == null) {
			result = new HashSet<TemplateDescriptorEntity>();
		}
		getTemplateDescriptorsForContext(context, resourceRelation, result, platform, testing);
		if (context.getParent() != null) {
			result = getTemplates(context.getParent(), resourceRelation, result, platform, testing);
		}
		result.addAll(getTemplates(resourceRelation.getResourceRelationType().getResourceTypeA(),
				resourceRelation.getResourceRelationType().getResourceTypeB(), context, platform, testing));
		return result;
	}

	private Set<TemplateDescriptorEntity> getTemplateDescriptorsForContext(ContextEntity context,
			AbstractResourceRelationEntity resourceRelation, Set<TemplateDescriptorEntity> result,
			Integer platform, boolean testing) {
		if (resourceRelation != null) {
			if (resourceRelation.getContexts() != null) {
				for (ResourceRelationContextEntity resourceRelationContext : resourceRelation
						.getContexts()) {
					if (resourceRelationContext.getContext() != null
							&& resourceRelationContext.getContext().getId().equals(context.getId())) {
						result = collectTemplateDescriptors(resourceRelationContext, result, platform,
								testing);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Looks up the properties within the given resource relation for a property with the specific name.
	 * 
	 * @param environment
	 * @param resourceRelation
	 * @param propertyName
	 * @return the found property or null if no property can be found
	 */
	public FreeMarkerProperty findPropertyValueByName(ContextEntity environment,
			AbstractResourceRelationEntity resourceRelation, String propertyName) {
		List<PropertyEntity> relationProperties = getPropertyValues(resourceRelation, environment);
		if(relationProperties!=null && propertyName!=null){
			for(PropertyEntity relationProp : relationProperties){
				if(propertyName.equals(relationProp.getDescriptor().getPropertyName())){
					return relationProp.toFreemarkerProperty();
				}
			}
		}
		return null;
	}

	/**
	 * Suche alle Property-Werte
	 * 
	 * @param relation
	 * @param context
	 * @return
	 */
	public List<PropertyEntity> getPropertyValues(AbstractResourceRelationEntity relation, ContextEntity context) {
		return translatePropertyList(getPropertyValues(relation,
				getPropertyDescriptors(context, relation, null), context));
	}

	private Set<PropertyDescriptorEntity> getPropertyDescriptors(ContextEntity context,
			AbstractResourceRelationEntity resourceRelation, Set<PropertyDescriptorEntity> result) {
		if (result == null) {
			result = new HashSet<PropertyDescriptorEntity>();
		}
		getPropertyDescriptorsForContext(context, resourceRelation, result);
		if (context.getParent() != null) {
			result = getPropertyDescriptors(context.getParent(), resourceRelation, result);
		}
		return result;
	}

	private Set<PropertyDescriptorEntity> getPropertyDescriptorsForContext(ContextEntity context,
			AbstractResourceRelationEntity resourceRelation, Set<PropertyDescriptorEntity> result) {
		if (resourceRelation != null) {
			// Get property descriptors of resource relations
			if (resourceRelation.getContexts() != null) {
				for (ResourceRelationContextEntity resourceRelationContext : resourceRelation
						.getContexts()) {
					if (resourceRelationContext.getContext() != null
							&& resourceRelationContext.getContext().getId().equals(context.getId())) {
						result = collectPropertyDescriptors(resourceRelationContext, result);
					}
				}
			}
			if (resourceRelation.getResourceRelationType() != null) {
				for (ResourceRelationTypeEntity relationType : TemplateUtils.getAllResourceRelationTypes(
						resourceRelation.getResourceRelationType().getResourceTypeA(), resourceRelation
						.getResourceRelationType().getResourceTypeB())) {
					if (relationType.getContexts() != null) {
						for (ResourceRelationTypeContextEntity resourceRelationTypeContext : resourceRelation
								.getResourceRelationType().getContexts()) {
							if (resourceRelationTypeContext.getContext() != null
									&& resourceRelationTypeContext.getContext().getId()
									.equals(context.getId())) {
								result = collectPropertyDescriptors(resourceRelationTypeContext,
										result);
							}
						}
					}
				}
			}
			// Hole die Property Descriptors von der abhängigen Ressource
			getPropertyDescriptorsForContext(context, resourceRelation.getSlaveResource(), result);
		}
		return result;
	}

	/**
	 * Finde die Werte für die angegebene Liste von PropertyDescriptors in der Beziehung
	 * 
	 * @param relation
	 * @param properties
	 * @param context
	 * @return
	 */
	private Map<PropertyDescriptorEntity, List<PropertyEntity>> getPropertyValues(
			AbstractResourceRelationEntity relation, Set<PropertyDescriptorEntity> properties,
			ContextEntity context) {
		Map<PropertyDescriptorEntity, List<PropertyEntity>> result = new LinkedHashMap<PropertyDescriptorEntity, List<PropertyEntity>>();
		for (PropertyDescriptorEntity property : properties) {
			result.put(property, TemplateUtils.getValueForProperty(relation, property, context));
		}
		return result;
	}

	/*************************************** END RELATED RESOURCES **********************************************/

	/*************************************** START RESOURCE **********************************************/

	public Set<TemplateDescriptorEntity> getTemplates(ResourceEntity resource, ContextEntity context,
			Set<TemplateDescriptorEntity> result, Integer platform, boolean testing) {
		if (result == null) {
			result = new HashSet<TemplateDescriptorEntity>();
		}
		if (resource != null) {
			if (resource.getContexts() != null) {
				for (ResourceContextEntity relatedResourceContext : resource.getContexts()) {
					if (relatedResourceContext.getContext() != null
							&& relatedResourceContext.getContext().getId().equals(context.getId())) {
						result = collectTemplateDescriptors(relatedResourceContext, result, platform,
								testing);
					}
				}
			}
			result = getTemplatesForContext(context, resource.getResourceType(), result, platform, testing);
			if(context.getParent()!=null) {
				result = getTemplates(resource, context.getParent(), result, platform, testing);
			}
		}
		return result;
	}

	public List<PropertyEntity> getPropertyValues(ResourceEntity resource, ContextEntity context,
			AMWTemplateExceptionHandler templateExceptionHandler) {
		Set<PropertyDescriptorEntity> properties = getPropertyDescriptors(context, resource, null);
		return translatePropertyList(getPropertyValues(resource, properties, context,
				templateExceptionHandler));
	}

	// dont validte property when loading properties
	private Map<PropertyDescriptorEntity, List<PropertyEntity>> getPropertyValues(ResourceEntity resource,
			Set<PropertyDescriptorEntity> properties, ContextEntity context,
			AMWTemplateExceptionHandler templateExceptionHandler) {
		Map<PropertyDescriptorEntity, List<PropertyEntity>> result = new LinkedHashMap<PropertyDescriptorEntity, List<PropertyEntity>>();
		for (PropertyDescriptorEntity property : properties) {
			result.put(property, TemplateUtils.getValueForProperty(resource, property, context, false,
					templateExceptionHandler));
		}
		return result;
	}

	private Set<PropertyDescriptorEntity> getPropertyDescriptors(ContextEntity context,
			ResourceEntity resource, Set<PropertyDescriptorEntity> result) {
		if (result == null) {
			result = new HashSet<PropertyDescriptorEntity>();
		}
		getPropertyDescriptorsForContext(context, resource, result);
		if (context.getParent() != null) {
			result = getPropertyDescriptors(context.getParent(), resource, result);
		}
		return result;
	}

	private Set<PropertyDescriptorEntity> getPropertyDescriptorsForContext(ContextEntity context,
			ResourceEntity resource, Set<PropertyDescriptorEntity> result) {
		if (resource != null) {
			if (resource.getContexts() != null) {
				for (ResourceContextEntity relatedResourceContext : resource.getContexts()) {
					if (relatedResourceContext.getContext() != null
							&& relatedResourceContext.getContext().getId().equals(context.getId())) {
						result = collectPropertyDescriptors(relatedResourceContext, result);
					}
				}
			}
			getPropertyDescriptorsForContext(context, resource.getResourceType(), result);
		}
		return result;
	}

	/*************************************** END RESOURCE **********************************************/

	/*************************************** START RELATED RESOURCE TYPES **********************************************/

	private Set<TemplateDescriptorEntity> getTemplates(ResourceTypeEntity resourceType,
			ResourceTypeEntity relatedResourceType, ContextEntity context, Integer targetPlatform,
			boolean testing) {
		Set<TemplateDescriptorEntity> result = new HashSet<TemplateDescriptorEntity>();
		for (ResourceRelationTypeEntity relType : TemplateUtils.getAllResourceRelationTypes(resourceType,
				relatedResourceType)) {
			for (ResourceRelationTypeContextEntity relContextType : relType
					.getContextsByLowestContext(context)) {
				if (relContextType.getTemplates() != null){
					for(TemplateDescriptorEntity template : relContextType.getTemplates()){
						if ((testing && template.isTesting())
								|| (!testing && !template.isTesting())
								&& (targetPlatform != null && containsRuntimeId(targetPlatform,
										template.getTargetPlatforms()))) {
							template.setRelationTemplate(true);
							result.add(template);
						}
					}
				}
			}
		}
		return result;
	}


	/*************************************** END RELATED RESOURCE TYPES **********************************************/

	/*************************************** START RESOURCE TYPE **********************************************/

	private Set<TemplateDescriptorEntity> getTemplatesForContext(ContextEntity context,
			ResourceTypeEntity resourceType, Set<TemplateDescriptorEntity> result, Integer platform,
			boolean testing) {
		if (resourceType != null) {
			ResourceTypeEntity resType = resourceType;
			while (resType != null) {
				if (resType.getContexts() != null) {
					// Get templates of resource type context
					for (ResourceTypeContextEntity resourceTypeContext : resType.getContexts()) {
						if (resourceTypeContext.getContext() != null
								&& resourceTypeContext.getContext().getId().equals(context.getId())) {
							result = collectTemplateDescriptors(resourceTypeContext, result, platform,
									testing);
						}
					}
				}
				resType = resType.getParentResourceType();
			}
		}
		return result;
	}

	private Set<PropertyDescriptorEntity> getPropertyDescriptorsForContext(ContextEntity context,
			ResourceTypeEntity resourceType, Set<PropertyDescriptorEntity> result) {
		if (resourceType != null) {
			ResourceTypeEntity resType = resourceType;
			while (resType != null) {
				if (resType.getContexts() != null) {
					// Get templates of resource type context
					for (ResourceTypeContextEntity resourceTypeContext : resType.getContexts()) {
						if (resourceTypeContext.getContext() != null
								&& resourceTypeContext.getContext().getId().equals(context.getId())) {
							result = collectPropertyDescriptors(resourceTypeContext, result);
						}
					}
				}
				resType = resType.getParentResourceType();
			}
		}
		return getPropertyDescriptors(context, result);
	}

	/*************************************** END RESOURCE TYPE **********************************************/

	/*************************************** START CONTEXT **********************************************/

	public List<PropertyEntity> getPropertyValues(ContextEntity context) {
		Set<PropertyDescriptorEntity> properties = getPropertyDescriptors(context, null);
        	Map<PropertyDescriptorEntity, List<PropertyEntity>> propertyValues = getPropertyValues(properties, context);
		return translatePropertyList(propertyValues);
	}

	private Set<PropertyDescriptorEntity> getPropertyDescriptors(ContextEntity context,
			Set<PropertyDescriptorEntity> result) {
		if (result == null) {
			result = new HashSet<>();
		}
		getPropertyDescriptorsForContext(context, result);
		if (context.getParent() != null) {
			result = getPropertyDescriptors(context.getParent(), result);
		}
		return result;
	}

	private Set<PropertyDescriptorEntity> getPropertyDescriptorsForContext(ContextEntity context,
			Set<PropertyDescriptorEntity> result) {
		return collectPropertyDescriptors(context, result);
	}

	private Map<PropertyDescriptorEntity, List<PropertyEntity>> getPropertyValues(
			Set<PropertyDescriptorEntity> properties, ContextEntity context) {
		Map<PropertyDescriptorEntity, List<PropertyEntity>> result = new LinkedHashMap<PropertyDescriptorEntity, List<PropertyEntity>>();
		for (PropertyDescriptorEntity propDescr : properties) {
			result.put(propDescr, TemplateUtils.getValueForProperty(propDescr, context));
		}
		return result;
	}

	/*************************************** END CONTEXT **********************************************/

	/*************************************** START HELPER METHODS **********************************************/

	/**
	 * Checks, if the given runtime id matches with one of the given resource groups.
	 * 
	 * @param runtime
	 * @param runtimes
	 * @return
	 */
	private boolean containsRuntimeId(Integer runtime, Set<ResourceGroupEntity> runtimes) {
		if (runtimes == null) {
			return false;
		}
		for (ResourceGroupEntity t : runtimes) {
			if (runtime.equals(t.getId())) {
				return true;
			}
		}
		return false;
	}

	public Set<TemplateDescriptorEntity> collectTemplateDescriptors(AbstractContext context,
			Set<TemplateDescriptorEntity> result, Integer targetPlatform, boolean testing) {
		if (result == null) {
			result = new HashSet<TemplateDescriptorEntity>();
		}
		if (context.getTemplates() != null) {
			for (TemplateDescriptorEntity template : context.getTemplates()) {
				// Only consider templates for the given platform
				if (targetPlatform != null
						&& containsRuntimeId(targetPlatform, template.getTargetPlatforms())) {
					// distinct between testing and non-testing templates
					if ((testing && template.isTesting()) || (!testing && !template.isTesting())) {
						template.setOwnerResource(context);
						result.add(template);
					}
				}
			}
		}
		return result;
	}

	private static Set<PropertyDescriptorEntity> collectPropertyDescriptors(AbstractContext context,
			Set<PropertyDescriptorEntity> result) {
		if (context.getPropertyDescriptors() != null) {
			for (PropertyDescriptorEntity p : context.getPropertyDescriptors()) {
				result.add(p);
			}
		}
		return result;
	}

	public List<PropertyEntity> translatePropertyList(
			Map<PropertyDescriptorEntity, List<PropertyEntity>> properties) {
		List<PropertyEntity> result = new ArrayList<>();
		if (properties != null) {
			for (PropertyDescriptorEntity p : properties.keySet()) {
			     //We're only interested in the first property entity since it's the highest prioritized and therefore the one which defines the actual value
			    List<PropertyEntity> propertyEntities = properties.get(p);
			    if(!propertyEntities.isEmpty()) {
				   result.add(properties.get(p).get(0));
			    }
			    else{
				   PropertyEntity emptyProperty = new PropertyEntity();
				   emptyProperty.setDescriptor(p);
				   result.add(emptyProperty);
			    }
			}
		}
		return result;
	}
	/*************************************** END HELPER METHODS **********************************************/

}
