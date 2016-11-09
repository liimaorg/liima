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

package ch.puzzle.itc.mobiliar.business.generator.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;


public class TemplateUtilsTest {

	private static final String ENCRYPTION_KEY = "78E76138D98F00BBF713136BC13DEE4B";
	private static final String ENCRYPT_TESTDATA_FILENAME = "src/test/resources/test-data/business/domain/generator/encrypt-testdata.txt";
	List<String> encryptStrings;

	@Before
	public void setUp() throws IOException {
		encryptStrings = new ArrayList<String>();

		Scanner scanner = new Scanner(new FileInputStream(ENCRYPT_TESTDATA_FILENAME), "UTF-8");
		try {
			while (scanner.hasNextLine()) {
				encryptStrings.add(scanner.nextLine());
			}
		}
		finally {
			scanner.close();
		}
	}
	
	@After
	public void tearDown(){
		// set Up Encription Key
		System.getProperties().remove(ConfigKey.ENCRYPTION_KEY.getValue());
	}

	@Test
	public void test_encrypt() {
		// given
		System.getProperties().put(ConfigKey.ENCRYPTION_KEY.getValue(), ENCRYPTION_KEY);
		// then
		assertEquals("FQkB54Qner8V/RfjV0XHGQ==", TemplateUtils.encrypt("test"));
		assertEquals("TBYA1lY8L9+fpD9NsdBjRg==", TemplateUtils.encrypt("äöü"));
		assertEquals("fVF4VCJ0EWvLi11aoFuNAA==", TemplateUtils.encrypt("_.%hdd"));
		assertEquals("SOaljN3np3UiLQGYJ5OQzw==", TemplateUtils.encrypt("è*;{}[]§"));
		assertEquals("ir6JtGptxKEfgbbJQN1KcQ==", TemplateUtils.encrypt("635654"));
		assertEquals(
				"n+igEaDyKnZCj1QFhGEMMyGcGYU/uPOqUJL8wJStX7KbZRj1N0iHwEYAHs+pl0CIk2KDDU4xuBVP"
						+ "X/RwkI4xcA8XKHecBODit9yRo8JEGgM8zdoHm5ni7aE/rWUItPJXzxFoar3gVye2USh0yIh89/d/"
						+ "k6FMomcc48jobNiwtcQbQHLGe8vxyY6016e7o63Sz1yd7zfWWzgwSZNgxgjpgr+syiEJbxSAHXrb"
						+ "CrmbGcnvj58xY8jffKdaRyh53P4/a+2T3IcRHtMpn5He0a+u+RP3PtK9QSwlXk4zI5Ud1cI=",
				TemplateUtils
						.encrypt("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
								+ "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam "
								+ "voluptua. At vero eos et accusam et justo duo dolores et ea rebum."));
	}

