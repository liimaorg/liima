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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EntityBuilderType {

	AS("APPLICATIONSERVER", "amw"), APP("APPLICATION", "ch_puzzle_itc_mobi_amw"), NODE1("NODE", "node_01"), NODE2("NODE", "node_02"), AD("ActiveDirectory", "adIntern"), CERT("CertLoginModule",
			"certAdIntern"), DB2("DB2", "db2Host"), JBOSS7MANAGEMENT("JBoss7Management", "jboss7Management_Ldap"), KEYSTORE("Keystore", "jspCertJKS"), MAIL("Mail", "mailrelay"), CLUSTER("ModCluster",
			"proxy01"), TRUSTSTORE("Truststore", "mobiTrustJKS"), ZUSER("Zuser", "user001"), WS("Webservice", "ws1"), LB("LoadBalancer", "lb"), RUNTIME("RUNTIME", "eap 6");

	public String type;
	public String name;

	EntityBuilderType(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public static List<String> typeNames() {
		return vals().stream()
				.map(input -> input.type)
				.collect(Collectors.toList());
	}

	static List<EntityBuilderType> vals() {
		return new ArrayList<>(Arrays.asList(EntityBuilderType.values()));
	}

}
