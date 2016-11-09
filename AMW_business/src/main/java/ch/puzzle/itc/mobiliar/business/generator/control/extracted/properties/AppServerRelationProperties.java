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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.*;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwAppServerNodeModel;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwTemplateModel;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

/**
 * Loads properties for {@link ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity}.
 * 
 * 
 * Only properties for relations passed via the following methods will be loaded.
 * 
 * <ul>
 * <li> {@link #addConsumedRelation(String, ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity, ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity)}</li>
 * <li> {@link #addProvidedRelation(String, ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity, ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity)}</li>
 * </ul>
 *
 * @author ama
 * 
 */
public class AppServerRelationProperties {

	private Logger log = Logger.getLogger(AppServerRelationProperties.class.getSimpleName());
	

	private ResourceEntity owner;
	private ContextEntity context;
	private BasePropertyCollector collector;
	private String identifier;

	private Map<String, FreeMarkerProperty> properties = Maps.newLinkedHashMap();
	@Setter
	private List<AmwFunctionEntity> functions;

	private List<AppServerRelationProperties> consumed = Lists.newArrayList();
	private List<AppServerRelationProperties> provided = Lists.newArrayList();
	
	@Getter
	@Setter
	private SoftlinkGenerationPackage softlinkPPIGenerationPackage;

	private SetMultimap<ResourceEntity, GeneratedTemplate> templatesCache;

	private boolean supportNesting = true;
	private ApplicationResolver resolver;
	private AMWTemplateExceptionHandler templateExceptionHandler;

	public AppServerRelationProperties(ContextEntity context, ResourceEntity owner, AMWTemplateExceptionHandler templateExceptionHandler) {
		this.owner = owner;
		this.context = context;
		this.templateExceptionHandler = templateExceptionHandler;
		this.collector = new BasePropertyCollector();
		this.properties = collectResourceProperties(context, owner);
	}

	private AppServerRelationProperties(ResourceEntity resource, Map<String, FreeMarkerProperty> properties,
			String identifier, AMWTemplateExceptionHandler templateExceptionHandler) {
		this.owner = resource;
		this.properties = properties;
		this.identifier = identifier;
		this.templateExceptionHandler = templateExceptionHandler;
	}
	
	/**
	 * Transforms Properties into HashMap as used by Generator.
	 * 
	 * @return
	 */
	public AmwResourceTemplateModel transformModel() {
		AmwResourceTemplateModel model = new AmwResourceTemplateModel();
		model.setProperties(properties);
		model.setFunctions(functions);
		model.setResourceEntity(owner);
        Map<String, Map<String, AmwResourceTemplateModel>> transformConsumedRelated = transformRelated(consumed);
		model.setConsumedResTypes(transformConsumedRelated);
		model.setProvidedResTypes(transformRelated(provided));

		AmwAppServerNodeModel amwAppServerNodeModel = new AmwAppServerNodeModel();
		addAppServerNodeViaResolver(amwAppServerNodeModel, resolver);
		model.setAppServerNodeViaResolver(amwAppServerNodeModel);
		
		log.fine("transforming: " + getOwner());

		return model;
	}

	public AppServerRelationProperties addConsumedRelation(String identifier, ResourceEntity resource,
			ConsumedResourceRelationEntity relation) {
		return addRelation(identifier, resource, relation, consumed);
	}

	public AppServerRelationProperties addProvidedRelation(String identifier, ResourceEntity resource,
			ProvidedResourceRelationEntity relation) {
		return addRelation(identifier, resource, relation, provided);
	}

	/**
	 * FIXME: [ama] can we get rid of this?
	 * 
	 * We merge because ASR creates two entries for consumed resource that consumes other resources, e.g.
	 * 
	 * <pre>
	 * app-> >ws->lb
	 * creates ws->lb
	 * creates app->ws
	 * </pre>
	 * 
	 * we merge to ensure the consumed lists are also present when rendering the slave part (app->ws)
	 */
	public void merge(AppServerRelationProperties other) {
		if (this.consumed.isEmpty()) {
			this.consumed = other.consumed;
		}
		if (other.consumed.isEmpty()) {
			other.consumed = this.consumed;
		}
		if (this.provided.isEmpty()) {
			this.provided = other.provided;
		}
		if (other.provided.isEmpty()) {
			other.provided = this.provided;
		}
		if (other.resolver != null) {
			this.resolver = other.resolver;
		}
		if (this.resolver != null) {
			other.resolver = this.resolver;
		}
	}

