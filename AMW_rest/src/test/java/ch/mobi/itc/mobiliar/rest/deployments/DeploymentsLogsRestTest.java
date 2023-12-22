package ch.mobi.itc.mobiliar.rest.deployments;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentsLogsRestTest {

    @InjectMocks
    private DeploymentsLogRest service;

    @Mock
    private DeploymentBoundary deploymentBoundary;

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

        Response response = service.getDeploymentLogFileContent(deploymentEntity.getId(), "log1");
        assertThat(response.getStatus(), is(200));
        String content = (String) response.getEntity();
        assertThat(content, is("content 1"));
        verify(deploymentBoundary).getDeploymentLog("log1");
    }

    @Test
    public void getDeploymentLogFileContent_notFound() throws IllegalAccessException {
        when(deploymentBoundary.getLogFileNames(deploymentEntity.getId())).thenReturn(new String[] {});

        Response response = service.getDeploymentLogFileContent(deploymentEntity.getId(), "test");
        assertThat(response.getStatus(), is(400));
        verify(deploymentBoundary, never()).getDeploymentLog(anyString());
    }

    @Test
    public void getDeploymentLogs_withIllegalAccess() throws IllegalAccessException {
        String[] fileNames = {"log1", "log2"};
        when(deploymentBoundary.getLogFileNames(deploymentEntity.getId())).thenReturn(fileNames);

        Response response = service.getDeploymentLogFileContent(deploymentEntity.getId(), "unknown");
        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        String msg = (String) response.getEntity();
        assertThat(msg, is("No logfile with name unknown for deployment with id 123"));
    }

    @Test
    public void getDeploymentLogFileNames_nothingFound() {
        String[] fileNames = {};
        when(deploymentBoundary.getLogFileNames(deploymentEntity.getId())).thenReturn(fileNames);

        Response response = service.getDeploymentLogFileNames(deploymentEntity.getId());

        assertNotNull(response);
        assertThat(Response.Status.OK.getStatusCode(), is(response.getStatus()));
        List<DeploymentLog> expected = Collections.emptyList();
        assertThat(expected, is(response.getEntity()));
    }

    @Test
    public void getDeploymentLogfileNames() {
        String[] fileNames = {"log1", "log2"};
        when(deploymentBoundary.getLogFileNames(deploymentEntity.getId())).thenReturn(fileNames);

        Response response = service.getDeploymentLogFileNames(deploymentEntity.getId());

        assertNotNull(response);
        assertThat(Response.Status.OK.getStatusCode(), is(response.getStatus()));
        verify(deploymentBoundary).getLogFileNames(deploymentEntity.getId());

        List<DeploymentLog> expected = List.of(
                new DeploymentLog(deploymentEntity.getId(), "log1"),
                new DeploymentLog(deploymentEntity.getId(), "log2"));

        assertThat(expected, is(response.getEntity()));
    }

}
