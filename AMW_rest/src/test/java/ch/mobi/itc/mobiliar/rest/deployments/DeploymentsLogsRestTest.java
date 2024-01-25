package ch.mobi.itc.mobiliar.rest.deployments;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLogContentCommand;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLogContentUseCase;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.ListDeploymentLogsUseCase;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentsLogsRestTest {

    @InjectMocks
    private DeploymentsLogRest resource;

    @Mock
    private ListDeploymentLogsUseCase listDeploymentLogsUseCase;

    @Mock
    private DeploymentLogContentUseCase logContentUseCase;

    @Before
    public void setUp() {
    }

    @Test
    public void getDeploymentLogFileContent() throws ValidationException, IOException {
        doReturn("log-file-content").when(logContentUseCase).getContent(any(DeploymentLogContentCommand.class));
        Response response = resource.getDeploymentLogFileContent(123, "log-file-name");
        assertThat(response.getStatus(), is(200));
        String content = (String) response.getEntity();
        assertThat(content, is("log-file-content"));
    }

    @Test(expected = ConstraintViolationException.class)
    public void getDeploymentLogFileContent_invalidFileName() throws ValidationException, IOException {
        // given

        // when
        resource.getDeploymentLogFileContent(123456, "path/../../whit-file-traversal");

        // then
        fail("should have thrown exception");
    }

    @Test(expected = ConstraintViolationException.class)
    public void getDeploymentLogFileContent_id_is_null() throws ValidationException, IOException {
        // given

        // when
        resource.getDeploymentLogFileContent(null, "deployment.log");

        // then
        fail("should have thrown exception");
    }

    @Test(expected = ConstraintViolationException.class)
    public void getDeploymentLogFileContent_filename_is_null() throws ValidationException, IOException {
        // given

        // when
        resource.getDeploymentLogFileContent(12345, null);

        // then
        fail("should have thrown exception");
    }

    @Test
    public void getDeploymentLogs_nothingFound() throws ValidationException, NotFoundException {
        when(listDeploymentLogsUseCase.logsFor(123456)).thenReturn(List.of());

        Response response = resource.getDeploymentLogs(123456);

        assertNotNull(response);
        assertThat(Response.Status.OK.getStatusCode(), is(response.getStatus()));
        List<?> expected = Collections.emptyList();
        assertThat(expected, is(response.getEntity()));
    }

    @Test
    public void getDeploymentLogs() throws ValidationException, NotFoundException {
        List<ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog> logs = List.of(
                new DeploymentLog(123456, "log1"),
                new DeploymentLog(123456, "log2"));

        when(listDeploymentLogsUseCase.logsFor(123456)).thenReturn(logs);

        Response response = resource.getDeploymentLogs(123456);

        assertNotNull(response);
        assertThat(Response.Status.OK.getStatusCode(), is(response.getStatus()));

        assertThat(logs, is(response.getEntity()));
    }

    @Test(expected = ValidationException.class)
    public void getDeploymentLogs_throwsValidationExcpetionWhenDeploymentIdIsNull() throws ValidationException, NotFoundException {
        // given

        // when
        resource.getDeploymentLogs(null);

        // then
        fail("should have thrown exception");
    }
}
