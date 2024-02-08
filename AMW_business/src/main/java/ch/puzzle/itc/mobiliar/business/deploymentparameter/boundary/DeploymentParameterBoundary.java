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

package ch.puzzle.itc.mobiliar.business.deploymentparameter.boundary;

import ch.puzzle.itc.mobiliar.business.deploymentparameter.control.DeploymentParameterRepository;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.control.KeyRepository;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.DeploymentParameter;
import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.Key;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.*;

@Stateless
public class DeploymentParameterBoundary {

    @Inject
    private KeyRepository keyRepository;

    @Inject
    private DeploymentParameterRepository deploymentParameterRepository;

    @Inject
    protected Logger log;


    public List<Key> findAllKeys() {
        return keyRepository.findAll();
    }

    public List<DeploymentParameter> findAllDeploymentParameterFor(Integer deploymentId) {
        return deploymentParameterRepository.findByDeploymentId(deploymentId);
    }

    @HasPermission(permission = Permission.MANAGE_DEPLOYMENT_PARAMETER, action = DELETE)
    public void deleteDeployParameterKey(Key keyToDelete) throws NotFoundException {
        Key attachedKeyToDelete = keyRepository.findFirstKeyByName(keyToDelete.getName());
        this.requireNotNull(attachedKeyToDelete);
        keyRepository.remove(attachedKeyToDelete);
    }

    private void requireNotNull(Key key) throws NotFoundException {
        if (key == null) {
            throw new NotFoundException("Key not found.");
        }
    }

    @HasPermission(permission = Permission.MANAGE_DEPLOYMENT_PARAMETER, action = UPDATE)
    public void changeDeployParameterKey(Integer keyToDeleteId, String changedName) throws ValidationException {
        if (changedName != null && !changedName.trim().isEmpty()) {
            Key attachedKeyToChange = keyRepository.find(Objects.requireNonNull(keyToDeleteId, "Must not be null"));

            attachedKeyToChange.setName(changedName);
            keyRepository.merge(attachedKeyToChange);
        } else {
            throw new ValidationException("invalid empty name", changedName);
        }
    }

    @HasPermission(permission = Permission.MANAGE_DEPLOYMENT_PARAMETER, action = CREATE)
    public void createDeployParameterKey(String deployParameterKeyName) throws ValidationException {
        if (deployParameterKeyName != null && !deployParameterKeyName.trim().isEmpty()) {
            Key newKey = new Key(deployParameterKeyName.trim());
            if (keyRepository.findFirstKeyByName(newKey.getName()) != null) {
                throw new ValidationException("A key with same name exists", deployParameterKeyName);
            }
            keyRepository.createDeployParameterKey(newKey);
        } else {
            throw new ValidationException("invalid empty name", deployParameterKeyName);
        }
    }
}