	public ResourceEntity getOwner() {
		return owner;
	}

	public Map<String, FreeMarkerProperty> getProperties() {
		return properties;
	}

	public List<AppServerRelationProperties> getConsumed() {
		return consumed;
	}

	public List<AppServerRelationProperties> getProvided() {
		return provided;
	}

	public void setTemplatesCache(SetMultimap<ResourceEntity, GeneratedTemplate> templatesCache) {
		this.templatesCache = templatesCache;
	}

	private AmwResourceTemplateModel getPropertiesWithTemplates() {
        AmwResourceTemplateModel model = new AmwResourceTemplateModel();
        model.setProperties(getProperties());
		model.setFunctions(functions);
		model.setResourceEntity(owner);
        setTemplates(model);
        return model;
	}

    private void setTemplates(AmwResourceTemplateModel model) {
        if (templatesCache != null && getOwner() != null) {
            Map<String, GeneratedTemplate> templates = Maps.newLinkedHashMap();
            for (GeneratedTemplate template : templatesCache.get(getOwner())) {
                templates.put(template.getName(), template);
            }
            model.setTemplates(templates);
        }
    }


    private Map<String, Map<String, AmwResourceTemplateModel>> transformRelated(List<AppServerRelationProperties> relations) {
		return transformRelatedInternal(relations, supportNesting);
	}

	private Map<String, Map<String, AmwResourceTemplateModel>> transformRelatedAndStop(List<AppServerRelationProperties> relations) {
		return transformRelatedInternal(relations, false);
	}

	private Map<String, Map<String,AmwResourceTemplateModel>> transformRelatedInternal(List<AppServerRelationProperties> relations, boolean transformNested) {
		Map<String, Map<String,AmwResourceTemplateModel>> map = Maps.newLinkedHashMap();
		for (AppServerRelationProperties relation : relations) {
			ResourceEntity slave = relation.getOwner();

			String name = StringUtils.isBlank(relation.identifier) ? slave.getName() : relation.identifier;

			String typeName = slave.getResourceType().getName();
			
			if (!map.containsKey(typeName)) {

                Map<String,AmwResourceTemplateModel> typeMap = Maps.newLinkedHashMap();
				map.put(typeName, typeMap);
			}
            Map<String,AmwResourceTemplateModel> typeMap = map.get(typeName);

            AmwResourceTemplateModel relationModel = relation.getPropertiesWithTemplates();

            typeMap.put(name, relationModel);

			AmwAppServerNodeModel appServerNodeViaResolver = new AmwAppServerNodeModel();
            addAppServerNodeViaResolver(appServerNodeViaResolver, relation.resolver);
            relationModel.setAppServerNodeViaResolver(appServerNodeViaResolver);
            
            // This Resource has a Softlink
            SoftlinkGenerationPackage generationPackage = relation.getSoftlinkPPIGenerationPackage();
            if(generationPackage != null && generationPackage.getGenerationPackage()!= null && generationPackage.getPpiResourceEntity() != null){
            	GenerationSubPackage asPackage = generationPackage.getGenerationPackage().getGenerationSubPackages().get(generationPackage.getGenerationPackage().
                        getGenerationSubPackages().size() - 1);
            	AmwResourceTemplateModel asModelPPI = asPackage.getPackageGenerationUnit().getPropertiesAsModel();
                // find AmwResourceTemplateModel for the PPI
				GenerationUnit generationUnitPPI = GenerationUnit.forResource(new HashSet<GenerationUnit>(asPackage.getSubGenerationUnitsAsList()), generationPackage.getPpiResourceEntity());
                // find AmwResourceTemplateModel for the Runtime of Provider side
                GenerationUnit generationUnitRuntimePPI = GenerationUnit.getRuntimeGenerationUnit(new HashSet<GenerationUnit>(asPackage.getSubGenerationUnitsAsList()));

                // set Softlinkref on cpi side (twice the same id so take the id from PPI Resource Entity)
                relationModel.setSoftlinkRef(generationPackage.getPpiResourceEntity().getSoftlinkId());

                AmwResourceTemplateModel unitResourceTemplateModel = generationUnitPPI.getPropertiesAsModel();
                // set Softlinkid on ppi side
                unitResourceTemplateModel.setSoftlinkId(generationPackage.getPpiResourceEntity().getSoftlinkId());

                AmwTemplateModel model = new AmwTemplateModel();
				model.setGlobalFunctionTemplates(generationPackage.getGlobalFunctions());
				model.setUnitResourceTemplateModel(unitResourceTemplateModel);
				model.setAsProperties(asModelPPI);
                model.setRuntimeProperties(generationUnitRuntimePPI.getPropertiesAsModel());

				model.populateBaseProperties();

				relationModel.setPpiAmwTemplateModel(model);
            }
            
			if (transformNested) {
				addRelated(relationModel, relation);
			}
		}

		return map;
	}

