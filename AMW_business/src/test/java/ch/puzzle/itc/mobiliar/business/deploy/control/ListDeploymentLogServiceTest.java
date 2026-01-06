package ch.puzzle.itc.mobiliar.business.deploy.control;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentBoundary;
import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentLog;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
public class ListDeploymentLogServiceTest {

    @InjectMocks
    private ListDeploymentLogService service;

    @Mock
    private DeploymentBoundary deploymentBoundary;

    @Test
    public void shouldGetLogsFor() throws NotFoundException {
        // given
        String[] fileNames = new String[] { "log-file-1", "log-file-2" };
        doReturn(fileNames).when(deploymentBoundary).getLogFileNames(81552);

        // when
        List<DeploymentLog> logFiles = service.logsFor(81552L);

        // then
        assertThat(logFiles.size(), is(2));
        assertThat(logFiles.stream().map(DeploymentLog::getFilename).collect(Collectors.toList()),
                hasItems("log-file-1", "log-file-2"));
        Set<Long> deploymentIds = logFiles.stream().map(DeploymentLog::getId).collect(Collectors.toSet());
        assertThat(deploymentIds.size(), is(1));
        assertThat(deploymentIds, hasItems(81552L));
    }

    @Test
    public void expectNullPointerException() throws NotFoundException {
        assertThrows(NullPointerException.class, () -> {
            service.logsFor(null);
        });
    }
}