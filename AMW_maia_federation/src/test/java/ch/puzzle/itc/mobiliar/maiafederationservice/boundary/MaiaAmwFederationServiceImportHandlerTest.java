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
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.PropertyDeclaration;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.ProvidedPortID;
import ch.mobi.xml.datatype.common.commons.v3.MessageSeverity;
import ch.puzzle.itc.mobiliar.builders.ReleaseEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.SoftlinkRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyImportService;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyTypeScreenDomainService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtPersistenceService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceImportService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.RelationImportService;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.control.SoftlinkRelationService;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.entity.SoftlinkRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.maiafederationservice.entity.ResourceHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static ch.puzzle.itc.mobiliar.maiafederationservice.utils.FederationTestHelper.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MaiaAmwFederationServiceImportHandlerTest {


    private static final String APPNAME = "appName";
    private static final String TECHSTACK = "EAP6";
    private static final String FC_EXT_KEY = "TestAppFcKey";
    private static final String FC_EXT_LINK = "fcLink";
    private static final ReleaseEntity RELEASE_ALPHA = ReleaseEntityBuilder.createMainReleaseEntity("Alpha", 1);
    private static final ReleaseEntity RELEASE_BETA = ReleaseEntityBuilder.createMainReleaseEntity("Beta", 2);
    private static final PropertyTypeEntity MAIA_PROPERTY_TYPE = new PropertyTypeEntity();


    private static final PropertyDeclaration PROPERTY = createPropertyDeclaration();

    private static PropertyDeclaration createPropertyDeclaration() {
        return new PropertyDeclaration("technicalKey", "displayName", new ArrayList<String>(), "defaultValue", "exampleValue", false, "machineInterpretationKey", "validationPattern", false, false);
    }

    @Mock
    private ResourceBoundary resourceBoundaryMock;

    @Mock
    private ContextDomainService contextServiceMock;

    @Mock
    private ResourceTypeDomainService resourceTypeServiceMock;

    @Mock
    private PropertyImportService propertyImportServiceMock;

    @Mock
    private RelationImportService relationImportServiceMock;

    @Mock
    private ResourceRepository resourceRepositoryMock;

    @Mock
    private PropertyTypeScreenDomainService propertyTypeScreenDomainServiceMock;

    @Mock
    private ResourceDependencyResolverService resourceDependencyResolverService;

    @Mock
    private SoftlinkRelationService softlinkRelationService;

    @Mock
    private ReleaseMgmtPersistenceService releaseServiceMock;

    @Mock
    private Logger loggerMock;

    @Mock
    private ResourceImportService resourceImportServiceMock;

    private ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Application applicationMock = mock(ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Application.class);
    private ContextEntity globalContextMock = mock(ContextEntity.class);


    @InjectMocks
    private MaiaAmwFederationServiceImportHandler serviceImportHandler;

    @Before
    public void setUp() throws ResourceTypeNotFoundException, ElementAlreadyExistsException {
        MockitoAnnotations.initMocks(this);

        when(resourceBoundaryMock.createNewApplicationWithoutAppServerByName(ForeignableOwner.MAIA, FC_EXT_KEY, FC_EXT_LINK, APPNAME, RELEASE_ALPHA.getId(), true)).thenReturn(applicationMock);
        when(contextServiceMock.getGlobalResourceContextEntity()).thenReturn(globalContextMock);
        // mock a not yet existing app
        when(resourceRepositoryMock.getResourceByNameAndRelease(APPNAME,RELEASE_ALPHA)).thenReturn(null);

        when(releaseServiceMock.findByName(RELEASE_ALPHA.getName())).thenReturn(RELEASE_ALPHA);
        when(releaseServiceMock.findByName(RELEASE_BETA.getName())).thenReturn(RELEASE_BETA);

        MAIA_PROPERTY_TYPE.setPropertyTypeName(MaiaAmwFederationServiceImportHandler.MAIA_PROPERTY_TYPE);
        serviceImportHandler.maiaPropertyType = MAIA_PROPERTY_TYPE;
    }


    @Test
    public void updateForOneApplicationShouldReturnUpdateResultContainingAppName() throws Exception {
        // given
        when(applicationMock.getEntity()).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, 1));
        ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK, createAppReleaseBinding(RELEASE_ALPHA.getName(), null));

        // when
        ResourceHelper updateResult = serviceImportHandler.handleUpdateAggregate(app1);

        // then
        assertNotNull(updateResult);
        assertThat(updateResult.getAppName(), is(APPNAME));
    }

    @Test
    public void updateForOneApplicationShouldReturnUpdateResponseContainingAmwLink() throws Exception {
        // given
        Integer appId = 99;
        when(applicationMock.getEntity()).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, appId));
        when(resourceDependencyResolverService.findMostRelevantResource(anyList(),any(Date.class))).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, appId));
        ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK, createAppReleaseBinding(RELEASE_ALPHA.getName(), null));

        // when
        ResourceHelper updateResult = serviceImportHandler.handleUpdateAggregate(app1);

        // then
        assertNotNull(updateResult);
        assertThat(updateResult.getAppLink(), is(resourceImportServiceMock.getImportedResourceBacklink()+appId));
    }

    @Test(expected = AMWRuntimeException.class)
    public void updateForOneApplicationShouldThrowRuntimeExceptionWhenReleaseNotFound() throws Exception {
        // given
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK, createAppReleaseBinding(RELEASE_ALPHA.getName(), null));
        when(releaseServiceMock.findByName(RELEASE_ALPHA.getName())).thenReturn(null);

        // when
        ResourceHelper updateResult = serviceImportHandler.handleUpdateAggregate(app1);
    }

    @Test
    public void handleUpdateApplicationReleaseWithApplicationShouldDelegateApplicationCreationServiceCall() throws Exception {
        // given
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(), null);
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);

        when(applicationMock.getEntity()).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, 1));

        // when
        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);

        // then
        verify(resourceBoundaryMock).createNewApplicationWithoutAppServerByName(ForeignableOwner.MAIA,FC_EXT_KEY, FC_EXT_LINK, APPNAME, RELEASE_ALPHA.getId(), true);
    }

    @Test
    public void handleUpdateApplicationReleaseWithApplicationShouldReturnMessageWithInfoSeverity() throws Exception {
        // given
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(), null);
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);

        when(applicationMock.getEntity()).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, 1));

        // when
        ResourceHelper resourceHelper = serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);

        // then
        assertThat(resourceHelper.getMessages().get(0).getSeverity(), is(MessageSeverity.INFO.INFO));
    }

    @Test(expected = AMWRuntimeException.class)
    public void handleUpdateApplicationReleaseWithApplicationWhenNoResourceCreatedShouldThrowException() throws Exception {
        // given
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(), null);
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);

        when(resourceBoundaryMock.createNewApplicationWithoutAppServerByName(ForeignableOwner.MAIA, FC_EXT_KEY, FC_EXT_LINK, APPNAME, RELEASE_ALPHA.getId(), true)).thenReturn(null);

        // when
        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);
    }

    @Test(expected = AMWRuntimeException.class)
    public void handleUpdateApplicationReleaseWhenMaiaPropertyTypeIsNotSetShouldThrowException() throws Exception {
        // given
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(), createApplicationPropertiesPayload(PROPERTY));
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);

        serviceImportHandler.maiaPropertyType = null;

        when(applicationMock.getEntity()).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, 1));

        // when
        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);

    }

    @Test
    public void handleUpdateApplicationReleaseWithApplicationAndPropertiesShouldDelegatePropertyPersistCall() throws Exception {
        // given
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(), createApplicationPropertiesPayload(PROPERTY));
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);
        ResourceEntity application = ResourceEntityBuilder.createResourceEntity(APPNAME, 1);
        when(applicationMock.getEntity()).thenReturn(application);

        // when
        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);

        // then
        ResourceContextEntity resourceContext = application.getOrCreateContext(globalContextMock);
        verify(propertyImportServiceMock).savePropertyDescriptorWithTags(propertyDescriptorEntityEq(PROPERTY, MAIA_PROPERTY_TYPE),Matchers.any(List.class), eq(application), eq(ForeignableOwner.MAIA));
    }

    @Test(expected = AMWRuntimeException.class)
    public void handleUpdateApplicationReleaseWhenNoResourceTypeForCPIDefinedShouldThrowException() throws Exception {
        // given
        String consumedPortFcKey = "consumedPortFcKey";
        String consumedPortResourceType = "consumedPortResourceType";
        String providedPortRefName = "providedPortRefName";
        String consumedPortFcLink = null;
        String consumedPortDisplayName = "consumedPortDisplayName";
        String consumedPortLocalPortID = null;
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(), createConsumedPortPayload(createConsumedPortRelation(consumedPortFcKey, consumedPortFcLink, consumedPortDisplayName, consumedPortLocalPortID, consumedPortResourceType, providedPortRefName)));
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);

        when(applicationMock.getEntity()).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, 1));
        when(resourceTypeServiceMock.getResourceTypeIdByResourceTypeName(consumedPortResourceType)).thenReturn(null);

        // when
        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);

    }

    @Test
    public void handleUpdateApplicationReleaseWhenConsumedPortShouldCreationOfCpiResource() throws Exception {
        // given
        String consumedPortFcKey = "consumedPortFcKey";
        String consumedPortResourceType = "consumedPortResourceType";
        String providedPortRefName = "providedPortRefName";
        String consumedPortFcLink = null;
        String consumedPortDisplayName = "consumedPortDisplayName";
        String consumedPortLocalPortID = null;
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(), createConsumedPortPayload(createConsumedPortRelation(consumedPortFcKey, consumedPortFcLink, consumedPortDisplayName, consumedPortLocalPortID, consumedPortResourceType,providedPortRefName )));
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);

        Integer cpiTypeId = 99;
        ResourceEntity cpiResource = ResourceEntityBuilder.createResourceEntity(consumedPortDisplayName, 100);

        when(applicationMock.getEntity()).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, 1));
        when(resourceTypeServiceMock.getResourceTypeIdByResourceTypeName(consumedPortResourceType)).thenReturn(cpiTypeId);

        Resource resourceMock = mock(Resource.class);
        when(resourceMock.getEntity()).thenReturn(cpiResource);
        when(resourceBoundaryMock.getOrCreateNewResourceByName(ForeignableOwner.MAIA, consumedPortDisplayName, cpiTypeId, RELEASE_ALPHA.getId())).thenReturn(resourceMock);

        // when
        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);

        // then
        verify(resourceBoundaryMock).getOrCreateNewResourceByName(ForeignableOwner.MAIA, cpiResource.getName(), cpiTypeId, RELEASE_ALPHA.getId());
    }

    @Test
    public void handleUpdateApplicationReleaseWhenConsumedPortWithPropertiesShouldPersistPropertiesForCpi() throws Exception {
        // given
        String consumedPortFcKey = "consumedPortFcKey";
        String consumedPortResourceType = "consumedPortResourceType";
        String providedPortRefName = "providedPortRefName";
        String consumedPortFcLink = null;
        String consumedPortDisplayName = "consumedPortDisplayName";
        String consumedPortLocalPortID = null;
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(), createConsumedPortPayload(createConsumedPortRelation(consumedPortFcKey, consumedPortFcLink, consumedPortDisplayName, consumedPortLocalPortID, consumedPortResourceType, providedPortRefName, PROPERTY)));
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);

        Integer cpiTypeId = 99;
        ResourceEntity cpiResource = ResourceEntityBuilder.createResourceEntity(consumedPortDisplayName, 100);

        when(applicationMock.getEntity()).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, 1));
        when(resourceTypeServiceMock.getResourceTypeIdByResourceTypeName(consumedPortResourceType)).thenReturn(cpiTypeId);

        Resource resourceMock = mock(Resource.class);
        when(resourceMock.getEntity()).thenReturn(cpiResource);
        when(resourceBoundaryMock.getOrCreateNewResourceByName(ForeignableOwner.MAIA, consumedPortDisplayName, cpiTypeId, RELEASE_ALPHA.getId())).thenReturn(resourceMock);

        // when
        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);

        // then
        ResourceContextEntity resourceContext = cpiResource.getOrCreateContext(globalContextMock);
        verify(propertyImportServiceMock).savePropertyDescriptorWithTags(propertyDescriptorEntityEq(PROPERTY, MAIA_PROPERTY_TYPE),Matchers.any(List.class), eq(cpiResource), eq(ForeignableOwner.MAIA));
    }

