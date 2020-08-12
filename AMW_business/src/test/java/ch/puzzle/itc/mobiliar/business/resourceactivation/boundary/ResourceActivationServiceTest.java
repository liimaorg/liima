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

package ch.puzzle.itc.mobiliar.business.resourceactivation.boundary;

import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.resourceactivation.control.ResourceActivation;
import ch.puzzle.itc.mobiliar.business.resourceactivation.entity.ResourceActivationEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationContextRepository;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.*;

public class ResourceActivationServiceTest {

    @Mock
    EntityManager entityManager;

    @Mock
    ResourceActivation resourceActivation;

    @Mock
    ContextDomainService contextDomainService;

    @Mock
    ResourceRelationService resourceRelationService;

    @Mock
    ResourceRelationContextRepository resourceRelationContextRepository;

    @InjectMocks
    ResourceActivationService resourceActivationService;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testActivateDeactivateResources() throws Exception {
        ConsumedResourceRelationEntity consumedResourceRelationEntity = mock(ConsumedResourceRelationEntity.class);
        when(entityManager.find(ConsumedResourceRelationEntity.class, -1)).thenReturn(consumedResourceRelationEntity);
        ContextEntity contextEntity = mock(ContextEntity.class);
        when(entityManager.find(ContextEntity.class, -1)).thenReturn(contextEntity);
        ResourceActivationEntity activateResourceActivationEntity = mock(ResourceActivationEntity.class);
        ResourceGroupEntity activateResourceGroupEntity = mock(ResourceGroupEntity.class);
        when(activateResourceActivationEntity.getResourceGroup()).thenReturn(activateResourceGroupEntity);
        when(activateResourceGroupEntity.getId()).thenReturn(1);
        ResourceGroupEntity deactivateResourceGroupEntity = mock(ResourceGroupEntity.class);
        when(entityManager.find(ResourceGroupEntity.class, 1)).thenReturn(activateResourceGroupEntity);
        when(entityManager.find(ResourceGroupEntity.class, 2)).thenReturn(deactivateResourceGroupEntity);
        when(resourceActivation
                  .getMostRelevantResourceActivationEntities(consumedResourceRelationEntity,
                            contextEntity)).thenReturn(new ArrayList(
                  Arrays.asList(activateResourceActivationEntity)));
        ResourceRelationContextEntity resourceRelationContextEntity = mock(ResourceRelationContextEntity.class);
        when(resourceRelationContextRepository.getResourceRelationContextWithResourceActivations(consumedResourceRelationEntity,
                 contextEntity)).thenReturn(resourceRelationContextEntity);

        resourceActivationService.activateDeactivateResources(-1, -1, Arrays.asList(2), Arrays.asList(1));

        verify(resourceActivation, times(1)).setResourceActivation(resourceRelationContextEntity, activateResourceGroupEntity, activateResourceActivationEntity, true);
        verify(resourceActivation, times(1)).setResourceActivation(resourceRelationContextEntity, deactivateResourceGroupEntity, null, false);
    }
}