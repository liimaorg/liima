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

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sts")
public class STS {
	@XmlElement(name = "remoteHost")
	private String remoteHost;
	@XmlElement(name = "user")
	private String user;
	@XmlElement(name = "remoteSTPPath")
	private String remoteSTPPath;
	@XmlElement(name = "testId")
	private Integer testId;
	@XmlElementWrapper(name = "shakedowntests")
	@XmlElement(name = "shakedowntest")
	private List<String> shakedowntests;

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public String getRemoteSTPPath() {
		return remoteSTPPath;
	}

	public void setRemoteSTPPath(String remoteSTPPath) {
		this.remoteSTPPath = remoteSTPPath;
	}

	public Integer getTestId() {
		return testId;
	}

	public void setTestId(Integer testId) {
		this.testId = testId;
	}

	public List<String> getShakedowntests() {
		return shakedowntests;
	}

	public void setShakedowntests(List<String> shakedowntests) {
		this.shakedowntests = shakedowntests;
	}

	/**
	 * @return cleans line separators (replaces them with space) and returns all shakedowntest lines in a
	 *         single string
	 */
	public String getShakedowntestsAsCSV() {
		StringBuilder sb = new StringBuilder();
		sb.append(getTestId()).append(System.lineSeparator());
		if (shakedowntests != null) {
			for (String s : shakedowntests) {
				s = s.trim().replace(System.lineSeparator(), " ");
				int firstSpace = s.indexOf(' ');
				if (firstSpace != -1) {
					sb.append(s.substring(0, firstSpace)).append('\t');
					if (firstSpace < s.length() + 1) {
						sb.append(s.substring(firstSpace + 1));
					}
				}
				else {
					sb.append(s);
				}
				sb.append(System.lineSeparator());
			}
		}
		return sb.toString();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
