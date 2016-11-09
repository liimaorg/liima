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

package ch.puzzle.itc.mobiliar.business.function.control;


import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

public class FunctionRepository {

    @Inject
    EntityManager entityManager;


	public AmwFunctionEntity getFunctionById(Integer functionId) {
		AmwFunctionEntity amwFunction;
		amwFunction = entityManager.find(AmwFunctionEntity.class, functionId);
		return amwFunction;
	}

    public AmwFunctionEntity getFunctionByIdWithChildFunctions(Integer functionId) {
        List<AmwFunctionEntity> entity =  entityManager
                .createQuery("select f from AmwFunctionEntity f " +
                        "left join fetch f.overwritingChildFunction c " +
                        "where f.id=:functionId", AmwFunctionEntity.class)
                .setParameter("functionId", functionId).setMaxResults(1).getResultList();
        return entity.isEmpty() ? null : entity.get(0);
    }

    public AmwFunctionEntity getFunctionByIdWithMiksAndParentChildFunctions(Integer functionId) {
        List<AmwFunctionEntity> entity =  entityManager
                .createQuery("select f from AmwFunctionEntity f " +
                        "left join fetch f.miks m " +
                        "left join fetch f.overwrittenParent p " +
                        "left join fetch f.overwritingChildFunction c " +
                        "where f.id=:functionId", AmwFunctionEntity.class)
                .setParameter("functionId", functionId).setMaxResults(1).getResultList();
        return entity.isEmpty() ? null : entity.get(0);
    }



    public void persistOrMergeFunction(AmwFunctionEntity amwFunction) {
        if (amwFunction.getId() == null) {
            entityManager.persist(amwFunction);
        }
        else {
            entityManager.merge(amwFunction);
        }
    }

    public void remove(AmwFunctionEntity amwFunction){
        entityManager.remove(amwFunction);
    }
}
