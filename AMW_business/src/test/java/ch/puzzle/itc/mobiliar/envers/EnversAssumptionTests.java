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

package ch.puzzle.itc.mobiliar.envers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.test.AbstractPersistenceEnverseTest;

/**
 * This is a test to ensure the assumptions made on envers
 */
public class EnversAssumptionTests extends AbstractPersistenceEnverseTest {

	@PersistenceContext
	EntityManager entityManager;

	ContextEntity c;
	PropertyDescriptorEntity property;
	AuditReader auditReader;
	Number revisionInBetween;
	Number revisionAfter;
	Number revisionLatest;

	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}

	@BeforeEach
	public void setup() throws Exception {
		auditReader = AuditReaderFactory.get(entityManager);

		// We create a context entity
		c = new ContextEntity();
		persistAndCommitTransaction(c);
		// and we memorize the last revision number before we continue
		revisionInBetween = auditReader.getRevisionNumberForDate(new Date());

		// We define a property and add it to the context
		property = new PropertyDescriptorEntity();
		property.setPropertyComment("comment");
		// save first revision
		c.addPropertyDescriptor(property);
		persistAndCommitTransaction(property);
		revisionAfter = auditReader.getRevisionNumberForDate(new Date());
		
		property.setPropertyComment("newComment");
		persistAndCommitTransaction(property);
		revisionLatest = auditReader.getRevisionNumberForDate(new Date());
	}

	@Test
	public void propertyDescriptorDoesNotExistInEarlierRevision() {
		PropertyDescriptorEntity propertyDescriptor = auditReader.find(PropertyDescriptorEntity.class,
				property.getId(), revisionInBetween);
		assertNull(propertyDescriptor);
	}

	@Test
	public void propertyDescriptorIsNotAttachedInEarlierRevision() {
		ContextEntity contextInFirstRevision = auditReader.find(ContextEntity.class, c.getId(),
				revisionInBetween);
		assertNotNull(contextInFirstRevision);
		assertTrue(contextInFirstRevision.getPropertyDescriptors().isEmpty());
	}

	@Test
	public void propertyDescriptorDoesExistInLaterRevision() {
		PropertyDescriptorEntity propertyDescriptor = auditReader.find(PropertyDescriptorEntity.class,
				property.getId(), revisionAfter);
		assertNotNull(propertyDescriptor);
		assertEquals("comment", propertyDescriptor.getPropertyComment());
	}

	/**
	 * This test ensures, that even if context entity does not exist in the given revision (but in an earlier
	 * one), the requested revision is kept for walking down the relation tables and therefore, the related
	 * {@link PropertyDescriptorEntity} (which has been added in a later revision) is visible. This means, that all
	 * resources are requested depending on the primarily requested revision and not on the latest revision,
	 * the initially requested entity exists the first time.
	 */
	@Test
	public void propertyDescriptorIsAttachedInLaterRevision() {
		ContextEntity contextInLastRevision = auditReader.find(ContextEntity.class, c.getId(),
				revisionAfter);
		assertNotNull(contextInLastRevision);
		assertEquals(1, contextInLastRevision.getPropertyDescriptors().size());
		assertEquals("comment", contextInLastRevision.getPropertyDescriptors().iterator().next().getPropertyComment());
	}
	
	@Test
	public void propertyDescriptorIsAttachedInLatestRevisionAndHasUpdatedComment() {
		ContextEntity contextInLastRevision = auditReader.find(ContextEntity.class, c.getId(),
				revisionLatest);
		assertNotNull(contextInLastRevision);
		assertEquals(1, contextInLastRevision.getPropertyDescriptors().size());
		assertEquals("newComment", contextInLastRevision.getPropertyDescriptors().iterator().next().getPropertyComment());
	}

}
