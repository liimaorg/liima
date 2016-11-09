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

package ch.puzzle.itc.mobiliar.business.deploymentparameter.control;

import ch.puzzle.itc.mobiliar.business.deploymentparameter.entity.Key;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

public class KeyRepository {

    @Inject
    EntityManager entityManager;

    public Key getKeyById(Integer keyId) {
        Key key = entityManager.find(Key.class, keyId);
        return key;
    }

    public Key findFirstKeyByName(String name) {
        List<Key> result = entityManager.createQuery("select k from Key k where k.name=:keyname", Key.class)
                .setParameter("keyname", name)
                .setMaxResults(1)
                .getResultList();
        return !result.isEmpty() ? result.get(0) : null;
    }

    public List<Key> findAllKeys() {
        try {
            return entityManager.createQuery("select k from Key k ", Key.class).getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    public void deleteDeployParameterKey(Key deployParameterKey) {
        entityManager.remove(deployParameterKey);
    }

    public void createDeployParameterKey(Key deployParameterKey) {
        entityManager.persist(deployParameterKey);
    }

    public void changeDeployParameterKey(Key deployParameterKey) {
        entityManager.merge(deployParameterKey);
    }
}
