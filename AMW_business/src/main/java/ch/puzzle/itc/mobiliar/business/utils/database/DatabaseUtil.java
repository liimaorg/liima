package ch.puzzle.itc.mobiliar.business.utils.database;

import javax.inject.Inject;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseUtil {

    @Inject
    private
    EntityManager entityManager;

    public boolean isH2() {
        return isEngine("H2");
    }

    public boolean isOracle() {
        return isEngine("oracle");
    }

    private boolean isEngine(String engine) {
        Session session = entityManager.unwrap(Session.class);
        String databaseProductName = null;
        databaseProductName = session.doReturningWork(connection -> {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDatabaseProductName();
        });

        return Objects.requireNonNull(databaseProductName).equalsIgnoreCase(engine);
    }
}
