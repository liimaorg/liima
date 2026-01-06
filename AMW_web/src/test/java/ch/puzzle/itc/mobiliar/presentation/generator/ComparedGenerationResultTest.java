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

package ch.puzzle.itc.mobiliar.presentation.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.puzzle.itc.mobiliar.business.generator.control.ApplicationGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.EnvironmentGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.generator.control.GenerationUnitGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.NodeGenerationResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

@ExtendWith(MockitoExtension.class)
public class ComparedGenerationResultTest {

	ComparedGenerationResult result;

	@Mock
	EnvironmentGenerationResult originalResult;
	
	@Mock
	EnvironmentGenerationResult comparisonResult;
		
	NodeGenerationResult nodeResult;
	
	@Mock
	ApplicationGenerationResult appResult;
	
	@Mock
	GenerationUnitGenerationResult appServerResult;
	
	@Mock
	ResourceEntity app;
	
	@Mock
	ResourceGroupEntity appGrp;
	
	List<NodeGenerationResult> nodeResultList;
	
	@BeforeEach
	public void setUp() throws Exception {
		nodeResult = createNewNodeGenerationResultWithNode();
		nodeResultList = new ArrayList<NodeGenerationResult>();
		nodeResultList.add(nodeResult);
		Mockito.when(originalResult.getNodeGenerationResults()).thenReturn(nodeResultList);		
		List<ApplicationGenerationResult> applicationGenerationResult = new ArrayList<ApplicationGenerationResult>();
		applicationGenerationResult.add(appResult);		
		List<GenerationUnitGenerationResult> appServerResults = new ArrayList<GenerationUnitGenerationResult>();
		appServerResults.add(appServerResult);		
		Mockito.when(appResult.getApplication()).thenReturn(app);
		Mockito.when(app.getResourceGroup()).thenReturn(appGrp);		
		Mockito.when(nodeResult.getApplicationResults()).thenReturn(applicationGenerationResult);	
		Mockito.when(nodeResult.getApplicationServerResults()).thenReturn(appServerResults);
	}

	@Test
	public void testGetAppServerResults() {
		GeneratedTemplate templA = Mockito.mock(GeneratedTemplate.class);
		Mockito.when(templA.getPath()).thenReturn("templA");		
		GeneratedTemplate templB = Mockito.mock(GeneratedTemplate.class);				
		Mockito.when(templB.getPath()).thenReturn("templB");	
		GeneratedTemplate templC = Mockito.mock(GeneratedTemplate.class);	
		Mockito.when(templC.getPath()).thenReturn("templC");	
		GeneratedTemplate templD = Mockito.mock(GeneratedTemplate.class);
		Mockito.when(templD.getPath()).thenReturn("templD");	
		Mockito.when(appServerResult.getGeneratedTemplates()).thenReturn(Arrays.asList(templA, templB));		
		Mockito.when(appResult.getGeneratedTemplates()).thenReturn(Arrays.asList(templC, templD));		
		
		result = new ComparedGenerationResult(originalResult);
		
		List<ComparedGeneratedTemplates> templates = result.getAppServerResults(nodeResult.getNode());
		assertEquals(2, templates.size());
		
		//Only templates A and B are returned, since templC and templD belong to the application
		assertTrue(templates.get(0).getOriginalTemplate() == templA);
		assertTrue(templates.get(1).getOriginalTemplate() == templB);
	}
	
	@Test
	public void testGetApplicationResults() {
		GeneratedTemplate templA = Mockito.mock(GeneratedTemplate.class);
		Mockito.when(templA.getPath()).thenReturn("templA");		
		GeneratedTemplate templB = Mockito.mock(GeneratedTemplate.class);				
		Mockito.when(templB.getPath()).thenReturn("templB");	
		GeneratedTemplate templC = Mockito.mock(GeneratedTemplate.class);	
		Mockito.when(templC.getPath()).thenReturn("templC");	
		GeneratedTemplate templD = Mockito.mock(GeneratedTemplate.class);
		Mockito.when(templD.getPath()).thenReturn("templD");	
		Mockito.when(appServerResult.getGeneratedTemplates()).thenReturn(Arrays.asList(templA, templB));		
		Mockito.when(appResult.getGeneratedTemplates()).thenReturn(Arrays.asList(templC, templD));		
		
		result = new ComparedGenerationResult(originalResult);
		
		List<ComparedGeneratedTemplates> templates = result.getApplicationResults(nodeResult.getNode(), appGrp);
		assertEquals(2, templates.size());
		
		//Only templates C and D are returned, since templA and templB belong to the application server
		assertTrue(templates.get(0).getOriginalTemplate() == templC);
		assertTrue(templates.get(1).getOriginalTemplate() == templD);	
	}

	@Test
	public void testGetApplications() {
		result = new ComparedGenerationResult(originalResult);
		
		List<ResourceGroupEntity> res = result.getApplications(nodeResult.getNode());
		
		assertEquals(1, res.size());
		assertTrue(appGrp == res.get(0));		
	}
	
