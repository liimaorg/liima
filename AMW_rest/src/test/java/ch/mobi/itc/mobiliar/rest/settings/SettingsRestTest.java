package ch.mobi.itc.mobiliar.rest.settings;

import ch.mobi.itc.mobiliar.rest.dtos.ConfigurationDTO;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ConfigurationKeyValuePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static ch.mobi.itc.mobiliar.rest.settings.SettingsRest.OBFUSCATED;
import static ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey.ENCRYPTION_KEY;
import static ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey.GENERATOR_PATH;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class SettingsRestTest {

    @InjectMocks
    SettingsRest rest;

    @Before
    public void configure() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSetCorrectValuesOnCreateConfigurationDTO() {
        // given
        String expectedValue = "expected";
        ConfigurationKeyValuePair pair = new ConfigurationKeyValuePair();
        pair.setKey(GENERATOR_PATH);
        pair.setValue(expectedValue);

        // when
        ConfigurationDTO configurationDTO = rest.createConfigurationDTO(pair);

        // then
        assertThat(configurationDTO.getValue(), is(pair.getValue()));
        assertThat(configurationDTO.getKey(), is(GENERATOR_PATH.getValue()));
        assertThat(configurationDTO.getEnv(), is(GENERATOR_PATH.getEnvName()));
        assertThat(configurationDTO.getDefaultValue(), is(GENERATOR_PATH.getDefaultValue()));
    }

    @Test
    public void shouldObfuscateSecretValuesOnCreateConfigurationDTO() {
        // given
        String secretString = "iamSecret";
        ConfigurationKeyValuePair pair = new ConfigurationKeyValuePair();
        pair.setKey(ENCRYPTION_KEY);
        pair.setValue(secretString);

        // when
        ConfigurationDTO configurationDTO = rest.createConfigurationDTO(pair);

        // then
        assertThat(configurationDTO.getValue(), is(OBFUSCATED));
        assertThat(configurationDTO.getKey(), is(ENCRYPTION_KEY.getValue()));
        assertThat(configurationDTO.getEnv(), is(ENCRYPTION_KEY.getEnvName()));
        assertThat(configurationDTO.getDefaultValue(), is(ENCRYPTION_KEY.getDefaultValue()));
    }

}