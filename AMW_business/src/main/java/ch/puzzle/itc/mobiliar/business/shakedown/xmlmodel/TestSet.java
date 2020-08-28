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

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "testset")
@Getter
@Setter
public class TestSet {

	@XmlAttribute(required = true)
	protected int id;
	@XmlElement(name = "test")
	private ArrayList<Test> tests;
	@XmlElement(name = "failure")
	private String failure;
	
	public OverallStatus getOverallStatus() {
		if(StringUtils.isNotEmpty(failure)){
			return OverallStatus.failed;
		}
		if (tests != null) {
			boolean potentialWarning = false;
			for (Test t : tests) {
				if (t.getTestStatus().equals("failed")) {
					return OverallStatus.failed;
				} else if (t.getTestStatus().equals("missing")) {
					potentialWarning = true;
				}
			}
			return potentialWarning ? OverallStatus.warning : OverallStatus.success;
		}
		return OverallStatus.warning;

	}

	public String getTestMessage() {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotEmpty(failure)){
			sb.append("<span class=\"testErrorMsg\">General failure: ").append(failure).append("</span><br/>");
		}
		if (tests != null) {
			for (Test test : tests) {
				sb.append(test.getName()).append(' ');
				if(test.getVersion()!=null && !test.getVersion().trim().isEmpty()) {
					sb.append(" (").append(test.getVersion().trim()).append(")");
				}
				sb.append(": <span class=\"testStatus\">").append(test.getTestStatus()).append("</span><br/>");
				if (test.getStdErr() != null && !test.getStdErr().trim().isEmpty()) {
					sb.append("<span class=\"testErrorMsg\">").append(test.getStdErr().trim()).append("</span><br/>");
				}
				if (test.getStdOut() != null && !test.getStdOut().trim().isEmpty()) {
					sb.append("<span class=\"testMsg\">").append(test.getStdOut().trim()).append("</span><br/>");
				}
				sb.append("<br/>");
			}
		}
		return sb.toString();

	}

	public enum OverallStatus {
		success, warning, failed;
	}
}
