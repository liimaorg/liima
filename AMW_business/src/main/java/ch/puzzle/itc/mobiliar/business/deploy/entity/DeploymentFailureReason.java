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

package ch.puzzle.itc.mobiliar.business.deploy.entity;

import lombok.Getter;

@Getter
public enum DeploymentFailureReason {

    PRE_DEPLOYMENT_GENERATION("pre deployment generation"),
    DEPLOYMENT_GENERATION("deployment generation"),
    PRE_DEPLOYMENT_SCRIPT("pre deployment script"),
    DEPLOYMENT_SCRIPT("deployment script"),
    NODE_MISSING("node missing"),
    TIMEOUT("timeout"),
    UNEXPECTED_ERROR("unexpected error"),
    RUNTIME_ERROR("runtime error");

    private String displayName;

    DeploymentFailureReason(String displayName) {
        this.displayName = displayName;
    }
}
