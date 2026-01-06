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

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException.CAUSE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeGenerationResultTest {

	NodeGenerationResult result;
	
	List<TemplatePropertyException> templatePropertyExceptions;
	
	ResourceEntity node;
	
	GenerationUnitGenerationResult unitResult;
	
	GenerationUnitGenerationResult unitResult2;
	
	ApplicationGenerationResult appResult;
	
	ApplicationGenerationResult appResult2;
	
	GeneratedTemplate template1;
	
	GeneratedTemplate template2;
	
	
	List<GeneratedTemplate> templates1;
	List<GeneratedTemplate> templates2;
	
	@BeforeEach
	public void setUp(){
		result = new NodeGenerationResult();
		
		// exceptions
		templatePropertyExceptions = new ArrayList<TemplatePropertyException>();
		templatePropertyExceptions.add(new TemplatePropertyException("error", CAUSE.INVALID_PROPERTY));
		
		//node
		node = ResourceFactory.createNewResource();
		node.setName("node");
		
		// templates
		template1 = new GeneratedTemplate("test", "test/test", "content");
		templates1 = new ArrayList<GeneratedTemplate>();
		templates1.add(template1);
		
		template2 = new GeneratedTemplate("test2", "test/test2", "content2");
		templates2 = new ArrayList<GeneratedTemplate>();
		templates2.add(template2);
	}
	
	@Test
	public void should_NothaveErrors_non_added() {
		// given
		// when
		
		// then
		assertFalse(result.hasErrors());
		assertEquals("", result.getErrorMessage());
	}
	
	@Test
	public void should_haveErrors_PropertyValidation() {
		// given
		
		result.addAllPropertyValidationExceptions(templatePropertyExceptions);
		// when
		
		// then
		assertTrue(result.hasErrors());
	}
	
	@Test
	public void should_haveErrors_inAsResults() {
		// given
		unitResult = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult.isSuccess()).thenReturn(Boolean.FALSE);
		
		List<GenerationUnitGenerationResult> applicationServerResults = new ArrayList<GenerationUnitGenerationResult>();
		
		applicationServerResults.add(unitResult);
		
		result.setApplicationServerResults(applicationServerResults);
		// when
		
		// then
		assertTrue(result.hasErrors());
	}
	
	@Test
	public void should_haveErrors_inAsResults_if_the_second_is_unsucessful() {
		// given
		unitResult = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult.isSuccess()).thenReturn(Boolean.TRUE);
		
		unitResult2 = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult2.isSuccess()).thenReturn(Boolean.FALSE);
		
		List<GenerationUnitGenerationResult> applicationServerResults = new ArrayList<GenerationUnitGenerationResult>();
		
		applicationServerResults.add(unitResult);
		applicationServerResults.add(unitResult2);
		
		result.setApplicationServerResults(applicationServerResults);
		// when
		
		// then
		assertTrue(result.hasErrors());
	}
	
	@Test
	public void should_haveErrors_inAppResults() {
		// given
		appResult = Mockito.mock(ApplicationGenerationResult.class);
		Mockito.when(appResult.hasErrors()).thenReturn(Boolean.TRUE);
		
		List<ApplicationGenerationResult> applicationResults = new ArrayList<ApplicationGenerationResult>();
		
		applicationResults.add(appResult);
		
		result.setApplicationResults(applicationResults);
		// when
		
		// then
		assertTrue(result.hasErrors());
	}
	
	@Test
	public void should_haveErrors_inAppResults_if_the_second_is_unsucessful() {
		// given
		appResult = Mockito.mock(ApplicationGenerationResult.class);
		Mockito.when(appResult.hasErrors()).thenReturn(Boolean.FALSE);
		
		appResult2 = Mockito.mock(ApplicationGenerationResult.class);
		Mockito.when(appResult2.hasErrors()).thenReturn(Boolean.TRUE);
		
		List<ApplicationGenerationResult> applicationResults = new ArrayList<ApplicationGenerationResult>();
		
		applicationResults.add(appResult);
		applicationResults.add(appResult2);
		
		result.setApplicationResults(applicationResults);
		// when
		
		// then
		assertTrue(result.hasErrors());
	}
	
	@Test
	public void should_return_errorMessage() {
		// given
		result.setNode(node);
		result.addAllPropertyValidationExceptions(templatePropertyExceptions);
		
		ApplicationGenerationResult unitResult = Mockito.mock(ApplicationGenerationResult.class);
		Mockito.when(unitResult.hasErrors()).thenReturn(Boolean.TRUE);
		Mockito.when(unitResult.getErrorMessageAsString()).thenReturn("appError\n");
		
		GenerationUnitGenerationResult unitResult2 = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(unitResult2.isSuccess()).thenReturn(Boolean.FALSE);
		Mockito.when(unitResult2.getErrorMessageAsString()).thenReturn("asError\n");
		
		
		List<ApplicationGenerationResult> applicationResults = new ArrayList<ApplicationGenerationResult>();
		
		applicationResults.add(unitResult);
		result.setApplicationResults(applicationResults);
		
		List<GenerationUnitGenerationResult> applicationServerResults = new ArrayList<GenerationUnitGenerationResult>();
		
		applicationServerResults.add(unitResult2);
		result.setApplicationServerResults(applicationServerResults);
		// when
		String message = result.getErrorMessage();
		// then
		
		String expected = "Error on Node: " + node.getName() + "\n"
						+ "error\n"
						+ "asError\n"
						+ "appError\n";
		
		assertEquals(expected, message);
	}
	
	
	
	@Test
	public void should_return_no_templates() {
		// given
		
		// then
		assertEquals(0, result.getGeneratedTemplates().size());
	}
	
	@Test
	public void should_return_templates() {
		// given
		GenerationUnitGenerationResult asResult = Mockito.mock(GenerationUnitGenerationResult.class);
		Mockito.when(asResult.getGeneratedTemplates()).thenReturn(templates1);
		
		List<GenerationUnitGenerationResult> asResults = new ArrayList<GenerationUnitGenerationResult>();
		asResults.add(asResult);
		result.setApplicationServerResults(asResults);
		
		ApplicationGenerationResult appResult = Mockito.mock(ApplicationGenerationResult.class);
		Mockito.when(appResult.getGeneratedTemplates()).thenReturn(templates2);
		
		List<ApplicationGenerationResult> applicationResults = new ArrayList<ApplicationGenerationResult>();
		applicationResults.add(appResult);
		result.setApplicationResults(applicationResults );
		// when
		
		List<GeneratedTemplate> generatedTemplates = result.getGeneratedTemplates();
		
		// then
		assertEquals(2, generatedTemplates.size());
		assertEquals(template1, generatedTemplates.get(0));
		assertEquals(template2, generatedTemplates.get(1));
		
	}


    @Test
    public void should_omitTemplates(){
	   //given
	   GenerationUnitGenerationResult asResult = Mockito.mock(GenerationUnitGenerationResult.class);
	   Mockito.when(asResult.getGeneratedTemplates()).thenReturn(templates1);

	   List<GenerationUnitGenerationResult> asResults = new ArrayList<GenerationUnitGenerationResult>();
	   asResults.add(asResult);
	   result.setApplicationServerResults(asResults);

	   ApplicationGenerationResult appResult = Mockito.mock(ApplicationGenerationResult.class);
	   Mockito.when(appResult.getGeneratedTemplates()).thenReturn(templates2);

	   List<ApplicationGenerationResult> applicationResults = new ArrayList<ApplicationGenerationResult>();
	   applicationResults.add(appResult);
	   result.setApplicationResults(applicationResults );


	   assertFalse(template1.isOmitted());
	   assertFalse(template2.isOmitted());
	   //when
	   result.omitAllTemplates();
	   //then
	   assertTrue(template1.isOmitted());
	   assertTrue(template2.isOmitted());
	   assertTrue(template1.getContent().contains("omitted"));
    }

}
