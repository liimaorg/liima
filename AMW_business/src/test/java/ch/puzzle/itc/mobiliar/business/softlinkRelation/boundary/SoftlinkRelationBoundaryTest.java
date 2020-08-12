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

package ch.puzzle.itc.mobiliar.business.softlinkRelation.boundary;

import static org.mockito.Mockito.*;

import javax.persistence.EntityManager;

import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.SoftlinkRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.control.SoftlinkRelationService;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.entity.SoftlinkRelationEntity;

// disable Strict Stubbing because of extra mocking of the EntityBuilders
@RunWith(MockitoJUnitRunner.Silent.class)
public class SoftlinkRelationBoundaryTest {

    @Mock
    private SoftlinkRelationService softlinkRelationServiceMock;

    @Mock
    private EntityManager entityManagerMock;

    @Mock
    private ForeignableService foreignableServiceMock;

    @Mock
    private PermissionService permissionServiceMock;

    @InjectMocks
    private SoftlinkRelationBoundary softlinkRelationBoundary;

    ReleaseEntity pastRelease;

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void createSoftlinkRelationWhenNotYetExistShouldCreateNewSoftlinkRelationWithValues() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner creatingOwner = ForeignableOwner.AMW;
        Integer cpiResourceId = 1;
        String softlinkRef = "softlinkIdRef";

        ResourceEntity resourceEntity = new ResourceEntityBuilder().withId(cpiResourceId).build();
        when((entityManagerMock).find(ResourceEntity.class, cpiResourceId)).thenReturn(resourceEntity);

        // when
        softlinkRelationBoundary.createSoftlinkRelation(creatingOwner, cpiResourceId, softlinkRef);

        // then
        ArgumentCaptor<SoftlinkRelationEntity> argCapt = ArgumentCaptor.forClass(SoftlinkRelationEntity.class);
        verify(softlinkRelationServiceMock).setSoftlinkRelation(eq(resourceEntity), argCapt.capture());

