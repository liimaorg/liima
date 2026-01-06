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

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;

public class ContextEntityBuilder extends BaseEntityBuilder {

	private Integer id;

	public ContextEntity mockContextEntity(String name, ContextEntity parent, Set<ContextEntity> children) {
		ContextEntity mock = mock(ContextEntity.class);
		Integer id = getNextId();
		lenient().when(mock.getId()).thenReturn(id);
		lenient().when(mock.getName()).thenReturn(name);
		lenient().when(mock.getParent()).thenReturn(parent);
		if (parent != null && Mockito.mockingDetails(parent).isMock()) {
			Set<ContextEntity> childs = parent.getChildren();
			childs.add(mock);
			lenient().when(parent.getChildren()).thenReturn(childs);
		}

		lenient().when(mock.getChildren()).thenReturn(children);
		if (children != null) {
			for (ContextEntity child : children) {
				if (Mockito.mockingDetails(child).isMock()) {
					lenient().when(child.getParent()).thenReturn(mock);
				}
			}
		}

		return mock;
	}

	public ContextEntity buildContextEntity(String name, ContextEntity parent, Set<ContextEntity> children, boolean withId) {
		ContextEntity entity = new ContextEntity();
		entity.setId(this.id);

		if (withId) {
			Integer id = getNextId();
			entity.setId(id);
		}
		entity.setName(name);
		entity.setParent(parent);
		if (parent != null) {
			if (parent.getChildren().isEmpty()) {
				Set<ContextEntity> pc = new HashSet<>();
				pc.add(entity);
				parent.setChildren(pc);
			} else {
				parent.getChildren().add(entity);
			}
		}

		entity.setChildren(children);
		if (children != null) {
			for (ContextEntity child : children) {
				child.setParent(entity);
			}
		}

		return entity;
	}

	public ContextEntityBuilder id(Integer id) {
		this.id = id;
		return this;
	}

}
