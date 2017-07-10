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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import ch.puzzle.itc.mobiliar.business.security.entity.PermissionEntity;

/**
 * @author aromano
 * 
 */
@Interceptors(HasPermissionInterceptor.class)
@Stateless
public class SecurityScreenDomainService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager entityManager;
    
    @Inject
    private PermissionRepository permissionRepository;

    @Inject
    private RestrictionRepository restrictionRepository;

    /**
     * Diese Methode sucht eine Berechtigung durch einen Name
     * 
     * @param permissionValue
     * @return permissionEntity
     */
    private PermissionEntity getUniquePermissionByName(String permissionValue) {
        PermissionEntity permissionEntity = null;
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<PermissionEntity> p = cb
                    .createQuery(PermissionEntity.class);
            Root<PermissionEntity> root = p.from(PermissionEntity.class);
            p.where(cb.equal(root.get("value"), permissionValue));
            permissionEntity = entityManager.createQuery(p).getSingleResult();
        } catch (NoResultException nre) {
            String message = "Die Erlaubnis: " + permissionValue
                    + " existiert nicht auf der DB";
            log.log(Level.WARNING, message);
        }
        return permissionEntity;
    }

    /**
     * Diese Methode benennt eine Berechtigung um
     * 
     * @param permissionValue
     * @param newPermissionValue
     */
    public void renamePermissionByName(String permissionValue, String newPermissionValue)  {
        PermissionEntity permissionEntity = getUniquePermissionByName(permissionValue);
        if (permissionEntity == null) {
            String message = "Die Erlaubnis mit dem Namen:" + permissionValue + " ist nicht vorhanden";
            log.info(message);
        } else {
            permissionEntity.setValue(newPermissionValue);
        }
    }

    public void deleteRestrictionsWithContext(ContextEntity context) {
        restrictionRepository.deleteAllWithContext(context);
        permissionRepository.setReloadRolesAndPermissionsList(true);
        permissionRepository.setReloadDeployableRoleList(true);
    }

}