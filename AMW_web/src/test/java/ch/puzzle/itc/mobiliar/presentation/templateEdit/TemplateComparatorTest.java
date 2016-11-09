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

package ch.puzzle.itc.mobiliar.presentation.templateEdit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

public class TemplateComparatorTest {

	private TemplateComparator templateComparator;
	
	@Before
	public void setUp(){
		templateComparator = new TemplateComparator();
	}
	
	@Test
	public void shouldBeEqual_emptyTemplate() {
		// given
		TemplateDescriptorEntity t1 = new TemplateDescriptorEntity();
		TemplateDescriptorEntity t2 = new TemplateDescriptorEntity();
		
		// when 
		int compare = templateComparator.compare(t1, t2);
		
		// then
		assertEquals(0,compare);
	}
	
	@Test
	public void shouldBeEqual_null() {
		// given
		
		// when 
		int compare = templateComparator.compare(null, null);
		
		// then
		assertEquals(0,compare);
	}
	
	@Test
	public void shouldNotBeEqual_t1() {
		// given
		TemplateDescriptorEntity t1 = new TemplateDescriptorEntity();
		t1.setName("test");
		TemplateDescriptorEntity t2 = new TemplateDescriptorEntity();
		
		// when 
		int compare = templateComparator.compare(t1, t2);
		
		// then
		assertEquals(1,compare);
	}
	
	@Test
	public void shouldNotBeEqual_t2() {
		// given
		TemplateDescriptorEntity t1 = new TemplateDescriptorEntity();
		TemplateDescriptorEntity t2 = new TemplateDescriptorEntity();
		t2.setName("test");
		
		// when 
		int compare = templateComparator.compare(t1, t2);
		
		// then
		assertEquals(-1,compare);
	}
	
	@Test
	public void shouldBeEqual_both() {
		// given
		TemplateDescriptorEntity t1 = new TemplateDescriptorEntity();
		t1.setName("test");
		TemplateDescriptorEntity t2 = new TemplateDescriptorEntity();
		t2.setName("test");
		
		// when 
		int compare = templateComparator.compare(t1, t2);
		
		// then
		assertEquals(0,compare);
	}
	
	@Test
	public void shouldbet1() {
		// given
		TemplateDescriptorEntity t1 = new TemplateDescriptorEntity();
		t1.setName("atest");
		TemplateDescriptorEntity t2 = new TemplateDescriptorEntity();
		t2.setName("btest");
		
		// when 
		int compare = templateComparator.compare(t1, t2);
		
		// then
		assertEquals(-1,compare);
	}
	
	@Test
	public void shouldSortCorrect() {
		// given
		TemplateDescriptorEntity t1 = new TemplateDescriptorEntity();
		t1.setName("btest");
		TemplateDescriptorEntity t2 = new TemplateDescriptorEntity();
		t2.setName("atest");
		
		ArrayList<TemplateDescriptorEntity> list = new ArrayList<TemplateDescriptorEntity>();
		list.add(t1);
		list.add(t2);
		// when 
		Collections.sort(list, templateComparator);
		
		// then
		assertEquals(t2,list.get(0));
		assertEquals(t1,list.get(1));
		
	}

}
