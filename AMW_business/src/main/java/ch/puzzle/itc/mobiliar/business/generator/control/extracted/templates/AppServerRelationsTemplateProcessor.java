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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.generator.control.GenerationUnitGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.container.DeploymentProperties;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwConsumerTemplateModel;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwProviderTemplateModel;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwTemplateModel;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Generates Templates for {@link GenerationUnit} set.
 */
public class AppServerRelationsTemplateProcessor {

	/**
	 * "hostName" und "active" sind reservierte Propertie, welche auf dem Node definiert sind und welche für die
	 * Angabe des Host pro AppServer Node und Context verwendet wird.
	 */
	public static final String HOST_NAME = "hostName";
    public static final String NODE_ACTIVE = "active";

	private AmwResourceTemplateModel appServerProperties;
	protected AmwResourceTemplateModel nodeProperties;
    private AmwResourceTemplateModel runtimeProperties;

	private Map<String, AmwResourceTemplateModel> applications;
	private Map<String, FreeMarkerProperty> contextProperties;

	private BaseTemplateProcessor processor;

	private Logger log;

	private GenerationContext generationContext;

	private Map<ResourceEntity, Set<GeneratedTemplate>> templatesCache;
	private Map<String, GeneratedTemplate> templateFiles;

	public AppServerRelationsTemplateProcessor(Logger log, GenerationContext generationContext) {
		super();
		this.log = log;
		this.generationContext = generationContext;
		processor = new BaseTemplateProcessor();
	}

	public void setGenerationContext(GenerationContext generationContext){
		this.generationContext = generationContext;
	}

	public void setGlobals(GenerationPackage work) {
		// prepare globals
		GenerationOptions options = work.getGenerationOptions();

		appServerProperties = GenerationUnit.forAppServer(work.getAsSet()).getPropertiesAsModel();
	     if(options.getContext().getTargetPlatform()!=null) {
		    GenerationUnit runtimeGenerationUnit = GenerationUnit
				    .forResource(work.getAsSet(), options.getContext().getTargetPlatform());
		    runtimeProperties = runtimeGenerationUnit!=null ? runtimeGenerationUnit
				    .getPropertiesAsModel() : null;
		}

		applications = options.getApplications();
		templateFiles = options.getTemplateFiles();
		contextProperties = options.getContextProperties();
		templatesCache = new LinkedHashMap<>();
	}

     public void setRuntimeProperties(Set<GenerationUnit> work, ResourceEntity runtime){
	    runtimeProperties = GenerationUnit.forResource(work, runtime).getPropertiesAsModel();
	}

	public void setNodeProperties(Set<GenerationUnit> work, ResourceEntity node){
		nodeProperties = GenerationUnit.forResource(work, node).getPropertiesAsModel();
	}
	/**
	 * Generates an ApplicationServer and returns its List<GenerationUnitGenerationResult>
	 *
	 * @param work
	 * @param node
	 * @return
	 * @throws IOException
	 */
	public List<GenerationUnitGenerationResult> generateAppServer(Set<GenerationUnit> work, ResourceEntity node) throws	IOException {
		setNodeProperties(work, node);
	     return generateInternal(work);
	}


