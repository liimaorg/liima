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

package ch.puzzle.itc.mobiliar.business.foreignable.control;

import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.Foreignable;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;

public class ForeignableService {


	@Inject
	PermissionService permissionService;


    /**
     * A given owner can create/edit a foreignable if he is either the owner of the foreignable or the foreignable is a new object or the user has super user (chuck norris) role which can overrule foreignable rules or if only decorable elements on the foreignable object has changed.
     * If one of this criteria fails an ecxeption will be thrown
     * @param editingOwner editing owner
     * @param beforeChangeForeignableHashCode hashcode of foreignable before any changes affected: can be 0 when foreignable hasn't been existing
     * @param afterChangeForeignable foreignable after changed are affected.
     */
    public void verifyEditableByOwner(ForeignableOwner editingOwner, int beforeChangeForeignableHashCode,  Foreignable<?> afterChangeForeignable) throws ForeignableOwnerViolationException {
        if (!isNewObject(afterChangeForeignable)
                && !isForeignableModifiableByOwner(editingOwner, afterChangeForeignable)
                && !hasOnlyDecorationsChanged(beforeChangeForeignableHashCode, afterChangeForeignable)) {
            throw new ForeignableOwnerViolationException(afterChangeForeignable, editingOwner, "Edit foreignable object not allowed by this owner");
        }
    }

    /**
     * A given owner can delete an existing foreignable if he is either the owner of the foreignable or if the user has super user (chuck norris) role which can overrule foreignable rules
     */
	public void verifyDeletableByOwner(ForeignableOwner deletingOwner, Foreignable<?> foreignable) throws ForeignableOwnerViolationException {
		if (!isForeignableModifiableByOwner(deletingOwner, foreignable)) {
			throw new ForeignableOwnerViolationException(foreignable, deletingOwner, "Delete foreignable object not allowed by this owner");
		}
	}

    /**
     * Checks if only decorable fields on the foreignable element has changed
     */
    private boolean hasOnlyDecorationsChanged(int beforeChangeForeignableHashCode,  Foreignable afterChangeForeignable) {
        return beforeChangeForeignableHashCode == afterChangeForeignable.foreignableFieldHashCode();
    }

	/**
	 * If user has super user (chuck norris) role then he can overrule foreignable rules
	 */
	private boolean isChuckNorris() {
		return permissionService.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER);
	}

    /**
     * Checks if foreignable element is a new created object (not yet persistet - no id)
     */
	private boolean isNewObject(Foreignable<?> foreignable) {
		return ((Identifiable) foreignable).getId() == null;
	}

    /**
     * Verifies if a given owner is the owner of the foreignable element or if he has super user (chuck norris) role which can overrule foreignable rules
     */
	public boolean isForeignableModifiableByOwner(ForeignableOwner editingOwner, Foreignable<?> foreignable) {
		return isForeignableModifiableByOwner(editingOwner, foreignable.getOwner());
	}

    /**
     * Verifies if a given editing owner is equals the foreignable owner or if the the user has super user (chuck norris) role which can overrule foreignable rules
     */
    public boolean isForeignableModifiableByOwner(ForeignableOwner editingOwner, ForeignableOwner foreignableOwner) {
        return isChuckNorris() || foreignableOwner.isSameOwner(editingOwner);
    }


}
