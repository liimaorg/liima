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

package ch.puzzle.itc.mobiliar.business.utils;

import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Class tests and sets up the Environment for the Running application eg. paths, config and so on.
 */
@Startup
@Singleton
public class ApplicationEnvironmentSetup {

    @Inject
    private Logger log;


    @PostConstruct
    public void init() {
        checkFilesystemPrerequisites();
    }

    /**
     * checks if the configured directories exist and if not can be created
     */
    private void checkFilesystemPrerequisites(){
        // genrator directory
        if(!directoryExists(ConfigurationService.getProperty(ConfigurationService.ConfigKey.GENERATOR_PATH))){
            createDirectory(ConfigurationService.getProperty(ConfigurationService.ConfigKey.GENERATOR_PATH));
        }
        // simulation directory
        if(!directoryExists(ConfigurationService.getProperty(ConfigurationService.ConfigKey.GENERATOR_PATH_SIMULATION))){
            createDirectory(ConfigurationService.getProperty(ConfigurationService.ConfigKey.GENERATOR_PATH_SIMULATION));
        }
        // generator Test directory
        if(!directoryExists(ConfigurationService.getProperty(ConfigurationService.ConfigKey.GENERATOR_PATH_TEST))){
            createDirectory(ConfigurationService.getProperty(ConfigurationService.ConfigKey.GENERATOR_PATH_TEST));
        }
        // test Results directory
        if(!directoryExists(ConfigurationService.getProperty(ConfigurationService.ConfigKey.TEST_RESULT_PATH))){
            createDirectory(ConfigurationService.getProperty(ConfigurationService.ConfigKey.TEST_RESULT_PATH));
        }
        // logs directory
        if(!directoryExists(ConfigurationService.getProperty(ConfigurationService.ConfigKey.LOGS_PATH))){
            createDirectory(ConfigurationService.getProperty(ConfigurationService.ConfigKey.LOGS_PATH));
        }
    }

    private boolean directoryExists(String pathStr){
        if(pathStr == null || pathStr.equals("")){
            return true;
        }
        Path path = Paths.get(pathStr);
        return Files.exists(path);
    }

    private boolean createDirectory(String pathStr){
        if(ConfigurationService.getPropertyAsBoolean(ConfigurationService.ConfigKey.CREATE_NOT_EXISTING_DIRECTORIES_ON_STARTUP)){
            File f = new File(pathStr);
            return f.mkdirs();
        }

        log.log(Level.SEVERE, "Trying to create directory ("+ pathStr +") but disabled by Configuration");
        return false;
    }
}
