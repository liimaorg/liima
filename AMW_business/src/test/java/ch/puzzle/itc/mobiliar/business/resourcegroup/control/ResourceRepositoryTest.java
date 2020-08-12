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


package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.builders.ContextEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.DeploymentEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ReleaseEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.Collections.EMPTY_SET;
import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class ResourceRepositoryTest {

    @InjectMocks
    ResourceRepository repository;

    @Mock
    EntityManager entityManager;

    @Mock
    Logger log;

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldPreserveDeploymentOnResourceRemoval() {

        // given
        ResourceTypeEntity appType = new ResourceTypeEntity();
        appType.setName("application");

        ResourceGroupEntity resGroup = new ResourceGroupEntity();
        resGroup.setResourceType(appType);
        resGroup.setName("test");
        resGroup.setId(12);

        ResourceEntity resource =  new ResourceEntity();
        resource.setResourceGroup(resGroup);
        resource.setName(resGroup.getName());
        resource.setResourceType(appType);
        resource.setId(21);

        ReleaseEntity release = ReleaseEntityBuilder.createMainReleaseEntity("release", null);
        ContextEntity global = new ContextEntityBuilder().buildContextEntity("global", null, EMPTY_SET, true);
        ContextEntity aContext = new ContextEntityBuilder().buildContextEntity("aContext", global, EMPTY_SET, true);

        DeploymentEntity deployment = new DeploymentEntityBuilder().buildDeploymentEntity(123, release, resource.getResourceGroup(), resource, true, aContext, true);
        Set<DeploymentEntity> deployments = new HashSet<>();
        deployments.add(deployment);
        resource.setDeployments(deployments);

        // when
        repository.remove(resource);

        // then
        assertThat(deployment.getExResourceId(), is(21));
        assertNull(deployment.getResource());
    }

    @Test
    public void shouldPreserveDeploymentOnRuntimeRemoval() {

        // given
        ResourceTypeEntity runtimeType = new ResourceTypeEntity();
        runtimeType.setName("RUNTIME");

        ResourceGroupEntity resGroup = new ResourceGroupEntity();
        resGroup.setResourceType(runtimeType);
        resGroup.setName("eap25");
        resGroup.setId(13);

        ReleaseEntity release = ReleaseEntityBuilder.createMainReleaseEntity("release", null);
        ResourceEntity runtimeResourceMock = new ResourceEntityBuilder().mockRuntimeEntity(resGroup.getName(), resGroup, release);
        ContextEntity global = new ContextEntityBuilder().buildContextEntity("global", null, EMPTY_SET, true);
        ContextEntity aContext = new ContextEntityBuilder().buildContextEntity("aContext", global, EMPTY_SET, true);

        DeploymentEntity deployment = new DeploymentEntityBuilder().buildDeploymentEntity(1234, release, runtimeResourceMock.getResourceGroup(), runtimeResourceMock, true, aContext, true);
        Set<DeploymentEntity> deployments = new HashSet<>();
        deployments.add(deployment);

        when(runtimeResourceMock.getId()).thenReturn(31);
        when(runtimeResourceMock.getDeploymentsOfRuntime()).thenReturn(deployments);

        // when
        repository.remove(runtimeResourceMock);

        // then
        assertThat(deployment.getExRuntimeResourceId(), is(31));
        assertNull(deployment.getRuntime());
    }
}
