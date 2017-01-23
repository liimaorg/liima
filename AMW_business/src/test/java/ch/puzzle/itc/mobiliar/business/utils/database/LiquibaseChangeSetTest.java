package ch.puzzle.itc.mobiliar.business.utils.database;

import ch.puzzle.itc.mobiliar.business.database.control.EntityManagerProducerIntegrationTestImpl;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.Test;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;


public class LiquibaseChangeSetTest {


    private Database database;
    private java.sql.Connection conn = null;


    @Test
    public void shouldLoadInitialDataSetInEmptyDatabase() throws LiquibaseException, SQLException, ClassNotFoundException {
        // given
        Class.forName("org.h2.Driver");
        conn = DriverManager.getConnection("jdbc:h2:mem:testdata", "sa", "");

        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
        Liquibase liquibase = new Liquibase("liquibase/auto.db.changelog-initialrun.xml", new ClassLoaderResourceAccessor(), database);

        //when
        liquibase.update(new Contexts(), new LabelExpression());

        // then must not fail
        if(conn !=null) {
            conn.close();
        }
    }

    @Test
    public void shouldNotHaveAnyOpenChangeSets() throws LiquibaseException, SQLException, ClassNotFoundException, IOException {
        // given

        EntityManagerProducerIntegrationTestImpl.copyIntegrationTestDB("amwFileDbIntegrationOpenChangeSets.h2.db");

        Class.forName("org.h2.Driver");
        conn = DriverManager.getConnection("jdbc:h2:file:../AMW_business/src/test/resources/integration-test/testdb/amwFileDbIntegrationOpenChangeSets", "sa", "");

        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
        Liquibase liquibase = new Liquibase("liquibase/auto.db.changelog.xml", new ClassLoaderResourceAccessor(), database);

        //when
        //liquibase.changeLogSync(new Contexts(), new LabelExpression());
        List<ChangeSet> unrunChangeSets = liquibase.listUnrunChangeSets(new Contexts(), new LabelExpression());

        // if there are open Changesets apply them and look for errors
        if(unrunChangeSets.size()>0){
            try {
                liquibase.update(new Contexts(), new LabelExpression());
            }catch (LiquibaseException e){
                fail("There are open Database Changesets on the local H2 Database, which fail when you apply them, you need to fix that and apply them afterwards with: AMW_db_scripts/update_h2_test_db.sh");
            }finally {
                if(conn !=null) {
                    conn.close();
                }
            }
            fail("There are open Database Changesets on the local H2 Database, that can be applied without error, run AMW_db_scripts/update_h2_test_db.sh to apply them directly");
        }

        if(conn !=null) {
            conn.close();
        }
    }

}
