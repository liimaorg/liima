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

package ch.puzzle.itc.mobiliar.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RuntimeEnvironment.class)
public class ConfigurationServiceTest {

	@Before
	public void setUp(){
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.LOGS_PATH.getValue(), "/dev/");
		props.setProperty(ConfigKey.DEPLOYMENT_IN_PROGRESS_TIMEOUT.getValue(), "3600");
		props.setProperty(ConfigKey.DELIVER_MAIL.getValue(), "true");
		//used for booleanTests
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "true");

		PowerMockito.mockStatic(RuntimeEnvironment.class);
		Mockito.when(RuntimeEnvironment.getValueOfEnvironmentVariable(ConfigKey.EXTERNAL_RESOURCE_BACKLINK_HOST.getEnvName())).thenReturn("envvalue");
	}
	
	@After
	public void tearDown(){
		Properties props = System.getProperties();
		props.remove(ConfigKey.LOGS_PATH.getValue());
		props.remove(ConfigKey.DEPLOYMENT_IN_PROGRESS_TIMEOUT.getValue());
		props.remove(ConfigKey.DELIVER_MAIL.getValue());
		props.remove(ConfigKey.GENERATOR_PATH.getValue());
	}
	
	@Test
	public void getProperty_for_a_nondefined_key() {
		// given
		
		// when
		String property = ConfigurationService.getProperty(ConfigKey.ENCRYPTION_KEY);
		
		// then
		assertNull(property);
	}
	
	@Test
	public void getProperty_for_a_defined_key() {
		// given
		
		// when
		String property = ConfigurationService.getProperty(ConfigKey.LOGS_PATH);
		
		// then
		assertEquals("/dev/", property);
	}
	
	@Test
	public void getProperty_for_a_nondefined_key_default_value() {
		// given
		
		// when
		String property = ConfigurationService.getProperty(ConfigKey.ENCRYPTION_KEY, "default");
		
		// then
		assertEquals("default", property);
	}
	
	@Test
	public void getProperty_for_a_defined_key_default_value() {
		// given
		
		// when
		String property = ConfigurationService.getProperty(ConfigKey.LOGS_PATH, "default");
		
		// then
		assertEquals("/dev/", property);
	}
	
	@Test
	public void getPropertyAsInt_for_a_nondefined_key_default_value() {
		// given
		
		// when
		int property = ConfigurationService.getPropertyAsInt(ConfigKey.ENCRYPTION_KEY, 1);
		
		// then
		assertEquals(1, property);
	}
	
	@Test
	public void getPropertyAsInt_for_a_defined_key_default_value() {
		// given
		
		// when
		int property = ConfigurationService.getPropertyAsInt(ConfigKey.DEPLOYMENT_IN_PROGRESS_TIMEOUT, 3601);
		
		// then
		assertEquals(3600, property);
	}
	
	@Test
	public void getPropertyAsInt_for_non_int() {
		// given
		
		// when
		int property = ConfigurationService.getPropertyAsInt(ConfigKey.LOGS_PATH, 3601);
		
		// then
		assertEquals(3601, property);
	}
	
	@Test
	public void getPropertyAsBoolean() {
		// given
		
		// when
		boolean property = ConfigurationService.getPropertyAsBoolean(ConfigKey.DELIVER_MAIL, false);
		
		// then
		assertTrue(property);
	}
	
	@Test
	public void getPropertyAsBoolean_notDefinedProp_defaultfalse() {
		// given
		
		// when
		boolean property = ConfigurationService.getPropertyAsBoolean(ConfigKey.ENCRYPTION_KEY, false);
		
		// then
		assertFalse(property);
	}
	
	@Test
	public void getPropertyAsBoolean_notDefinedProp_defaulttrue() {
		// given
		
		// when
		boolean property = ConfigurationService.getPropertyAsBoolean(ConfigKey.ENCRYPTION_KEY, true);
		
		// then
		assertTrue(property);
	}
	
	@Test
	public void getPropertyAsBoolean_true() {
		// given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "true");

		// when
		boolean property = ConfigurationService.getPropertyAsBoolean(ConfigKey.GENERATOR_PATH, false);
		
		// then
		assertTrue(property);
	}
	
	@Test
	public void getPropertyAsBoolean_tRue() {
		// given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "tRue");

		// when
		boolean property = ConfigurationService.getPropertyAsBoolean(ConfigKey.GENERATOR_PATH, false);
		
		// then
		assertTrue(property);
	}
	
	@Test
	public void getPropertyAsBoolean_TRUE() {
		// given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "TRUE");

		// when
		boolean property = ConfigurationService.getPropertyAsBoolean(ConfigKey.GENERATOR_PATH, false);
		
		// then
		assertTrue(property);
	}
	
	@Test
	public void getPropertyAsBoolean_false() {
		// given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "false");

		// when
		boolean property = ConfigurationService.getPropertyAsBoolean(ConfigKey.GENERATOR_PATH, true);
		
		// then
		assertFalse(property);
	}
	
	@Test
	public void getPropertyAsBoolean_false_test() {
		// given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.GENERATOR_PATH.getValue(), "test");

		// when
		boolean property = ConfigurationService.getPropertyAsBoolean(ConfigKey.GENERATOR_PATH, true);
		
		// then
		assertFalse(property);
	}
	
	/**
	 * Paranoiatest for Encryption_key, to not show its value in the GUI
	 */
	@Test
	public void should_be_a_secret_config_key() {
		// then
		assertTrue(ConfigKey.ENCRYPTION_KEY.isSecretValue());
	}
	
	@Test
	public void getKeyDefault() {
		// given
		Properties props = System.getProperties();
		props.remove(ConfigKey.LOCAL_ENV);
		
		// when
		String property = ConfigurationService.getProperty(ConfigKey.LOCAL_ENV);
		
		// then
		assertEquals(ConfigKey.LOCAL_ENV.getDefaultValue(), property);
	}

	@Test
	public void getDefaultValue() {
		// when
		String defaultValue = ConfigurationService.getDefaultValue(ConfigKey.LOCAL_ENV);

		// then
		assertEquals(ConfigKey.LOCAL_ENV.getDefaultValue(), defaultValue);
	}

	@Test
	public void shouldReturnEnvironmentVariableValue() {
		// when
		String envvalue = ConfigurationService.getProperty(ConfigKey.EXTERNAL_RESOURCE_BACKLINK_HOST);

		// then
		assertEquals("envvalue", envvalue);
	}

    @Test
    public void shouldReturnSystemPropertyBeforeEnvironmentVariableValue() {
        // given
        System.getProperties().put(ConfigKey.EXTERNAL_RESOURCE_BACKLINK_HOST.getValue(), "systempropertyvalue");

        // when
        String envvalue = ConfigurationService.getProperty(ConfigKey.EXTERNAL_RESOURCE_BACKLINK_HOST);

        // then
        assertEquals("systempropertyvalue", envvalue);

        System.getProperties().remove(ConfigKey.EXTERNAL_RESOURCE_BACKLINK_HOST.getValue());
    }

	
	@Test
	public void getKeyDefaultOverride() {
		String testEnvName = "test";
		// given
		Properties props = System.getProperties();
		props.remove(ConfigKey.LOCAL_ENV);
		
		// when
		String property = ConfigurationService.getProperty(ConfigKey.LOCAL_ENV, testEnvName);
		
		// then
		assertEquals(testEnvName, property);
	}
	
}
