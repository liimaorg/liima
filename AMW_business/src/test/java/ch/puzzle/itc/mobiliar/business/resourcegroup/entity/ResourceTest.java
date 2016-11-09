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

package ch.puzzle.itc.mobiliar.business.resourcegroup.entity;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ResourceTest {
	
	ResourceEntity master;
	ResourceTypeEntity resTypeMaster;
	ResourceTypeEntity resTypeSlave;
	ResourceTypeEntity resTypeSecondSlave;
	

	ResourceEntity slave;
	ConsumedResourceRelationEntity resourceRelation;
	ResourceEntity secondSlave;
	
	private int generateRandomId(){
		return (int)(Math.random()*1000.0);
	}
	
	private String generateRandomName(){
		return UUID.randomUUID().toString();
	}
	
	private ResourceEntity createResourceEntity(ResourceTypeEntity resourceType){
		ResourceEntity e = ResourceFactory.createNewResource(generateRandomName());
		e.setId(generateRandomId());
		e.setName(generateRandomName());
		e.setResourceType(resourceType);
		return e;
	}
	
	
	private ResourceTypeEntity createResourceTypeEntity(DefaultResourceTypeDefinition name){
		ResourceTypeEntity e = new ResourceTypeEntity();
		e.setId(generateRandomId());
		e.setName(name.name());
		return e;
	}
	
	private ConsumedResourceRelationEntity createResourceRelation(ResourceEntity e1, ResourceEntity e2){
		ConsumedResourceRelationEntity e = new ConsumedResourceRelationEntity();
		e.setId(generateRandomId());
		e.setMasterResource(e1);
		e.setSlaveResource(e2);
		return e;
	}
	
	@Before
	public void setUp() {
		resTypeMaster = createResourceTypeEntity(DefaultResourceTypeDefinition.APPLICATIONSERVER);
		
		resTypeSlave = createResourceTypeEntity(DefaultResourceTypeDefinition.APPLICATION);
		resTypeSecondSlave = createResourceTypeEntity(DefaultResourceTypeDefinition.NODE);
		
		master = createResourceEntity(resTypeMaster);
		slave = createResourceEntity(resTypeSlave);
		secondSlave = createResourceEntity(resTypeSlave);
		resourceRelation = createResourceRelation(master, slave);
		master.addConsumedRelation(resourceRelation);
	}	

	@After
	public void tearDown() {
	}
			
	@Test
	public void getRelation() {
		assertTrue(master.getConsumedRelation(slave) == resourceRelation);
	}
	
	@Test
	public void getRelationById(){
		assertTrue(master.getConsumedRelationById(slave.getId()) == resourceRelation);
	}	
	
	@Test
	public void getRelatedResources(){
		assertTrue(master.getConsumedRelatedResources().size()==1);
		assertTrue(master.getConsumedRelatedResources().contains(slave));
		master.addConsumedRelation(createResourceRelation(master, secondSlave));
		assertTrue(master.getConsumedRelatedResources().size()==2);
	}

	@Test
	public void getRelatedResourcesByResourceType(){
		assertTrue(master.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION).size()==1);
		assertEquals(master.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION).get(0), slave);
		master.addConsumedRelation(createResourceRelation(master, secondSlave));
		assertTrue(master.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION).size()==2);
		assertTrue(master.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION).contains(secondSlave));
		secondSlave.setResourceType(resTypeSecondSlave);
		assertTrue(master.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION).size()==1);
		assertEquals(master.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION).get(0), slave);
		assertTrue(master.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.NODE).size()==1);
		assertEquals(master.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.NODE).get(0), secondSlave);
	}
	
	@Test
	public void canAddResourceRelationTwice() throws ElementAlreadyExistsException{
		ResourceRelationTypeEntity resRelationType = new ResourceRelationTypeEntity();
		master.addConsumedResourceRelation(secondSlave, resRelationType, null, ForeignableOwner.AMW);
		assertEquals(master.getConsumedRelation(secondSlave).getResourceRelationType(), resRelationType);
		master.addConsumedResourceRelation(secondSlave, resRelationType, null, ForeignableOwner.AMW);
	}
	
	@Test
	public void changeName() {
		// given
		ResourceEntity res1 = ResourceFactory.createNewResource("foo");
		ResourceGroupEntity group = res1.getResourceGroup();
		ResourceEntity res2 = ResourceFactory.createNewResource(group);
		assertEquals("foo", group.getName());
		assertEquals("foo", res2.getName());
		assertEquals("foo", res1.getName());

		// when
		res1.setName("bar");

		// then
		assertEquals("bar", group.getName());
		assertEquals("bar", res1.getName());
		assertEquals("bar", res2.getName());
	}

}
