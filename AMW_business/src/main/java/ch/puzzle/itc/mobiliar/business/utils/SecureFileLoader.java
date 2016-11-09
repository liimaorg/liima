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

import javax.ejb.Stateless;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

/**
 * This service is meant to provide secure access to files located on the file system.
 * 
 * @author oschmid
 */
@Stateless
public class SecureFileLoader {

	private final static long MAXFILESIZE = 10000000;
	
	/**
	 * @param folder
	 *             - the folder in which the file is expected. IMPORTANT: Make sure, that this field
	 *             originates from a controlled source (e.g. a server configuration). This field MUST NOT be
	 *             set dynamically from a specific GUI context, since otherwise, the control mechanism could
	 *             be worked around.
	 * @param filePath
	 *             - the path of the file which should be loaded (full path).
	 * @return
	 * @throws IllegalAccessException
	 * @throws IOException 
	 */
	public String loadFileFromFileSystem(String folder, String filePath) throws IllegalAccessException, IOException {
		Path baseFolder = Paths.get(folder);
		if (!Files.exists(baseFolder) || !Files.isDirectory(baseFolder)) {
			throw new IllegalAccessException("Tried to access a file from " + folder
					+ "! This path does not exist or is not a directory");
		}
		Path relativeFile = Paths.get(filePath);		
		if (Files.exists(relativeFile)) {
			if (Files.isSymbolicLink(relativeFile)) {
				throw new IllegalAccessException("Tried to access a symlink - this is not permitted!");
			}			
			if(!isFileLocatedInDirectory(baseFolder, relativeFile)){
				throw new IllegalAccessException("The requested file is not part of the given directory!");
			}
			if(Files.size(relativeFile)>MAXFILESIZE){
				throw new IOException("The file which should be read is too big (> "+MAXFILESIZE+" bytes)");
			}
			StringBuilder sb = new StringBuilder();
			List<String> lines = Files.readAllLines(relativeFile, StandardCharsets.UTF_8);
			Iterator<String> lineIt = lines.iterator();
			while(lineIt.hasNext()){
				sb.append(lineIt.next());
				if(lineIt.hasNext()){
					sb.append(System.lineSeparator());
				}
			}			
			return sb.toString();
		}
		return null;

	}

	/**
	 * @param dir
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	boolean isFileLocatedInDirectory(Path dir, Path file) throws IOException{		
		try {
			Path realDir = dir.toRealPath();
			Path realFile= file.toRealPath();
			
			Path parent = realFile.getParent();
			while(parent!=null){
				if(parent.equals(realDir)){
					return true;
				}
				parent = parent.getParent();
			}
			return false;
		}
		catch (NoSuchFileException e) {
			return false;
		}		
	}
}
