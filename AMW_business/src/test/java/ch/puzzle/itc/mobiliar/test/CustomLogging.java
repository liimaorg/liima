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

package ch.puzzle.itc.mobiliar.test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Use to control logging in tests when necessary
 * 
 * @author ama
 * 
 */
public class CustomLogging {

	static Logger log = Logger.getLogger(CustomLogging.class.getName());

	public static void setup(Level level) {
		new CustomLogging().setLevel(level);
	}

	public static void main(String[] args) {
		new CustomLogging().setLevel(Level.INFO);
		log.info("asdf");
	}

	public class StdoutConsoleHandler extends ConsoleHandler {
		protected void setOutputStream(OutputStream out) throws SecurityException {
			super.setOutputStream(System.out); // kitten killed here :-(
		}
	}

	public class VerySimpleFormatter extends Formatter {

		@Override
		public String format(LogRecord record) {
			return record.getLevel() + ": " + record.getMessage() + "\n";

		}
	}

	public void setLevel(Level level) {
		setLevel(level, new StdoutConsoleHandler());
	}

	public void setup(Level level, String fileName) throws SecurityException, IOException {
		setLevel(level, new FileHandler(fileName));
	}

	private void setLevel(Level level, Handler customHandler) {
		try {
			Logger globalLogger = Logger.getLogger("");
			globalLogger.setLevel(level);
			Handler[] handlers = globalLogger.getHandlers();
			for (Handler handler : handlers) {
				globalLogger.removeHandler(handler);

			}

			customHandler.setFormatter(new VerySimpleFormatter());
			customHandler.setLevel(level);
			globalLogger.addHandler(customHandler);
			globalLogger.fine("customization finished");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
