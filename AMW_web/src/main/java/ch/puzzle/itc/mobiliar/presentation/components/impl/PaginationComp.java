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

import java.util.ArrayList;
import java.util.List;

/**
 * Pagination Component
 */
public abstract class PaginationComp {

	private int currentPage = 0;
	private int pageSize = 10;

	/**
	 * Jump to first page
	 */
	public void firstPage() {
		currentPage = 0;
		reloadData();
	}

	/**
	 * Jump to previous page (if available)
	 */
	public void previousPage() {
		currentPage = Math.max(0, currentPage - 1);
		reloadData();
	}

	/**
	 * Jump to a specific page
	 * 
	 * @param page
	 */
	public void goToPage(Integer page) {
		currentPage = Math.min(Math.max(0, page), getNumberOfPages() - 1);
		reloadData();
	}

	/**
	 * Jump to the next page (if available)
	 */
	public void nextPage() {
		currentPage = Math.min(currentPage + 1, getNumberOfPages());
		reloadData();
	}

	/**
	 * Jump to the last page (if available)
	 */
	public void lastPage() {
		currentPage = getNumberOfPages() - 1;
		reloadData();
	}

	/**
	 * @return the number of available pages
	 */
	public int getNumberOfPages() {
		return getTotalCount() / pageSize + (getTotalCount() % pageSize > 0 ? 1 : 0);
	}

	/**
	 * Calculates the start-index based on the current page as well as the page size
	 * 
	 * @return
	 */
	public int getStartIndex() {
		return currentPage * pageSize;
	}

	/**
	 * @param pageSize
	 */
	public void setPageSize(int pageSize) {
		if (pageSize < 0) {
			pageSize = 0;
		}
		if (pageSize != this.pageSize) {
			this.pageSize = pageSize;
			this.currentPage = 0;
			reloadData();
		}
	}

	/**
	 * @return list with available pages
	 */
	public List<Integer> availablePages() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(0);
		for (int i = 1; i < getNumberOfPages(); i++) {
			list.add(i);
		}
		return list;
	}

	/**
	 * @return true if current page is the last one
	 */
	public boolean isLastPage() {
		return currentPage >= getNumberOfPages() - 1;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * Load data for current page
	 */
	public abstract void reloadData();

	/**
	 * @return total amount of data
	 */
	public abstract int getTotalCount();

	// ////////////////////////////////////////////////


	/**
	 * @return the label of the current page (+1 of the page-value because we start at an index of 0)
	 */
	public int getPageLabel() {
		return currentPage + 1;
	}

}