	@Test
	public void testMergeNodeGenerationResultsForOriginalAndComparisonResult() {
		result = new ComparedGenerationResult(originalResult,comparisonResult);		
		ComparedGenerationResult spiedResult = Mockito.spy(result);		
		//when
		spiedResult.mergeNodeGenerationResults();			
		//then
		Mockito.verify(spiedResult).extractNodes(originalResult);
		Mockito.verify(spiedResult).extractNodes(comparisonResult);	
	}
	
	private NodeGenerationResult createNewNodeGenerationResultWithNode(){
		NodeGenerationResult result = Mockito.mock(NodeGenerationResult.class);
		ResourceEntity node = Mockito.mock(ResourceEntity.class);
		Mockito.when(result.getNode()).thenReturn(node);
		return result;
	}
	
	@Test
	public void testMergeNodeGenerationResults() {
		result = new ComparedGenerationResult(originalResult);		
		NodeGenerationResult nodeResult2 = createNewNodeGenerationResultWithNode();
		Mockito.when(nodeResult2.getNode().compareTo(Mockito.any(ResourceEntity.class))).thenCallRealMethod();
		Mockito.when(nodeResult.getNode().getName()).thenReturn("bbb");		
		Mockito.when(nodeResult2.getNode().getName()).thenReturn("aaa");
		nodeResultList.add(nodeResult2);
		ComparedGenerationResult spiedResult = Mockito.spy(result);
		
		//when
		spiedResult.mergeNodeGenerationResults();	
		
		//then
		Mockito.verify(spiedResult).extractNodes(originalResult);
		List<ResourceEntity> nodes = result.getNodes();
		assertEquals(2, nodes.size());
				
		//Assure correct order
		assertTrue(nodeResult2.getNode()==nodes.get(0));
		assertTrue(nodeResult.getNode()==nodes.get(1));		
	}

	@Test
	public void testGetResultForNode() {
		result = new ComparedGenerationResult(originalResult);						
		NodeGenerationResult newResult = result.getResultForNode(nodeResult.getNode(), originalResult);		
		assertTrue(newResult == this.nodeResult);
	}

	@Test
	public void testGetApplicationResult() {
		result = new ComparedGenerationResult(originalResult);		
		ApplicationGenerationResult res = result.getApplicationResult(nodeResult, appGrp);
		assertTrue(res == appResult);
	}

	@Test
	public void testGetApplicationServerTemplates() {
		result = new ComparedGenerationResult(originalResult);				
		GeneratedTemplate templ = Mockito.mock(GeneratedTemplate.class);
		Mockito.when(appServerResult.getGeneratedTemplates()).thenReturn(Arrays.asList(templ));
		
		List<GeneratedTemplate> templates = result.getApplicationServerTemplates(nodeResult);		
		
		assertEquals(1, templates.size());
		assertTrue(templ == templates.get(0));
	}

	@Test
	public void testExtractNodes() {
		result = new ComparedGenerationResult(originalResult);
				
		//when
		result.extractNodes(originalResult);		
		
		//then
		assertTrue(result.getNodes().contains(nodeResult.getNode()));
		assertTrue(appResult == result.getApplicationResult(nodeResult, appGrp));		
	}

	@Test
	public void testExtractComparedTemplates() {
		result = new ComparedGenerationResult(originalResult);
		
		//given
		GeneratedTemplate originalA = Mockito.mock(GeneratedTemplate.class);
		Mockito.when(originalA.getPath()).thenReturn("templateA");		
		GeneratedTemplate compareA = Mockito.mock(GeneratedTemplate.class);
		Mockito.when(compareA.getPath()).thenReturn("templateA");	
				
		GeneratedTemplate originalB = Mockito.mock(GeneratedTemplate.class);
		Mockito.when(originalB.getPath()).thenReturn("templateB");	
		
		GeneratedTemplate compareC = Mockito.mock(GeneratedTemplate.class);
		Mockito.when(compareC.getPath()).thenReturn("templateC");	
		
		//when		
		List<ComparedGeneratedTemplates> templates = result.extractComparedTemplates(Arrays.asList(originalB, originalA), Arrays.asList(compareA, compareC));
		
		//then
		assertEquals(3, templates.size());
		
		assertEquals("templateA", templates.get(0).getPath());
		assertEquals("templateB", templates.get(1).getPath());
		assertEquals("templateC", templates.get(2).getPath());
		
		assertTrue(originalA == templates.get(0).getOriginalTemplate());
		assertTrue(compareA == templates.get(0).getComparedTemplate());
		
		assertTrue(originalB == templates.get(1).getOriginalTemplate());
		assertNull(templates.get(1).getComparedTemplate());
		
		assertNull(templates.get(2).getOriginalTemplate());
		assertTrue(compareC == templates.get(2).getComparedTemplate());
	}

	@Test
	public void testIsCompareModeFalse() {
		result = new ComparedGenerationResult(originalResult);
		assertFalse(result.isCompareMode());
	}
	
	@Test
	public void testIsCompareModeTrue() {
		result = new ComparedGenerationResult(originalResult, comparisonResult);
		assertTrue(result.isCompareMode());
	}

}
