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

import freemarker.template.*;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 */
public class AmwAppServerNodeModel implements TemplateHashModelEx {

    private static final String RESERVED_PROPERTY_APP = "app";
    private static final String RESERVED_PROPERTY_NODES = "nodes";

    @Setter
    private Map<String, FreeMarkerProperty> appProperties;
    @Setter
    private Map<String, FreeMarkerProperty> appServerProperties;
    @Setter
    private List<Map<String, FreeMarkerProperty>> nodePropertyList;


    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        DefaultObjectWrapper beansWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_21);
        if(RESERVED_PROPERTY_APP.equals(key)){
            return new AmwSimpleHashTemplateModel(appProperties, beansWrapper);
        }
        if(AmwTemplateModel.RESERVED_PROPERTY_APP_SERVER.equals(key)){
            return new AmwSimpleHashTemplateModel(appServerProperties, beansWrapper);
        }
        if(RESERVED_PROPERTY_NODES.equals(key)){
            return beansWrapper.wrap(nodePropertyList);
        }
        return null;
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

    @Override
    public TemplateCollectionModel values() throws TemplateModelException {
        Collection<TemplateModel> collection = new ArrayList<>();
        DefaultObjectWrapper beansWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_21);

        if(appProperties!= null && !appProperties.isEmpty()){
            collection.add(new SimpleHash(appProperties, beansWrapper));
        }
        if(appServerProperties!= null && !appServerProperties.isEmpty()){
            collection.add(new SimpleHash(appServerProperties, beansWrapper));
        }
        if(nodePropertyList!= null && !nodePropertyList.isEmpty()){
            collection.add(beansWrapper.wrap(nodePropertyList));
        }
        return new SimpleCollection(collection, beansWrapper);
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        if(appProperties != null && !appProperties.isEmpty()){
            return false;
        }
        if(appServerProperties != null && !appServerProperties.isEmpty()){
            return false;
        }
        if(nodePropertyList != null && !nodePropertyList.isEmpty()){
            return false;
        }
        return true;
    }

    public boolean containsKey(String key){
        return keySet().contains(key);
    }


    public Collection<? extends String> keySet() {
        Collection<String> collection = new ArrayList<>();
        if(appProperties != null && !appProperties.isEmpty()){
            collection.add(RESERVED_PROPERTY_APP);
        }
        if(appServerProperties != null && !appServerProperties.isEmpty()){
            collection.add(AmwTemplateModel.RESERVED_PROPERTY_APP_SERVER);
        }
        if(nodePropertyList != null && !nodePropertyList.isEmpty()){
            collection.add(RESERVED_PROPERTY_NODES);
        }
        return collection;
    }
}
