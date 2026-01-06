package ch.puzzle.itc.mobiliar.business.applicationinfo.boundary;

import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ConfigurationKeyValuePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class ApplicationVersionServiceTest {

    @InjectMocks
    ApplicationVersionService applicationVersionService;


    @Test
    public void getObfuscatedApplicationConfigurationKeyValuePairsShouldObfuscateValuesCorrectly() {
        // given
        applicationVersionService.init();

        // when
        List<ConfigurationKeyValuePair> obfuscatedKeyValuePairs = applicationVersionService.getObfuscatedApplicationConfigurationKeyValuePairs();

        // then
        for (ConfigurationKeyValuePair keyValuePair : obfuscatedKeyValuePairs) {
            if (keyValuePair.getKey().isSecretValue()) {
                assertThat(keyValuePair.getValue(), is(ApplicationVersionService.OBFUSCATED));
                assertThat(keyValuePair.getDefaultValue(), is(ApplicationVersionService.OBFUSCATED));
            } else {
                assertThat(keyValuePair.getValue(), not(ApplicationVersionService.OBFUSCATED));
                assertThat(keyValuePair.getDefaultValue(), not(ApplicationVersionService.OBFUSCATED));
            }
        }

    }
}