//    @Test
//    public void handleUpdateApplicationReleaseWhenApplicationWithConsumedPortShouldCreateRelation() throws Exception {
//        // given
//        String consumedPortFcKey = "consumedPortFcKey";
//        String consumedPortResourceType = "consumedPortResourceType";
//        String providedPortRefName = "providedPortRefName";
//        String consumedPortFcLink = null;
//        String consumedPortDisplayName = "consumedPortDisplayName";
//        String consumedPortLocalPortID = null;
//        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(), createConsumedPortPayload(createConsumedPortRelation(consumedPortFcKey, consumedPortFcLink, consumedPortDisplayName, consumedPortLocalPortID, consumedPortResourceType,providedPortRefName )));
//        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);
//
//        Integer cpiTypeId = 99;
//        ResourceEntity cpiResource = createResourceEntity("cpiName", 100);
//
//        ResourceEntity application = createResourceEntity(APPNAME, 1);
//        when(applicationMock.getEntity()).thenReturn(application);
//        when(resourceTypeServiceMock.getResourceTypeIdByResourceTypeName(consumedPortResourceType)).thenReturn(cpiTypeId);
//
//        Resource resourceMock = mock(Resource.class);
//        when(resourceMock.getEntity()).thenReturn(cpiResource);
//        when(resourcesServiceMock.getOrCreateNewResourceByName(ForeignableOwner.MAIA, consumedPortDisplayName, cpiTypeId, RELEASE_ALPHA.getId())).thenReturn(resourceMock);
//
//        // when
//        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);
//
//        // then
//        verify(relationServiceMock).doAddResourceRelationForSpecificRelease(application.getId(), cpiResource.getResourceGroup().getId(), false, null, null, RELEASE_ALPHA.getId(), ForeignableOwner.MAIA);
//    }


    @Test(expected = AMWRuntimeException.class)
    public void handleUpdateApplicationReleaseWhenNoResourceTypeForPPIDefinedShouldThrowException() throws Exception {
        // given
        String providedPortName = "providedPortName";
        String providedPortFcKey = "providedPortFcKey";
        String providedPortResourceType = "providedPortResourceType";
        String providedPortFcLink = "providedPortFcLink";
        String providedPortDisplayName = "providedPortDisplayName";
        String providedPortLocalPortID = null;
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(),createProvidedPortPayload(createProvidedPortRelation(providedPortName, providedPortFcKey, providedPortFcLink, providedPortDisplayName, providedPortLocalPortID, providedPortResourceType)));

        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);

        when(applicationMock.getEntity()).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, 1));
        when(resourceTypeServiceMock.getResourceTypeIdByResourceTypeName(providedPortResourceType)).thenReturn(null);

        // when
        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);

    }

    @Test
    public void handleUpdateApplicationReleaseWhenProvidedPortShouldCreationOfPpiResource() throws Exception {
        // given
        String providedPortName = "providedPortName";
        String providedPortFcKey = "providedPortFcKey";
        String providedPortResourceType = "providedPortResourceType";
        String providedPortFcLink = "providedPortFcLink";
        String providedPortDisplayName = "providedPortDisplayName";
        String providedPortLocalPortID = "providedPortLocalPortID";
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(),createProvidedPortPayload(createProvidedPortRelation(providedPortName, providedPortFcKey, providedPortFcLink, providedPortDisplayName, providedPortLocalPortID, providedPortResourceType)));
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);

        Integer ppiTypeId = 99;
        ResourceEntity ppiResource = ResourceEntityBuilder.createResourceEntity(providedPortDisplayName, 100);

        ResourceEntity appEntity = ResourceEntityBuilder.createResourceEntity(APPNAME, 1);
        appEntity.setOwner(ForeignableOwner.MAIA);
                when(resourceRepositoryMock.getApplicationByNameAndRelease(APPNAME, RELEASE_ALPHA)).thenReturn(appEntity);
        when(applicationMock.getEntity()).thenReturn(appEntity);
        when(resourceTypeServiceMock.getResourceTypeIdByResourceTypeName(providedPortResourceType)).thenReturn(ppiTypeId);

        Resource resourceMock = mock(Resource.class);
        when(resourceMock.getEntity()).thenReturn(ppiResource);
        when(resourceBoundaryMock.getOrCreateNewResourceByName(ForeignableOwner.MAIA, providedPortDisplayName, ppiTypeId, RELEASE_ALPHA.getId())).thenReturn(resourceMock);

        // when
        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);

        // then
        verify(resourceBoundaryMock).getOrCreateNewResourceByName(ForeignableOwner.MAIA, ppiResource.getName(), ppiTypeId, RELEASE_ALPHA.getId());
    }

    @Test
    public void handleUpdateApplicationReleaseWhenProvidedPortWithPropertiesShouldPersistPropertiesForPpi() throws Exception {

        // given
        String providedPortName = "providedPortName";
        String providedPortFcKey = "providedPortFcKey";
        String providedPortResourceType = "providedPortResourceType";
        String providedPortFcLink = "providedPortFcLink";
        String providedPortDisplayName = "providedPortDisplayName";
        String providedPortLocalPortID = "providedPortLocalPortID";
        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(), createProvidedPortPayload(createProvidedPortRelation(providedPortName, providedPortFcKey, providedPortFcLink, providedPortDisplayName, providedPortLocalPortID, providedPortResourceType, PROPERTY)));
        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);

        Integer ppiTypeId = 99;
        ResourceEntity ppiResource = ResourceEntityBuilder.createResourceEntity("ppiName", 100);


        when(applicationMock.getEntity()).thenReturn(ResourceEntityBuilder.createResourceEntity(APPNAME, 1));
        when(resourceTypeServiceMock.getResourceTypeIdByResourceTypeName(providedPortResourceType)).thenReturn(ppiTypeId);

        Resource resourceMock = mock(Resource.class);
        when(resourceMock.getEntity()).thenReturn(ppiResource);
        when(resourceBoundaryMock.getOrCreateNewResourceByName(ForeignableOwner.MAIA, providedPortDisplayName, ppiTypeId, RELEASE_ALPHA.getId())).thenReturn(resourceMock);

        // when
        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);

        // then
        verify(propertyImportServiceMock).savePropertyDescriptorWithTags(propertyDescriptorEntityEq(PROPERTY, MAIA_PROPERTY_TYPE),Matchers.any(List.class), eq(ppiResource), eq(ForeignableOwner.MAIA));
    }

