package ch.puzzle.itc.mobiliar.stm;

import static org.junit.Assert.*;

import org.junit.Test;

public class STPTest {

	@Test
	public void test_getCombinedFileName() {
		// given
		STP stp = new STP("name", "version");
		// when
		String combinedFileName = stp.getCombinedFileName();
		// then
		assertEquals("name-version.zip", combinedFileName);
	}
	
	@Test
	public void test_getCombinedFileName_null() {
		// given
		STP stp = new STP(null,null);
		// when
		String combinedFileName = stp.getCombinedFileName();
		// then
		assertEquals("null-null.zip", combinedFileName);
	}
	
	@Test
	public void test_getCombinedFileName_empty() {
		// given
		STP stp = new STP("", "version");
		// when
		String combinedFileName = stp.getCombinedFileName();
		// then
		assertEquals("-version.zip", combinedFileName);
	}
	
	@Test
	public void test_getCombinedFileName_empty_both() {
		// given
		STP stp = new STP("", "");
		// when
		String combinedFileName = stp.getCombinedFileName();
		// then
		assertEquals("-.zip", combinedFileName);
	}

}
