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

import java.util.ArrayList;
import java.util.List;

public class GenerationSubPackage {
	
	private GenerationUnit packageGenerationUnit;
	
	private List<GenerationUnit> generationUnits = new ArrayList<GenerationUnit>();
	
	private List<GenerationSubPackage> generationSubPackages = new ArrayList<GenerationSubPackage>();
	
	public void addGenerationUnit(GenerationUnit generationUnit) {
		if(generationUnit != null){
			generationUnits.add(generationUnit);
		}
	}
	
	public List<GenerationUnit> getGenerationUnits() {
		return generationUnits;
	}

	/**
	 * Recursive Method to get all GenerationUnits as List including the resourceGenerationUnit
	 * 
	 * @return List of all GenerationUnits starting with the leaves
	 */
	public List<GenerationUnit> getSubGenerationUnitsAsList() {
		List<GenerationUnit> gus = new ArrayList<GenerationUnit>();
		if(generationSubPackages != null){
			for (GenerationSubPackage generationSubPackage : generationSubPackages) {
				gus.addAll(generationSubPackage.getSubGenerationUnitsAsList());
			}
		}
		gus.addAll(generationUnits);
		if(packageGenerationUnit != null){
			gus.add(packageGenerationUnit);
		}
		return GenerationPackage.removeDuplicates(gus);
	}

	public void place(GenerationSubPackage subPackage) {
		
		for (GenerationUnit generationUnit : generationUnits) {
			// add to Subpackages if on correct position
			if(generationUnit.getSlaveResource().equals(subPackage.getPackageGenerationUnit().getSlaveResource())){
				generationSubPackages.add(subPackage);
			}
		}
		
		if(generationSubPackages != null){
			for (GenerationSubPackage generationSubPackage : generationSubPackages) {
				generationSubPackage.place(subPackage);
			}
		}
	}
	
	public GenerationUnit getPackageGenerationUnit() {
		return packageGenerationUnit;
	}

	public void setPackageGenerationUnit(GenerationUnit packageGenerationUnit) {
		this.packageGenerationUnit = packageGenerationUnit;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((generationSubPackages == null) ? 0 : generationSubPackages.hashCode());
		result = prime * result + ((generationUnits == null) ? 0 : generationUnits.hashCode());
		result = prime * result + ((packageGenerationUnit == null) ? 0 : packageGenerationUnit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GenerationSubPackage other = (GenerationSubPackage) obj;
		if (generationSubPackages == null) {
			if (other.generationSubPackages != null) {
				return false;
			}
		}
		else if (!generationSubPackages.equals(other.generationSubPackages)) {
			return false;
		}
		if (generationUnits == null) {
			if (other.generationUnits != null) {
				return false;
			}
		}
		else if (!generationUnits.equals(other.generationUnits)) {
			return false;
		}
		if (packageGenerationUnit == null) {
			if (other.packageGenerationUnit != null) {
				return false;
			}
		}
		else if (!packageGenerationUnit.equals(other.packageGenerationUnit)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "GenerationSubPackage [packageGenerationUnit=" + packageGenerationUnit
				+ ", generationUnits=" + generationUnits + ", generationSubPackages="
				+ generationSubPackages + "]";
	}

	
}
