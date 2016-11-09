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

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.Application;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.ApplicationReleaseBinding;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.ApplicationUpdateResult;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.Message;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateRequest;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateResponse;
import ch.mobi.xml.datatype.common.commons.v3.CallContext;
import ch.mobi.xml.datatype.common.commons.v3.MessageSeverity;
import ch.puzzle.itc.mobiliar.builders.ReleaseEntityBuilder;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtPersistenceService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ejb.EJBException;
import java.util.logging.Logger;

import static ch.puzzle.itc.mobiliar.maiafederationservice.utils.FederationTestHelper.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MaiaAmwFederationServiceApplicationBeanTest {

    @Mock
    private ReleaseMgmtPersistenceService releaseServiceMock;

    @Mock
    private MaiaAmwFederationServiceImportHandler federationServiceImportHandlerMock;

    @Mock
    private ResourceLocator resourceLocatorMock;

    @Mock
    private Logger loggerMock;

    @InjectMocks
    private MaiaAmwFederationServiceApplicationBean maiaAmwFederationServiceApplicationBean;

    private static final String OWNER = ForeignableOwner.MAIA.name();
    private static final CallContext CALLCONTEXT = new CallContext("caller", "callerUser", "callerUUID");

    private static final String APPNAME = "appName";
    private static final String TECHSTACK = "EAP6";
    private static final String FC_EXT_KEY = "TestAppFcKey";
    private static final String FC_EXT_LINK = "fcLink";
    private static final ReleaseEntity RELEASE_ALPHA = ReleaseEntityBuilder.createMainReleaseEntity("Alpha", 1);
    private static final ReleaseEntity RELEASE_BETA = ReleaseEntityBuilder.createMainReleaseEntity("Beta", 2);

    @Before
    public void setup(){
        // mock (create new release entity)
        when(releaseServiceMock.findByName(RELEASE_ALPHA.getName())).thenReturn(RELEASE_ALPHA);
        when(releaseServiceMock.findByName(RELEASE_BETA.getName())).thenReturn(RELEASE_BETA);
    }

    @Test
    public void updateWhenEjbAmwRuntimeExceptionIsThrownShouldReturnUpdateResponseContainingErrorMessage() throws Exception{
        // given
        ApplicationReleaseBinding appReleaseBinding1 = createAppReleaseBinding(RELEASE_ALPHA.getName(), null);
        Application application = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK, appReleaseBinding1);
        UpdateRequest updateRequest = createUpdateRequest(application);

        when(federationServiceImportHandlerMock.handleUpdateAggregate(application)).thenThrow(new EJBException(new AMWRuntimeException("some amw runtime exception")));

        // when
        UpdateResponse updateResponse = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, OWNER, updateRequest);

        // then
        assertNotNull(updateResponse);
        ApplicationUpdateResult processedApplicationInfo = updateResponse.getProcessedApplications().get(0);
        Message message = processedApplicationInfo.getMessages().get(0);
        assertThat(message.getSeverity(), is(MessageSeverity.ERROR));
    }

    @Test
    public void updateWhenAmwRuntimeExceptionIsThrownShouldReturnUpdateResponseContainingErrorMessage() throws Exception{
        // given
        ApplicationReleaseBinding appReleaseBinding1 = createAppReleaseBinding(RELEASE_ALPHA.getName(), null);
        Application application = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK, appReleaseBinding1);
        UpdateRequest updateRequest = createUpdateRequest(application);

        when(federationServiceImportHandlerMock.handleUpdateAggregate(application)).thenThrow(new AMWRuntimeException("some amw runtime exception"));

        // when
        UpdateResponse updateResponse = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, OWNER, updateRequest);

        // then
        assertNotNull(updateResponse);
        ApplicationUpdateResult processedApplicationInfo = updateResponse.getProcessedApplications().get(0);
        Message message = processedApplicationInfo.getMessages().get(0);
        assertThat(message.getSeverity(), is(MessageSeverity.ERROR));
    }

    @Test(expected = EJBException.class)
    public void updateWhenAnyEjbOtherThanAmwRuntimeExceptionIsThrownShouldThrowException() throws Exception {
        // given
        ApplicationReleaseBinding appReleaseBinding1 = createAppReleaseBinding(RELEASE_ALPHA.getName(), null);
        Application application = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK, appReleaseBinding1);
        UpdateRequest updateRequest = createUpdateRequest(application);

        when(federationServiceImportHandlerMock.handleUpdateAggregate(application)).thenThrow(new EJBException(new NullPointerException("some nullpointer exception")));

        // when
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, OWNER, updateRequest);
    }

}