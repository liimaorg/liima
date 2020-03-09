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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

@Specializes
public class EntityManagerProducerIntegrationTestImpl extends EntityManagerProducer {

    private static EntityManager em;

    private static String persistenceUnit = "persistence-integration-test";

    @Produces
    public EntityManager createEntityManager() {
        // only create the EntityManger once
        if (em == null) {
            try {
                copyIntegrationTestDB();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            em = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
        }
        return em;
    }

    public static void copyIntegrationTestDB() throws IOException {
        copyIntegrationTestDB("amwFileDbIntegrationEmptyRunning.mv.db");
    }

    public static File copyIntegrationTestDB(String newName) throws IOException {
        // also copy local integration Test DB to gitignored location
        File from;
        File to;

        URL location = EntityManagerProducerIntegrationTestImpl.class.getProtectionDomain().getCodeSource().getLocation();

        from = new File(location.getPath() + "/../../src/test/resources/integration-test/testdb/amwFileDbIntegrationEmpty.mv.db");
        to = new File(location.getPath() + "/../../src/test/resources/integration-test/testdb/"+ newName);
        Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return to;
    }

}
