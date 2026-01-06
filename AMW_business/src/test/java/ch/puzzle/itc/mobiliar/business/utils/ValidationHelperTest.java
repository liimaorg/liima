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

package ch.puzzle.itc.mobiliar.business.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

public class ValidationHelperTest {

	@Test
	public void validateNotNullOrEmptyOnOneValidArgumentShouldValidate() throws ValidationException {
		// given
		String arg1 = "not empty argument1";
		// when
		ValidationHelper.validateNotNullOrEmptyChecked(arg1);
	}

	@Test
	public void validateNotNullOrEmptyOnManyValidArgumentsShouldValidate() throws ValidationException {
		// given
		String arg1 = "not empty argument1";
		String arg2 = "not empty argument2";
		String arg3 = "not empty argument3";

		// when
		ValidationHelper.validateNotNullOrEmptyChecked(arg1, arg2, arg3);
	}

	@Test
	public void validateNotNullOrEmptyOnOneEmptyArgumentShouldThrowException() {
		// given
		String arg1 = "";

		// when
		assertThrows(ValidationException.class, () -> {
			ValidationHelper.validateNotNullOrEmptyChecked(arg1);
		});
	}

	@Test
	public void validateNotNullOrEmptyOnOneNullArgumentShouldThrowException() {
		// given
		String arg1 = null;

		// when
		assertThrows(ValidationException.class, () -> {
			ValidationHelper.validateNotNullOrEmptyChecked(arg1);
		});
	}

	@Test
	public void validateNotNullOrEmptyOnAtLeastOneInvalidArgumentShouldThrowException() {
		// given
		String arg1 = "not empty argument1";
		String arg2 = "";
		String arg3 = "not empty argument3";

		// when
		assertThrows(ValidationException.class, () -> {
			ValidationHelper.validateNotNullOrEmptyChecked(arg1, arg2, arg3);
		});
	}
}