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

package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "deploymentDetail")
@Data
@NoArgsConstructor
public class DeploymentDetailDTO {

    private Integer deploymentId;
    private String stateMessage;
    private boolean buildSuccess;
    private boolean executed;
    private boolean deploymentConfirmed;
    private Date stateToDeploy;

    // confirm flags
    private boolean sendEmailWhenDeployed;
    private boolean simulateBeforeDeployment;
    private boolean shakedownTestsWhenDeployed;
    private boolean neighbourhoodTest;


    public DeploymentDetailDTO(DeploymentEntity entity) {
        this.setDeploymentId(entity.getId());
        this.setStateMessage(entity.getStateMessage());
        this.setBuildSuccess(entity.isBuildSuccess());
        this.setExecuted(entity.isExecuted());
        this.setDeploymentConfirmed(entity.getDeploymentConfirmed() != null ? entity.getDeploymentConfirmed() : false);
        this.setStateToDeploy(entity.getStateToDeploy());

        this.setSendEmailWhenDeployed(entity.isSendEmail());
        this.setSimulateBeforeDeployment(entity.isSimulating());
        this.setShakedownTestsWhenDeployed(entity.isCreateTestAfterDeployment());
        this.setNeighbourhoodTest(entity.isCreateTestForNeighborhoodAfterDeployment());
    }
}
