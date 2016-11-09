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

package ch.puzzle.itc.mobiliar.business.releasing.boundary;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseRepository;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ReleaseLocator {

    // TODO add permission check
    @Inject
    protected Logger log;

    @Inject
    ReleaseRepository releaseRepository;

    public ReleaseEntity getReleaseByName(String name) throws NoResultException{
        try {
            return releaseRepository.getReleaseByName(name);
        }
        catch (NoResultException e) {
            log.warning("Error occurred in query: " + e.getMessage());
            throw e;
        }
        catch (Exception e) {
            log.warning("Error occurred on database access: " + e.getMessage());
            throw new RuntimeException("Boundary exception", e);
        }
    }

    public List<ReleaseEntity> getReleasesForResourceGroup(ResourceGroupEntity resourceGroup) throws NoResultException{
        try {
            return releaseRepository.getReleasesForResourceGroup(resourceGroup);
        }
        catch (NoResultException e) {
            log.warning("Error occurred in query: " + e.getMessage());
            throw e;
        }
        catch (Exception e) {
            log.warning("Error occurred on database access: " + e.getMessage());
            throw new RuntimeException("Boundary exception", e);
        }
    }
}
