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

package ch.puzzle.itc.mobiliar.business.environment.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import org.junit.jupiter.api.Test;

/**
 * The abstract HasContexts class is tested with the ResourceTypeEntity
 * Implementation
 */
public class HasContextsTest
{

	@Test
	public void getContext_no_contexts() {
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));

		r.createContext();

		ContextEntity contextToLookfor = new ContextEntity();
		contextToLookfor.setId(Integer.valueOf(1));
		// when
		ResourceTypeContextEntity contextResult = r
				.getContext(contextToLookfor);

		// then
		assertNull(contextResult);
	}

	@Test
	public void getContext_wrong_context_to_lookfor() {
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));

		ContextEntity context = new ContextEntity();
		context.setId(Integer.valueOf(12));

		ResourceTypeContextEntity createdContext = r.createContext();

		createdContext.setContext(context);

		Set<ResourceTypeContextEntity> contexts = new HashSet<ResourceTypeContextEntity>();
		contexts.add(createdContext);
		r.setContexts(contexts);

		ContextEntity contextToLookfor = new ContextEntity();
		contextToLookfor.setId(Integer.valueOf(1));
		// when
		ResourceTypeContextEntity contextResult = r
				.getContext(contextToLookfor);

		// then
		assertNull(contextResult);
	}

	@Test
	public void getContext() {
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));

		ContextEntity context = new ContextEntity();
		context.setId(Integer.valueOf(12));

		ResourceTypeContextEntity createdContext = r.createContext();

		createdContext.setContext(context);

		Set<ResourceTypeContextEntity> contexts = new HashSet<ResourceTypeContextEntity>();
		contexts.add(createdContext);
		r.setContexts(contexts);

		ContextEntity contextToLookfor = new ContextEntity();
		contextToLookfor.setId(Integer.valueOf(12));
		// when
		ResourceTypeContextEntity contextResult = r
				.getContext(contextToLookfor);

		// then
		assertNotNull(contextResult);
		assertEquals(contextResult, createdContext);
	}

	@Test
	public void getOrCreateContext_No_context() {
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));
		
		ContextEntity contextToLookfor = new ContextEntity();
		contextToLookfor.setId(Integer.valueOf(12));
		// when
		ResourceTypeContextEntity contextResult = r
				.getOrCreateContext(contextToLookfor);

		// then
		assertNotNull(contextResult);
		assertEquals(Integer.valueOf(12), contextResult.getContext().getId());
	}
	
	@Test
	public void getOrCreateContext() {
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));
		
		ContextEntity context = new ContextEntity();
		context.setId(Integer.valueOf(12));
		
		ResourceTypeContextEntity createdContext = r.createContext();
		createdContext.setContext(context);

		Set<ResourceTypeContextEntity> contexts = new HashSet<ResourceTypeContextEntity>();
		contexts.add(createdContext);
		r.setContexts(contexts);
		
		ContextEntity contextToLookfor = new ContextEntity();
		contextToLookfor.setId(Integer.valueOf(12));
		// when
		ResourceTypeContextEntity contextResult = r
				.getOrCreateContext(contextToLookfor);

		// then
		assertNotNull(contextResult);
		assertEquals(context, contextResult.getContext());
		assertEquals(Integer.valueOf(12), contextResult.getContext().getId());
	}
	
	@Test
	public void getContextsByLowestContext_null(){
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));
		
		
		// when
		List<ResourceTypeContextEntity> contextResult = r.getContextsByLowestContext(null);

		// then
		assertNotNull(contextResult);
		assertEquals(0, contextResult.size());
	}
	
	@Test
	public void getContextsByLowestContext_null_Contexts(){
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));
		
		ContextEntity context = new ContextEntity();
		context.setId(Integer.valueOf(12));
		
		ResourceTypeContextEntity createdContext = r.createContext();
		createdContext.setContext(context);

		Set<ResourceTypeContextEntity> contexts = new HashSet<ResourceTypeContextEntity>();
		contexts.add(createdContext);
		r.setContexts(contexts);
		
		// when
		List<ResourceTypeContextEntity> contextResult = r.getContextsByLowestContext(null);

		// then
		assertNotNull(contextResult);
		assertEquals(0, contextResult.size());
	}
	
	@Test
	public void getContextsByLowestContext(){
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));
		
		ContextEntity context = new ContextEntity();
		context.setId(Integer.valueOf(12));
		
		ResourceTypeContextEntity createdContext = r.createContext();
		createdContext.setContext(context);

		Set<ResourceTypeContextEntity> contexts = new HashSet<ResourceTypeContextEntity>();
		contexts.add(createdContext);
		r.setContexts(contexts);
		
		// when
		List<ResourceTypeContextEntity> contextResult = r.getContextsByLowestContext(context);

		// then
		assertNotNull(contextResult);
		assertEquals(1, contextResult.size());
		assertEquals(createdContext, contextResult.get(0));
	}
	
	@Test
	public void getContextsByLowestContext_withParent_butImplemented_isNull(){
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));
		
		ContextEntity context = new ContextEntity();
		context.setId(Integer.valueOf(12));
		
		ContextEntity contextParent = new ContextEntity();
		contextParent.setId(Integer.valueOf(13));
		context.setParent(contextParent);
		
		ResourceTypeContextEntity createdContext = r.createContext();
		createdContext.setContext(context);

		Set<ResourceTypeContextEntity> contexts = new HashSet<ResourceTypeContextEntity>();
		contexts.add(createdContext);
		r.setContexts(contexts);
		
		// when
		List<ResourceTypeContextEntity> contextResult = r.getContextsByLowestContext(context);

		// then
		assertNotNull(contextResult);
		assertEquals(1, contextResult.size());
		assertEquals(createdContext, contextResult.get(0));
	}
	
	@Test
	public void getContextsByLowestContext_withParent(){
		// given
		ResourceTypeEntity r = new ResourceTypeEntity();
		r.setId(Integer.valueOf(1));
		
		ContextEntity context = new ContextEntity();
		context.setId(Integer.valueOf(12));
		
		ContextEntity contextParent = new ContextEntity();
		contextParent.setId(Integer.valueOf(13));
		context.setParent(contextParent);
		
		ResourceTypeContextEntity createdContext = r.createContext();
		createdContext.setContext(context);
		
		ResourceTypeContextEntity createdContext2 = r.createContext();
		createdContext2.setContext(contextParent);

		Set<ResourceTypeContextEntity> contexts = new HashSet<ResourceTypeContextEntity>();
		contexts.add(createdContext);
		contexts.add(createdContext2);
		r.setContexts(contexts);
		
		// when
		List<ResourceTypeContextEntity> contextResult = r.getContextsByLowestContext(context);

		// then
		assertNotNull(contextResult);
		assertEquals(2, contextResult.size());
		assertEquals(createdContext, contextResult.get(0));
		assertEquals(createdContext2, contextResult.get(1));
	}

}
