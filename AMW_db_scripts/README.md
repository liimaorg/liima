# AMW Databasescripts
This maven modul is used to generate databasemigration scripts, when not executed directly during the start up of the application.

IMPORTANT: Execute the following maven commands within the current project (`AMW_db_scripts`)!

## Supported properties:
* db: specifies the database to run Liquibase against. The file src/main/resources/liquibase/liquibase-$db.properties is used to configure Liquibase. Default: h2.test.
* goal: specifies the Liquibase goal to run: http://www.liquibase.org/documentation/maven/. Default: updateSQL

## Generate DB Scripts against a specific Database
Local Oracle server in Docker image
* mvn compile -Pliquibase,ora -Ddb=ora.local

H2 test database (AMW_business/src/test/resources/integration-test/testdb):
* mvn compile -Pliquibase,h2 -Ddb=h2.test

Local PostgreSQL database:
* mvn compile -Pliquibase,pg -Ddb=pg

The script is generated in target/liquibase.

To apply the open changesets to the H2 test Database run `update_h2_test_db.sh`.

## Check which changesets are going to be applied
* mvn compile -Pliquibase -Ddb=ora.local -Dgoal=status

## Create Initial Database Changelog from Oracle Database
First generate Changelog XML
* mvn compile -Pliquibase -Ddb=ora.local -Dgoal=generateChangeLog -Dliquibase.changeSetAuthor=initial-data

Fix the Changelog
* replace changeset id (eg. 1481468384714) with initial-model
* remove all create index elements when there is the same addPrimaryKey element
* run converter script fixchangelog.php
** php -f src/main/resources/liquibase/converter/fixchangelog.php
** target/liquibase/auto.db.changelog-base.xml
** remove view from the changelog

Additional if done multiple times
* rebase databasechanlog table
** take the content of the databasechangelog table and add it to the initial.data.xml change set

## Compare two databases
Generates a Liquibase changelog between the current development database and the current entity model:
* mvn compile -Pliquibasecompare -Dgoal=diff
