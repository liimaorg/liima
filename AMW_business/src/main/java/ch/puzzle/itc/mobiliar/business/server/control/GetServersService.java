package ch.puzzle.itc.mobiliar.business.server.control;

import ch.puzzle.itc.mobiliar.business.server.boundary.GetServersUseCase;
import ch.puzzle.itc.mobiliar.business.server.boundary.Server;
import ch.puzzle.itc.mobiliar.business.server.boundary.ServerView;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class GetServersService implements GetServersUseCase {

    @Inject
    private ServerView serverView;

    @Override
    public List<Server> all() {
        return serverView.getServers(null, null, null, null, null, true)
                .stream()
                .map(Server::new)
                .collect(Collectors.toList());
    }
}
