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

package ch.puzzle.itc.mobiliar.presentation.components.impl;

import ch.puzzle.itc.mobiliar.business.deploy.entity.ComparatorFilterOption;
import ch.puzzle.itc.mobiliar.business.deploy.entity.CustomFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class CustomFilterComp {

	private List<CustomFilter> selectedFilterList;

	private ComparatorFilterOption selectedComperator;

	private List<FilterSelectionItem> filterSelectionList;

	private String selectedFilterItemEnumName;

	public class FilterSelectionItem {
		private String enumName;

		private String displayName;
		public FilterSelectionItem(String enumName, String displayName) {
			this.enumName = enumName;
			this.displayName = displayName;
		}

		public String getEnumName() {
			return enumName;
		}

		public String getDisplayName() {
			return displayName;
		}

	}

	class FilterSelectionItemComparator implements Comparator<FilterSelectionItem> {
		@Override
		public int compare(FilterSelectionItem a, FilterSelectionItem b) {

			return a.getDisplayName().compareTo(b.getDisplayName());
		}
	}

	public boolean hasSelectedFiltersInList() {
		return !getSelectedFilterList().isEmpty();
	}

	public void removeAllFilter() {
		getSelectedFilterList().clear();
	}

	public boolean isFirstFilter(CustomFilter filter) {
		CustomFilter cusomFilter = getUpdatedSelectedFilterList().get(0);
		return cusomFilter.equals(filter);
	}
	
	public List<CustomFilter> getUpdatedSelectedFilterList() {
		List<CustomFilter> specialFilterAtEndOfList = new ArrayList<CustomFilter>();
		List<CustomFilter> specialFilterType = new ArrayList<CustomFilter>();

		for (CustomFilter filter : getSelectedFilterList()) {
			
			if (filter.isSpecialFilterType()) {
				specialFilterType.add(filter);
			} else {
				specialFilterAtEndOfList.add(setFilterDropDownOptions(filter));
			}
		}

		specialFilterAtEndOfList.addAll(specialFilterType);

		return specialFilterAtEndOfList;
	}

	public void removeFilter(CustomFilter filter) {

		List<CustomFilter> removeSelection = getSelectedFilterList();
		if (removeSelection.contains(filter)) {
			removeSelection.remove(filter);
		}
	}

	public List<CustomFilter> getSelectedFilterList() {
		if (selectedFilterList == null) {
			selectedFilterList = new ArrayList<CustomFilter>();
		}
		return selectedFilterList;
	}

	protected abstract CustomFilter setFilterDropDownOptions(CustomFilter filter);





	public void getSelectionComperatorChangeListener(CustomFilter filter) {
		ComparatorFilterOption actualSelectedComperator = getSelectedComperator();
		filter.setComparatorSelection(actualSelectedComperator);
	}

	public ComparatorFilterOption getSelectedComperator() {
		return selectedComperator;
	}
	public List<FilterSelectionItem> getFilterSelectionList() {
		if (filterSelectionList == null) {
			loadFilterList();
			Collections.sort(filterSelectionList, new FilterSelectionItemComparator());
		}

		return filterSelectionList;
	}

	protected abstract void loadFilterList();

	
	public void setSelectedFilterItemEnumName(String selectedFilterEnumName) {
		this.selectedFilterItemEnumName = selectedFilterEnumName;
	}
	
	public String getSelectedFilterItemEnumName() {
		return selectedFilterItemEnumName;
	}
	
	public void getSelectionChangeListener() {
		addSelectedFilter();
		selectedFilterItemEnumName = null;
	}

	
	public abstract void addSelectedFilter();


	public ComparatorFilterOption getActualSelectedComperator() {
		return selectedComperator;
	}

	public void setSelectedComperator(ComparatorFilterOption selectedComperator) {
		this.selectedComperator = selectedComperator;
	}

	protected boolean hasAlreadySpecialTypeFilter(List<CustomFilter> selectedFilterList) {
		for (CustomFilter deploymentFilter : selectedFilterList) {
			if (deploymentFilter.isSpecialFilterType()) {
				return true;
			}
		}
		return false;
	}

	protected void setFilterSelectionList(List<FilterSelectionItem> filterSelectionList) {
		this.filterSelectionList = filterSelectionList;
	}

}
