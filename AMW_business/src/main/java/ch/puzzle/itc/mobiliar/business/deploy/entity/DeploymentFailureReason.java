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

    pre_deployment_generation("pre_deployment_generation"),
    deployment_generation("deployment_generation"),
    pre_deployment_script("pre_deployment_script"),
    deployment_script("deployment_script"),
    node_missing("node_missing"),
    timeout("timeout"),
    error_unexpected("unexpected_error"),
    error_runtime("runtime_error");

    private String displayName;

    DeploymentFailureReason(String displayName) {
        this.displayName = displayName;
    }
}
