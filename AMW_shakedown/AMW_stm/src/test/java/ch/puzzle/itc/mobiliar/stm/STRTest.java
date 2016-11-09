package ch.puzzle.itc.mobiliar.stm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class STRTest {


	
	@Test
	public void test_STR_with_empty_str() {
		// given
		STR str = new STR("");
		
		//when
		List<STP> stps = str.getStps();
		List<String> stpNames = str.getStpNames();
		
		STP stpByName = str.getSTPByName("name");
		
		//then
		assertEquals(0,stps.size());
		assertEquals(0,stpNames.size());
		assertNull(stpByName);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_STR_with_null() {
		// given
		new STR(null);
	}
	
	@Test
	public void test_STR_Full() {
		// given
		String csv = "name" + STR.STR_CSV_ROW_DELIM + "test" + STR.STR_CSV_LINE_DELIM 
					+ "name2" + STR.STR_CSV_ROW_DELIM + "version2" + STR.STR_CSV_LINE_DELIM ;
		STR str = new STR(csv);
		
		//when
		List<STP> stps = str.getStps();
		List<String> stpNames = str.getStpNames();
		
		STP stp1 = str.getSTPByName("name");
		STP stp2 = str.getSTPByName("name2");
		
		//then
		assertEquals(2,stps.size());
		assertEquals(2,stpNames.size());
		assertEquals("name-test.zip", stpNames.get(0));
		assertEquals("name2-version2.zip", stpNames.get(1));
		
		assertEquals("name", stp1.getName());
		assertEquals("test", stp1.getVersion());
		
		assertEquals("name2", stp2.getName());
		assertEquals("version2", stp2.getVersion());
		// the second time its taken out of the 
		assertEquals(stpNames, str.getStpNames());
	}
	
	@Test
	public void test_parseSTR_one_row() {
		// given
		String csv = "name" + STR.STR_CSV_ROW_DELIM + "test";

		//when
		STR str = new STR("");
		List<STP> parseSTR = str.parseSTR(csv);
		
		//then
		assertNotNull(parseSTR);
		assertEquals(1,parseSTR.size());
		assertEquals("name", parseSTR.get(0).getName());
		assertEquals("test", parseSTR.get(0).getVersion());
		assertNull(parseSTR.get(0).getParams());
	}
	
	@Test
	public void test_parseSTR_only_one_col() {
		// given
		String csv = "name";

		//when
		STR str = new STR("");
		List<STP> parseSTR = str.parseSTR(csv);
		
		//then
		assertNotNull(parseSTR);
		assertEquals(0,parseSTR.size());
	}
	
	@Test
	public void test_parseSTR_empty() {
		// given

		//when
		STR str = new STR("");
		List<STP> parseSTR = str.parseSTR("");
		
		//then
		assertNotNull(parseSTR);
		assertEquals(0,parseSTR.size());
	}
	
	@Test(expected=NullPointerException.class)
	public void test_parseSTR_null() {
		// given

		//when
		STR str = new STR("");
		str.parseSTR(null);
		
	}
	
	@Test
	public void test_parseSTR_only_one_col_inkl_tab() {
		// given
		String csv = "name" + STR.STR_CSV_ROW_DELIM;

		//when
		STR str = new STR("");
		List<STP> parseSTR = str.parseSTR(csv);
		
		//then
		assertNotNull(parseSTR);
		assertEquals(0,parseSTR.size());
	}
	
	@Test
	public void test_parseSTR_two_rows() {
		// given
		String csv = "name" + STR.STR_CSV_ROW_DELIM + "test" + STR.STR_CSV_LINE_DELIM 
					+ "name2" + STR.STR_CSV_ROW_DELIM + "version2" + STR.STR_CSV_LINE_DELIM ;

		//when
		STR str = new STR("");
		List<STP> parseSTR = str.parseSTR(csv);
		
		//then
		assertNotNull(parseSTR);
		assertEquals(2,parseSTR.size());
		assertEquals("name", parseSTR.get(0).getName());
		assertEquals("test", parseSTR.get(0).getVersion());
		assertNull(parseSTR.get(0).getParams());
		assertEquals("name2", parseSTR.get(1).getName());
		assertEquals("version2", parseSTR.get(1).getVersion());
		assertNull(parseSTR.get(1).getParams());
	}
	@Test
	public void test_parseSTR_two_rows_no_DelimLine() {
		// given
		String csv = "name" + STR.STR_CSV_ROW_DELIM + "test" + STR.STR_CSV_LINE_DELIM 
					+ "name2" + STR.STR_CSV_ROW_DELIM + "version2"  ;

		//when
		STR str = new STR("");
		List<STP> parseSTR = str.parseSTR(csv);
		
		//then
		assertNotNull(parseSTR);
		assertEquals(2,parseSTR.size());
		assertEquals("name", parseSTR.get(0).getName());
		assertEquals("test", parseSTR.get(0).getVersion());
		assertNull(parseSTR.get(0).getParams());
		assertEquals("name2", parseSTR.get(1).getName());
		assertEquals("version2", parseSTR.get(1).getVersion());
		assertNull(parseSTR.get(1).getParams());
	}
	
	@Test
	public void test_check_constants() {
		// be avare if changing the Constant, all Templates must be changed too
		assertEquals("\n\r",STR.STR_CSV_LINE_DELIM);
		assertEquals("\t",STR.STR_CSV_ROW_DELIM);
		
	}
	
	@Test
	public void testCreateSTPByName(){
		STR str = new STR("test"+STR.STR_CSV_ROW_DELIM+"version");
		STP stp = str.createSTPByName("test");
		Assert.assertNotNull(stp);
		Assert.assertEquals("test", stp.getName());
		Assert.assertEquals("version", stp.getVersion());
		
	}
	

}
