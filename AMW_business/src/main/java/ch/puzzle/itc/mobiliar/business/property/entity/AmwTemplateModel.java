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
import java.util.List;
import java.util.Map;

import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import freemarker.template.*;
import lombok.Getter;
import lombok.Setter;
import ch.puzzle.itc.mobiliar.business.generator.control.AmwModelPreprocessExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.container.DeploymentProperties;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;

public class AmwTemplateModel implements TemplateHashModelEx {
	
	protected static final String RESERVED_PROPERTY_NODE = "node";
	public static final String RESERVED_PROPERTY_APP_SERVER = "appServer";
    private static final String RESERVED_PROPERTY_APPLICATIONS = "applications";
	
	private static final String RESERVED_PROPERTY_ENV = "env";
	private static final String RESERVED_PROPERTY_DEPLOYMENT = "deployment";
    private static final String RESERVED_PROPERTY_DEPLOYMENT_ID = "deploymentId";
    private static final String RESERVED_PROPERTY_DEPLOYMENT_ISDEPLOY = "deploy";
	private static final String RESERVED_PROPERTY_RUNTIME = "runtime";
	
	private static final String RESERVED_PROPERTY_PROVIDER = "provider";
	private static final String RESERVED_PROPERTY_CONSUMER = "consumer";
    private static final String RESERVED_PROPERTY_TEMPLATEFILES = "templatefiles";
	
	
	@Setter
	@Getter
	private AmwModelPreprocessExceptionHandler amwModelPreprocessExceptionHandler;
    @Setter
    private Map<String, AmwResourceTemplateModel> applications;
	@Setter
	@Getter
	private AmwResourceTemplateModel unitResourceTemplateModel;
	@Setter
	@Getter
	private AmwResourceTemplateModel asProperties;
	@Setter
	@Getter
	private AmwResourceTemplateModel nodeProperties;
	@Setter
	@Getter
	private Map<String, FreeMarkerProperty> contextProperties;
	@Setter
	@Getter
	private AmwResourceTemplateModel runtimeProperties;
	@Setter
	@Getter
	private DeploymentProperties deploymentProperties;
	@Setter
	private AmwConsumerTemplateModel consumerModel;
	@Setter
	private AmwProviderTemplateModel providerModel;
    @Setter
    private Map<String, GeneratedTemplate> templateFiles;
	
	@Getter
	@Setter
	private List<GlobalFunctionEntity> globalFunctionTemplates;
	

	@Override
	public TemplateModel get(String key) throws TemplateModelException {
		
		DefaultObjectWrapper beansWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_21);
		if(RESERVED_PROPERTY_APP_SERVER.equals(key)){
			return asProperties;
		}else if(RESERVED_PROPERTY_NODE.equals(key)){
			return nodeProperties;
		}else if(RESERVED_PROPERTY_ENV.equals(key)){
			return new SimpleHash(contextProperties, beansWrapper);
		}else if(RESERVED_PROPERTY_RUNTIME.equals(key)){
			return runtimeProperties;
		}else if(RESERVED_PROPERTY_DEPLOYMENT_ID.equals(key)){
            // fast access to deploymentId
            if(deploymentProperties != null && deploymentProperties.getDeployment() != null){
                return beansWrapper.wrap(deploymentProperties.getDeployment().getId());
            }
        }else if(RESERVED_PROPERTY_DEPLOYMENT_ISDEPLOY.equals(key)){
            // fast access to deploy
            if(deploymentProperties != null ){
                return beansWrapper.wrap(Boolean.valueOf(deploymentProperties.isDeployGenerationModus()).toString());
            }
        }else if(RESERVED_PROPERTY_DEPLOYMENT.equals(key)){
			return new SimpleHash(deploymentProperties.asMap(), beansWrapper);
		}else if(RESERVED_PROPERTY_CONSUMER.equals(key)){
			return consumerModel;
		}else if(RESERVED_PROPERTY_PROVIDER.equals(key)){
			return providerModel;
		}else if(RESERVED_PROPERTY_TEMPLATEFILES.equals(key)){
            return new SimpleHash(AmwTemplateModelHelper.convertTemplatesToHash(templateFiles), beansWrapper);
        }else if(RESERVED_PROPERTY_APPLICATIONS.equals(key)){
            return new SimpleHash(applications, beansWrapper);
        }
		
		if(unitResourceTemplateModel != null){
			return unitResourceTemplateModel.get(key);
		}
		
