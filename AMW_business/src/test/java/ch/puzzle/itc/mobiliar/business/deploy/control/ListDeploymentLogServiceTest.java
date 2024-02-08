package ch.puzzle.itc.mobiliar.business.deploy.control;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;


@RunWith(MockitoJUnitRunner.class)
public class ListDeploymentLogServiceTest {


    @InjectMocks
    private ListDeploymentLogService service;

    @Mock
    private DeploymentBoundary deploymentBoundary;

    @Test
    public void shouldGetLogsFor() throws NotFoundException {
        // given
        String[] fileNames = new String[] {"log-file-1", "log-file-2"};
        doReturn(fileNames).when(deploymentBoundary).getLogFileNames(81552);

        // when
        List<DeploymentLog> logFiles = service.logsFor(81552);

        // then
        assertThat(logFiles.size(), is(2));
        assertThat(logFiles.stream().map(DeploymentLog::getFilename).collect(Collectors.toList()), hasItems("log-file-1", "log-file-2"));
        Set<Integer> deploymentIds = logFiles.stream().map(DeploymentLog::getDeploymentId).collect(Collectors.toSet());
        assertThat(deploymentIds.size(), is(1));
        assertThat(deploymentIds, hasItems(81552));
    }

    @Test(expected = NullPointerException.class)
    public void expectNullPointerException() throws NotFoundException {
        // given

        // when
        service.logsFor(null);


        // then
        fail("should have thrown exception");
    }
}