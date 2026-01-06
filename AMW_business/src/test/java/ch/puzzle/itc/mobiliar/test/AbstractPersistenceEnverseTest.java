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

package ch.puzzle.itc.mobiliar.test;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.extension.ExtendWith;

import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;

@ExtendWith(PersistenceTestExtension.class)
public abstract class AbstractPersistenceEnverseTest {

	protected abstract EntityManager getEntityManager();

	/**
	 * persists and commits Transaction to fire Envers EvenListener
	 * Only use this code to setup envers test data because Transactions are commited after each persist so transactional handling is overloaded
	 * @param entity
	 */
	protected void persistAndCommitTransaction(Object entity){

		boolean isManaged = getEntityManager().contains(entity);

		if(isManaged){
			getEntityManager().merge(entity);
		}

		getEntityManager().persist(entity);
		getEntityManager().getTransaction().commit();
		getEntityManager().getTransaction().begin();
	}

}
