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


import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.AmwModelPreprocessExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.BaseTemplateProcessor;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import freemarker.core.ParseException;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This Helper contains all Helper Methods for the Model
 */
public class AmwTemplateModelHelper {

    public static String evaluateMik(AmwTemplateModel baseModel, FreeMarkerProperty property, List<AmwFunctionEntity> functions, AmwResourceTemplateModel amwResourceTemplateModel) {
        String mik = property.get_descriptor().getMachineInterpretationKey();

        AmwFunctionEntity functionMatchingMik = null;
        if(functions != null){
	        for (AmwFunctionEntity function : functions) {
	            if(function != null && function.getMikNames() != null && function.getMikNames().contains(mik)){
	                functionMatchingMik = function;
	            }
	        }
        }
        if(functionMatchingMik !=null){
            // evaluate Function
            BaseTemplateProcessor processor = new BaseTemplateProcessor();

            // context Wechsel
            AmwTemplateModel model = getAmwTemplateModelContextSwitched(baseModel, amwResourceTemplateModel);
            try {
                return processor.evaluateAmwFunction(functionMatchingMik, model, model.getAmwModelPreprocessExceptionHandler());
            } catch (ParseException | TemplateException pe) {
                addMikErrorToHandler(model, functionMatchingMik, pe);
            } catch (IOException e) {
                throw new AMWRuntimeException("Error evaluating function: "
                        + functionMatchingMik.getName());
            }

        }else{
            if(baseModel.getAmwModelPreprocessExceptionHandler() != null) {
                baseModel.getAmwModelPreprocessExceptionHandler().addTemplatePropertyException(new TemplatePropertyException("No Function found for Mik: " + mik, TemplatePropertyException.CAUSE.INVALID_PROPERTY));
            }
            throw new AMWRuntimeException("No Function found for Mik: " + mik);
        }
        return null;
    }

    public static String evaluateValue(AmwTemplateModel baseModel, FreeMarkerProperty property, AmwResourceTemplateModel amwResourceTemplateModel) {
        if(property != null){
            BaseTemplateProcessor processor = new BaseTemplateProcessor();

            // context Wechsel
            AmwTemplateModel model = getAmwTemplateModelContextSwitched(baseModel, amwResourceTemplateModel);
            try {
                return processor.evaluateStringPropertyAsTemplate(property.getCurrentValue(), model);
            } catch (ParseException | TemplateException pe) {
                addPropertyErrorToHandler(model, property, pe);
            } catch (IOException e) {
                throw new AMWRuntimeException("Error evaluating property", e);
            }
        }
        return null;
    }

    private static void addMikErrorToHandler(AmwTemplateModel model, AmwFunctionEntity function, Exception e) {
        String message = "Error evaluating function: ";
        if(function != null ){
            message = message + ": " + function.getName();
        }

        addToHandler(model, e, message);
    }

    private static void addPropertyErrorToHandler(AmwTemplateModel model, FreeMarkerProperty property, Exception e) {
        String message = "Invalid Innerproperty";
        if(property != null && property.get_descriptor() != null){
            message = message + ": " + property.get_descriptor().getTechnicalKey();
        }

        addToHandler(model, e, message);
    }

    private static void addToHandler(AmwTemplateModel model, Exception e, String message) {
        TemplatePropertyException te = new TemplatePropertyException(message + "; " + e.getMessage(), TemplatePropertyException.CAUSE.INVALID_PROPERTY, e);
        if (model != null && model.getAmwModelPreprocessExceptionHandler() != null) {
            model.getAmwModelPreprocessExceptionHandler().addTemplatePropertyException(te);
        }else{
            throw new AMWRuntimeException("no Errorhandler defined on Model", te);
        }
    }

    public static boolean valueContainsEvaluatableElements(FreeMarkerProperty property) {
        // a property with a Value overwrites the Mik
        if(property == null || !property.hasValue()){
            return false;
        }

        return valueContainsEvaluatableElements(property.getCurrentValue());
    }

    public static boolean valueContainsEvaluatableElements(String value){
        // if the value contains { the it must be treated like a template
        if(value != null && (value.contains("{"))){
            return true;
        }

        return false;
    }

    public static boolean isMikProperty(FreeMarkerProperty property) {
        // a property with a Value overwrites the Mik
        if(property == null || property.hasValue()){
            return false;
        }

        if(property.get_descriptor() != null){
            FreeMarkerPropertyDescriptor desc = property.get_descriptor();
            if(desc.getMachineInterpretationKey() != null && !desc.getMachineInterpretationKey().isEmpty()){
                return true;
            }
        }
        return false;
    }


    public static AmwTemplateModel getAmwTemplateModelContextSwitched(AmwTemplateModel baseModel, AmwResourceTemplateModel amwResourceTemplateModel){
        // context Wechsel
        AmwTemplateModel model = new AmwTemplateModel();
        model.setUnitResourceTemplateModel(amwResourceTemplateModel);
        if(baseModel != null){
        	model.setGlobalFunctionTemplates(baseModel.getGlobalFunctionTemplates());
            model.setAsProperties(baseModel.getAsProperties());
            model.setNodeProperties(baseModel.getNodeProperties());
            model.setRuntimeProperties(baseModel.getRuntimeProperties());
            model.setContextProperties(baseModel.getContextProperties());
            model.setDeploymentProperties(baseModel.getDeploymentProperties());
            model.setAmwModelPreprocessExceptionHandler(baseModel.getAmwModelPreprocessExceptionHandler());
        }else{
        	model.setAmwModelPreprocessExceptionHandler(new AmwModelPreprocessExceptionHandler());
        }
        return model;
    }

    public static Map<String, Map<String, String>> convertTemplatesToHash(Map<String, GeneratedTemplate> templates){
        if(templates == null){
            return null;
        }
        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        for (String key : templates.keySet()) {
            result.put(key, templates.get(key).toHash());
        }
        return result;
    }

    public static TemplateModel wrapFreemarkerProperty(FreeMarkerProperty property, DefaultObjectWrapper beansWrapper) throws TemplateModelException {
        if(property!= null && property.hasValue()) {
            return beansWrapper.wrap(property);
        }else{
            return null;
        }
    }
}
