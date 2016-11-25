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

package ch.puzzle.itc.mobiliar.business.property.boundary;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;

public class PropertyTagEditorTest {
	
	PropertyTagEditor p = new PropertyTagEditor();
	
	@Test
	public void getTagsAsCommaSeparatedString_shouldReturnTags_as_list() {
		// given
		PropertyTagEntity tag = new PropertyTagEntity();
		tag.setName("tag1");
		
		PropertyTagEntity tag2 = new PropertyTagEntity();
		tag2.setName("tag2");
		
		List<PropertyTagEntity> propertyTags = new ArrayList<>();
		propertyTags.add(tag);
		propertyTags.add(tag2);
		
		// when
		String result = p.getTagsAsCommaSeparatedString(propertyTags);
		// then
		assertEquals("tag1,tag2,", result);
	}
	
	@Test
	public void getTagsAsCommaSeparatedString_shouldReturnTags_as_list_withnull_name() {
		// given
		PropertyTagEntity tag = new PropertyTagEntity();
		tag.setName("tag1");
		
		PropertyTagEntity tag2 = new PropertyTagEntity();
		tag2.setName(null);
		
		List<PropertyTagEntity> propertyTags = new ArrayList<>();
		propertyTags.add(tag);
		propertyTags.add(tag2);
		
		// when
		String result = p.getTagsAsCommaSeparatedString(propertyTags);
		// then
		assertEquals("tag1,", result);
	}
	
	@Test
	public void getTagsAsCommaSeparatedString_shouldReturnTags_as_list_with_empty_name() {
		// given
		PropertyTagEntity tag = new PropertyTagEntity();
		tag.setName("tag1");
		
		PropertyTagEntity tag2 = new PropertyTagEntity();
		tag2.setName("");
		
		List<PropertyTagEntity> propertyTags = new ArrayList<>();
		propertyTags.add(tag);
		propertyTags.add(tag2);
		
		// when
		String result = p.getTagsAsCommaSeparatedString(propertyTags);
		// then
		assertEquals("tag1,", result);
	}
	
	@Test
	public void getTagsAsCommaSeparatedString_shouldReturnEmptyString() {
		// given
		
		List<PropertyTagEntity> propertyTags = new ArrayList<>();
		
		// when
		String result = p.getTagsAsCommaSeparatedString(propertyTags);
		// then
		assertEquals("", result);
	}
	@Test
	public void getTagsAsCommaSeparatedString_shouldReturnEmptyString_null() {
		// when
		String result = p.getTagsAsCommaSeparatedString(null);
		// then
		assertEquals("", result);
	}
	
	@Test
	public void getTagsAsList_shouldReturnTags_as_list() {
		// given
		PropertyTagEntity tag = new PropertyTagEntity();
		tag.setName("tag1");
		
		PropertyTagEntity tag2 = new PropertyTagEntity();
		tag2.setName("tag2");
		
		List<PropertyTagEntity> propertyTags = new ArrayList<>();
		propertyTags.add(tag);
		propertyTags.add(tag2);
		
		// when
		String result = p.getTagsAsList(propertyTags);
		// then
		assertEquals("'tag1', 'tag2'", result);
	}
	
	@Test
	public void getTagsAsList_shouldReturnTags_as_list_withnull_name() {
		// given
		PropertyTagEntity tag = new PropertyTagEntity();
		tag.setName("tag1");
		
		PropertyTagEntity tag2 = new PropertyTagEntity();
		tag2.setName(null);
		
		List<PropertyTagEntity> propertyTags = new ArrayList<>();
		propertyTags.add(tag);
		propertyTags.add(tag2);
		
		// when
		String result = p.getTagsAsList(propertyTags);
		// then
		assertEquals("'tag1'", result);
	}
	
	@Test
	public void getTagsAsList_shouldReturnTags_as_list_with_empty_name() {
		// given
		PropertyTagEntity tag = new PropertyTagEntity();
		tag.setName("tag1");
		
		PropertyTagEntity tag2 = new PropertyTagEntity();
		tag2.setName("");
		
		List<PropertyTagEntity> propertyTags = new ArrayList<>();
		propertyTags.add(tag);
		propertyTags.add(tag2);
		
		// when
		String result = p.getTagsAsList(propertyTags);
		// then
		assertEquals("'tag1'", result);
	}
	
	@Test
	public void getTagsAsList_shouldReturnEmptyString() {
		// given
		
		List<PropertyTagEntity> propertyTags = new ArrayList<>();
		
		// when
		String result = p.getTagsAsList(propertyTags);
		// then
		assertEquals("", result);
	}
	@Test
	public void getTagsAsList_shouldReturnEmptyString_null() {
		// when
		String result = p.getTagsAsList(null);
		// then
		assertEquals("", result);
	}

}
