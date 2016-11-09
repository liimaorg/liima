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
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DeploymentInfo implements Comparable<DeploymentInfo> {
	private Gson gson = new GsonBuilder().create();
	private Type collectionType = new TypeToken<List<ApplicationWithVersion>>() {
	}.getType();

	public List<ApplicationWithVersion> apps;
	public ContextEntity context;
	public ResourceEntity appServer;

	public DeploymentInfo(ContextEntity context, ResourceEntity appServer, String appInfo) {
		this.context = context;
		this.appServer = appServer;
		this.apps = gson.fromJson(appInfo, collectionType);
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

	@SuppressWarnings("unchecked")
	public static List<DeploymentInfo> load(EntityManager entityManager) {
		Query query = entityManager
				.createQuery("select distinct d.resource, d.context, d.applicationsWithVersion from DeploymentEntity d where d.buildSuccess = true");
		List<Object[]> resultList = query.getResultList();
		List<DeploymentInfo> list = Lists.newArrayList();

		for (Object[] objects : resultList) {
			ResourceEntity resource = (ResourceEntity) objects[0];
			ContextEntity context = (ContextEntity) objects[1];
			String appInfo = (String) objects[2];

			list.add(new DeploymentInfo(context, resource, appInfo));
		}
		Collections.sort(list);
		return list;

	}

	public static List<DeploymentInfo> load(EntityManager entityManager, String server) {
		List<DeploymentInfo> deployments = load(entityManager);
		return filter(deployments, server);

	}

	public static List<DeploymentInfo> lastForContext(List<DeploymentInfo> allMos) {
		final Set<String> contextNames = Sets.newHashSet();
		return Lists.newArrayList(Collections2.filter(allMos, new Predicate<DeploymentInfo>() {

			@Override
			public boolean apply(DeploymentInfo input) {
				if (contextNames.add(input.context.getName())) {
					return true;
				}
				return false;
			}

		}));
	}

	public static List<DeploymentInfo> latest(List<DeploymentInfo> deployments, String server) {
		return lastForContext(filter(deployments, server));
	}

	public static List<DeploymentInfo> filter(List<DeploymentInfo> deployments, final String serverName) {
		return Lists.newArrayList(Collections2.filter(deployments, new Predicate<DeploymentInfo>() {
			@Override
			public boolean apply(DeploymentInfo input) {
				return input.appServer.getName().equals(serverName);
			}
		}));
	}

	public static Set<String> serverNames(List<DeploymentInfo> deployments) {
		Set<String> serverNames = Sets.newTreeSet();
		for (DeploymentInfo info : deployments) {
			serverNames.add(info.appServer.getName());
		}
		return serverNames;

	}
}
