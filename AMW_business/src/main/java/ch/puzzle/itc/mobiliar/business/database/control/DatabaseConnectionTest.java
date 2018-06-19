package ch.puzzle.itc.mobiliar.business.database.control;

import ch.puzzle.itc.mobiliar.business.environment.control.ContextRepository;
import ch.puzzle.itc.mobiliar.common.util.ContextNames;

import javax.inject.Inject;

public class DatabaseConnectionTest {

    @Inject
    ContextRepository contextRepository;

    public void testConnection() {
        contextRepository.getContextByName(ContextNames.GLOBAL.getDisplayName());
    }

}