	@Test
	public void test_decrypt() {
		// given
		System.getProperties().put(ConfigKey.ENCRYPTION_KEY.getValue(), ENCRYPTION_KEY);
		// then
		assertEquals("test", TemplateUtils.decrypt("FQkB54Qner8V/RfjV0XHGQ==\r\n"));
		assertEquals("äöü", TemplateUtils.decrypt("TBYA1lY8L9+fpD9NsdBjRg==\r\n"));
		assertEquals("_.%hdd", TemplateUtils.decrypt("fVF4VCJ0EWvLi11aoFuNAA==\r\n"));
		assertEquals("è*;{}[]§", TemplateUtils.decrypt("SOaljN3np3UiLQGYJ5OQzw==\r\n"));
		assertEquals("635654", TemplateUtils.decrypt("ir6JtGptxKEfgbbJQN1KcQ==\r\n"));
		assertEquals("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
				+ "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam "
				+ "voluptua. At vero eos et accusam et justo duo dolores et ea rebum."
				,
				TemplateUtils
						.decrypt("n+igEaDyKnZCj1QFhGEMMyGcGYU/uPOqUJL8wJStX7KbZRj1N0iHwEYAHs+pl0CIk2KDDU4xuBVP\r\n"
								+ "X/RwkI4xcA8XKHecBODit9yRo8JEGgM8zdoHm5ni7aE/rWUItPJXzxFoar3gVye2USh0yIh89/d/\r\n"
								+ "k6FMomcc48jobNiwtcQbQHLGe8vxyY6016e7o63Sz1yd7zfWWzgwSZNgxgjpgr+syiEJbxSAHXrb\r\n"
								+ "CrmbGcnvj58xY8jffKdaRyh53P4/a+2T3IcRHtMpn5He0a+u+RP3PtK9QSwlXk4zI5Ud1cI=\r\n"));
		
		assertEquals(TemplateUtils.decrypt("FQkB54Qner8V/RfjV0XHGQ=="), TemplateUtils.decrypt("FQkB54Qner8V/RfjV0XHGQ==\r\n"));
		assertEquals(TemplateUtils.decrypt("n+igEaDyKnZCj1QFhGEMMyGcGYU/uPOqUJL8wJStX7KbZRj1N0iHwEYAHs+pl0CIk2KDDU4xuBVP"
				+ "X/RwkI4xcA8XKHecBODit9yRo8JEGgM8zdoHm5ni7aE/rWUItPJXzxFoar3gVye2USh0yIh89/d/"
				+ "k6FMomcc48jobNiwtcQbQHLGe8vxyY6016e7o63Sz1yd7zfWWzgwSZNgxgjpgr+syiEJbxSAHXrb"
				+ "CrmbGcnvj58xY8jffKdaRyh53P4/a+2T3IcRHtMpn5He0a+u+RP3PtK9QSwlXk4zI5Ud1cI="), 
				TemplateUtils.decrypt("n+igEaDyKnZCj1QFhGEMMyGcGYU/uPOqUJL8wJStX7KbZRj1N0iHwEYAHs+pl0CIk2KDDU4xuBVP\r\n"
				+ "X/RwkI4xcA8XKHecBODit9yRo8JEGgM8zdoHm5ni7aE/rWUItPJXzxFoar3gVye2USh0yIh89/d/\r\n"
				+ "k6FMomcc48jobNiwtcQbQHLGe8vxyY6016e7o63Sz1yd7zfWWzgwSZNgxgjpgr+syiEJbxSAHXrb\r\n"
				+ "CrmbGcnvj58xY8jffKdaRyh53P4/a+2T3IcRHtMpn5He0a+u+RP3PtK9QSwlXk4zI5Ud1cI=\r\n"));
	}

	@Test
	public void test_encrypt_decrypt() {
		// given
		System.getProperties().put(ConfigKey.ENCRYPTION_KEY.getValue(), ENCRYPTION_KEY);
		// then
		for (String str : encryptStrings) {
			String encrypted = TemplateUtils.encrypt(str);
			String decrypted = TemplateUtils.decrypt(encrypted);
			assertEquals(str, decrypted);
		}
	}
	
	@Test
	public void test_encrypt_nullvalue() {
		// then
		assertNull(TemplateUtils.encrypt(null));
	}
	
	@Test(expected=RuntimeException.class)
	public void test_encrypt_No_key() {
		// given
		System.getProperties().put(ConfigKey.ENCRYPTION_KEY.getValue(), null);
		// when
		TemplateUtils.encrypt("test");
	}
	
	@Test(expected=RuntimeException.class)
	public void test_encrypt_invalid_key() {
		// given
		System.getProperties().put(ConfigKey.ENCRYPTION_KEY.getValue(), "123");
		// when
		TemplateUtils.encrypt("test");
	}
	
	
	@Test
	public void test_decrypt_nullvalue() {
		// then
		assertNull(TemplateUtils.decrypt(null));
	}
	@Test(expected=RuntimeException.class)
	public void test_decrypt_No_key() {
		// given
		System.getProperties().put(ConfigKey.ENCRYPTION_KEY.getValue(), null);
		// when
		TemplateUtils.decrypt("test");
	}
	
	@Test(expected=RuntimeException.class)
	public void test_decrypt_invalid_key() {
		// given
		System.getProperties().put(ConfigKey.ENCRYPTION_KEY.getValue(), "123");
		// when
		TemplateUtils.decrypt("test");
	}
	
	@Test(expected=RuntimeException.class)
	public void test_decrypt_wrong_input() {
		// given
		System.getProperties().put(ConfigKey.ENCRYPTION_KEY.getValue(), ENCRYPTION_KEY);
		// when
		TemplateUtils.decrypt("test");
	}

}
