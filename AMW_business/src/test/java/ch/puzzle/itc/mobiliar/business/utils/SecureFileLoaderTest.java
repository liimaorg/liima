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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class SecureFileLoaderTest {
	Path dir;
	Path f;
	
	
	SecureFileLoader fileLoader = new SecureFileLoader();
	
	@Before
	public void setUp() throws Exception {
		dir = Paths.get(FileUtils.getTempDirectoryPath(), "/test/foo");
		Files.createDirectories(dir);
		f = Paths.get(dir.toString(), "test.txt");
		Files.createFile(f);
	}
	
	@After
	public void tearDown() throws IOException{
		Files.delete(f);		
	}

	@Test
	public void testIsFileLocatedInDirectory() throws IOException {
		Assert.assertTrue(fileLoader.isFileLocatedInDirectory(dir, f));		
	}
	
	@Test
	public void testIsFileLocatedInDirectoryWithManipulatedPath() throws IOException {
		Assert.assertTrue(fileLoader.isFileLocatedInDirectory(dir, Paths.get(dir.toString(), "../foo/test.txt")));		
	}
	
	@Test
	public void testIsFileLocatedInDirectoryInexistentFile() throws IOException {
		Assert.assertFalse(fileLoader.isFileLocatedInDirectory(dir, Paths.get(dir.toString(), "something")));		
	}
	
	@Test
	public void testIsFileLocatedInDirectoryHomeNok() throws IOException {
		Assert.assertFalse(fileLoader.isFileLocatedInDirectory(dir, Paths.get("~/test.txt")));		
	}
	
	@Test
	public void testIsFileLocatedInDirectoryHomeAlthoughExistsNok() throws IOException {
		Path f2 = Paths.get(FileUtils.getTempDirectoryPath(), "test.txt");
		try {
			Files.createFile(f2);
		} catch (FileAlreadyExistsException e) {
			//Only thrown on Windows
		}
		
		Assert.assertFalse(fileLoader.isFileLocatedInDirectory(dir, f2));
		Files.delete(f2);
	}
	
	
	@Test
	public void testIsFileLocatedInDirectoryAbsoluteNok() throws IOException {
		Assert.assertFalse(fileLoader.isFileLocatedInDirectory(dir, Paths.get("/test.txt")));		
	}
	
	@Test
	public void testIsFileLocatedInDirectoryParentDirNok() throws IOException {
		Assert.assertFalse(fileLoader.isFileLocatedInDirectory(dir, Paths.get("../test.txt")));		
	}

	@Test
	public void testIsFileLocatedInDirectorySymbolicLinkOk() throws IOException {
		//symlinks work only on unix
		Assume.assumeTrue
        (isUnix());
		Path symlink = Paths.get(dir.toString(), "symlink.txt");
		//We create a symbolic link inside of the permitted folder pointing to a file inside of the permitted folder. This should be ok.
		Files.createSymbolicLink(symlink, f);		
		Assert.assertTrue(fileLoader.isFileLocatedInDirectory(dir, symlink));		
		Files.delete(symlink);
	}
	
	@Test
	public void testIsFileLocatedInDirectorySymbolicLinkFromOutsideOk() throws IOException {
		//symlinks work only on unix
		Assume.assumeTrue
        (isUnix());
		Path symlink = Paths.get(FileUtils.getTempDirectoryPath(), "symlink.txt");
		//TODO check: is this statement true? I can't think of any harm this could do...
		//We create a symbolic link outside of the permitted folder pointing to a file inside of the permitted folder. This should be ok as well.
		Files.createSymbolicLink(symlink, f);		
		Assert.assertTrue(fileLoader.isFileLocatedInDirectory(dir, symlink));		
		Files.delete(symlink);
	}
	
	@Test
	public void testIsFileLocatedInDirectorySymbolicLinkNok() throws IOException {
		//symlinks work only on unix
		Assume.assumeTrue
        (isUnix());
		Path f2 = Paths.get(FileUtils.getTempDirectoryPath(), "test.txt");
		Files.createFile(f2);
		
		Path symlink = Paths.get(dir.toString(), "symlink.txt");
		//We create a symbolic link inside of the permitted folder pointing to a file outside of the permitted folder. This should be failing.
		Files.createSymbolicLink(symlink, f2);		
		Assert.assertFalse(fileLoader.isFileLocatedInDirectory(dir, symlink));	
		Files.delete(symlink);
		Files.delete(f2);
		
	}
	
	
	@Test
	public void testLoadFileFromFileSystem() throws IOException, IllegalAccessException{		
		String s = "Hello"+System.lineSeparator()+"World"+System.lineSeparator()+"How are you?";	
		Files.write(f, Arrays.asList(s.split(System.lineSeparator())), StandardCharsets.UTF_8);		
		String result = fileLoader.loadFileFromFileSystem(dir.toString(), f.toString());
		
		Assert.assertEquals(s, result);
		
	}
	
	@Test
	public void testLoadFileFromFileSystemEmpty() throws IOException, IllegalAccessException{		
		String result = fileLoader.loadFileFromFileSystem(dir.toString(), f.toString());		
		Assert.assertEquals("", result);		
	}
	
	@Test
	public void testLoadFileFromFileSystemNotExisting() throws IOException, IllegalAccessException{		
		String result = fileLoader.loadFileFromFileSystem(dir.toString(), Paths.get(dir.toString(), "something").toString());		
		Assert.assertNull(result);
	}
	
	
	public static boolean isUnix() {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 );
	}
}
