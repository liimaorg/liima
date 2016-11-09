package ch.puzzle.itc.mobiliar.stm;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.List;

import static org.junit.Assert.*;

public class AMW_STMTest {

	AMW_STM stm;
	
	
	@Before
	public void setUp() throws Exception {
		stm = new AMW_STM();
	}
	
	@Test
	public void test_createSTMbasedOnArgs_noArgs(){
		// given
		String[] args = {};
		
		// when
		AMW_STM stm = AMW_STM.createSTMbasedOnArgs(args);
		
		// then default values are set
		assertEquals(null, stm.pathToSTPs);
		assertEquals(20, stm.numberOfThreads);
		assertFalse(stm.dependencyManagement);
		assertEquals(30000, stm.testTimeout);
	}
	
	@Test
	public void test_createSTMbasedOnArgs_only_first_arg(){
		// given
		String[] args = {"path/to/"};
		
		// when
		AMW_STM stm = AMW_STM.createSTMbasedOnArgs(args);
		
		// then
		assertEquals("path/to/", stm.pathToSTPs);
		assertEquals(20, stm.numberOfThreads);
		assertFalse(stm.dependencyManagement);
		assertEquals(30000, stm.testTimeout);
	}
	
	@Test
	public void test_createSTMbasedOnArgs_two_args_NumberOfThreads(){
		// given
		String[] args = {"path/to/", "2"};
		
		// when
		AMW_STM stm = AMW_STM.createSTMbasedOnArgs(args);
		
		// then
		assertEquals("path/to/", stm.pathToSTPs);
		assertEquals(2, stm.numberOfThreads);
		assertFalse(stm.dependencyManagement);
		assertEquals(30000, stm.testTimeout);
	}
	
	@Test(expected=NumberFormatException.class)
	public void test_createSTMbasedOnArgs_two_args_NumberOfThreads_Not_Parsable(){
		// given
		String[] args = {"path/to/", "int"};
		
		// when
		AMW_STM.createSTMbasedOnArgs(args);
		
		// then
	}
	
	@Test(expected=NumberFormatException.class)
	public void test_createSTMbasedOnArgs_three_args_timeOutNotParsable(){
		// given
		String[] args = {"path/to/", "3", "int"};
		
		// when
		AMW_STM.createSTMbasedOnArgs(args);
		
		// then
	}
	
	@Test
	public void test_createSTMbasedOnArgs_allArgs_dependencyManagement(){
		// given
		String[] args = {"path/to/", "dependencyManagement", "1"};
		
		// when
		AMW_STM stm = AMW_STM.createSTMbasedOnArgs(args);
		
		// then
		assertEquals("path/to/", stm.pathToSTPs);
		assertTrue(stm.dependencyManagement);
		// is not set if dependencyManagement true
		assertEquals(30000, stm.testTimeout);
	}
	
	@Test
	public void test_createSTMbasedOnArgs_allArgs_NumOf_threads(){
		// given
		String[] args = {"path/to/", "2", "1"};
		
		// when
		AMW_STM stm = AMW_STM.createSTMbasedOnArgs(args);
		
		// then
		assertEquals("path/to/", stm.pathToSTPs);
		assertEquals(2, stm.numberOfThreads);
		assertFalse(stm.dependencyManagement);
		assertEquals(1, stm.testTimeout);
	}
	
	@Test
	public void test_createZip(){
		// given
		AMW_STM stm = new AMW_STM();
		File resultsDir = new File("src/test/resources/test_result_dir/result1");
		// when
		stm.createZip(resultsDir , "testId");
		
		// then
		File zipFile = new File("src/test/resources/test_result_dir/testId.zip");
		
		assertNotNull(zipFile);
		
		zipFile.delete();
	}
	
	
	
	

	@Test
	public void getSTR() throws IOException {
		STR str = stm.getSTR();
		assertNotNull(str);
		assertNotNull(str.getStps());
		assertEquals(4,str.getStps().size());
		
	}
	
	@Test
	public void getSTS() throws IOException {
		STS sts = stm.getSTS(stm.getSTR());
		assertNotNull(sts);
		assertNotNull(sts.getStps());
		assertEquals(3, sts.getStps().size());
		
	}
	
	@Test
	public void getListOfSTPs(){
		List<String> stps = stm.getHousekeeping().getListOfSTPs("test/stps");
		assertNotNull(stps);
		for(String stp : stps){
			System.out.println(stp);
		}
	}
	
	@Test
	public void getOldSTPsToDelete() throws IOException{
		List<String> stps = stm.getHousekeeping().getOldSTPsToDelete(stm.getSTR(), "test/stps");
		assertNotNull(stps);
		assertEquals(0,stps.size());
		File f = new File("test/stps/somefile.zip");
		f.createNewFile();
		stps = stm.getHousekeeping().getOldSTPsToDelete(stm.getSTR(), "test/stps");
		assertNotNull(stps);
		assertEquals(1,stps.size());
		f.delete();
	}
	
	@Test
	public void cleanOldSTPsToDelete() throws IOException{
		File f = new File("test/stps/somefile.zip");
		f.createNewFile();
		List<String> stps = stm.getHousekeeping().getOldSTPsToDelete(stm.getSTR(), "test/stps");
		assertNotNull(stps);
		assertEquals(1,stps.size());
		stm.getHousekeeping().cleanOldSTPsToDelete(stm.getSTR(), "test/stps");
		assertFalse(f.exists());
		stps = stm.getHousekeeping().getOldSTPsToDelete(stm.getSTR(), "test/stps");
		assertNotNull(stps);
		assertEquals(0,stps.size());
	}
	
	@Test
	public void getMissingSTPs() throws IOException{
		List<String> stps = stm.getHousekeeping().getMissingSTPs(stm.getSTS(stm.getSTR()), "test/stps");
		assertNotNull(stps);
		assertEquals(1,stps.size());
		assertTrue(stps.get(0).equals("AnotherSTP-null.zip"));
		File f = new File("test/stps/AnotherSTP-null.zip");
		f.createNewFile();
		stps = stm.getHousekeeping().getMissingSTPs(stm.getSTS(stm.getSTR()), "test/stps");
		assertNotNull(stps);
		assertEquals(0,stps.size());
		f.delete();
	}

    @Test
   @Ignore("This test is for experimenting with quotes only and is not thought to be executed otherwise.")
    public void testProcessSTPs() throws Exception {
	   Runtime r = Runtime.getRuntime();
	   Process p = r.exec(new String[]{"sh", "-c", "/home/oschmid/projects/amw/git/amw_projects/AMW_stm/src/test/resources/test.sh simple \"complex argument\""});
	   InputStream errorin = p.getErrorStream();
	   InputStream in = p.getInputStream();
	   BufferedInputStream buf = new BufferedInputStream(in);
	   BufferedInputStream errbuf = new BufferedInputStream(errorin);
	   InputStreamReader inread = new InputStreamReader(buf);
	   InputStreamReader errinread = new InputStreamReader(errbuf);
	   BufferedReader bufferedreader = new BufferedReader(inread);
	   BufferedReader bufferederrreader = new BufferedReader(errinread);
	   String errorline;
	   while ((errorline = bufferederrreader.readLine()) != null) {
		  System.err.println(errorline);
	   }
	   String line;
	   while ((line = bufferedreader.readLine()) != null) {
		 System.out.println(line);
	   }
    }
}
