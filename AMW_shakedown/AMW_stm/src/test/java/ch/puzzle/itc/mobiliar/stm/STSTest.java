package ch.puzzle.itc.mobiliar.stm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class STSTest {

	STS sts = new STS("", new STR(""), "sh");

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testUnzip() throws IOException {
		File tempPath = sts.unzip("src/test/resources/zippedSTP.zip");
		Assert.assertTrue(tempPath.exists());
		sts.deleteDirectoryRecursively(tempPath.getAbsolutePath());
	}
	
	@Test
	public void testParseSTS(){
		STR str = Mockito.mock(STR.class);
		STP testStp = Mockito.mock(STP.class);
		STP anotherStp = Mockito.mock(STP.class);
		Mockito.when(str.getSTPByName("Test_STP")).thenReturn(testStp);
		Mockito.when(str.getSTPByName("Another_STP")).thenReturn(anotherStp);
		String stsCSV = "1111"+System.lineSeparator()
				+ "Test_STP\tsomeargument"+System.lineSeparator()
				+ "Test_STP\tanotherargument"+System.lineSeparator()
				+ "Another_STP\tanotherargument dsfsa fasf asd"+System.lineSeparator();
				
		List<STP> stps = sts.parseSTS(stsCSV, str);
		
		Assert.assertEquals(3, stps.size());
		Mockito.verify(str, Mockito.times(2)).createSTPByName("Test_STP");
		Assert.assertEquals("Test_STP", stps.get(0).getName());
		Assert.assertEquals("Test_STP", stps.get(1).getName());
		Assert.assertEquals("Another_STP", stps.get(2).getName());
	}
	



}
