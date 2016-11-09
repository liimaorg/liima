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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.container;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.NodeJobEntity;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.DeploymentParameter;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DeploymentProperties {

    protected static final String DEPLOYMENT_PROPERTIES_KEY_ID = "id";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_NAME = "name";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_TRACKINGID = "trackingId";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_CONF_USER = "confirmationUser";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_CONF_DATE = "confirmationDate";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_REQ_USER = "requestUser";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_CREA_DATE = "jobCreationDate";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_GENERATIONSTATE_DATE = "generationStateDate";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_GEN_MODUS = "generationModus";
    protected static final String DEPLOYMENT_PROPERTIES_TARGET_LOGPREFIX = "targetLogPrefix";
    protected static final String DEPLOYMENT_PROPERTIES_AMWLOGFILE = "amwLogFile";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_GENERATIONDIR = "generationdir";

    protected static final String DEPLOYMENT_PROPERTIES_KEY_RELEASE = "release";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_INSTALLATION_DATE = "installationDate";

    protected static final String DEPLOYMENT_PROPERTIES_KEY_RUNTIME = "runtime";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_PARAMETER = "params";
    protected static final String DEPLOYMENT_PROPERTIES_KEY_NODEJOB = "nodejob";

    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.S";

    @Getter
    @Setter
    private String generationDir;
    @Getter
    @Setter
    private DeploymentEntity deployment;
    @Getter
    @Setter
    private ReleaseEntity release;
    @Getter
    @Setter
    private GenerationModus generationModus;
    @Getter
    @Setter
    private ResourceEntity targetPlatform;
    @Getter
    @Setter
    private ResourceEntity node;
    @Getter
    @Setter
    private NodeJobEntity nodeJobEntity;

    /**
     * Returns the deployment Properties as Map to use it in Templates
     *
     * @return
     */
    public Map<String, Object> asMap() {
        final Map<String, Object> map = new HashMap<String, Object>();
        if (deployment != null) {
            String targetLogPrefix = ConfigurationService.getProperty(ConfigurationService.ConfigKey.LOGS_PATH)
                    + File.separator + deployment.getId() + '_';
            map.put(DEPLOYMENT_PROPERTIES_TARGET_LOGPREFIX, targetLogPrefix);
            if (node != null) {
                String nodeName = node.getName();
                map.put(DEPLOYMENT_PROPERTIES_AMWLOGFILE, targetLogPrefix + nodeName + ".log");
            }
            map.put(DEPLOYMENT_PROPERTIES_KEY_ID, deployment.getId());
            map.put(DEPLOYMENT_PROPERTIES_KEY_TRACKINGID, deployment.getTrackingId());
            map.put(DEPLOYMENT_PROPERTIES_KEY_GENERATIONSTATE_DATE,
                    convertDateToString(deployment.getStateToDeploy()));
            map.put(DEPLOYMENT_PROPERTIES_KEY_CONF_USER, deployment.getDeploymentConfirmationUser());
            map.put(DEPLOYMENT_PROPERTIES_KEY_CONF_DATE,
                    convertDateToString(deployment.getDeploymentConfirmationDate()));
            map.put(DEPLOYMENT_PROPERTIES_KEY_REQ_USER, deployment.getDeploymentRequestUser());
            map.put(DEPLOYMENT_PROPERTIES_KEY_CREA_DATE,
                    convertDateToString(deployment.getDeploymentJobCreationDate()));
            map.put(DEPLOYMENT_PROPERTIES_KEY_PARAMETER, getDeploymentProperiesAsMap(deployment));
        }
        if (generationModus != null) {
            map.put(DEPLOYMENT_PROPERTIES_KEY_GEN_MODUS, generationModus.name());
        }
        if (release != null) {
            map.put(DEPLOYMENT_PROPERTIES_KEY_RELEASE, getReleaseAsMap(release));
        }
        if (targetPlatform != null) {
            map.put(DEPLOYMENT_PROPERTIES_KEY_RUNTIME, getRuntimeAsMap(targetPlatform));
        }
        if (nodeJobEntity != null) {
            map.put(DEPLOYMENT_PROPERTIES_KEY_NODEJOB, getNodeJobEntityAsMap(nodeJobEntity));
        }
        map.put(DEPLOYMENT_PROPERTIES_KEY_GENERATIONDIR, generationDir);

        return map;
    }

    public boolean isDeployGenerationModus() {
        return GenerationModus.DEPLOY.equals(generationModus);
    }

    private Map<String, Object> getReleaseAsMap(ReleaseEntity release) {
        Map<String, Object> hash = new HashMap<String, Object>();
        if (release != null) {
            hash.put(DEPLOYMENT_PROPERTIES_KEY_ID, release.getId());
            hash.put(DEPLOYMENT_PROPERTIES_KEY_NAME, release.getName());
            hash.put(DEPLOYMENT_PROPERTIES_KEY_INSTALLATION_DATE,
                    convertDateToString(release.getInstallationInProductionAt()));
        }
        return hash;
    }

    private Map<String, Object> getRuntimeAsMap(ResourceEntity runtime) {
        Map<String, Object> hash = new HashMap<String, Object>();
        if (runtime != null) {
            hash.put(DEPLOYMENT_PROPERTIES_KEY_ID, runtime.getId());
            hash.put(DEPLOYMENT_PROPERTIES_KEY_NAME, runtime.getName());
        }
        return hash;
    }

    private Map<String, Object> getDeploymentProperiesAsMap(DeploymentEntity deployment) {
        Map<String, Object> hash = new HashMap<String, Object>();
        if (deployment != null && deployment.getDeploymentParameters() != null) {
            for (DeploymentParameter parameter : deployment.getDeploymentParameters()) {
                hash.put(parameter.getKey(), parameter.getValue());
            }
        }

        return hash;
    }

    private Map<String, Object> getNodeJobEntityAsMap(NodeJobEntity nodeJobEntity) {
        Map<String, Object> hash = new HashMap<String, Object>();
        if (nodeJobEntity != null) {
            hash.put(DEPLOYMENT_PROPERTIES_KEY_ID, nodeJobEntity.getId());
        }
        return hash;
    }


    private String convertDateToString(Date d) {
        String dateStr = "";
        if (d != null) {
            dateStr = new SimpleDateFormat(DATE_FORMAT).format(d);
        }
        return dateStr;
    }

}