        Assert.assertEquals(softlinkRef, argCapt.getValue().getSoftlinkRef());
        Assert.assertEquals(creatingOwner, argCapt.getValue().getOwner());
        Assert.assertEquals(resourceEntity.getId(), argCapt.getValue().getCpiResource().getId());
    }

    @Test
    public void createSoftlinkRelationWhenExistOwnerShouldVerifyForeignability() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner creatingOwner = ForeignableOwner.AMW;
        Integer cpiResourceId = 1;
        String softlinkRef = "softlinkIdRef";

        SoftlinkRelationEntity softlinkRelationEntity = new SoftlinkRelationEntityBuilder().withOwner(ForeignableOwner.MAIA).build();

        ResourceEntity resourceEntity = new ResourceEntityBuilder().withId(cpiResourceId).withSoftlinkRelation(softlinkRelationEntity).mock();


        when((entityManagerMock).find(ResourceEntity.class, cpiResourceId)).thenReturn(resourceEntity);

        // when
        softlinkRelationBoundary.createSoftlinkRelation(creatingOwner, cpiResourceId, softlinkRef);

        // then
        verify(foreignableServiceMock).verifyEditableByOwner(eq(creatingOwner), anyInt(), eq(softlinkRelationEntity));
    }

    @Test
    public void createSoftlinkRelationWhenExistShouldCreateNewSoftlinkRelationWithChangedValues() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner creatingOwner = ForeignableOwner.AMW;
        Integer cpiResourceId = 1;
        String oldSoftlinkRef = "oldSoftlinkIdRef";
        String softlinkRef = "softlinkIdRef";

        SoftlinkRelationEntity softlinkRelationEntity = new SoftlinkRelationEntityBuilder().withSoftlinkRef(oldSoftlinkRef).withOwner(creatingOwner).build();
        ResourceEntity resourceEntity = new ResourceEntityBuilder().withId(cpiResourceId).withSoftlinkRelation(softlinkRelationEntity).mock();
        when((entityManagerMock).find(ResourceEntity.class, cpiResourceId)).thenReturn(resourceEntity);

        // when
        softlinkRelationBoundary.createSoftlinkRelation(creatingOwner, cpiResourceId, softlinkRef);

        // then
        ArgumentCaptor<SoftlinkRelationEntity> argCapt = ArgumentCaptor.forClass(SoftlinkRelationEntity.class);
        verify(softlinkRelationServiceMock).setSoftlinkRelation(eq(resourceEntity), argCapt.capture());

        Assert.assertEquals(softlinkRef, argCapt.getValue().getSoftlinkRef());
        Assert.assertEquals(creatingOwner, argCapt.getValue().getOwner());
        Assert.assertEquals(resourceEntity.getId(), argCapt.getValue().getCpiResource().getId());
    }

    @Test(expected = RuntimeException.class)
    public void createSoftlinkRelationWhenNoResourceFoundForIdShouldThrowException() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner creatingOwner = ForeignableOwner.AMW;
        Integer cpiResourceId = 1;
        String softlinkRef = "softlinkIdRef";

        when((entityManagerMock).find(ResourceEntity.class, cpiResourceId)).thenReturn(null);

        // when
        softlinkRelationBoundary.createSoftlinkRelation(creatingOwner, cpiResourceId, softlinkRef);

    }


    @Test(expected = RuntimeException.class)
    public void removeRelationForResourceWhenNoResourceFoundForIdShouldThrowException() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        Integer cpiResourceId = 1;

        when((entityManagerMock).find(ResourceEntity.class, cpiResourceId)).thenReturn(null);

        // when
        softlinkRelationBoundary.removeRelationForResource(deletingOwner, cpiResourceId);

    }

    @Test
    public void removeRelationForResourceWhenResourceHasNoRelationShouldNotDelegateRemoveCall() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        Integer cpiResourceId = 1;

        ResourceEntity resourceEntity = new ResourceEntityBuilder().withId(cpiResourceId).mock();
        when((entityManagerMock).find(ResourceEntity.class, cpiResourceId)).thenReturn(resourceEntity);

        // when
        softlinkRelationBoundary.removeRelationForResource(deletingOwner, cpiResourceId);

        // then
        verify(softlinkRelationServiceMock, never()).removeSoftlinkRelation(resourceEntity);

    }

    @Test
    public void removeRelationForResourceShouldDelegateRemoveCall() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        Integer cpiResourceId = 1;

        SoftlinkRelationEntity softlinkRelationEntity = new SoftlinkRelationEntityBuilder().withOwner(deletingOwner).build();
        ResourceEntity resourceEntity = new ResourceEntityBuilder().withId(cpiResourceId).withSoftlinkRelation(softlinkRelationEntity).mock();
        when((entityManagerMock).find(ResourceEntity.class, cpiResourceId)).thenReturn(resourceEntity);

        // when
        softlinkRelationBoundary.removeRelationForResource(deletingOwner, cpiResourceId);

        // then
        verify(softlinkRelationServiceMock).removeSoftlinkRelation(resourceEntity);

    }

    @Test
    public void removeRelationForResourceShouldDelegateCheckForForeignableCall() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        Integer cpiResourceId = 1;

        SoftlinkRelationEntity softlinkRelationEntity = new SoftlinkRelationEntityBuilder().withOwner(deletingOwner).build();
        ResourceEntity resourceEntity = new ResourceEntityBuilder().withId(cpiResourceId).withSoftlinkRelation(softlinkRelationEntity).mock();
        when((entityManagerMock).find(ResourceEntity.class, cpiResourceId)).thenReturn(resourceEntity);

        // when
        softlinkRelationBoundary.removeRelationForResource(deletingOwner, cpiResourceId);

        // then
        verify(foreignableServiceMock).verifyDeletableByOwner(deletingOwner, softlinkRelationEntity);

    }



    @Test(expected = RuntimeException.class)
    public void editSoftlinkRelationWhenNoResourceFoundOnDbShouldThrowException() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner editingOwner = ForeignableOwner.AMW;
        Integer softlinkRelationId = 1;

        SoftlinkRelationEntity softlinkRelationEntity = new SoftlinkRelationEntityBuilder().withId(softlinkRelationId).withOwner(editingOwner).build();
        when((entityManagerMock).find(SoftlinkRelationEntity.class, softlinkRelationId)).thenReturn(null);

        // when
        softlinkRelationBoundary.editSoftlinkRelation(editingOwner, softlinkRelationEntity);

    }

    @Test(expected = NullPointerException.class)
    public void editSoftlinkRelationWithArgumentNullShouldThrowException() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner editingOwner = ForeignableOwner.AMW;

        // when
        softlinkRelationBoundary.editSoftlinkRelation(editingOwner, null);
    }

    @Test
    public void editSoftlinkRelationShouldDelegateCheckForForeignableCall() throws ForeignableOwnerViolationException {
        // given
        ForeignableOwner editingOwner = ForeignableOwner.AMW;
        Integer softlinkRelationId = 1;

        SoftlinkRelationEntity softlinkRelationEntity = new SoftlinkRelationEntityBuilder().withId(softlinkRelationId)
                .withOwner(editingOwner).withCpiResource(new ResourceEntityBuilder().mockResourceEntity("test", null, null, pastRelease, null)).build();
        when((entityManagerMock).find(SoftlinkRelationEntity.class, softlinkRelationId)).thenReturn(softlinkRelationEntity);

        SoftlinkRelationEntity mergedSoftlinkRelationEntity = new SoftlinkRelationEntityBuilder().withId(softlinkRelationId).withOwner(editingOwner).build();
        when((entityManagerMock).merge(softlinkRelationEntity)).thenReturn(mergedSoftlinkRelationEntity);

        // when
        softlinkRelationBoundary.editSoftlinkRelation(editingOwner, softlinkRelationEntity);

        // then
        verify(foreignableServiceMock).verifyEditableByOwner(eq(editingOwner), anyInt(), eq(mergedSoftlinkRelationEntity));
    }


    @Test
    public void getSoftlinkResolvableSlaveResourceWhenRelationIsNullShouldReturnNull() throws ForeignableOwnerViolationException {

        // when
        ResourceEntity result = softlinkRelationBoundary.getSoftlinkResolvableSlaveResource(null, pastRelease);

        // then
        Assert.assertNull(result);
    }

    @Test
    public void getSoftlinkResolvableSlaveResourceShouldDelegateToService() throws ForeignableOwnerViolationException {
        // given
        String softlinkId = "softlinkId";

        SoftlinkRelationEntity softlinkRelationEntity = new SoftlinkRelationEntityBuilder().withSoftlinkRef(softlinkId).build();

        // when
        softlinkRelationBoundary.getSoftlinkResolvableSlaveResource(softlinkRelationEntity, pastRelease);

        // then
        verify(softlinkRelationServiceMock).getSoftlinkResolvableSlaveResource(softlinkId, pastRelease);
    }
}