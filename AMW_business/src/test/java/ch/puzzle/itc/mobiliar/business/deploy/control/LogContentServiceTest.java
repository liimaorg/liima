package ch.puzzle.itc.mobiliar.business.deploy.control;

import static ch.puzzle.itc.mobiliar.business.deploy.control.LogContentService.MAX_FILE_SIZE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLogContentCommand;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
public class LogContentServiceTest {

    @InjectMocks
    private LogContentService service;

    @Mock
    private DeploymentBoundary deploymentBoundary;

    @Test
    public void should_throw_exception_when_log_file_not_found() throws ValidationException, IOException {
        assertThrows(ValidationException.class, () -> {
            // given
            doReturn(new String[0]).when(deploymentBoundary).getLogFileNames(12345);

            // when
            service.getContent(new DeploymentLogContentCommand(12345, "file-name.log"));
        });
    }

    @Test
    public void should_get_content_of_log_file() throws ValidationException, IOException {
        // given
        doReturn(new String[] { "file-name.log" }).when(deploymentBoundary).getLogFileNames(12345);
        doReturn("log-file-content").when(deploymentBoundary).readContentOfDeploymentLog("file-name.log", MAX_FILE_SIZE);

        // when
        DeploymentLog content = service.getContent(new DeploymentLogContentCommand(12345, "file-name.log"));

        // then
        assertThat(content.getId(), is(12345L));
        assertThat(content.getFilename(), is("file-name.log"));
        assertThat(content.getContent(), is("log-file-content"));
    }
}