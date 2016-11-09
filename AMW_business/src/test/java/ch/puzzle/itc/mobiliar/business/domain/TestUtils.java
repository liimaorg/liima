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

package ch.puzzle.itc.mobiliar.business.domain;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.Charsets;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.AppServerRelationProperties;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationUnit;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwResourceTemplateModel;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.test.EntityBuilderType;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class TestUtils {
	public static String readRecursionTemplate() {
		try {
			return Resources.toString(Resources.getResource("rekursiv_macro_3.txt"), Charsets.UTF_8);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<ResourceEntity> filterResources(List<ResourceEntity> list, final String typeName) {
		return Lists.newArrayList(Collections2.filter(list, new Predicate<ResourceEntity>() {

			@Override
			public boolean apply(ResourceEntity input) {
				return input.getResourceType().getName().equals(typeName);
			}
		}));
	}

	public static String get(Map<String, Object> map, String... keys) {
		Object fromMap = getFromMap(map, keys);
		return fromMap == null ? null : fromMap.toString();
	}

    public static TemplateHashModel asHashModel(TemplateHashModel properties, String... keys) throws TemplateModelException {

        TemplateHashModel hashModel = properties;

        for (String key : keys) {
            TemplateModel templateModel = hashModel.get(key);
            if(templateModel instanceof TemplateHashModel){
                hashModel = (TemplateHashModel)templateModel;
            }
        }

        return hashModel;
    }

    public static TemplateHashModel asHashModel(TemplateHashModel properties, String key) throws TemplateModelException {
        return (TemplateHashModel)properties.get(key);
    }

	// get values from a nested map
	private static Object getFromMap(Map<String, Object> map, String... keys) {
		Object result = null;
        AmwResourceTemplateModel model = null;

		for (String key : keys) {
            if(model != null){
                try {
                    result = model.get(key);
                } catch (TemplateModelException e) {
                    e.printStackTrace();
                }
            }
			Object nested = map.get(key);
			if (nested instanceof Map) {
				map = (Map<String, Object>) nested;
				result = map;
			}
            else if(nested instanceof AmwResourceTemplateModel){
                model = ((AmwResourceTemplateModel)nested);
            }
			else {
				result = nested;
			}
			if (result == null) {
				return null;
			}
		}
		return result;
	}


	public static AppServerRelationProperties propertiesForRelation(List<AppServerRelationProperties> list,
			final EntityBuilderType entityType) {
		return Iterables.find(list, new Predicate<AppServerRelationProperties>() {

			@Override
			public boolean apply(AppServerRelationProperties input) {
				return input.getOwner().getResourceType().getName().equals(entityType.type);
			}
		});
	}

	public static GenerationUnit unitFor(Set<GenerationUnit> units, final ResourceEntity entity) {
		return Iterables.find(units, new Predicate<GenerationUnit>() {

			@Override
			public boolean apply(GenerationUnit input) {
				return input.getSlaveResource().equals(entity);
			}
		});
	}

	public static GenerationUnit unitFor(Set<GenerationUnit> units, final EntityBuilderType type) {
		return Iterables.find(units, new Predicate<GenerationUnit>() {

			@Override
			public boolean apply(GenerationUnit input) {
				return input.getSlaveResource().getName().equals(type.name);
			}
		});
	}

	public static List<GenerationUnit> unitsFor(Set<GenerationUnit> units, final EntityBuilderType type) {
		return Lists.newArrayList(Iterables.filter(units, new Predicate<GenerationUnit>() {

			@Override
			public boolean apply(GenerationUnit input) {
				return input.getSlaveResource().getName().equals(type.name);
			}
		}));
	}

	public static AppServerRelationProperties propertiesFor(Set<GenerationUnit> units, final EntityBuilderType type) {
		return Iterables.find(units, new Predicate<GenerationUnit>() {

			@Override
			public boolean apply(GenerationUnit input) {
				return input.getSlaveResource().getName().equals(type.name);
			}
		}).getAppServerRelationProperties();
	}

	public static ImmutableSet<Iterable<ResourceEntity>> resources(Set<GenerationUnit> units) {
		return ImmutableSet.of(Iterables.transform(units, new Function<GenerationUnit, ResourceEntity>() {

			@Override
			public ResourceEntity apply(GenerationUnit input) {
				return input.getSlaveResource();
			}
		}));
	}

	public static LinkedList<AppServerRelationProperties> properties(Set<GenerationUnit> units) {

		return Lists.newLinkedList(Iterables.transform(units, new Function<GenerationUnit, AppServerRelationProperties>() {

			@Override
			public AppServerRelationProperties apply(GenerationUnit input) {
				return input.getAppServerRelationProperties();
			}
		}));
	}
}
