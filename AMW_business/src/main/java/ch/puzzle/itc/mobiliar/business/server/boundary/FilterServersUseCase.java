package ch.puzzle.itc.mobiliar.business.server.boundary;

import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;

import java.util.List;

public interface FilterServersUseCase {

    List<ServerTuple> filter();
}
