/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.utils.database;

import java.sql.Connection;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import ch.puzzle.itc.mobiliar.common.util.ConfigKey;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import liquibase.Scope;
import liquibase.UpdateSummaryEnum;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.command.core.helpers.ShowSummaryArgument;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.ui.LoggerUIService;
import liquibase.util.LiquibaseUtil;

@Startup
@Singleton
public class LiquibaseBootstrap {

    @Inject
    Logger log;

    @PostConstruct
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // liquibase manages the transaction by itself
    private void onStartup() {
        log.info("Booting Liquibase " + LiquibaseUtil.getBuildVersionInfo());
        try (Connection connection = getDataSource().getConnection()) {
            update(connection, "liquibase/auto.db.changelog.xml");
        } catch (Exception e) {
            // PostConstruct is only allowed to throw runtime exceptions
            throw new RuntimeException(e);
        }
    }

    private void update(Connection connection, String changelogFile) throws Exception {
        // See https://github.com/liquibase/liquibase/issues/2396
        String scopeId = Scope.enter(Map.of(Scope.Attr.ui.name(), new LoggerUIService()));
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelogFile);
        updateCommand.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.LOG);
        updateCommand.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.SUMMARY);
        updateCommand.execute();
        Scope.exit(scopeId);
    }

    private DataSource getDataSource() throws NamingException {
        DataSource ds = null;
        String jndi = ConfigurationService.getProperty(ConfigKey.LIQUIBASE_DATASOURCE_JNDI);
        Context initCtx = new InitialContext();
        ds = (DataSource) initCtx.lookup(jndi);
        return ds;
    }

}
