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

package ch.puzzle.itc.mobiliar.business.environment.control;

import ch.puzzle.itc.mobiliar.business.database.control.QueryUtils;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextTypeEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.GlobalContext;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;
import ch.puzzle.itc.mobiliar.common.exception.GeneralDBException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.ContextNames;

import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use {@link ContextRepository} - move all functionality to the control to fulfill the cec pattern
 */
@Stateless
@Deprecated
public class ContextDomainService {
	
	@Inject
	private EntityManager entityManager;
	
	@Inject
	ResourceTypeProvider resourceTypeProvider;
	
	@Inject
	public Logger log;
	
	private ContextEntity globalContext;

    /**
     * Returns all Environments
     *
     * @return
     * @throws GeneralDBException
     */
    public List<ContextEntity> getEnvironments() {
		List<ContextEntity> allContexts = QueryUtils.fetch(ContextEntity.class, fetchAllContexts(), 0, -1);

		return allContexts;
	}

	protected Query fetchAllContexts() {
		return entityManager.createQuery("select n from ContextEntity n left join fetch n.children as c left join fetch n.contextType left join fetch c.contextType order by n.name asc");
	}


    @Produces
    @GlobalContext
	public ContextEntity getGlobalResourceContextEntity() {
		if (globalContext == null || !entityManager.contains(globalContext)) {
			ContextTypeEntity globalType = resourceTypeProvider.getOrCreateContextType(ContextNames.GLOBAL.name());
			if (globalType.getContexts() == null || globalType.getContexts().size() == 0) {
				// There is not context defined. Create one!
				globalContext = new ContextEntity();
				globalContext.setContextType(globalType);
				entityManager.persist(globalContext);
				entityManager.flush();
			} else {
				for (ContextEntity tmpC : globalType.getContexts()) {
					globalContext = tmpC;
					break;
				}
			}
			if (globalContext == null) {
				throw new AMWRuntimeException("Es ist kein ContextEntity für den Kontext-Typ \"Global\" definiert!");
			}
		}
		return globalContext;
	}
	
	/**
	 * @param contextId
	 * @return the context entity by the given id
	 * @throws ResourceNotFoundException
	 */
	public ContextEntity getContextEntityById(Integer contextId) throws ResourceNotFoundException {
		ContextEntity result = null;
		if (contextId != null) {
			try {
				result = entityManager.find(ContextEntity.class, contextId);
			} catch (NoResultException nre) {
				String message = "Der Kontext mit der Id: " + contextId + " existiert nicht auf der DB";
				log.log(Level.WARNING, message, nre);
				throw new ResourceNotFoundException(message, nre);
			}
		}
		return verifyContextEntity(result);
	}

	
	/**
	 * Null-safe check for a context - if the given context is null, it returns the global context.
	 * 
	 * @param context
	 * @return
	 */
	private ContextEntity verifyContextEntity(ContextEntity context) {
		if (context != null) {
			return context;
		}
		return getGlobalResourceContextEntity();
	}

}