	/**
	 * Generates an Application and returns its List<GenerationUnitGenerationResult>
	 *
	 * @param work
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public List<GenerationUnitGenerationResult> generateApp(Set<GenerationUnit> work, ResourceEntity application) throws IOException {
		GenerationUnit generationUnit = GenerationUnit.forResource(work, application);
		if (generationUnit != null) {
			AmwResourceTemplateModel appProperties = generationUnit.getPropertiesAsModel();
			applications.put(application.getName(), appProperties);
		}
		return generateInternal(work);
	}

	private List<GenerationUnitGenerationResult> generateInternal(Set<GenerationUnit> work)
			throws IOException {
		List<GenerationUnitGenerationResult> results = new ArrayList<GenerationUnitGenerationResult>();
		Set<String> currentTemplateNames = Set.copyOf(templateFiles.keySet());

		for (GenerationUnit unit : work) {
			if(unit.isGenerateTemplates()){
				log.fine("templatesCache: " + templatesCache);
				unit.getAppServerRelationProperties().setTemplatesCache(templatesCache);

				ResourceEntity resource = unit.getSlaveResource();
				log.info("processing resource " + resource.getName());

				GenerationUnitGenerationResult resourceTemplateResult = generateResourceTemplates(unit);
				addTemplatesToCache(resource, resourceTemplateResult.getGeneratedTemplates());
				results.add(resourceTemplateResult);

				if (!unit.getRelationTemplates().isEmpty()) {

					GenerationUnitGenerationResult resourceRelationTemplateResult = generateResourceRelationTemplates(work, unit);
					addTemplatesToCache(resource, resourceRelationTemplateResult.getGeneratedTemplates());
					results.add(resourceRelationTemplateResult);
				}

				if (templateFiles.keySet().size() != currentTemplateNames.size()) {
					log.info("templates: " + String.join(", ", templateFiles.keySet()));
					currentTemplateNames = Set.copyOf(templateFiles.keySet());
				}


			}

			if (templateFiles.keySet().size() != currentTemplateNames.size()) {
				log.info("templates: " + String.join(", ", templateFiles.keySet()));
				currentTemplateNames = Set.copyOf(templateFiles.keySet());
			}
		}
		return results;
	}

	private GenerationUnitGenerationResult generateResourceRelationTemplates(Set<GenerationUnit> work, GenerationUnit unit)
			throws IOException {

		ResourceEntity consumer = unit.getResource();

	    GenerationUnit generationUnit = GenerationUnit.forResource(work, consumer);
	    
	    AmwConsumerTemplateModel consumerModel = new AmwConsumerTemplateModel();
		consumerModel.setAsProperties(appServerProperties);
		consumerModel.setNodeProperties(nodeProperties);
	    if(generationUnit != null){
	    	consumerModel.setConsumerUnit(generationUnit.getPropertiesAsModel());
	    }
	    
		AmwProviderTemplateModel providerModel = new AmwProviderTemplateModel();
		providerModel.setProviderUnit(unit.getPropertiesAsModel());
		
		AmwTemplateModel model = new AmwTemplateModel();
		model.setGlobalFunctionTemplates(unit.getGlobalFunctionTemplates());
		model.setProviderModel(providerModel);
		model.setConsumerModel(consumerModel);
		model.setContextProperties(contextProperties);
		model.setDeploymentProperties(getDeploymentProperties(generationContext));
		model.setRuntimeProperties(runtimeProperties);
		model.populateBaseProperties();
		return processor.generateResourceRelationTemplates(unit, model);
	}

	private GenerationUnitGenerationResult generateResourceTemplates(GenerationUnit unit) throws IOException{
		AmwTemplateModel model = new AmwTemplateModel();
		model.setGlobalFunctionTemplates(unit.getGlobalFunctionTemplates());
		model.setUnitResourceTemplateModel(unit.getPropertiesAsModel());
		model.setAsProperties(appServerProperties);
		model.setNodeProperties(nodeProperties);
		model.setContextProperties(contextProperties);
		model.setDeploymentProperties(getDeploymentProperties(generationContext));
		model.setRuntimeProperties(runtimeProperties);
		model.setApplications(applications);
		model.setTemplateFiles(templateFiles);
		model.populateBaseProperties();
		return processor.generateResourceTemplates(unit, model);
    }

    private DeploymentProperties getDeploymentProperties(GenerationContext generationContext){
		return generationContext.getDeploymentProperties();
	}

	/**
	 * Add Templates to cache
	 * @param resource
	 * @param files
	 */
	private void addTemplatesToCache(ResourceEntity resource, List<GeneratedTemplate> files) {
		for (GeneratedTemplate template : files) {
			String key = template.getName();

			if (templateFiles.containsKey(key)) {
				key += UUID.randomUUID(); // make sure we dont loose any templates, see 4380
			}

			getOrCreateTemplateSet(resource).add(template);
			templateFiles.put(key, template);
		}
	}

	private Set<GeneratedTemplate> getOrCreateTemplateSet(ResourceEntity resource) {
		return templatesCache.computeIfAbsent(resource, r -> new LinkedHashSet<>());
	}

	/**
	 * checks if the defined Property HOST_NAME is available and not null or empty
	 *
	 * @return
	 */
	public boolean isNodeEnabled() {
        if(nodeProperties != null){
        	FreeMarkerProperty isNodeActive = nodeProperties.getProperty(NODE_ACTIVE);
            if(isNodeActive != null){
            	return Boolean.parseBoolean(isNodeActive.getCurrentValue());
            }
        }
        return false;
	}

    public void setNodeEnabledForTestGeneration(){
        if(nodeProperties == null){
            nodeProperties = new AmwResourceTemplateModel();
        }
        nodeProperties.put(NODE_ACTIVE, new FreeMarkerProperty("true", NODE_ACTIVE));
    }

	/**
	 * returns the hostname out of the nodeProperties make sure it set
	 * @return
	 */
	public String getHostname(){
		if(nodeProperties != null){
			FreeMarkerProperty hostname = nodeProperties.getProperty(HOST_NAME);
			if(hostname != null){
				return hostname.getCurrentValue();
			}
		}
		return null;
	}


	public void setTestNodeHostname() {
		if(!isNodeEnabled() && nodeProperties != null){
			nodeProperties.put(HOST_NAME, new FreeMarkerProperty("testhostname", HOST_NAME));
		}
	}
}