//    @Test
//    public void handleUpdateApplicationReleaseWhenApplicationWithProvidedPortShouldCreateRelation() throws Exception {
//
//        // given
//        String providedPortName = "providedPortName";
//        String providedPortFcKey = "providedPortFcKey";
//        String providedPortResourceType = "providedPortResourceType";
//        String providedPortFcLink = "providedPortFcLink";
//        String providedPortDisplayName = "providedPortDisplayName";
//        String providedPortLocalPortID = "providedPortLocalPortID";
//        ApplicationReleaseBinding appBinding = createAppReleaseBinding(RELEASE_ALPHA.getName(),createProvidedPortPayload(createProvidedPortRelation(providedPortName, providedPortFcKey, providedPortFcLink, providedPortDisplayName, providedPortLocalPortID, providedPortResourceType, PROPERTY)));
//        Application app1 = createApplication(APPNAME, TECHSTACK, FC_EXT_KEY, FC_EXT_LINK);
//
//        Integer ppiTypeId = 99;
//        ResourceEntity ppiResource = createResourceEntity(providedPortDisplayName, 100);
//
//
//        ResourceEntity application = createResourceEntity(APPNAME, 1);
//        when(applicationMock.getEntity()).thenReturn(application);
//        when(resourceTypeServiceMock.getResourceTypeIdByResourceTypeName(providedPortResourceType)).thenReturn(ppiTypeId);
//
//        Resource resourceMock = mock(Resource.class);
//        when(resourceMock.getEntity()).thenReturn(ppiResource);
//        when(resourcesServiceMock.getOrCreateNewResourceByName(ForeignableOwner.MAIA, providedPortDisplayName, ppiTypeId, RELEASE_ALPHA.getId())).thenReturn(resourceMock);
//
//        // when
//        serviceImportHandler.handleUpdateApplicationRelease(app1, appBinding, RELEASE_ALPHA);
//
//        // then
//        verify(resourcesServiceMock).getOrCreateNewResourceByName(ForeignableOwner.MAIA, ppiResource.getName(), ppiTypeId, RELEASE_ALPHA.getId());
//        verify(relationServiceMock).doAddResourceRelationForSpecificRelease(application.getId(), ppiResource.getResourceGroup().getId(), true, null, null, RELEASE_ALPHA.getId(), ForeignableOwner.MAIA);
//    }

    @Test
    public void shouldCreateSoftlinkRelation(){
        // given
        ResourceEntity cpi = new ResourceEntityBuilder().withId(1).withName("cpiResource").withOwner(ForeignableOwner.MAIA).build();
        String softLinkRef = "softLinkRef";
        ProvidedPortID providedPortID = new ProvidedPortID(softLinkRef);

        // when
        SoftlinkRelationEntity result = serviceImportHandler.createOrUpdateSoftlinkRelation(cpi, providedPortID);

        // then
        verify(softlinkRelationService, times(1)).setSoftlinkRelation(any(ResourceEntity.class), any(SoftlinkRelationEntity.class));
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(softLinkRef, result.getSoftlinkRef());
        assertEquals(cpi, result.getCpiResource());
        assertEquals(cpi.getOwner(), result.getOwner());
    }

    @Test
    public void shouldReplaceExistingSoftlinkRelation(){
        // given
        String existingSoftLinkRef = "existingSoftLinkRef";
        SoftlinkRelationEntity existingSoftlinkRel = new SoftlinkRelationEntityBuilder().withSoftlinkRef(existingSoftLinkRef).withId(111).withOwner
                (ForeignableOwner.MAIA).build();
        ResourceEntity cpi = new ResourceEntityBuilder().withId(1).withName("cpiResource").withSoftlinkRelation(existingSoftlinkRel).withOwner(ForeignableOwner.MAIA).mock();

        String softLinkRef = "softLinkRef";
        ProvidedPortID pDto = new ProvidedPortID(softLinkRef);

        // when
        SoftlinkRelationEntity result = serviceImportHandler.createOrUpdateSoftlinkRelation(cpi, pDto);

        // then
        verify(softlinkRelationService, times(1)).setSoftlinkRelation(any(ResourceEntity.class), any(SoftlinkRelationEntity.class));
        assertNotNull(result);
        assertEquals(softLinkRef, result.getSoftlinkRef());
        assertEquals(cpi, result.getCpiResource());
        assertEquals(cpi.getOwner(), result.getOwner());
    }

    @Test
    public void shouldRemoveExistingSoftlinkRelation(){
        // given
        String existingSoftLinkRef = "existingSoftLinkRef";
        SoftlinkRelationEntity existingSoftlinkRel = new SoftlinkRelationEntityBuilder().withSoftlinkRef(existingSoftLinkRef).withId(111).withOwner(ForeignableOwner.MAIA).build();
        ResourceEntity cpi = new ResourceEntityBuilder().withId(1).withName("cpiResource").withSoftlinkRelation(existingSoftlinkRel).withOwner(ForeignableOwner.MAIA).mock();

        // when
        SoftlinkRelationEntity result = serviceImportHandler.createOrUpdateSoftlinkRelation(cpi, null);

        // then
        verify(softlinkRelationService, times(1)).removeSoftlinkRelation(cpi);
        assertNull(result);
    }

    @Test
    public void shouldUpdateSoftlinkRelation(){
        // given
        SoftlinkRelationEntity slRel = new SoftlinkRelationEntity();
        String softLinkRef = "softLinkRef";
        Integer slRelId = 111;
        slRel.setOwner(ForeignableOwner.MAIA);
        slRel.setSoftlinkRef(softLinkRef);
        slRel.setId(slRelId);
        ResourceEntity cpi = new ResourceEntityBuilder().withId(1).withName("cpiResource").withOwner(ForeignableOwner.MAIA).withSoftlinkRelation(slRel).mock();
        ProvidedPortID pDto = new ProvidedPortID(softLinkRef);

        // when
        SoftlinkRelationEntity result = serviceImportHandler.createOrUpdateSoftlinkRelation(cpi, pDto);

        // then
        verify(softlinkRelationService, times(1)).setSoftlinkRelation(any(ResourceEntity.class), any(SoftlinkRelationEntity.class));
        assertNotNull(result);
        assertEquals(slRelId, result.getId());
        assertEquals(softLinkRef, result.getSoftlinkRef());
        assertEquals(cpi, result.getCpiResource());
        assertEquals(cpi.getOwner(), result.getOwner());
    }


}