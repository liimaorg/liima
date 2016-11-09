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

import ch.puzzle.itc.mobiliar.business.generator.control.*;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import lombok.Getter;

import java.util.*;

public class ComparedGenerationResult {

	private final EnvironmentGenerationResult originalGenerationResult;

	private final EnvironmentGenerationResult comparisonGenerationResult;

	@Getter
	private final List<ResourceEntity> nodes = new ArrayList<ResourceEntity>();

	private final Map<ResourceEntity, List<ResourceGroupEntity>> applicationGroupsForNode = new HashMap<ResourceEntity, List<ResourceGroupEntity>>();

	private final Map<ResourceEntity, List<ComparedGeneratedTemplates>> appServerGeneratedResults = new HashMap<ResourceEntity, List<ComparedGeneratedTemplates>>();

	private final Map<ResourceEntity, Map<ResourceGroupEntity, List<ComparedGeneratedTemplates>>> applicationGeneratedResults = new HashMap<ResourceEntity, Map<ResourceGroupEntity, List<ComparedGeneratedTemplates>>>();

	public List<ComparedGeneratedTemplates> getAppServerResults(ResourceEntity node) {
		return appServerGeneratedResults.get(node);
	}

	public List<ResourceGroupEntity> getApplications(ResourceEntity node) {
		return applicationGroupsForNode.get(node);
	}

	public List<ComparedGeneratedTemplates> getApplicationResults(ResourceEntity node,
			ResourceGroupEntity app) {
		Map<ResourceGroupEntity, List<ComparedGeneratedTemplates>> applicationResults = applicationGeneratedResults
				.get(node);
		return applicationResults != null ? applicationResults.get(app) : null;
	}

	public ComparedGenerationResult(EnvironmentGenerationResult originalGenerationResult) {
		this(originalGenerationResult, null);
	}

	public ComparedGenerationResult(EnvironmentGenerationResult originalGenerationResult,
			EnvironmentGenerationResult comparisonGenerationResult) {
		this.comparisonGenerationResult = comparisonGenerationResult;
		this.originalGenerationResult = originalGenerationResult;
		mergeNodeGenerationResults();
		mergeApplicationServerTemplates();
	}

	void mergeNodeGenerationResults() {
		extractNodes(originalGenerationResult);
		if (comparisonGenerationResult != null) {
			extractNodes(comparisonGenerationResult);
		}
		Collections.sort(nodes);
	}
	
	public NodeGenerationResult getResultForNodeOriginal(ResourceEntity node){
		return getResultForNode(node, originalGenerationResult);
	}
	
	public NodeGenerationResult getResultForNodeCompare(ResourceEntity node){
		return getResultForNode(node, comparisonGenerationResult);
	}

	NodeGenerationResult getResultForNode(ResourceEntity node, EnvironmentGenerationResult result) {
		if (result != null) {
			for (NodeGenerationResult r : result.getNodeGenerationResults()) {
				if (r.getNode().equals(node)) {
					return r;
				}
			}
		}
		return null;
	}

	void mergeApplicationServerTemplates() {
		for (ResourceEntity node : nodes) {
			NodeGenerationResult nodeOriginal = getResultForNode(node, originalGenerationResult);
			NodeGenerationResult nodeCompare = getResultForNode(node, comparisonGenerationResult);
			List<GeneratedTemplate> asOriginal = getApplicationServerTemplates(nodeOriginal);
			List<GeneratedTemplate> asCompare = getApplicationServerTemplates(nodeCompare);
			appServerGeneratedResults.put(node, extractComparedTemplates(asOriginal, asCompare));
			Map<ResourceGroupEntity, List<ComparedGeneratedTemplates>> appTemplates = applicationGeneratedResults
					.get(node);
			Map<Integer, ResourceGroupEntity> applications = new LinkedHashMap<Integer, ResourceGroupEntity>();
			if (nodeOriginal != null) {
				for (ApplicationGenerationResult originalApps : nodeOriginal.getApplicationResults()) {
					applications.put(originalApps.getApplication().getResourceGroup().getId(),
							originalApps.getApplication().getResourceGroup());
				}
			}
			if (nodeCompare != null) {
				for (ApplicationGenerationResult compareApps : nodeCompare.getApplicationResults()) {
					applications.put(compareApps.getApplication().getResourceGroup().getId(),
							compareApps.getApplication().getResourceGroup());
				}
			}
			for (ResourceGroupEntity app : applications.values()) {
				ApplicationGenerationResult appOriginalResult = getApplicationResult(nodeOriginal, app);
				List<GeneratedTemplate> appOriginal = appOriginalResult == null ? Collections
						.<GeneratedTemplate> emptyList() : appOriginalResult.getGeneratedTemplates();
				ApplicationGenerationResult appCompareResult = getApplicationResult(nodeCompare, app);
				List<GeneratedTemplate> appCompare = appCompareResult == null ? Collections
						.<GeneratedTemplate> emptyList() : appCompareResult.getGeneratedTemplates();
				appTemplates.put(app, extractComparedTemplates(appOriginal, appCompare));
			}
			List<ResourceGroupEntity> appList = new ArrayList<ResourceGroupEntity>(applications.values());
			Collections.sort(appList);
			applicationGroupsForNode.put(node, appList);
		}
	}

