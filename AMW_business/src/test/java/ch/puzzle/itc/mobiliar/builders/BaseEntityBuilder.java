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

package ch.puzzle.itc.mobiliar.builders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class BaseEntityBuilder {
	private int id = 0;
	private Set<Integer> usedIds = new HashSet<Integer>();

    protected BaseEntityBuilder(){

    }

    protected BaseEntityBuilder(Integer[] usedIds){
        this.usedIds.addAll(Arrays.asList(usedIds));
    }

	protected int getNextId() {
		int nextId = id++;
		usedIds.add(nextId);
		return nextId;
	}

	protected String getName(String name, Class<?> clas, int id) {
		if (!StringUtils.isEmpty(name)) {
			return name;
		}
		return clas.getSimpleName() + id;
	}

}
