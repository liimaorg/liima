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

package ch.puzzle.itc.mobiliar.test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.System;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public class CustomLoggingTest {

	Logger log = Logger.getLogger(CustomLogging.class.getName());

	@BeforeEach
	public void before() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void test() throws IOException {
		String tmpDir = System.getProperty("java.io.tmpdir");
		new CustomLogging().setup(Level.FINEST, tmpDir + "/out.log");
		log.info("test");
	}

}
