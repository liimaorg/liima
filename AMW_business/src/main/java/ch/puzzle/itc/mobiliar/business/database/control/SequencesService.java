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

package ch.puzzle.itc.mobiliar.business.database.control;

import ch.puzzle.itc.mobiliar.business.database.entity.SequencesEntity;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.util.logging.Logger;

/**
 * Services for sequences
 * 
 * @author cweber
 */
@Stateless
public class SequencesService {
	@Inject
	EntityManager em;

	@Inject
	Logger log;

	/**
	 * Returns the nextValue for the sequence with the given name and increments the value on db.
	 * 
	 * @param sequenceName
	 * @return
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Integer getNextValueAndUpdate(String sequenceName) {

	    SequencesEntity lockedSeq = em.find(SequencesEntity.class, sequenceName, LockModeType.PESSIMISTIC_WRITE);
	    Integer nextVal = lockedSeq.getNextValue();
	    lockedSeq.setNextValue(nextVal + 1);
         return nextVal;
	}

}
