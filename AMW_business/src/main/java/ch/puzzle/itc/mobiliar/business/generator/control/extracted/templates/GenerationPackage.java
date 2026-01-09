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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenerationPackage {

	private List<GenerationSubPackage> generationSubPackages = new ArrayList<GenerationSubPackage>();
	private GenerationOptions generationOptions;

	/**
	 * @returns a flattened Set of GenerationUnits of the Tree starting on the leaves
	 */
	public Set<GenerationUnit> getAsSet() {
		List<GenerationUnit> all = new ArrayList<GenerationUnit>();
		for (GenerationSubPackage subPackage : generationSubPackages) {
			all.addAll(subPackage.getSubGenerationUnitsAsList());
		}
		return new LinkedHashSet<>(removeDuplicates(all));
	}

	public void addGenerationSubPackage(GenerationSubPackage generationPackage) {
		this.generationSubPackages.add(generationPackage);
		restructureTree();
	}

	/**
	 * @return all Application GenerationUnit Sets
	 */
	public Map<ResourceEntity, Set<GenerationUnit>> getAppGenerationBatches() {
		List<GenerationSubPackage> result = new ArrayList<GenerationSubPackage>();

		// Applications with consumed Resources are Added as generationSubPackage
		for (GenerationSubPackage generationPackage : generationSubPackages) {
			if (generationPackage.getPackageGenerationUnit().getSlaveResource().getResourceType()
					.isApplicationResourceType()) {
				result.add(generationPackage);
			}
		}

		Map<ResourceEntity, Set<GenerationUnit>> map = new LinkedHashMap<>();

		for (GenerationSubPackage generationSubPackage : result) {
			ResourceEntity resource = generationSubPackage.getPackageGenerationUnit().getSlaveResource();
			map.put(resource, new LinkedHashSet<>(generationSubPackage.getSubGenerationUnitsAsList()));
		}

		List<GenerationSubPackage> applicationServerSubPackages = getApplicationServerSubPackages();
		// also add empty Applications attached on the ApplicationServer, but without a SubPackage
		for (GenerationSubPackage generationSubPackage : applicationServerSubPackages) {
			for (GenerationUnit gu : generationSubPackage.getGenerationUnits()) {
				if (gu.getSlaveResource().getResourceType().isApplicationResourceType()) {
					if (!map.containsKey(gu.getSlaveResource())) {
						Set<GenerationUnit> units = new LinkedHashSet<>();
						units.add(gu);
						map.put(gu.getSlaveResource(), units);
					}
				}
			}
		}

		return map;
	}

	public Set<GenerationUnit> getAsWithGivenNode(ResourceEntity node) {

		Set<GenerationUnit> result = new LinkedHashSet<>();

		List<GenerationSubPackage> applicationServerSubPackages = getApplicationServerSubPackages();
		// GenerationSubPackage node = null;
		// List<GenerationSubPackage> otherResources = new ArrayList<>();
		for (GenerationSubPackage generationSubPackage : applicationServerSubPackages) {

			GenerationUnit nodeUnit = null;
			GenerationUnit runtimeUnit = null;
			List<GenerationUnit> otherUnits = new ArrayList<>();
		    for (GenerationUnit unit : generationSubPackage.getGenerationUnits()) {
				if (unit.getSlaveResource() != null && unit.getSlaveResource().getResourceType() != null) {
					if (unit.getSlaveResource().getId() != null
							&& unit.getSlaveResource().getId().equals(node.getId())) {
						nodeUnit = unit;
					}
					else if (unit.getSlaveResource().getResourceType().isRuntimeType()) {
						runtimeUnit = unit;
						// Applications have been processed in an earlier step. We exclude them to
						// prevent a duplication of templates.
					}
					else if (!unit.getSlaveResource().getResourceType().isApplicationResourceType()) {
						otherUnits.add(unit);
					}
				}
			}
			// add other resources
			result.addAll(otherUnits);

			// add Node
			if (nodeUnit != null) {
				result.add(nodeUnit);
			}
			// add Application Server
			result.add(generationSubPackage.getPackageGenerationUnit());

			// add runtime unit
			if (runtimeUnit != null) {
				result.add(runtimeUnit);
			}
		}
		return result;
	}

	List<GenerationSubPackage> getApplicationServerSubPackages() {
		List<GenerationSubPackage> result = new ArrayList<GenerationSubPackage>();
		for (GenerationSubPackage generationPackage : generationSubPackages) {
			if (generationPackage.getPackageGenerationUnit().getSlaveResource().getResourceType()
					.isApplicationServerResourceType()) {
				result.add(generationPackage);
			}
		}
		return result;
	}

	/**
	 * @return the Generation Units for Nodes
	 */
	public Set<GenerationUnit> getNodeGenerationUnits() {
		Set<GenerationUnit> all = getAsSet();
		Set<GenerationUnit> result = new LinkedHashSet<>();
		for (GenerationUnit generationUnit : all) {
			if (generationUnit.getSlaveResource().getResourceType().isNodeResourceType()) {
				result.add(generationUnit);
			}
		}

		return result;
	}

	public List<GenerationSubPackage> getGenerationSubPackages() {
		return generationSubPackages;
	}

	/**
	 * Places the subPackages as Children of each other
	 */
	private void restructureTree() {
		for (GenerationSubPackage generationPackage : generationSubPackages) {
			placeInSubPackage(generationPackage);
		}
	}

	private void placeInSubPackage(GenerationSubPackage subPackage) {
		for (GenerationSubPackage generationPackage : generationSubPackages) {
			if (!generationPackage.equals(subPackage)) {
				generationPackage.place(subPackage);
			}
		}

	}

	public static List<GenerationUnit> removeDuplicates(List<GenerationUnit> gus) {
		List<GenerationUnit> gusRet = new ArrayList<GenerationUnit>();
		for (GenerationUnit generationUnit : gus) {
			if (!GenerationPackage.isAlreadyInList(generationUnit, gusRet)) {
				gusRet.add(generationUnit);
			}
		}
		return gusRet;
	}

	private static boolean isAlreadyInList(GenerationUnit generationUnit,
			List<GenerationUnit> generationUnits) {

		for (GenerationUnit gu : generationUnits) {
			// check if for this Resource in this Position is already a GenerationUnit in the
			// list.
			if (gu != null
					&& generationUnit != null
					// TODO Remove ApplicationType check, this check is because before the refactoring it
					// appeared also
					&& generationUnit.getSlaveResource().getResourceType().isApplicationResourceType()
					&& gu.getSlaveResource().equals(generationUnit.getSlaveResource())
					&& hasSameParent(gu, generationUnit)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasSameParent(GenerationUnit gu1, GenerationUnit gu2) {
		if (gu1.getResource() == null && gu2.getResource() == null) {
			return true;
		}
		if (gu1.getResource() == null && gu2.getResource() != null) {
			return true;
		}
		if (gu1.getResource() != null && gu2.getResource() == null) {
			return false;
		}

		return gu1.getResource().equals(gu2.getResource());
	}

	@Override
	public String toString() {
		return "GenerationPackage [generationSubPackages=" + generationSubPackages + "]";
	}

	public void setGenerationOptions(GenerationOptions options) {
		this.generationOptions = options;

	}

	public GenerationOptions getGenerationOptions() {
		return generationOptions;
	}

}
