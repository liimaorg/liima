package ch.mobi.itc.mobiliar.rest.deployments;

import ch.mobi.itc.mobiliar.rest.dtos.AppWithVersionDTO;
import ch.mobi.itc.mobiliar.rest.dtos.DeploymentParameterDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class DeploymentDtoCsvBodyWriterTest {

    @InjectMocks
    DeploymentDtoCsvBodyWriter writer;

    @Test
    public void shouldFormatAppsWithVersion() {
        // given
        AppWithVersionDTO dtoA = new AppWithVersionDTO();
        dtoA.setApplicationName("testAppA");
        dtoA.setVersion("0.1.a");
        AppWithVersionDTO dtoB = new AppWithVersionDTO();
        dtoB.setApplicationName("testAppB");
        dtoB.setVersion("0.1.b");
        String expected = "\"testAppA 0.1.a\ntestAppB 0.1.b\"";

        // when
        String formatted = writer.formatAppsWithVersion(Arrays.asList(dtoA, dtoB));

        // then
        assertThat(formatted, is(expected));
    }

    @Test
    public void shouldFormatDeploymentParameters() {
        // given
        DeploymentParameterDTO dtoA = new DeploymentParameterDTO();
        dtoA.setKey("keyA");
        dtoA.setValue("valueA");
        DeploymentParameterDTO dtoB = new DeploymentParameterDTO();
        dtoB.setKey("keyB");
        dtoB.setValue("valueB");
        String expected = "\"keyA valueA\nkeyB valueB\"";

        // when
        String formatted = writer.formatDeploymentParameters(Arrays.asList(dtoA, dtoB));

        // then
        assertThat(formatted, is(expected));
    }

    @Test
    public void shouldFormatStatusMessage() {
        // given
        String message = "Foo: \"Bar\"\nTest: \"Bar\"\n";
        String expected = "\"Foo: Bar\nTest: Bar\"";

        // when
        String formatted = writer.formatStatusMessage(message);

        // then
        assertThat(formatted, is(expected));
    }

    @Test
    public void shouldFormatNullStatusMessage() {
        // given
        String message = null;
        String expected = "";

        // when
        String formatted = writer.formatStatusMessage(message);

        // then
        assertThat(formatted, is(expected));
    }
}