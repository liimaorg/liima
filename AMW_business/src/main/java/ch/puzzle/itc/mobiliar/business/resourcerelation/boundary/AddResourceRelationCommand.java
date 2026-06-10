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

package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import lombok.Getter;

import javax.validation.constraints.NotNull;

import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

/**
 * Command to add a resource relation (consumed or provided).
 */
@Getter
public class AddResourceRelationCommand {

    @NotNull(message = "Master resource ID is required")
    private final Integer masterResourceId;

    @NotNull(message = "Slave resource group ID is required")
    private final Integer slaveResourceGroupId;

    @NotNull(message = "Provided flag is required")
    private final Boolean provided;

    private final String relationName;

    /**
     * @param masterResourceId      the ID of the master resource
     * @param slaveResourceGroupId  the ID of the slave resource group
     * @param provided              true for provided relation, false for consumed
     * @param relationName          optional relation name/identifier
     */
    public AddResourceRelationCommand(Integer masterResourceId, Integer slaveResourceGroupId, 
                                       Boolean provided, String relationName) {
        this.masterResourceId = masterResourceId;
        this.slaveResourceGroupId = slaveResourceGroupId;
        this.provided = provided;
        this.relationName = relationName;
        validate(this);
    }
}
