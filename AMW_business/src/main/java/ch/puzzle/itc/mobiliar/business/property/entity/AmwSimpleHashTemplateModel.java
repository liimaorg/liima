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

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.Map;

/**
 */
public class AmwSimpleHashTemplateModel extends SimpleHash {

    public AmwSimpleHashTemplateModel(Map subModel, DefaultObjectWrapper beansWrapper) {
        super(subModel,beansWrapper);
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        TemplateModel result = super.get(key);

        if(result != null && result instanceof FreeMarkerProperty && !((FreeMarkerProperty) result).hasValue()){
            return null;
        }

        return result;
    }
}
