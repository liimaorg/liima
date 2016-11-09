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

package ch.puzzle.itc.mobiliar.test.testrunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import ch.puzzle.itc.mobiliar.business.database.control.EntityManagerProducerIntegrationTestImpl;
import org.junit.rules.ExternalResource;

import com.google.common.io.Files;

public class PersistenceTestRule extends ExternalResource {

    private static EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;
    private EntityTransaction transaction;

    private final Object target;
    
    private boolean entityManagerFactoryAsSingleton;
    
    private String persistenceUnit;

    public PersistenceTestRule(Object target, boolean entityManagerFactoryAsSingleton, String persistenceUnit) {
        this.target = target;
        this.entityManagerFactoryAsSingleton = entityManagerFactoryAsSingleton;
        this.persistenceUnit = persistenceUnit;
    }

    @Override
    protected void before() throws IOException {
    	
    	EntityManagerFactory entityManagerFactory;
    	
    	if(entityManagerFactoryAsSingleton){
    		if (PersistenceTestRule.entityManagerFactory == null) {
                EntityManagerProducerIntegrationTestImpl.copyIntegrationTestDB();
    			PersistenceTestRule.entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);
            }
    		entityManagerFactory = PersistenceTestRule.entityManagerFactory;
    	}else{
    		entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);
    	}
        
        entityManager = entityManagerFactory.createEntityManager();

        injectPersistence(target);

        transaction = entityManager.getTransaction();
        transaction.begin();
    }

    @Override
    protected void after() {
        transaction.rollback();
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

    private void injectPersistence(Object target) {
        Field[] fields = target.getClass().getDeclaredFields();
        if(fields != null && fields.length > 0){
        	handleClassFields(target, fields);
        }else{
        	fields = target.getClass().getSuperclass().getDeclaredFields();
        	handleClassFields(target, fields);
        }
        
    }

	private void handleClassFields(Object target, Field[] fields) {
		for (Field field : fields) {
            if (field.isAnnotationPresent(PersistenceContext.class)) {
                injectFieldValue(field, target, entityManager);
            } else if (field.isAnnotationPresent(PersistenceUnit.class)) {
                injectFieldValue(field, target, entityManagerFactory);
            }
        }
	}

    private void injectFieldValue(Field field, Object target, Object value) {
        if (field.getType().isAssignableFrom(value.getClass())) {
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            try {
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Field not accessable", e);
            } finally {
                field.setAccessible(wasAccessible);
            }
        }
    }
}