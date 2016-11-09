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

package ch.puzzle.itc.mobiliar.test.testrunner.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.test.AbstractPersistenceEnverseTest;

@Ignore("is failing on sonar")
public class ExamplePersistenceEnversTest extends AbstractPersistenceEnverseTest{

	@PersistenceContext
	EntityManager entityManager;

	
	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}
	
	@Before
    public void setup() throws Exception {
		// set up Envers Data with a commit for each persist
        PropertyDescriptorEntity property = new PropertyDescriptorEntity();
        property.setPropertyComment("comment");
        // save first revision
        persistAndCommitTransaction(property);
        // save second revision
        property.setPropertyComment("comment2");
        persistAndCommitTransaction(property);
    }
    
    @Test
    public void shouldFindPropertyDescriptorEntity_inDifferentVersions() throws Exception {
        // when
    	PropertyDescriptorEntity result = entityManager.find(PropertyDescriptorEntity.class, 1);

    	AuditReader auditReader = AuditReaderFactory.get(entityManager);
    	
    	List<Number> revisions = auditReader.getRevisions(PropertyDescriptorEntity.class, 1);
    	PropertyDescriptorEntity revision1 = auditReader.find(PropertyDescriptorEntity.class, 1, 1);
    	PropertyDescriptorEntity revision2 = auditReader.find(PropertyDescriptorEntity.class, 1, 2);
    	
        // then
    	assertNotNull(revisions);
    	assertNotNull(revision1);
        assertNotNull(result);
        assertEquals(2, revisions.size());
        assertEquals("comment2", result.getPropertyComment());
        assertEquals("comment", revision1.getPropertyComment());
        assertEquals("comment2", revision2.getPropertyComment());
    }

}