	/**
	 * Adds properties of app, as and node to the map
	 * 
	 * @param model
	 * @param resolver
	 */
	private void addAppServerNodeViaResolver(AmwAppServerNodeModel model, ApplicationResolver resolver) {
		if (resolver != null) {
			resolver.transform(templateExceptionHandler, model);
		}
	}

	private void addRelated(AmwResourceTemplateModel model, AppServerRelationProperties relation) {
		Map<String, Map<String, AmwResourceTemplateModel>> transformConsumedProperties = transformRelated(relation.getConsumed());
        model.setConsumedResTypes(transformConsumedProperties);

        Map<String, Map<String, AmwResourceTemplateModel>> transformProvidedProperties = transformRelatedAndStop(relation.getProvided());
		model.setProvidedResTypes(transformProvidedProperties);
	}


	/**
	 * Adds and collects properties for related Resource
	 *
	 * constructor
	 * 
	 * @param identifier
	 * @param resource
	 * @param resourceRelation
	 * @param list
	 * @return
	 */
	private AppServerRelationProperties addRelation(String identifier, ResourceEntity resource, AbstractResourceRelationEntity resourceRelation,
			List<AppServerRelationProperties> list) {
		Map<String, FreeMarkerProperty> properties = collectResourceProperties(context, resource);

		// relation overrides properties
		properties.putAll(collectRelationProperties(context, resource, resourceRelation));

		AppServerRelationProperties asrProperties = new AppServerRelationProperties(resource, properties, identifier, templateExceptionHandler);
		list.add(asrProperties);
		return asrProperties;

	}

	/**
	 * Collects resource Properties, populates typed with types of collected properties.
	 * 
	 * Populates properties
	 * 
	 * @param context
	 * @param resource
	 * @return properties of resource
	 */
	private Map<String, FreeMarkerProperty> collectResourceProperties(ContextEntity context, ResourceEntity resource) {
		return collector.propertiesForResource(resource, context, templateExceptionHandler);
	}

	/**
	 * Collects relation Properties, populates typed with types of collected properties.
	 * 
	 * Populates properties
	 * 
	 * @param context
	 * @param resource
	 * @return properties of resource
	 */
	private Map<String, FreeMarkerProperty> collectRelationProperties(ContextEntity context, ResourceEntity resource,
			AbstractResourceRelationEntity resourceRelation) {
		return collector.propertiesForRelation(resource, context, resourceRelation);
	}

	@Override
	public String toString() {
		return "AppServerRelationProperties [properties=" + properties + ", consumed=" + consumed.size() + ", provided="
				+ provided.size() + "]";
	}

	public void addResolver(ApplicationResolver applicationResolver) {
		this.resolver = applicationResolver;
		log.info("adding resolver to " + getOwner().getName());
	}

}
