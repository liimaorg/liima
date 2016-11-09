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

import lombok.Setter;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class AmwConsumerTemplateModel implements TemplateHashModel{

	@Setter
	private AmwResourceTemplateModel asProperties;
	@Setter
	private AmwResourceTemplateModel nodeProperties;
	@Setter
	private AmwResourceTemplateModel consumerUnit;
	

	@Override
	public TemplateModel get(String key) throws TemplateModelException {
		
		if(AmwTemplateModel.RESERVED_PROPERTY_APP_SERVER.equals(key)){
			return asProperties;
		}else if(AmwTemplateModel.RESERVED_PROPERTY_NODE.equals(key)){
			return nodeProperties;
		}
		
		if(consumerUnit != null){
			return consumerUnit.get(key);
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
		if(consumerUnit!= null && !consumerUnit.isEmpty()){
			return false;
		}
		return true;
	}

    public void preProcess(AmwTemplateModel amwTemplateModel) {
        if(asProperties != null){
            asProperties.preProcess(amwTemplateModel);
        }
        if(nodeProperties != null){
            nodeProperties.preProcess(amwTemplateModel);
        }
        if(consumerUnit != null){
            consumerUnit.preProcess(amwTemplateModel);
        }
    }

    public void populateBaseProperties(AmwTemplateModel amwTemplateModel) {
        if(asProperties != null){
            asProperties.populateBaseProperties(amwTemplateModel);
        }
        if(nodeProperties != null){
            nodeProperties.populateBaseProperties(amwTemplateModel);
        }
        if(consumerUnit != null){
            consumerUnit.populateBaseProperties(amwTemplateModel);
        }
    }
}
