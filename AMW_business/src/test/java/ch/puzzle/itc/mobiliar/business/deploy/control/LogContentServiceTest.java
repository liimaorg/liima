package ch.puzzle.itc.mobiliar.business.deploy.control;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLogContentCommand;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import java.io.IOException;

import static ch.puzzle.itc.mobiliar.business.deploy.control.LogContentService.MAX_FILE_SIZE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class LogContentServiceTest {

    @InjectMocks
    private LogContentService service;

    @Mock
    private DeploymentBoundary deploymentBoundary;

    @Test(expected = ValidationException.class)
    public void should_throw_exception_when_log_file_not_found() throws ValidationException, IOException {
        // given
        doReturn(new String[0]).when(deploymentBoundary).getLogFileNames(12345);

        // when
        service.getContent(new DeploymentLogContentCommand(12345, "file-name.log"));

        // then
        fail("should have thrown exception");
    }

    @Test
    public void should_get_content_of_log_file() throws ValidationException, IOException {
        // given
        doReturn(new String[]{"file-name.log"}).when(deploymentBoundary).getLogFileNames(12345);
        doReturn("log-file-content").when(deploymentBoundary).readContentOfDeploymentLog("file-name.log", MAX_FILE_SIZE);

        // when
        DeploymentLog content = service.getContent(new DeploymentLogContentCommand(12345, "file-name.log"));

        // then
        assertThat(content.getId(), is(12345L));
        assertThat(content.getFilename(), is("file-name.log"));
        assertThat(content.getContent(), is("log-file-content"));
    }
}