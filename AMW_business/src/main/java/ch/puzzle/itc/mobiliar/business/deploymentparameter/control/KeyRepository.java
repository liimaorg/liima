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
import ch.puzzle.itc.mobiliar.business.utils.BaseRepository;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import java.util.List;

public class KeyRepository extends BaseRepository<Key> {

    @Inject
    EntityManager entityManager;

    public Key findFirstKeyByName(String name) {
        List<Key> result = entityManager.createQuery("select k from Key k where k.name=:keyname", Key.class)
                .setParameter("keyname", name)
                .setMaxResults(1)
                .getResultList();
        return !result.isEmpty() ? result.get(0) : null;
    }

    public void createDeployParameterKey(Key deployParameterKey) {
        entityManager.persist(deployParameterKey);
    }

}
