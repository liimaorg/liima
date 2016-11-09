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
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.puzzle.itc.mobiliar.test.testrunner.WeldJUnit4Runner;

@RunWith(WeldJUnit4Runner.class)
public class ExampleWeldTest {

	// The Component to be tested
	@InjectMocks
	@Inject
	ExampleService exampleService;
	
	// Mock service
	@Mock
	ExampleService2 exampleService2;
	
	// inject real Bean
	@Inject
	ExampleService3 exampleService3;
	
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void test() {
		// given
		
		String value = "value";
		
		// the ExampleService2 call is mocked
		when(exampleService2.executeBusinessFunctionality(value)).thenReturn(value + "ExampleService2MockedAnswer");
		
		// when
		
		String result = exampleService.executeBusinessFunctionality(value);
		
		// then
		assertNotNull(result);
		assertEquals(value+":" + value + "ExampleService2MockedAnswer" +":"+value + "ExampleService3" ,result);
	}

}
