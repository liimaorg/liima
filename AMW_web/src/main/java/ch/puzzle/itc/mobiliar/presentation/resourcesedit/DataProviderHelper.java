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

package ch.puzzle.itc.mobiliar.presentation.resourcesedit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;

/*
 * Collect helper methods for which we dont know yet where they should go.
 */
public class DataProviderHelper {

	/**
	 * find the next free identifier
	 * 
	 * @param identifiers
	 *             identifiers
	 * @param prefix
	 *             the String before "_"
	 * @param count
	 *             the count after "_", can be null
	 * @return
	 */
	public String nextFreeIdentifier(List<String> identifiers, String prefix, Integer count) {
		if (identifiers.size() <= 0) {
			return prefix;
		}
		else if (count == null) {
			return nextFreeIdentifier(identifiers, prefix, identifiers.size());
		}
		else {
			String sPrefix = !StringUtils.isBlank(prefix) ? prefix : "";
			String nextIdentifier = sPrefix + "_" + String.valueOf(count);

			if (identifiers.contains(nextIdentifier)) {
				return nextFreeIdentifier(identifiers, prefix, count + 1);
			}
			return nextIdentifier;
		}
	}

	/**
	 * find next free identifier for slave resource
	 *
	 * @param relations            - list of relations
	 * @param slaveResourceGroupId - related slave resource id
	 * @return next free identifier
	 */
	public String nextFreeIdentifierForResourceEditRelations(List<ResourceEditRelation> relations, Integer slaveResourceGroupId, String slavePrefix) {
		int count = 0;
		List<String> identifiers = new ArrayList<>();
		String prefix = null;
		for (ResourceEditRelation relation : relations) {
			if (relation.getSlaveGroupId().equals(slaveResourceGroupId)) {
				if (prefix == null) {
					prefix = relation.getSlaveName();
				}
				identifiers.add(relation.getIdentifier());
				++count;
			}
		}
		prefix = prefix == null ? slavePrefix : prefix;
		return nextFreeIdentifier(identifiers, prefix, count);
	}

	public List<ResourceEditRelation> relationsWithSameSlaveGroup(List<ResourceEditRelation> relations, Integer slaveResourceGroupId) {
		List<ResourceEditRelation> same = new ArrayList<>();
		for (ResourceEditRelation relation : relations) {
			if (relation.getSlaveGroupId().equals(slaveResourceGroupId)) {
				same.add(relation);
			}
		}
		return same;
	}

	/**
	* Converts a Map to a List filled with its entries. This is needed since very few if any JSF iteration
	* components are able to iterate over a map.
	*
	* @see http://stackoverflow.com/questions/8552804/uirepeat-doesnt-work-with-map
	*/
    public static <T, S> List<Map.Entry<T, S>> mapToList(Map<T, S> map) {

	   if (map == null) {
		  return null;
	   }

	   List<Map.Entry<T, S>> list = new ArrayList<>();
	   Set<Map.Entry<T, S>> entrySet = map.entrySet();

	   list.addAll(entrySet);

	   return list;
    }

	public <T> List<T> flattenMap(Map<?, List<T>> map) {
		List<T> list = new ArrayList<>();
		if (map != null) {
			for (Object key : map.keySet()) {
				list.addAll(map.get(key));
			}
		}
		return list;
	}
}
