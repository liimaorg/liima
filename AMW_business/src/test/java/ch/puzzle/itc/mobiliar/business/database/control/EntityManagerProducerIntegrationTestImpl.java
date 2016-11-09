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

package ch.puzzle.itc.mobiliar.business.database.control;

import java.io.File;
import java.io.IOException;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import com.google.common.io.Files;

@Specializes
public class EntityManagerProducerIntegrationTestImpl extends EntityManagerProducer {

    private static EntityManager em;

    private static String persistenceUnit = "persistence-integration-test";

    private static boolean emptyDB;

    private static boolean memoryDB;

    @Produces
    public EntityManager createEntityManager() {
        // only create the EntityManger once
        if (em == null) {
            try {
                if (!memoryDB)
                    copyIntegrationTestDB();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            em = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
        }
        return em;
    }

    public static void useMemoryPU() {
        persistenceUnit = "persistence-test";
        memoryDB = true;
    }

    public static void useEmptyDB() {
        emptyDB = true;
    }

    public static void copyIntegrationTestDB() throws IOException {
        // also copy local integration Test DB to gitignored location
        File from;
        File to;
        if (!emptyDB) {
            from = new File("../AMW_testing/src/test/resources/integration-test/testdb/amwFileDbIntegration.h2.db");
            to = new File(
                    "../AMW_testing/src/test/resources/integration-test/testdb/amwFileDbIntegrationRunning.h2.db");
        } else {
            from = new File("../AMW_business/src/test/resources/integration-test/testdb/amwFileDbIntegrationEmpty.h2.db");
            to = new File("../AMW_business/src/test/resources/integration-test/testdb/amwFileDbIntegrationEmptyRunning.h2.db");
        }
        Files.copy(from, to);
    }

}
