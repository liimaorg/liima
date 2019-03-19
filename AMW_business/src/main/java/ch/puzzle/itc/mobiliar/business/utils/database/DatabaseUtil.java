package ch.puzzle.itc.mobiliar.business.utils.database;

import javax.inject.Inject;
import javax.persistence.EntityManager;
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
        org.hibernate.engine.spi.SessionImplementor sessionImp =
                (org.hibernate.engine.spi.SessionImplementor) entityManager.getDelegate();
        String databaseProductName = null;
        try {
            databaseProductName = sessionImp.connection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(databaseProductName).equalsIgnoreCase(engine);
    }


}
