package ch.mobi.itc.mobiliar.rest.deployments;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.*;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeploymentsLogsRestTest {

    @InjectMocks
    private DeploymentsLogRest resource;

    @Mock
    private ListDeploymentLogsUseCase listDeploymentLogsUseCase;

    @Mock
    private DeploymentLogContentUseCase logContentUseCase;

    @Test
    public void getDeploymentLogFileContent() throws ValidationException, IOException {
        doReturn(new DeploymentLog(123, "log-file-name", "log-file-content")).when(logContentUseCase).getContent(any(DeploymentLogContentCommand.class));
        Response response = resource.getDeploymentLogFileContent(123, "log-file-name");
        assertThat(response.getStatus(), is(200));
        DeploymentLog deploymentLogContent = (DeploymentLog) response.getEntity();
        assertThat(deploymentLogContent.getContent(), is("log-file-content"));
    }

    @Test
    public void getDeploymentLogFileContent_invalidFileName() {
        // given

        // when / then
        assertThrows(ConstraintViolationException.class, () -> resource.getDeploymentLogFileContent(123456, "path/../../whit-file-traversal"));
    }

    @Test
    public void getDeploymentLogFileContent_id_is_null() {
        // given

        // when / then
        assertThrows(ConstraintViolationException.class, () -> resource.getDeploymentLogFileContent(null, "deployment.log"));
    }

    @Test
    public void getDeploymentLogFileContent_filename_is_null() {
        // given

        // when / then
        assertThrows(ConstraintViolationException.class, () -> resource.getDeploymentLogFileContent(12345, null));
    }

    @Test
    public void getDeploymentLogs_nothingFound() throws ValidationException, NotFoundException {
        when(listDeploymentLogsUseCase.logsFor(123456L)).thenReturn(List.of());

        Response response = resource.getDeploymentLogs(123456L);

        assertNotNull(response);
        assertThat(Response.Status.OK.getStatusCode(), is(response.getStatus()));
        List<?> expected = Collections.emptyList();
        assertThat(expected, is(response.getEntity()));
    }

    @Test
    public void getDeploymentLogs() throws ValidationException, NotFoundException {
        List<ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog> logs = List.of(
                new DeploymentLog(123456L, "log1"),
                new DeploymentLog(123456L, "log2"));

        when(listDeploymentLogsUseCase.logsFor(123456L)).thenReturn(logs);

        Response response = resource.getDeploymentLogs(123456L);

        assertNotNull(response);
        assertThat(Response.Status.OK.getStatusCode(), is(response.getStatus()));

        assertThat(logs, is(response.getEntity()));
    }

    @Test
    public void getDeploymentLogs_throwsValidationExceptionWhenDeploymentIdIsNull() {
        // given

        // when / then
        assertThrows(ValidationException.class, () -> resource.getDeploymentLogs(null));
    }
}
