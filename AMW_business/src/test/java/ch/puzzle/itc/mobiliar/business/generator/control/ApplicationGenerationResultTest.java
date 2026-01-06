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

package ch.puzzle.itc.mobiliar.business.generator.control;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationGenerationResultTest {

	@Test
	public void should_NothaveErrors_non_added() {
		// given
		ApplicationGenerationResult result = new ApplicationGenerationResult();
		// when
		
		// then
		assertFalse(result.hasErrors());
		assertEquals("", result.getErrorMessageAsString());
	}
	
	@Test
	public void should_haveUnsucessfull_UnitResults_added() {
		// given
		ApplicationGenerationResult result = new ApplicationGenerationResult();

		GenerationUnitGenerationResult unitResult = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult.isSuccess()).thenReturn(Boolean.FALSE);
		
		List<GenerationUnitGenerationResult> generationResults = new ArrayList<GenerationUnitGenerationResult>();
		generationResults.add(unitResult);
		result.setGenerationResults(generationResults);
		// when
		
		// then
		assertTrue(result.hasErrors());
	}
	
	@Test
	public void should_haveSucessfull_UnitResults_added() {
		// given
		ApplicationGenerationResult result = new ApplicationGenerationResult();

		GenerationUnitGenerationResult unitResult = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult.isSuccess()).thenReturn(Boolean.TRUE);
		
		List<GenerationUnitGenerationResult> generationResults = new ArrayList<GenerationUnitGenerationResult>();
		generationResults.add(unitResult);
		result.setGenerationResults(generationResults);
		// when
		
		// then
		assertFalse(result.hasErrors());
		assertEquals("", result.getErrorMessageAsString());
	}
	
	@Test
	public void should_haveUnsucessfull_UnitResults_added_but_first_is_ok() {
		// given
		ApplicationGenerationResult result = new ApplicationGenerationResult();

		GenerationUnitGenerationResult unitResult = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult.isSuccess()).thenReturn(Boolean.TRUE);
		
		GenerationUnitGenerationResult unitResult2 = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult2.isSuccess()).thenReturn(Boolean.FALSE);
		
		List<GenerationUnitGenerationResult> generationResults = new ArrayList<GenerationUnitGenerationResult>();
		generationResults.add(unitResult);
		generationResults.add(unitResult2);
		result.setGenerationResults(generationResults);
		// when
		
		// then
		assertTrue(result.hasErrors());
	}
	
	@Test
	public void should_return_errorMessage() {
		// given
		ApplicationGenerationResult result = new ApplicationGenerationResult();

		GenerationUnitGenerationResult unitResult = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult.getErrorMessageAsString()).thenReturn("error1\n");
		
		GenerationUnitGenerationResult unitResult2 = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult2.getErrorMessageAsString()).thenReturn("error2\n");
		
		List<GenerationUnitGenerationResult> generationResults = new ArrayList<GenerationUnitGenerationResult>();
		generationResults.add(unitResult);
		generationResults.add(unitResult2);
		result.setGenerationResults(generationResults);
		// when
		String message = result.getErrorMessageAsString();
		// then
		
		String expected = "Application Template Errors\n"
				+ "error1\n"
				+ "error2\n";
		
		assertEquals(expected, message);
	}
	
	@Test
	public void should_return_errorMessage_only_one_unsuccessfull() {
		// given
		ApplicationGenerationResult result = new ApplicationGenerationResult();

		GenerationUnitGenerationResult unitResult = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult.isSuccess()).thenReturn(Boolean.TRUE);
		
		GenerationUnitGenerationResult unitResult2 = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult2.getErrorMessageAsString()).thenReturn("error2\n");
		
		List<GenerationUnitGenerationResult> generationResults = new ArrayList<GenerationUnitGenerationResult>();
		generationResults.add(unitResult);
		generationResults.add(unitResult2);
		result.setGenerationResults(generationResults);
		// when
		String message = result.getErrorMessageAsString();
		// then
		
		String expected = "Application Template Errors\n"
				+ "error2\n";
		
		assertEquals(expected, message);
	}
	
	@Test
	public void should_return_no_templates() {
		// given
		ApplicationGenerationResult result = new ApplicationGenerationResult();
		
		// then
		assertEquals(0, result.getGeneratedTemplates().size());
	}
	
	@Test
	public void should_return_templates() {
		// given
		ApplicationGenerationResult result = new ApplicationGenerationResult();
		
		GeneratedTemplate template1 = new GeneratedTemplate("test", "test/test", "content");
		List<GeneratedTemplate> templates1 = new ArrayList<GeneratedTemplate>();
		templates1.add(template1);
		
		GeneratedTemplate template2 = new GeneratedTemplate("test2", "test/test2", "content2");
		List<GeneratedTemplate> templates2 = new ArrayList<GeneratedTemplate>();
		templates2.add(template2);
		
		GenerationUnitGenerationResult unitResult = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult.getGeneratedTemplates()).thenReturn(templates1);
		
		GenerationUnitGenerationResult unitResult2 = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult2.getGeneratedTemplates()).thenReturn(templates2);
		
		List<GenerationUnitGenerationResult> generationResults = new ArrayList<GenerationUnitGenerationResult>();
		generationResults.add(unitResult);
		generationResults.add(unitResult2);
		result.setGenerationResults(generationResults);
		
		// when
		List<GeneratedTemplate> templatesResult = result.getGeneratedTemplates();
		
		// then
		assertEquals(2, templatesResult.size());
		assertEquals(template1, templatesResult.get(0));
		assertEquals(template2, templatesResult.get(1));
	}
}
