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

package ch.puzzle.itc.mobiliar.business.shakedown.entity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ShakedownStpEntityTest {
	ShakedownStpEntity shakedownStpEntity;
	
	@Before
	public void initTest(){
		shakedownStpEntity = new ShakedownStpEntity();
		shakedownStpEntity.setStpName("STP");
		shakedownStpEntity.setVersion("Version");
	}
	
	@Test
	public void addParameter(){
		//when
		shakedownStpEntity.addParameter("param1");
		// then
		assertFalse(shakedownStpEntity.getComaSeperatedParameters().isEmpty());
		assertEquals("param1", shakedownStpEntity.getComaSeperatedParameters());
	}
	
	@Test
	public void add2Parameter(){
		//when
		shakedownStpEntity.addParameter("param1");
		shakedownStpEntity.addParameter("param2");
		// then
		assertFalse(shakedownStpEntity.getComaSeperatedParameters().isEmpty());
		assertEquals("param1,param2", shakedownStpEntity.getComaSeperatedParameters());
	}
	
	@Test
	public void addTwiceSameParameter(){
		
		// when
		shakedownStpEntity.addParameter("param1");
		shakedownStpEntity.addParameter("param1");
		// then
		assertFalse(shakedownStpEntity.getComaSeperatedParameters().isEmpty());
		assertEquals("param1", shakedownStpEntity.getComaSeperatedParameters());
		
	}
	
	
}
