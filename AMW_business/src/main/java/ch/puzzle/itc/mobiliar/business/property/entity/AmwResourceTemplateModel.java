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

package ch.puzzle.itc.mobiliar.business.property.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleCollection;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import lombok.Setter;

public class AmwResourceTemplateModel implements TemplateHashModelEx {

	private static final String AMW_PROPERTIES = "amwproperties";
	private static final String AMWFUNCTION = "amwfunction";
    private static final String SOFTLINK = "softlink";
    private static final String SOFTLINK_ID = "softlinkid";
    private static final String SOFTLINK_REF = "softlinkref";
    private static final String CALLER = "callercpi";
    private static final String RESOURCE_TYPE_NAME = "resourceTypeName";
	public static final String RESERVED_PROPERTY_PROVIDEDRES = "providedResTypes";
	public static final String RESERVED_PROPERTY_CONSUMEDRES = "consumedResTypes";
    private static final String RESERVED_PROPERTY_TEMPLATES = "templates";
    private static final String PARENT = "parent";

    @Setter
    private String identifier;
	@Setter
	private Map<String, FreeMarkerProperty> properties;
	@Setter
	private Map<String, Map<String, AmwResourceTemplateModel>> consumedResTypes;
	@Setter
	private Map<String, Map<String, AmwResourceTemplateModel>> providedResTypes;
	@Setter
	private AmwAppServerNodeModel appServerNodeViaResolver;
    /**
     * contains the ppi Side of a softlink
     */
	@Setter
	private AmwTemplateModel  ppiAmwTemplateModel;
    /**
     * is used to provide the caller CPI to the ppi when on ppi side
     */
    @Setter
    private AmwTemplateModel callerCpiTemplateModel;
    @Setter
    private String softlinkId;
    @Setter
    private String softlinkRef;
	@Setter
	private List<AmwFunctionEntity> functions = new ArrayList<>();
    @Setter
    Map<String, GeneratedTemplate> templates = new LinkedHashMap<>();
    /**
     * Consists also of not generated Templates
     */
    @Setter
    Set<TemplateDescriptorEntity> resourceTemplates = new HashSet<>();

    @Setter
    ResourceEntity resourceEntity;
    
    @Setter
	private AmwResourceTemplateModel parentResourceTemplateModel;
    
    private AmwTemplateModel baseModelForContextSwitch;

	@Override
	public TemplateModel get(String key) throws TemplateModelException {
		
		DefaultObjectWrapper beansWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_21);
		if(RESERVED_PROPERTY_CONSUMEDRES.equals(key)){
            return getAsSimpleHash(consumedResTypes, beansWrapper);
		}else if(RESERVED_PROPERTY_PROVIDEDRES.equals(key)){
            return getAsSimpleHash(providedResTypes, beansWrapper);
		}else if(RESERVED_PROPERTY_TEMPLATES.equals(key)){
			
			Map<String, Map<String, String>> allTemplates = new HashMap<>();
			// first add all non generated
			if(resourceTemplates != null){
				allTemplates.putAll(AmwTemplateModelHelper.convertTemplateDescriptorToHash(resourceTemplates));
			}
			// then the generated so that the content is available
			allTemplates.putAll(AmwTemplateModelHelper.convertTemplatesToHash(templates));
			
            return getAsSimpleHash(allTemplates, beansWrapper);
        }else if(AMW_PROPERTIES.equals(key)){
            return getAsSimpleHash(properties, beansWrapper);
        }else if(PARENT.equals(key)){
        	return parentResourceTemplateModel;
        }

        if(RESOURCE_TYPE_NAME.equals(key)){
            if(resourceEntity != null && resourceEntity.getResourceType() != null){
                return beansWrapper.wrap(resourceEntity.getResourceType().getName());
            }
            return null;
        }

		if(appServerNodeViaResolver != null && appServerNodeViaResolver.containsKey(key)){
			return beansWrapper.wrap(appServerNodeViaResolver.get(key));
        }
		
		if(properties != null && properties.containsKey(key)){
            return AmwTemplateModelHelper.wrapFreemarkerProperty(properties.get(key), beansWrapper);
        }
		if(AMWFUNCTION.equals(key)){
			return new AmwFunctionsModel(functions, this, baseModelForContextSwitch);
		}

        if(SOFTLINK.equals(key)){
            return ppiAmwTemplateModel;
        }
        if(SOFTLINK_ID.equals(key)){
            return beansWrapper.wrap(softlinkId);
        }
        if(SOFTLINK_REF.equals(key)){
            return beansWrapper.wrap(softlinkRef);
        }

        if(CALLER.equals(key)){
            return callerCpiTemplateModel;
        }
		
		// access Consumed Resources directly like Properties
		AmwResourceTemplateModel consumedResourceForKey = getConsumedResourceForKey(key);
		if(consumedResourceForKey != null){
			return consumedResourceForKey;
		}
		
