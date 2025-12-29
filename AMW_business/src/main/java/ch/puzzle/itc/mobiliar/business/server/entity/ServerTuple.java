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

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/* Must match AMW_angular/io/src/app/servers/server.ts */
@XmlRootElement(name="server")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class ServerTuple {

	private String host;
	private String appServer;
	private Integer appServerId;
	private String appServerRelease;
	private String runtime;
	private String node;
	private Integer nodeId;
	private String nodeRelease;
	private String environment;
	private Integer environmentId;
	private String domain;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ServerTuple)) return false;
		ServerTuple that = (ServerTuple) o;
		return definedOnNode == that.definedOnNode &&
				Objects.equals(host, that.host) &&
				Objects.equals(appServer, that.appServer) &&
				Objects.equals(appServerRelease, that.appServerRelease) &&
				Objects.equals(runtime, that.runtime) &&
				Objects.equals(node, that.node) &&
				Objects.equals(nodeRelease, that.nodeRelease) &&
				Objects.equals(environment, that.environment) &&
				Objects.equals(appServerId, that.appServerId) &&
				Objects.equals(nodeId, that.nodeId) &&
				Objects.equals(environmentId, that.environmentId) &&
				Objects.equals(domain, that.domain) &&
				Objects.equals(domainId, that.domainId);
	}

	@Override
	public int hashCode() {

		return Objects.hash(host, appServer, appServerRelease, runtime, node, nodeRelease, environment, appServerId, nodeId, environmentId, domain, domainId, definedOnNode);
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
