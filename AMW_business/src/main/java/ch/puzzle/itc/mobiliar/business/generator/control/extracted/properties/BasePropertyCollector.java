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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties;

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorUtils;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.util.ContextNames;

import javax.ejb.Stateless;
import java.util.*;

@Stateless
public class BasePropertyCollector {
	private static final String RESERVED_PROPERTY_NAME = "name";
	private static final String RESERVED_PROPERTY_DOMAIN = "domain";
	private static final String RESERVED_PROPERTY_ID = "id";
	private static final String RESERVED_PROPERTY_RELEASE = "release";
    private static final String RESERVED_PROPERTY_GROUP_ID = "resGroupId";
	private static final String RESERVED_PROPERTY_OUT_OF_SERVICE = "outOfServiceRelease";

	
	protected Map<String, FreeMarkerProperty> translatePropertyList(final List<PropertyEntity> properties, final String name, final Integer id, final Integer groupId, final String release, final String outOfServiceRelease) {
		final Map<String, FreeMarkerProperty> result = new TreeMap<>();
		if (properties != null) {
			for (final PropertyEntity p : properties) {
				// do not add Properties, which are KeyOptional and no Value is set
				if(p.getDescriptor().isOptional()){
					if(p.hasValue()){
						result.put(p.getDescriptor().getPropertyName(), p.toFreemarkerProperty());
					}
				} else {
					result.put(p.getDescriptor().getPropertyName(), p.toFreemarkerProperty());
				}
			}
		}
		if (name != null) {
			result.put(RESERVED_PROPERTY_NAME,  new FreeMarkerProperty(name, RESERVED_PROPERTY_NAME));
		}
		if (id != null) {
			result.put(RESERVED_PROPERTY_ID,  new FreeMarkerProperty(id.toString(), RESERVED_PROPERTY_ID));
		}
		if (release != null) {
			result.put(RESERVED_PROPERTY_RELEASE,  new FreeMarkerProperty(release, RESERVED_PROPERTY_RELEASE));
		}
		if(groupId !=null){
		     result.put(RESERVED_PROPERTY_GROUP_ID,  new FreeMarkerProperty(groupId.toString(), RESERVED_PROPERTY_GROUP_ID));
		}
		if(outOfServiceRelease != null){
			result.put(RESERVED_PROPERTY_OUT_OF_SERVICE,  new FreeMarkerProperty(outOfServiceRelease, RESERVED_PROPERTY_OUT_OF_SERVICE));
		}
		return result;
	}

	// returns flat property list <String,FreeMarkerProperty>
	protected Map<String, FreeMarkerProperty> propertiesForRelation(final ResourceEntity resource,
			final ContextEntity context,
			final AbstractResourceRelationEntity relation) {
		return translatePropertyList(getPropertiesForRelation(context, relation),
				resource.getName(), resource.getId(), resource.getResourceGroup().getId(), null, null);
	}

	// returns flat property list <String,FreeMarkerProperty>
	public Map<String, FreeMarkerProperty> propertiesForResource(final ResourceEntity resource,
			final ContextEntity context,
			AMWTemplateExceptionHandler templateExceptionHandler) {

		String outOfServiceRelease = resource.getResourceGroup().getOutOfServiceRelease() != null ? resource.getResourceGroup().getOutOfServiceRelease().getName() : null;

		return translatePropertyList(getPropertiesForResource(resource, context, templateExceptionHandler),
				resource.getName(), resource.getId(), resource.getResourceGroup().getId(), resource.getRelease() != null ? resource.getRelease().getName() : null, outOfServiceRelease);
	}

	// returns flat property list <String,FreeMarkerProperty>
	public Map<String, FreeMarkerProperty> propertiesForContext(final ContextEntity context) {
	    Map<String, FreeMarkerProperty> result = translatePropertyList(utils().getPropertyValues(context),
			    context.getName(), context.getId(), null, null, null);
	    result.put(RESERVED_PROPERTY_DOMAIN, new FreeMarkerProperty(getDomainName(context), RESERVED_PROPERTY_DOMAIN));
	    return result;
	}

	private String getDomainName(ContextEntity context) {
	     if(context.getContextType()!=null) {
		    ContextNames contextName = ContextNames.valueOf(context.getContextType().getName());
		    switch (contextName) {
		    case ENV:
			   return context.getParent() != null ? context.getParent().getName() : null;
		    case DOMAIN:
			   return context.getName();
		    default:
			   return null;
		    }
		}
	    	return null;
	}

	private List<PropertyEntity> getPropertiesForResource(final ResourceEntity applicationServer,
			final ContextEntity context, AMWTemplateExceptionHandler templateExceptionHandler) {
		return utils().getPropertyValues(applicationServer, context, templateExceptionHandler);
	}

	private List<PropertyEntity> getPropertiesForRelation(ContextEntity context,
			AbstractResourceRelationEntity relation) {
		return utils().getPropertyValues(relation, context);
	}

	protected GeneratorUtils utils() {
		return new GeneratorUtils();
	}

}