		return null;
	}

    private SimpleHash getAsSimpleHash(Map subModel, DefaultObjectWrapper beansWrapper){
        if(subModel != null){
            return new AmwSimpleHashTemplateModel(subModel, beansWrapper);
        }
        return null;
    }
	
	private AmwResourceTemplateModel getConsumedResourceForKey(String key){
		if(consumedResTypes!= null){
			for (String consumedResTypeKey : consumedResTypes.keySet()) {
				Map<String, AmwResourceTemplateModel> consumedResourcesPerType = consumedResTypes.get(consumedResTypeKey);
				if(consumedResourcesPerType != null){
					if(consumedResourcesPerType.containsKey(key)){
						return consumedResourcesPerType.get(key);
					}
				}
			}
		}
		
		return null;
	}

    private Map<String, AmwResourceTemplateModel> getConsumedResourcesAsMap(){
        Map<String, AmwResourceTemplateModel> result = new LinkedHashMap<>();
        if(consumedResTypes!= null){
            for (String consumedResTypeKey : consumedResTypes.keySet()) {
                Map<String, AmwResourceTemplateModel> consumedResourcesPerType = consumedResTypes.get(consumedResTypeKey);
                if(consumedResourcesPerType != null){
                    for (String key :consumedResourcesPerType.keySet()){
                        result.put(key, consumedResourcesPerType.get(key));
                    }
                }
            }
        }

        return result;
    }

	@Override
	public boolean isEmpty() throws TemplateModelException {
		if(properties != null && !properties.isEmpty()){
			return false;
		}
		if(consumedResTypes != null && !consumedResTypes.isEmpty()){
			return false;
		}
		if(providedResTypes != null && !providedResTypes.isEmpty()){
			return false;
		}
		if(parentResourceTemplateModel != null){
			return false;
		}
		if(appServerNodeViaResolver != null && !appServerNodeViaResolver.isEmpty()){
			return false;
		}
        if(ppiAmwTemplateModel != null){
            return false;
        }
		return true;
	}

	@Override
	public int size() throws TemplateModelException {
		return keySet().size();
	}

	@Override
	public TemplateCollectionModel keys() throws TemplateModelException {
        DefaultObjectWrapper beansWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_21);
        return new SimpleCollection(keySet(), beansWrapper);
	}

    /**
     * @return The KeySet of the AmwResourceTemplateModel
     */
    public Collection<? extends String> keySet() throws TemplateModelException{
        Collection<String> collection = new ArrayList<>();

        if(consumedResTypes!= null && !consumedResTypes.isEmpty()){
            collection.add(RESERVED_PROPERTY_CONSUMEDRES);
        }
        if(providedResTypes != null && !providedResTypes.isEmpty()){
            collection.add(RESERVED_PROPERTY_PROVIDEDRES);
        }
        if(parentResourceTemplateModel != null){
            collection.add(PARENT);
        }
        collection.add(RESERVED_PROPERTY_TEMPLATES);
        collection.add(AMW_PROPERTIES);

        if(appServerNodeViaResolver != null && !appServerNodeViaResolver.isEmpty()) {
            collection.addAll(appServerNodeViaResolver.keySet());
        }
        if(properties != null && !properties.isEmpty()){
            collection.addAll(properties.keySet());
        }

        collection.add(AMWFUNCTION);
        collection.add(SOFTLINK);
        collection.add(SOFTLINK_ID);
        collection.add(SOFTLINK_REF);
        collection.add(CALLER);

        //consumed directAccess to resource
        Map<String, AmwResourceTemplateModel> consumedResourcesAsMap = getConsumedResourcesAsMap();
        collection.addAll(consumedResourcesAsMap.keySet());

        return collection;
    }

	@Override
	public TemplateCollectionModel values() throws TemplateModelException {
        Collection<TemplateModel> collection = new ArrayList<>();
        DefaultObjectWrapper beansWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_21);

        if(consumedResTypes!= null && !consumedResTypes.isEmpty()){
            collection.add(getAsSimpleHash(consumedResTypes, beansWrapper));
        }
        if(providedResTypes != null && !providedResTypes.isEmpty()){
            collection.add(getAsSimpleHash(providedResTypes, beansWrapper));
        }
        if(parentResourceTemplateModel != null){
            collection.add(parentResourceTemplateModel);
        }
        if(templates != null && !templates.isEmpty()){
            collection.add(getAsSimpleHash(AmwTemplateModelHelper.convertTemplatesToHash(templates), beansWrapper));
        }
        if(properties != null && !properties.isEmpty()){
            collection.add(getAsSimpleHash(properties, beansWrapper));
            for (FreeMarkerProperty property : properties.values()){
                collection.add(beansWrapper.wrap(property));
            }

        }
        if(appServerNodeViaResolver != null && !appServerNodeViaResolver.isEmpty()) {
            collection.add(appServerNodeViaResolver.values());
        }

        if(ppiAmwTemplateModel != null){
            collection.add(ppiAmwTemplateModel);
        }
        if(softlinkId != null){
            collection.add(beansWrapper.wrap(softlinkId));
        }
        if(softlinkRef != null){
            collection.add(beansWrapper.wrap(softlinkRef));
        }

        if(callerCpiTemplateModel != null){
            collection.add(callerCpiTemplateModel);
        }

        //consumed directAccess to resource
        Map<String, AmwResourceTemplateModel> consumedResourcesAsMap = getConsumedResourcesAsMap();
        collection.addAll(consumedResourcesAsMap.values());

        return new SimpleCollection(collection, beansWrapper);
	}
	
	/**
	 * puts a FreemarkerProperty to the properties Element
	 * @param key
	 * @param freeMarkerProperty
	 */
	public void put(String key, FreeMarkerProperty freeMarkerProperty) {
		if(properties == null){
			properties = new HashMap<String, FreeMarkerProperty>();
		}
		properties.put(key, freeMarkerProperty);
	}

	/**
	 * get a Property from the properties Element
	 * @param key
	 * @return
	 */
	public FreeMarkerProperty getProperty(String key) {
		if(properties != null && properties.containsKey(key)){
			return properties.get(key);
		}
		return null;
	}

    /**
     * populates the base AmwTemplateModel to make the ContextSwitch during the call of a AmwFunction
     *
     * @param amwTemplateModel
     */
	public void populateBaseProperties(AmwTemplateModel amwTemplateModel) {
		this.baseModelForContextSwitch = amwTemplateModel;
		if(consumedResTypes != null){
			for (String key : consumedResTypes.keySet()) {
				Map<String, AmwResourceTemplateModel> map = consumedResTypes.get(key);
				for (String key2 : map.keySet()) {
					AmwResourceTemplateModel amwResourceTemplateModel = map.get(key2);
					amwResourceTemplateModel.populateBaseProperties(baseModelForContextSwitch);
				}
			}
		}
		if(providedResTypes != null){
			for (String key : providedResTypes.keySet()) {
				Map<String, AmwResourceTemplateModel> map = providedResTypes.get(key);
				for (String key2 : map.keySet()) {
					AmwResourceTemplateModel amwResourceTemplateModel = map.get(key2);
					amwResourceTemplateModel.populateBaseProperties(baseModelForContextSwitch);
				}
			}
		}
		if(ppiAmwTemplateModel != null){
            // set caller (cpi) on ppi side for call back functionality
            if(ppiAmwTemplateModel.getUnitResourceTemplateModel() != null){
                AmwTemplateModel callerAmwTemplateModelContextSwitched = AmwTemplateModelHelper.getAmwTemplateModelContextSwitched(baseModelForContextSwitch, this);
                ppiAmwTemplateModel.getUnitResourceTemplateModel().setCallerCpiTemplateModel(callerAmwTemplateModelContextSwitched);
            }

            ppiAmwTemplateModel.setContextProperties(baseModelForContextSwitch.getContextProperties());
            // TODO check if nodeProperties are needed
			//ppiAmwTemplateModel.setNodeProperties(baseModelForContextSwitch.getNodeProperties());
			ppiAmwTemplateModel.setDeploymentProperties(baseModelForContextSwitch.getDeploymentProperties());
		}
	}

    /**
     * PreProcesses the model to evaluate Miks and inner Properties
     */
    public void preProcess(AmwTemplateModel baseModel){

        if(consumedResTypes != null){
            for (String key : consumedResTypes.keySet()) {
                Map<String, AmwResourceTemplateModel> map = consumedResTypes.get(key);
                for (String key2 : map.keySet()) {
                    AmwResourceTemplateModel amwResourceTemplateModel = map.get(key2);
                    amwResourceTemplateModel.preProcess(baseModel);
                }
            }
        }
        if(providedResTypes != null){
            for (String key : providedResTypes.keySet()) {
                Map<String, AmwResourceTemplateModel> map = providedResTypes.get(key);
                for (String key2 : map.keySet()) {
                    AmwResourceTemplateModel amwResourceTemplateModel = map.get(key2);
                    amwResourceTemplateModel.preProcess(baseModel);
                }
            }
        }
        if(ppiAmwTemplateModel != null){
            ppiAmwTemplateModel.setAmwModelPreprocessExceptionHandler(baseModel.getAmwModelPreprocessExceptionHandler());
			ppiAmwTemplateModel.preProcess();
        }

        preProcessFreeMarkerProperties(baseModel, properties, functions);

    }

    private void preProcessFreeMarkerProperties(AmwTemplateModel baseModel, Map<String, FreeMarkerProperty> properties, List<AmwFunctionEntity> functions) {
        if(properties != null) {
            for (String key : properties.keySet()) {
                FreeMarkerProperty property = properties.get(key);
                if (AmwTemplateModelHelper.isMikProperty(property)) {
                    String value = AmwTemplateModelHelper.evaluateMik(baseModel, property, functions, this);
                    property.setEvaluatedValue(value);
                }

                if (AmwTemplateModelHelper.valueContainsEvaluatableElements(property)) {
                    String value = AmwTemplateModelHelper.evaluateValue(baseModel, property, this);
                    property.setEvaluatedValue(value);
                }
            }
        }
    }
}