		return null;
	}

	@Override
	public boolean isEmpty() throws TemplateModelException {
		if(asProperties!= null && !asProperties.isEmpty()){
			return false;
		}
		if(nodeProperties!= null && !nodeProperties.isEmpty()){
			return false;
		}
		if(contextProperties!= null && !contextProperties.isEmpty()){
			return false;
		}
		if(runtimeProperties!= null && !runtimeProperties.isEmpty()){
			return false;
		}
		if(deploymentProperties!= null && !deploymentProperties.asMap().isEmpty()){
			return false;
		}
		if(consumerModel!= null && !consumerModel.isEmpty()){
			return false;
		}
		if(providerModel!= null && !providerModel.isEmpty()){
			return false;
		}
        if(templateFiles!= null && !templateFiles.isEmpty()){
            return false;
        }
        if(applications!=null && !applications.isEmpty()){
            return false;
        }
		if(unitResourceTemplateModel!= null && !unitResourceTemplateModel.isEmpty()){
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

    private Collection<? extends String> keySet() throws TemplateModelException {
        Collection<String> collection = new ArrayList<>();

        if(asProperties!= null && !asProperties.isEmpty()){
            collection.add(RESERVED_PROPERTY_APP_SERVER);
        }
        if(nodeProperties!= null && !nodeProperties.isEmpty()){
            collection.add(RESERVED_PROPERTY_NODE);
        }
        if(contextProperties!= null && !contextProperties.isEmpty()){
            collection.add(RESERVED_PROPERTY_ENV);
        }
        if(runtimeProperties!= null && !runtimeProperties.isEmpty()){
            collection.add(RESERVED_PROPERTY_RUNTIME);
        }
        if(deploymentProperties!= null && !deploymentProperties.asMap().isEmpty()){
            collection.add(RESERVED_PROPERTY_DEPLOYMENT);
        }
        if(consumerModel!= null && !consumerModel.isEmpty()){
            collection.add(RESERVED_PROPERTY_CONSUMER);
        }
        if(providerModel!= null && !providerModel.isEmpty()){
            collection.add(RESERVED_PROPERTY_PROVIDER);
        }
        if(templateFiles!= null && !templateFiles.isEmpty()){
            collection.add(RESERVED_PROPERTY_TEMPLATEFILES);
        }
        if(applications!= null && !applications.isEmpty()){
            collection.addAll(applications.keySet());
        }
        if(unitResourceTemplateModel!= null && !unitResourceTemplateModel.isEmpty()){
            collection.addAll(unitResourceTemplateModel.keySet());
        }
        return collection;
    }

    @Override
    public TemplateCollectionModel values() throws TemplateModelException {
        Collection<TemplateModel> collection = new ArrayList<>();
        DefaultObjectWrapper beansWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_21);
        if(asProperties!= null && !asProperties.isEmpty()){
            collection.add(asProperties);
        }
        if(nodeProperties!= null && !nodeProperties.isEmpty()){
            collection.add(nodeProperties);
        }
        if(contextProperties!= null && !contextProperties.isEmpty()){
            collection.add(new SimpleHash(contextProperties, beansWrapper));
        }
        if(runtimeProperties!= null && !runtimeProperties.isEmpty()){
            collection.add(runtimeProperties);
        }
        if(deploymentProperties!= null && !deploymentProperties.asMap().isEmpty()){
            collection.add(new SimpleHash(deploymentProperties.asMap(), beansWrapper));
        }
        if(consumerModel!= null && !consumerModel.isEmpty()){
            collection.add(consumerModel);
        }
        if(providerModel!= null && !providerModel.isEmpty()){
            collection.add(providerModel);
        }
        if(templateFiles!= null && !templateFiles.isEmpty()){
            collection.add(new SimpleHash(AmwTemplateModelHelper.convertTemplatesToHash(templateFiles), beansWrapper));
        }
        if(applications!= null && !applications.isEmpty()){
            collection.add(new SimpleHash(applications, beansWrapper));
        }
        if(unitResourceTemplateModel!= null && !unitResourceTemplateModel.isEmpty()){
            collection.add(unitResourceTemplateModel);
        }
        return new SimpleCollection(collection, beansWrapper);
    }

	public void populateBaseProperties(){
		if(unitResourceTemplateModel != null){
			unitResourceTemplateModel.populateBaseProperties(this);
		}
		if(asProperties != null){
			asProperties.populateBaseProperties(this);
		}
		if(nodeProperties != null){
			nodeProperties.populateBaseProperties(this);
		}
		if(runtimeProperties != null){
			runtimeProperties.populateBaseProperties(this);
		}
        if(consumerModel != null){
            consumerModel.populateBaseProperties(this);
        }
        if(providerModel != null){
            providerModel.populateBaseProperties(this);
        }
        if(applications != null){
            for (String key : applications.keySet()) {
                applications.get(key).populateBaseProperties(this);
            }
        }
	}

    public void preProcess(){
        // call the preProcess twice to be sure, that all dynamic properties are replaced correctly
        preProcessInternal();
        preProcessInternal();
    }

    private void preProcessInternal(){
        if(unitResourceTemplateModel != null){
            unitResourceTemplateModel.preProcess(this);
        }
        if(asProperties != null){
            asProperties.preProcess(this);
        }
        if(nodeProperties != null){
            nodeProperties.preProcess(this);
        }
        if(runtimeProperties != null){
            runtimeProperties.preProcess(this);
        }
        if(consumerModel != null){
            consumerModel.preProcess(this);
        }
        if(providerModel != null){
            providerModel.preProcess(this);
        }

        if(applications != null){
            for (String key : applications.keySet()) {
                applications.get(key).preProcess(this);
            }
        }

        // no Functions on BaseModel so these Properties can not contain Functions but these Values can contain Properties in Properties
        this.preProcessFreeMarkerProperties(this.contextProperties);
    }

    private void preProcessFreeMarkerProperties(Map<String, FreeMarkerProperty> properties) {
        if(properties != null) {
            for (String key : properties.keySet()) {
                FreeMarkerProperty property = properties.get(key);

                if (AmwTemplateModelHelper.valueContainsEvaluatableElements(property)) {
                    AmwTemplateModelHelper.evaluateValue(this, property, this.unitResourceTemplateModel);
                }
            }
        }
    }
}
