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

package ch.puzzle.itc.mobiliar.business.foreignable.boundary;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.Foreignable;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableAttributesDTO;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;

@Stateless
public class ForeignableBoundary implements Serializable {

    @Inject
    ForeignableService foreignableService;


    /**
     * Checks if an owner can modify (edit/delete) a foreignable element
     * @param modifyingOwner owner who desired to edit or delete an existing foreignable
     * @param foreignable foreignable element to modify or delete
     * @return true if the owner is either the owner of the foreignable or if the owner has chuck norris role to edit everything
     */
    public boolean isModifiableByOwner(ForeignableOwner modifyingOwner, Foreignable<?> foreignable) {
        return foreignableService.isForeignableModifiableByOwner(modifyingOwner, foreignable);
    }

    /**
     * Checks if an editing owner can modify (edit/delete) object with foreignable attributes
     * @param modifyingOwner owner who desired to edit or delete an existing foreignable
     * @param foreignableAttributes foreignable element to modify or delete
     * @return true if the owner is either the owner of the foreignable attribute or if the owner has chuck norris role to edit everything
     */
    public boolean isModifiableByOwner(ForeignableOwner modifyingOwner, ForeignableAttributesDTO foreignableAttributes) {
        return foreignableService.isForeignableModifiableByOwner(modifyingOwner, foreignableAttributes.getOwner());
    }

    public boolean hasSameOwner(ForeignableOwner owner, Foreignable<?> foreignable){
        return owner.isSameOwner(foreignable.getOwner());
    }
}
