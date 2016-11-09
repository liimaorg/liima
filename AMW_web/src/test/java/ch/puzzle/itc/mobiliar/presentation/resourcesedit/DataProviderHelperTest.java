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

package ch.puzzle.itc.mobiliar.presentation.resourcesedit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class DataProviderHelperTest {

	private DataProviderHelper helper = new DataProviderHelper();

	@Test
	public void testNextFreeIdentifier2() {
		// without postfix
		assertEquals("db2", helper.nextFreeIdentifier(Collections.<String> emptyList(), "db2", null));

		// first with postfix
		List<String> list = new ArrayList<String>();
		list.add("db2");
		assertEquals("db2_1", helper.nextFreeIdentifier(list, "db2", null));

		// second with postfix
		list = new ArrayList<String>();
		list.add("db2");
		list.add("db2_4");
		assertEquals("db2_2", helper.nextFreeIdentifier(list, "db2", null));

		// null prefix
		assertEquals("_2", helper.nextFreeIdentifier(list, null, null));

		// to lower case
		assertEquals("node_2", helper.nextFreeIdentifier(list, "NODE", null));
	}
}
