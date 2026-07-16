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

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DeploymentInfo implements Comparable<DeploymentInfo> {
	private ObjectMapper objectMapper = new ObjectMapper();

	public List<ApplicationWithVersion> apps;
	public ContextEntity context;
	public ResourceEntity appServer;

	public DeploymentInfo(ContextEntity context, ResourceEntity appServer, String appInfo) {
		this.context = context;
		this.appServer = appServer;
		try {
			CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, ApplicationWithVersion.class);
			this.apps = objectMapper.readValue(appInfo, collectionType);
		} catch (IOException e) {
			throw new RuntimeException("Failed to parse application info JSON", e);
		}
		sortApps();
	}

	private void sortApps() {
		Collections.sort(this.apps, new Comparator<ApplicationWithVersion>() {
			@Override
			public int compare(ApplicationWithVersion arg0, ApplicationWithVersion arg1) {
				return new DefaultArtifactVersion(arg0.getVersion()).compareTo(new DefaultArtifactVersion(arg1.getVersion()));
			}

		});
	}

	private DefaultArtifactVersion appVersion() {
		return new DefaultArtifactVersion(apps.get(0).getVersion());
	}

	@Override
	public int compareTo(DeploymentInfo o) {
		int name = appServer.getName().compareTo(o.appServer.getName());
		if (name == 0) {
			int ctx = context.getName().compareTo(o.context.getName());
			if (ctx == 0) {
				return appVersion().compareTo(o.appVersion()) * -1;
			}
			else {
				return ctx;
			}
		}
		return name;
	}

	@Override
	public String toString() {
		return "Info [resource=" + appServer.getName() + "(" + appServer.getId() + ") " + ", context=" + context.getName() + "("
				+ context.getId() + "), firstApp: " + apps.get(0) + "]";
	}

	public static List<DeploymentInfo> filter(List<DeploymentInfo> deployments, final String serverName) {
		List<DeploymentInfo> result = new ArrayList<>();
		for (DeploymentInfo deployment : deployments) {
			if (deployment.appServer.getName().equals(serverName)) {
				result.add(deployment);
			}
		}
		return result;
	}
}
