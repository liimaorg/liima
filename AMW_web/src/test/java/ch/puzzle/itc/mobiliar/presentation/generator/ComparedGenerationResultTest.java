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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.puzzle.itc.mobiliar.business.generator.control.ApplicationGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.EnvironmentGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.generator.control.GenerationUnitGenerationResult;
import ch.puzzle.itc.mobiliar.business.generator.control.NodeGenerationResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

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
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
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
		Assert.assertEquals(2, templates.size());
		
		//Only templates A and B are returned, since templC and templD belong to the application
		Assert.assertTrue(templates.get(0).getOriginalTemplate() == templA);
		Assert.assertTrue(templates.get(1).getOriginalTemplate() == templB);
		
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
		Assert.assertEquals(2, templates.size());
		
		//Only templates C and D are returned, since templA and templB belong to the application server
		Assert.assertTrue(templates.get(0).getOriginalTemplate() == templC);
		Assert.assertTrue(templates.get(1).getOriginalTemplate() == templD);
		
	}
	

	@Test
	public void testGetApplications() {
		result = new ComparedGenerationResult(originalResult);
		
		List<ResourceGroupEntity> res = result.getApplications(nodeResult.getNode());
		
		Assert.assertEquals(1, res.size());
		Assert.assertTrue(appGrp == res.get(0));		
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
		Mockito.when(node.compareTo(Mockito.any(ResourceEntity.class))).thenCallRealMethod();
		Mockito.when(result.getNode()).thenReturn(node);
		return result;
	}
	
	@Test
	public void testMergeNodeGenerationResults() {
		result = new ComparedGenerationResult(originalResult);		
		NodeGenerationResult nodeResult2 = createNewNodeGenerationResultWithNode();
		Mockito.when(nodeResult.getNode().getName()).thenReturn("bbb");		
		Mockito.when(nodeResult2.getNode().getName()).thenReturn("aaa");
		nodeResultList.add(nodeResult2);
		ComparedGenerationResult spiedResult = Mockito.spy(result);
		
		//when
		spiedResult.mergeNodeGenerationResults();	
		
		//then
		Mockito.verify(spiedResult).extractNodes(originalResult);
		List<ResourceEntity> nodes = result.getNodes();
		Assert.assertEquals(2, nodes.size());
				
		//Assure correct order
		Assert.assertTrue(nodeResult2.getNode()==nodes.get(0));
		Assert.assertTrue(nodeResult.getNode()==nodes.get(1));		
	}

	@Test
	public void testGetResultForNode() {
		result = new ComparedGenerationResult(originalResult);						
		NodeGenerationResult newResult = result.getResultForNode(nodeResult.getNode(), originalResult);		
		Assert.assertTrue(newResult == this.nodeResult);
	}

	@Test
	public void testGetApplicationResult() {
		result = new ComparedGenerationResult(originalResult);		
		ApplicationGenerationResult res = result.getApplicationResult(nodeResult, appGrp);
		Assert.assertTrue(res == appResult);
	}

	@Test
	public void testGetApplicationServerTemplates() {
		result = new ComparedGenerationResult(originalResult);				
		GeneratedTemplate templ = Mockito.mock(GeneratedTemplate.class);
		Mockito.when(appServerResult.getGeneratedTemplates()).thenReturn(Arrays.asList(templ));
		
		List<GeneratedTemplate> templates = result.getApplicationServerTemplates(nodeResult);		
		
		Assert.assertEquals(1, templates.size());
		Assert.assertTrue(templ == templates.get(0));
	}

	@Test
	public void testExtractNodes() {
		result = new ComparedGenerationResult(originalResult);
				
		//when
		result.extractNodes(originalResult);		
		
		//then
		Assert.assertTrue(result.getNodes().contains(nodeResult.getNode()));
		Assert.assertTrue(appResult == result.getApplicationResult(nodeResult, appGrp));		
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
		Assert.assertEquals(3, templates.size());
		
		Assert.assertEquals("templateA", templates.get(0).getPath());
		Assert.assertEquals("templateB", templates.get(1).getPath());
		Assert.assertEquals("templateC", templates.get(2).getPath());
		
		Assert.assertTrue(originalA == templates.get(0).getOriginalTemplate());
		Assert.assertTrue(compareA == templates.get(0).getComparedTemplate());
		
		Assert.assertTrue(originalB == templates.get(1).getOriginalTemplate());
		Assert.assertNull(templates.get(1).getComparedTemplate());
		
		Assert.assertNull(templates.get(2).getOriginalTemplate());
		Assert.assertTrue(compareC == templates.get(2).getComparedTemplate());
	}

	@Test
	public void testIsCompareModeFalse() {
		result = new ComparedGenerationResult(originalResult);
		Assert.assertFalse(result.isCompareMode());
	}
	
	@Test
	public void testIsCompareModeTrue() {
		result = new ComparedGenerationResult(originalResult, comparisonResult);
		Assert.assertTrue(result.isCompareMode());
	}

}
