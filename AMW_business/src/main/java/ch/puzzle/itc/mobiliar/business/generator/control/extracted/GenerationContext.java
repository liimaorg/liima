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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.NodeJobEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties.container.DeploymentProperties;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.ApplicationWithVersionEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenerationContext {

	private DeploymentEntity deployment;

	private ContextEntity context;
	private ResourceEntity applicationServer;
	private ReleaseEntity targetRelease;
	private Date deploymentDate;
	private Integer deploymentId;
	private ResourceEntity targetPlatform;
	private GenerationModus generationModus;
	private Set<ApplicationWithVersionEntity> applicationsWithVersion;
	private ResourceEntity node;
	private boolean testing;
	private ResourceDependencyResolverService resourceDependencyResolver;

	private String generationDir;
	private NodeJobEntity nodeJobEntity;
	private List<GlobalFunctionEntity> globalFunctions;

	public ResourceEntity getResourceEntityForRelease(ResourceEntity r) {
		return getResourceEntityForRelease(r.getResourceGroup());
	}

	public ResourceEntity getResourceEntityForRelease(ResourceGroupEntity resourceGroup) {
		return resourceDependencyResolver.getResourceEntityForRelease(resourceGroup, targetRelease);
	}

	public GenerationContext(ContextEntity context, ResourceEntity applicationServer,
			DeploymentEntity deployment, Date deploymentDate, GenerationModus generationModus,
			ResourceDependencyResolverService resourceDependencyResolver) {
		this.context = context;
		this.applicationServer = applicationServer;
		this.deployment = deployment;
		if (deployment != null) {
			this.targetRelease = deployment.getRelease();
			this.deploymentId = deployment.getId();
			this.applicationsWithVersion = deployment.getApplicationsWithVersion() == null ? new HashSet<ApplicationWithVersionEntity>()
					: deployment.getApplicationsWithVersion();
			this.targetPlatform = deployment.getRuntime();
		}
		else {
			this.applicationsWithVersion = new HashSet<>();
		}

		this.deploymentDate = deploymentDate;
		this.generationModus = generationModus;
		this.resourceDependencyResolver = resourceDependencyResolver;
	}

	public ContextEntity getContext() {
		return context;
	}

	public ResourceEntity getApplicationServer() {
		return applicationServer;
	}

	public ReleaseEntity getTargetRelease() {
		return targetRelease;
	}

     public ResourceEntity getTargetPlatform(){
	    return targetPlatform;
	}
	/**
	 * Returns the identifier of the resource group representing the given target platform. We want to have
	 * the resource group id, since we want to be independent of the (potential) releases of a runtime.
	 * 
	 * @return
	 */
	public Integer getTargetPlatformId() {
		if (targetPlatform != null && targetPlatform.getResourceGroup() != null) {
			return targetPlatform.getResourceGroup().getId();
		}
		return null;
	}


	public GenerationModus getGenerationModus() {
		return generationModus;
	}

	public Date getDeploymentDate() {
		return deploymentDate;
	}

	public ResourceEntity getNode() {
		return node;
	}

	public void setNode(ResourceEntity node) {
		this.node = node;
	}

	public Set<ApplicationWithVersionEntity> getApplicationsWithVersion() {
		return applicationsWithVersion;
	}

	public boolean isTesting() {
		return testing;
	}

	public void setTesting(boolean testing) {
		this.testing = testing;
	}

	public Integer getDeploymentId() {
		return deploymentId;
	}

	public DeploymentEntity getDeployment() {
		return deployment;
	}

	public ResourceDependencyResolverService getResourceDependencyResolver() {
		return resourceDependencyResolver;
	}

	public void setGenerationDir(String generationDir) {
		this.generationDir = generationDir;
	}

	public void setNodeJobEntity(NodeJobEntity nodeJobEntity) {
		this.nodeJobEntity = nodeJobEntity;
	}
	
	public NodeJobEntity getNodeJobEntity() {
		return this.nodeJobEntity;
	}

	/**
	 * Returns the DeploymentProperties
	 * 
	 * @return
	 */
	public DeploymentProperties getDeploymentProperties() {
		DeploymentProperties prop = new DeploymentProperties();
		prop.setNode(node);
		prop.setNodeJobEntity(nodeJobEntity);
		prop.setDeployment(deployment);
		prop.setRelease(targetRelease);
		prop.setGenerationDir(generationDir);
		prop.setGenerationModus(generationModus);
		prop.setTargetPlatform(targetPlatform);

		return prop;
	}

	public void setGlobalFunctions(List<GlobalFunctionEntity> globalFunctions) {
		this.globalFunctions = globalFunctions;
	}

	public List<GlobalFunctionEntity> getGlobalFunctions() {
		return globalFunctions;
	}
	
	public GenerationContext copyGenerationContextForOtherAs(ResourceEntity as){
		GenerationContext c = new GenerationContext(context, as, deployment, deploymentDate, generationModus, resourceDependencyResolver);
		c.setTesting(testing);
		
		return c;
	}
	

}
