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

import static org.junit.Assert.*;

import org.junit.Test;

import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

public class ResourceTypeEntityTest
{

	@Test
	public void createContext() {
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));
		
		// when
		ResourceTypeContextEntity createdContext = r.createContext();
		
		// then
		assertNotNull(createdContext);
		assertEquals(r, createdContext.getContextualizedObject());
	}
	
	@Test
	public void shouldBeDefaultResourceType(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("APPLICATION");
		ResourceTypeEntity r2 = new ResourceTypeEntity();
		r2.setName("APPLICATIONSERVER");
		ResourceTypeEntity r3 = new ResourceTypeEntity();
		r3.setName("NODE");
		
		// then
		assertTrue(r.isDefaultResourceType());
		assertTrue(r2.isDefaultResourceType());
		assertTrue(r3.isDefaultResourceType());
	}
	
	@Test
	public void shouldNotBeDefaultResourceType(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("Webservice");
		ResourceTypeEntity r2 = new ResourceTypeEntity();
		r2.setName("Ad");
		ResourceTypeEntity r3 = new ResourceTypeEntity();
		r3.setName("Node");
		
		// then
		assertFalse(r.isDefaultResourceType());
		assertFalse(r2.isDefaultResourceType());
		assertFalse(r3.isDefaultResourceType());
	}
	
	@Test
	public void shouldBeApplicationResourceType(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("APPLICATION");
		
		// then
		assertTrue(r.isApplicationResourceType());
		assertTrue("APPLICATION".equals(DefaultResourceTypeDefinition.APPLICATION.name()));
		
	}
	
	@Test
	public void shouldBeApplicationServerResourceType(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("APPLICATIONSERVER");
		
		// then
		assertTrue(r.isApplicationServerResourceType());
		assertTrue("APPLICATIONSERVER".equals(DefaultResourceTypeDefinition.APPLICATIONSERVER.name()));
		
	}
	
	@Test
	public void shouldBeNodeResourceType(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("NODE");
		
		// then
		assertTrue(r.isNodeResourceType());
		assertTrue("NODE".equals(DefaultResourceTypeDefinition.NODE.name()));
		
	}
	
	@Test
	public void shouldBeNoDefaultResourceType(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("Webservice");
		ResourceTypeEntity r2 = new ResourceTypeEntity();
		r2.setName("Ad");
		ResourceTypeEntity r3 = new ResourceTypeEntity();
		r3.setName("Node");

		// then
		assertFalse(r.isDefaultResourceType());
		assertFalse(r2.isDefaultResourceType());
		assertFalse(r3.isDefaultResourceType());
		
		// then
		assertFalse(r.isApplicationResourceType());
		assertFalse(r.isApplicationServerResourceType());
		assertFalse(r.isNodeResourceType());
		
		assertFalse(r2.isApplicationResourceType());
		assertFalse(r2.isApplicationServerResourceType());
		assertFalse(r2.isNodeResourceType());
		
		assertFalse(r3.isApplicationResourceType());
		assertFalse(r3.isApplicationServerResourceType());
		assertFalse(r3.isNodeResourceType());
		
	}
	
	@Test
	public void shouldBeEqualTypeNode(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("NODE");
		
		// then
		assertTrue(r.isResourceType(DefaultResourceTypeDefinition.NODE));
	}
	
	@Test
	public void shouldBeEqualTypeApplication(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("APPLICATION");
		
		// then
		assertTrue(r.isResourceType(DefaultResourceTypeDefinition.APPLICATION));
		
	}
	
	@Test
	public void shouldBeEqualTypeApplicationServer(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("APPLICATIONSERVER");
		
		// then
		assertTrue(r.isResourceType(DefaultResourceTypeDefinition.APPLICATIONSERVER));
		
	}
	
	@Test
	public void shouldNotBeEqualTypeApplicationServer(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("APPLICATIONSERVER");
		
		// then
		assertFalse(r.isResourceType(DefaultResourceTypeDefinition.APPLICATION));
		
	}
	
	@Test
	public void shouldNotBeEqualType(){
		// given
		
		// when
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setName("ResourceTypeNotKnown");
		
		// then
		assertFalse(r.isResourceType(DefaultResourceTypeDefinition.NODE));
		
	}

}
