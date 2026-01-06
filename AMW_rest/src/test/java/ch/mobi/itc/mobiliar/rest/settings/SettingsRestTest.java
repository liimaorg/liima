package ch.mobi.itc.mobiliar.rest.settings;

import ch.puzzle.itc.mobiliar.business.applicationinfo.boundary.ApplicationVersionService;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ConfigurationKeyValuePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static ch.puzzle.itc.mobiliar.common.util.ConfigKey.GENERATOR_PATH;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettingsRestTest {

    @InjectMocks
    SettingsRest rest;

    @Mock
    ApplicationVersionService applicationVersionService;

    @Test
    public void  shouldReturnConfigurationPairsWithExpectedContentOnGetAppConfig() {
        // given
        String expectedValue = "expected";
        ConfigurationKeyValuePair pair = new ConfigurationKeyValuePair();
        pair.setKey(GENERATOR_PATH);
        pair.setValue(expectedValue);
        when(applicationVersionService.getObfuscatedApplicationConfigurationKeyValuePairs()).thenReturn(Collections.singletonList(pair));

        // when
        List<ConfigurationKeyValuePair> pairs = rest.getAppConfig();

        // then
        assertThat(pairs.get(0).getValue(), is(expectedValue));
    }

}