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

package ch.puzzle.itc.mobiliar.business.environment.entity;

import org.hibernate.envers.Audited;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import static javax.persistence.CascadeType.PERSIST;

@MappedSuperclass
@Audited
public abstract class ContextDependency<T> extends AbstractContext {
	
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(cascade = PERSIST)
	private ContextEntity context;
		
	public ContextEntity getContext() {
		return context;
	}
	public void setContext(ContextEntity context) {
		this.context = context;
	}
	
	public abstract T getContextualizedObject();
	
	public abstract void setContextualizedObject(T contextualizedObject);

	@Override
	public String toString() {
		return "ContextDependency [context=" + context + ", resource=" + getContextualizedObject() + "]";
	}

    @Override
    public Integer getId() {
        if (this.context != null) {
            return this.context.getId();
        }
        return super.getId();
    }

}
