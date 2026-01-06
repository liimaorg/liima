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

package ch.puzzle.itc.mobiliar.business.resourcerelation.control;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PersistenceTestExtension.class)
public class ResourceRelationContextRepositoryPersistenceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Mock
	Logger log;

	@InjectMocks
	ResourceRelationContextRepository resourceRelationContextRepository;

	@BeforeEach
	public void before() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void test_getResourceRelationContextEntitiesByContextIds() {
	    ResourceEntity master = ResourceFactory.createNewResource("master");
	    ResourceEntity slave = ResourceFactory.createNewResource("slave");
	    ConsumedResourceRelationEntity r = new ConsumedResourceRelationEntity();
	    ContextEntity someContext = new ContextEntity();
	    try {
		   r.setMasterResource(master);
		   r.setSlaveResource(slave);
		   entityManager.persist(master);
		   someContext.setName("bla");
		   entityManager.persist(someContext);
		   ResourceRelationContextEntity resRelCtxEntity = resourceRelationContextRepository
				   .createResourceRelationContext(r, someContext);
		   entityManager.flush();
		   List<ResourceRelationContextEntity> resRelCtxFromDB = resourceRelationContextRepository
				   .getResourceRelationContextEntitiesByContextIds(r, Arrays.asList(someContext.getId()));
		   assertEquals(1, resRelCtxFromDB.size());
		   assertEquals(resRelCtxEntity.getId(), resRelCtxFromDB.get(0).getId());
	    }
	    finally {
		   entityManager.remove(r);
		   entityManager.remove(slave);
		   entityManager.remove(master);
		   entityManager.remove(someContext);
	    }
	}

    @Test
    public void test_getResourceRelationContextWithResourceActivations() {
	   ResourceEntity master = ResourceFactory.createNewResource("master");
	   ResourceEntity slave = ResourceFactory.createNewResource("slave");
	   ConsumedResourceRelationEntity r = new ConsumedResourceRelationEntity();
	   ContextEntity someContext = new ContextEntity();
	   try {
		  r.setMasterResource(master);
		  r.setSlaveResource(slave);
		  entityManager.persist(master);
		  someContext.setName("bla");
		  entityManager.persist(someContext);
		  ResourceRelationContextEntity resRelCtxEntity = resourceRelationContextRepository
				  .createResourceRelationContext(r, someContext);
		  entityManager.flush();
		  ResourceRelationContextEntity resRelCtxFromDB = resourceRelationContextRepository
				  .getResourceRelationContextWithResourceActivations(r, someContext);
		 assertEquals(resRelCtxEntity.getId(), resRelCtxFromDB.getId());
	   }
	   finally {
		  entityManager.remove(r);
		  entityManager.remove(slave);
		  entityManager.remove(master);
		  entityManager.remove(someContext);
	   }
    }
}
