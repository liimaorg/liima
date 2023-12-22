package ch.mobi.itc.mobiliar.rest.deployments;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.ListDeploymentLogsUseCase;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentsLogsRestTest {

    @InjectMocks
    private DeploymentsLogRest resource;

    @Mock
    private DeploymentBoundary deploymentBoundary;

    @Mock
    private ListDeploymentLogsUseCase listDeploymentLogsUseCase;

    @Mock
    private Logger log;

    private DeploymentEntity deploymentEntity;

    @Before
    public void setUp() throws Exception {
        deploymentEntity = new DeploymentEntity();
        deploymentEntity.setId(123);
    }

    @Test
    public void getDeploymentLogFileContent() throws IllegalAccessException {
        String[] fileNames = {"log1", "log2"};
        when(deploymentBoundary.getLogFileNames(deploymentEntity.getId())).thenReturn(fileNames);
        when(deploymentBoundary.getDeploymentLog("log1")).thenReturn("content 1");

        Response response = resource.getDeploymentLogFileContent(deploymentEntity.getId(), "log1");
        assertThat(response.getStatus(), is(200));
        String content = (String) response.getEntity();
        assertThat(content, is("content 1"));
        verify(deploymentBoundary).getDeploymentLog("log1");
    }

    @Test
    public void getDeploymentLogFileContent_notFound() throws IllegalAccessException {
        when(deploymentBoundary.getLogFileNames(deploymentEntity.getId())).thenReturn(new String[]{});

        Response response = resource.getDeploymentLogFileContent(deploymentEntity.getId(), "test");
        assertThat(response.getStatus(), is(400));
        verify(deploymentBoundary, never()).getDeploymentLog(anyString());
    }

    @Test
    public void getDeploymentLogs_withIllegalAccess() throws IllegalAccessException {
        String[] fileNames = {"log1", "log2"};
        when(deploymentBoundary.getLogFileNames(deploymentEntity.getId())).thenReturn(fileNames);

        Response response = resource.getDeploymentLogFileContent(deploymentEntity.getId(), "unknown");
        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        String msg = (String) response.getEntity();
        assertThat(msg, is("No logfile with name unknown for deployment with id 123"));
    }

    @Test
    public void getDeploymentLogs_nothingFound() throws ValidationException {
        when(listDeploymentLogsUseCase.logsFor(deploymentEntity.getId())).thenReturn(List.of());

        Response response = resource.getDeploymentLogs(deploymentEntity.getId());

        assertNotNull(response);
        assertThat(Response.Status.OK.getStatusCode(), is(response.getStatus()));
        List<DeploymentLog> expected = Collections.emptyList();
        assertThat(expected, is(response.getEntity()));
    }

    @Test
    public void getDeploymentLogs() throws ValidationException {
        List<ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog> logs = List.of(
                new ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog(deploymentEntity.getId(), "log1"),
                new ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog(deploymentEntity.getId(), "log2"));

        when(listDeploymentLogsUseCase.logsFor(deploymentEntity.getId())).thenReturn(logs);

        Response response = resource.getDeploymentLogs(deploymentEntity.getId());

        assertNotNull(response);
        assertThat(Response.Status.OK.getStatusCode(), is(response.getStatus()));

        assertThat(logs, is(response.getEntity()));
    }

    @Test(expected = ValidationException.class)
    public void getDeploymentLogs_throwsValidationExcpetionWhenDeploymentIdIsNull() throws ValidationException {
        // given

        // when
        resource.getDeploymentLogs(null);

        // then
        fail("should have thrown exception");
    }
}
