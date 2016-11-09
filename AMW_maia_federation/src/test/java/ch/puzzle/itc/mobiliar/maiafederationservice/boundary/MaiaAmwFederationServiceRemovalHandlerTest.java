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

package ch.puzzle.itc.mobiliar.maiafederationservice.boundary;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.ProcessingState;
import ch.mobi.xml.datatype.common.commons.v3.MessageSeverity;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyImportService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceImportService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.RelationImportService;
import ch.puzzle.itc.mobiliar.maiafederationservice.entity.ResourceHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MaiaAmwFederationServiceRemovalHandlerTest {

    private static final String APPNAME_A = "antiMatter";
    private static final String APPNAME_B = "matter";

    @InjectMocks
    MaiaAmwFederationServiceRemovalHandler serviceRemovalHandler;

    @Mock
    private ResourceRepository resourceRepositoryMock;
    @Mock
    private ResourceImportService resourceImportServiceMock;

    @Mock
    private PropertyImportService propertyImportServiceMock;

    @Mock
    private RelationImportService relationImportServiceMock;

    @Mock
    private Logger logMock;

    @Test
    public void removalOfAnInexistentAppShouldFail() {

        // given
        when(resourceRepositoryMock.getResourcesByGroupNameWithRelations(APPNAME_A)).thenReturn(null);

        // when
        ResourceHelper rh = serviceRemovalHandler.handleRemoval(APPNAME_A);

        // then
        assertEquals(ProcessingState.FAILED,rh.getProcessingState());
        assertEquals(1,rh.getMessages().size());
        assertEquals(MessageSeverity.ERROR,rh.getMessages().get(0).getSeverity());
    }

    @Test
    public void removalOfAnExistingAppShouldFail_AMW_owned() {

        // given
        ResourceEntity resource = ResourceEntityBuilder.createResourceEntity(APPNAME_B,1);
        List<ResourceEntity> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceRepositoryMock.getResourcesByGroupNameWithRelations(APPNAME_B)).thenReturn(resources);

        // when
        ResourceHelper rh = serviceRemovalHandler.handleRemoval(APPNAME_B);

        // then
        assertEquals(ProcessingState.FAILED,rh.getProcessingState());
        assertEquals(1,rh.getMessages().size());
        assertEquals(MessageSeverity.WARNING,rh.getMessages().get(0).getSeverity());
    }

    @Test
    public void removalOfAnExistingAppShouldSucceed_Maia_owned() {

        // given
        ResourceEntity resource = ResourceEntityBuilder.createResourceEntity(APPNAME_B,1);
        resource.setOwner(ForeignableOwner.MAIA);
        List<ResourceEntity> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceRepositoryMock.getResourcesByGroupNameWithRelations(APPNAME_B)).thenReturn(resources);

        // when
        ResourceHelper rh = serviceRemovalHandler.handleRemoval(APPNAME_B);

        // then
        assertEquals(ProcessingState.OK,rh.getProcessingState());
        assertEquals(1,rh.getMessages().size());
        assertEquals(MessageSeverity.INFO,rh.getMessages().get(0).getSeverity());
    }

}
