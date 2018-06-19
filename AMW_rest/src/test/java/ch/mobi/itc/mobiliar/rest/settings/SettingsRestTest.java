package ch.mobi.itc.mobiliar.rest.settings;

import ch.mobi.itc.mobiliar.rest.dtos.ConfigurationDTO;
import ch.puzzle.itc.mobiliar.business.applicationinfo.boundary.ApplicationVersionService;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ApplicationConfigurationInfo;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ConfigurationKeyValuePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static ch.mobi.itc.mobiliar.rest.settings.SettingsRest.OBFUSCATED;
import static ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey.ENCRYPTION_KEY;
import static ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey.GENERATOR_PATH;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class SettingsRestTest {

    @InjectMocks
    SettingsRest rest;

    @Mock
    ApplicationVersionService applicationVersionService;

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

    @Test
    public void  shouldReturnDTOWithExpectedContentOnGetAppInfo() {
        // given
        String expectedValue = "expected";
        ConfigurationKeyValuePair pair = new ConfigurationKeyValuePair();
        pair.setKey(GENERATOR_PATH);
        pair.setValue(expectedValue);

        ApplicationConfigurationInfo appConfInfo = new ApplicationConfigurationInfo();
        appConfInfo.addConfigurationKeyValuePair(pair);

        when(applicationVersionService.getApplicationConfigurationInfo()).thenReturn(appConfInfo);

        // when
        List<ConfigurationDTO> result = rest.getAppInfo();

        // then
        assertThat(result.get(0).getValue(), is(expectedValue));
    }

}