	ApplicationGenerationResult getApplicationResult(NodeGenerationResult nodeResult, ResourceGroupEntity app) {
		if (nodeResult != null) {
			for (ApplicationGenerationResult asResult : nodeResult.getApplicationResults()) {
				if (asResult.getApplication().getResourceGroup().getId().equals(app.getId())) {
					return asResult;
				}
			}
		}
		return null;

	}

	List<GeneratedTemplate> getApplicationServerTemplates(NodeGenerationResult nodeResult) {
		if (nodeResult == null || nodeResult.getApplicationServerResults() == null
				|| nodeResult.getApplicationServerResults().isEmpty()) {
			return Collections.emptyList();
		}
		else {
			List<GeneratedTemplate> templates = new ArrayList<GeneratedTemplate>();
			for (GenerationUnitGenerationResult result : nodeResult.getApplicationServerResults()) {
				templates.addAll(result.getGeneratedTemplates());
			}
			return templates;
		}
	}

	void extractNodes(EnvironmentGenerationResult environmentResult) {
		if (environmentResult != null && environmentResult.getNodeGenerationResults() != null) {
			for (NodeGenerationResult result : environmentResult.getNodeGenerationResults()) {
				if (!nodes.contains(result.getNode())) {
					nodes.add(result.getNode());
				}
				Map<ResourceGroupEntity, List<ComparedGeneratedTemplates>> appTemplates = applicationGeneratedResults
						.get(result.getNode());
				if (appTemplates == null) {
					appTemplates = new HashMap<ResourceGroupEntity, List<ComparedGeneratedTemplates>>();
					applicationGeneratedResults.put(result.getNode(), appTemplates);
					for (ApplicationGenerationResult appResult : result.getApplicationResults()) {
						appTemplates.put(appResult.getApplication().getResourceGroup(), null);
					}
				}

			}
		}
	}

	List<ComparedGeneratedTemplates> extractComparedTemplates(List<GeneratedTemplate> original,
			List<GeneratedTemplate> compared) {
		List<ComparedGeneratedTemplates> results = new ArrayList<ComparedGeneratedTemplates>();
		Map<String, ComparedGeneratedTemplates> lookupMap = new HashMap<String, ComparedGeneratedTemplates>();
		for (GeneratedTemplate orig : original) {
			if (!lookupMap.containsKey(orig.getPath())) {
				ComparedGeneratedTemplates t = new ComparedGeneratedTemplates(orig.getPath());
				t.setOriginalTemplate(orig);
				results.add(t);
				lookupMap.put(orig.getPath(), t);
			}
		}
		for (GeneratedTemplate comp : compared) {
			ComparedGeneratedTemplates t = lookupMap.get(comp.getPath());
			if (t == null) {
				t = new ComparedGeneratedTemplates(comp.getPath());
				results.add(t);
				lookupMap.put(comp.getPath(), t);
			}
			t.setComparedTemplate(comp);
		}
		Collections.sort(results);
		return results;
	}

	public boolean isCompareMode() {
		return comparisonGenerationResult != null;
	}

}
