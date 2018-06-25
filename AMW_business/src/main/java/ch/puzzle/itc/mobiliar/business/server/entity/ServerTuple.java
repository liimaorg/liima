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

package ch.puzzle.itc.mobiliar.business.server.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name="server")
@XmlAccessorType(XmlAccessType.FIELD)
//JAXB Annotations are ignored by Jackson of JBoss
@Getter
@Setter
@JsonAutoDetect()
public class ServerTuple {

	private String host;
	private String appServer;
	private String appServerRelease;
	private String runtime;
	private String node;
	private String nodeRelease;
	private String environment;
	@XmlTransient
	@JsonIgnore
	private Integer appServerId;
	@XmlTransient
	@JsonIgnore
	private Integer nodeId;
	@XmlTransient
	@JsonIgnore
	private Integer environmentId;
	private String domain;
	@XmlTransient
	@JsonIgnore
	private Integer domainId;
	private boolean definedOnNode;
	
	public ServerTuple() {}

	public ServerTuple(String host, String appServer, Integer appServerId, String appServerRelease, String runtime, String node,
			Integer nodeId, String nodeRelease, String domain, Integer domainId, String environment, Integer environmentId, boolean definedOnNode) {
		this.appServer = appServer;
		this.appServerId = appServerId;
		this.appServerRelease = appServerRelease;
		this.runtime = runtime;
		this.node = node;
		this.nodeId = nodeId;
		this.nodeRelease = nodeRelease;
		this.domain = domain;
		this.domainId = domainId;
		this.environment = environment;
		this.environmentId = environmentId;
		this.host = host;
		this.definedOnNode = definedOnNode;
	}
	
	//workaround for cb.literal(true) throwing a null pointer exception
	public ServerTuple(String host, String appServer, Integer appServerId, String appServerRelease, String runtime, String node,
			Integer nodeId, String nodeRelease, String domain, Integer domainId, String environment, Integer environmentId, int definedOnNode) {
		this(host, appServer, appServerId, appServerRelease, runtime, node, nodeId, nodeRelease, domain, domainId, environment, environmentId, definedOnNode == 1);
	}

	@Override
	public String toString() {
		return host + " " + appServer + " " + appServerRelease + " " + runtime + " " + node + " " + nodeRelease + " " + environment + " " + definedOnNode;

	}
}
