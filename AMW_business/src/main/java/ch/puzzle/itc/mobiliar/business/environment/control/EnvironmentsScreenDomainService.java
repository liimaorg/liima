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
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.security.control.SecurityScreenDomainService;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import ch.puzzle.itc.mobiliar.common.exception.*;
import ch.puzzle.itc.mobiliar.common.util.ContextNames;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use {@link ContextRepository} - move all functionality to the control to fulfill the cec pattern
 */
@Interceptors(HasPermissionInterceptor.class)
@Stateless
@Deprecated
public class EnvironmentsScreenDomainService {

	@Inject
	private Logger log;
	
	@Inject
	private EntityManager entityManager;
	
	@Inject
	private CommonDomainService commonService;
	
	@Inject
	private ContextDomainService contextDomainService;
	
	@Inject
	protected ResourceTypeProvider resourceTypeProvider;
	
	@Inject
	private SecurityScreenDomainService securityService;

	/**
	 * Speichert die Änderungen auf einen Kontext.
	 * @throws ResourceNotFoundException
	 * @throws SavePropertyException
	 */
	@HasPermission(permission = Permission.SAVE_SETTINGS_ENV)
	public void saveEnvironment(Integer contextId, String newContextName) throws ResourceNotFoundException, SavePropertyException {
		try {
			ContextEntity context = QueryUtils.singleResult(loadContextEntityWithPropertyDescriptors(contextId));
			String oldContextName = context.getName();
			if(newContextName!=null && !newContextName.equals(oldContextName)){
				context.setName(newContextName);
				//Only environments have permissions...
			     if(context.isEnvironment()) {
				    securityService.renamePermissionByName(oldContextName, newContextName);
				}
			}
			entityManager.persist(context);
		} catch (NoResultException nre) {
			String message = "Es konnte keine Kontextresource für mit der Id: " + contextId + " gefunden werden.";
			log.log(Level.WARNING, message, nre);
			throw new ResourceNotFoundException(message, nre);
		}

	}

	/**
	 * @throws ElementAlreadyExistsException 
	 * Erstellt einen neuen Context anhand den Name und den superContext. Der Kontext ist an einen Benutzer zugewisen
	 * @param newName
	 * @param superContextId
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException 
	 * @throws  
	 */
	@HasPermission(permission = Permission.ADD_NEW_ENV_OR_DOM)
	public ContextEntity createContextByName(String newName, Integer superContextId) throws ResourceNotFoundException, ElementAlreadyExistsException{
		try{
			if(getContextByName(newName)!=null){
				String message = "Der Kontext mit dem Namen " + newName + " ist bereits vorhanden und kann nicht erstellen werden"; 
				throw new ElementAlreadyExistsException(message,ContextEntity.class,newName);
			}
		}catch (NoResultException e) {
			String message = "Der Kontext mit de Namen: " + newName + " existiert nicht auf der DB.";
			log.log(Level.WARNING, message);
		}
		ContextEntity superContext = contextDomainService.getContextEntityById(superContextId);
		ContextEntity entity = new ContextEntity();
		String contextName;
		if(superContext!=null){
			ContextNames childContext = ContextNames.valueOf(superContext.getContextType().getName()).getChildContext();			
			if(childContext==null) {
				throw new ResourceNotFoundException("Es existiert kein Unterkontext - die Erstellung ist nicht möglich!");
			}
			contextName = childContext.name();
			entity.setParent(superContext);
			superContext.getChildren().add(entity);
		}
		else{
			throw new ResourceNotFoundException("Der Superkontext mit der Id "+superContextId+" konnte nicht gefunden werden");
		}
		entity.setName(newName);
		entity.setContextType(resourceTypeProvider.getOrCreateContextType(contextName));
		entityManager.persist(entity);
		return entity;
	}

	/**
	 * Hole für eine Id (contextId) die aktuelle Context.
	 * @param contextId
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public ContextEntity getContextWithType(Integer contextId) throws ResourceNotFoundException {
		ContextEntity c = contextDomainService.getContextEntityById(contextId);
		if(c!=null && c.getContextType()!=null){
			return c;
		}
		return null;
	}


	/**
	 * Dublicate method use {@link ContextRepository#getContextByName(String)}
	 * @param contextName
	 * @return
	 * @throws NoResultException
	 */
	@Deprecated
	public ContextEntity getContextByName(String contextName) throws NoResultException{
		ContextEntity contextEntity = null;
		contextEntity =  (ContextEntity) entityManager.createQuery("from ContextEntity c where c.name=:contextName").setParameter("contextName", contextName).getSingleResult();
		if(contextEntity == null){
			String message = "Der Context mit dem Namen: " + contextName + " existiert nicht auf der DB"; 
			log.info(message);
			throw new NoResultException(message);
		}
		return contextEntity;
	}

	/**
	 * maybe return directly context
	 * @param contextId
	 * @return
	 */
	private Query loadContextEntityWithPropertyDescriptors(Integer contextId){
		return entityManager.createQuery("select n from ContextEntity n left join fetch n.propertyDescriptors where n.id = :id").setParameter("id", contextId);
	}
}
