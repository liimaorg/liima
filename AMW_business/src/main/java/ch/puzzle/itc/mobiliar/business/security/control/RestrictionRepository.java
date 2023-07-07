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

package ch.puzzle.itc.mobiliar.business.security.control;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.RestrictionEntity;
import ch.puzzle.itc.mobiliar.business.utils.BaseRepository;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Stateless
public class RestrictionRepository extends BaseRepository<RestrictionEntity> {

    @Inject
    EntityManager entityManager;

    @Inject
    protected Logger log;

    /**
     * Persists a new RestrictionEntity and returns its id
     *
     * @param restriction
     * @return Id of the newly created RestrictionEntity
     */
    public Integer create(RestrictionEntity restriction) {
        if (log.isLoggable(Level.FINE)) {
            log.info("Creating restriction: " + restriction.toString());
        }
        entityManager.persist(restriction);
        entityManager.flush();
        return restriction.getId();
    }

    /**
     * Deletes the Restriction with the specified id
     * This method is useful when a subsequent forceReloadingOfLists call is needed (which interferes with em.remove)
     *
     * @param id id of the Restriction to be removed
     */
    public void deleteRestrictionById(Integer id){
        entityManager.createQuery("delete from RestrictionEntity r where r.id =:id").setParameter("id", id)
                .executeUpdate();
    }

    /**
     * Deletes all Restrictions matching a specific Context
     *
     * @param context ContextEntity to match
     */
    public void deleteAllWithContext(ContextEntity context) {
        entityManager.createQuery("delete from RestrictionEntity r where r.context =:context")
                .setParameter("context", context).executeUpdate();
    }

    /**
     * Deletes all Restrictions matching a specific ResourceGroup
     *
     * @param resourceGroup ResourceGroupEntity to match
     */
    public void deleteAllWithResourceGroup(ResourceGroupEntity resourceGroup) {
        entityManager.createQuery("delete from RestrictionEntity r where r.resourceGroup =:resourceGroup")
                .setParameter("resourceGroup", resourceGroup).executeUpdate();
    }

    /**
     * Deletes all Restrictions matching a specific ResourceType
     *
     * @param resourceType ResourceTypeEntity to match
     */
    public void deleteAllWithResourceType(ResourceTypeEntity resourceType) {
        entityManager.createQuery("delete from RestrictionEntity r where r.resourceType =:resourceType")
                .setParameter("resourceType", resourceType).executeUpdate();
    }

}
