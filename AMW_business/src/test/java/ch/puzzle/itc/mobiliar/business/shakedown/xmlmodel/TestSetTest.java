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

package ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel;

import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.TestSet.OverallStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.StringReader;

public class TestSetTest {
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGeneralFailure() {
		String message = "<testset id=\"1504\"><test name=\"AMW_stp_tcpping\" version=\"0.0.1-SNAPSHOT\">"
				+ "<command executionTime=\"1413894337461\">/tmp/0.39768303476961575/runtest.sh localhost -c 5</command>"
				+ "<stderr><![CDATA[  		]]></stderr>"
				+ "<stdout><![CDATA[SUCCESS: PING localhost.localdomain (127.0.0.1) 56(84) bytes of data."
				+ "64 bytes from localhost.localdomain (127.0.0.1): icmp_seq=1 ttl=64 time=0.032 ms"
				+ "64 bytes from localhost.localdomain (127.0.0.1): icmp_seq=2 ttl=64 time=0.041 ms"
				+ "64 bytes from localhost.localdomain (127.0.0.1): icmp_seq=3 ttl=64 time=0.039 ms"
				+ "64 bytes from localhost.localdomain (127.0.0.1): icmp_seq=4 ttl=64 time=0.041 ms"
				+ "64 bytes from localhost.localdomain (127.0.0.1): icmp_seq=5 ttl=64 time=0.040 ms"
				+ "--- localhost.localdomain ping statistics ---"
				+ "5 packets transmitted, 5 received, 0% packet loss, time 4000ms"
				+ "rtt min/avg/max/mdev = 0.032/0.038/0.041/0.007 ms"
				+ "]]></stdout>"
				+ "<testStatus>successful</testStatus>"
				+ "</test><failure>Exception: java.io.FileNotFoundException: /tmp/0.6569873192883965/lib/commons-codec-1.9.jar (Not a directory)</failure></testset>";
		TestSet set = JAXB.unmarshal(new StringReader(message), TestSet.class);
		Assert.assertEquals(OverallStatus.failed, set.getOverallStatus());
		Assert.assertTrue(set.getTestMessage().contains("testErrorMsg"));
		Assert.assertTrue(set.getTestMessage().contains("FileNotFoundException"));
	}
	
	@Test
	public void testPartialFailure() {
		String message = "<testset id=\"1504\"><test name=\"AMW_stp_tcpping\" version=\"0.0.1-SNAPSHOT\">"
				+ "<command executionTime=\"1413894337461\">/tmp/0.39768303476961575/runtest.sh localhost -c 5</command>"
				+ "<stderr><![CDATA[  		]]></stderr>"
				+ "<stdout><![CDATA[SUCCESS   ]]></stdout>"
				+ "<testStatus>successful</testStatus>"
				
				+ "</test>"
				+ "<test name=\"AMW_stp_tcpping\" version=\"0.0.1-SNAPSHOT\">"
				+ "<command executionTime=\"1413894337461\">/tmp/0.39768303476961575/runtest.sh localhost -c 5</command>"
				+ "<stderr><![CDATA[  SOMETHING WRONG		]]></stderr>"
				+ "<stdout><![CDATA[      ]]></stdout>"
				+ "<testStatus>failed</testStatus>"				
				+ "</test></testset>";
		TestSet set = JAXB.unmarshal(new StringReader(message), TestSet.class);
		Assert.assertEquals(OverallStatus.failed, set.getOverallStatus());
		Assert.assertTrue(set.getTestMessage().contains("testErrorMsg"));
		Assert.assertTrue(set.getTestMessage().contains("SOMETHING WRONG"));
	}
	
	@Test
	public void testSuccess() {
		String message = "<testset id=\"1504\"><test name=\"AMW_stp_tcpping\" version=\"0.0.1-SNAPSHOT\">"
				+ "<command executionTime=\"1413894337461\">/tmp/0.39768303476961575/runtest.sh localhost -c 5</command>"
				+ "<stderr><![CDATA[  		]]></stderr>"
				+ "<stdout><![CDATA[SUCCESS   ]]></stdout>"
				+ "<testStatus>successful</testStatus>"				
				+ "</test>"
				+ "<test name=\"AMW_stp_tcpping\" version=\"0.0.1-SNAPSHOT\">"
				+ "<command executionTime=\"1413894337461\">/tmp/0.39768303476961575/runtest.sh localhost -c 5</command>"
				+ "<stderr><![CDATA[		]]></stderr>"
				+ "<stdout><![CDATA[      ]]></stdout>"
				+ "<testStatus>successful</testStatus>"				
				+ "</test></testset>";
		TestSet set = JAXB.unmarshal(new StringReader(message), TestSet.class);
		Assert.assertEquals(OverallStatus.success, set.getOverallStatus());
	}

	@Test
	public void testWarning() {
		String message = "<testset id=\"1504\"><test name=\"AMW_stp_tcpping\" version=\"0.0.1-SNAPSHOT\">"
				+ "<command executionTime=\"1413894337461\">/tmp/0.39768303476961575/runtest.sh localhost -c 5</command>"
				+ "<stderr><![CDATA[  		]]></stderr>"
				+ "<stdout><![CDATA[SUCCESS   ]]></stdout>"
				+ "<testStatus>successful</testStatus>"				
				+ "</test>"
				+ "<test name=\"AMW_stp_tcpping\" version=\"0.0.1-SNAPSHOT\">"
				+ "<testStatus>missing</testStatus>"				
				+ "</test></testset>";
		TestSet set = JAXB.unmarshal(new StringReader(message), TestSet.class);
		Assert.assertEquals(OverallStatus.warning, set.getOverallStatus());
	}
}
