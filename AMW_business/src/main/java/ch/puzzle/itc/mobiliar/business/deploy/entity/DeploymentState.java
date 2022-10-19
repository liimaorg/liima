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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum DeploymentState implements HasDisplayName{
    // the names of the enums are used in the database
    success("success"),
    failed("failed"),
    canceled("canceled"),
    rejected("rejected"),
    READY_FOR_DEPLOYMENT("ready_for_deploy"),
    PRE_DEPLOYMENT("pre_deploy"),
    progress("progress"),
    simulating("simulating"),
    delayed("delayed"),
    scheduled("scheduled"),
    requested("requested");

    static {
        PRE_DEPLOYMENT.setAllowedTransitions(success, failed, READY_FOR_DEPLOYMENT, progress);
        READY_FOR_DEPLOYMENT.setAllowedTransitions(canceled, rejected, progress);
        progress.setAllowedTransitions(failed, success);
        simulating.setAllowedTransitions(canceled, rejected, progress, delayed, scheduled, requested);
        delayed.setAllowedTransitions(canceled, rejected, progress, simulating, scheduled, PRE_DEPLOYMENT);
        scheduled.setAllowedTransitions(canceled, rejected, progress, simulating, delayed, PRE_DEPLOYMENT);
        requested.setAllowedTransitions(canceled, rejected, simulating, delayed, scheduled);
    }

    @Getter
    private String displayName;

    @Getter
    private List<DeploymentState> allowedTransitions = new ArrayList<>();

    private void setAllowedTransitions(DeploymentState... allowedTransitions) {
        this.allowedTransitions = Arrays.asList(allowedTransitions);
    }

    DeploymentState(String displayName) {
        this.displayName = displayName;
    }

    public boolean isTransitionAllowed(DeploymentState newState) {
        return allowedTransitions.contains(newState);
    }

    public static DeploymentState getByString(String statusStr) {
        for (DeploymentState state : DeploymentState.values()) {
            if (state.name().equalsIgnoreCase(statusStr)) {
                return state;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
