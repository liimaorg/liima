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

package ch.puzzle.itc.mobiliar.business.database.control;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

public class CriteriaRestriction<RootType, PathType> {
	
	private final Path<RootType> root;
	private final Predicate predicate;
	private final Path<PathType> path;
	
	public CriteriaRestriction(Path<RootType> root, Predicate predicate, Path<PathType> path) {
		super();
		this.root = root;
		this.predicate = predicate;
		this.path = path;
	}

	public Path<RootType> getRoot() {
		return root;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public Path<PathType> getPath() {
		return path;
	}
	
}
