package ch.puzzle.itc.mobiliar.business.server.control;

import ch.puzzle.itc.mobiliar.business.server.boundary.FilterServersUseCase;
import ch.puzzle.itc.mobiliar.business.server.boundary.ServerView;
import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;

import javax.inject.Inject;
import java.util.List;

public class FilterServersService implements FilterServersUseCase {

    @Inject
    private ServerView serverView;

    @Override
    public List<ServerTuple> filter() {
        return serverView.getServers(null, null, null, null, null, true);
    }
}
