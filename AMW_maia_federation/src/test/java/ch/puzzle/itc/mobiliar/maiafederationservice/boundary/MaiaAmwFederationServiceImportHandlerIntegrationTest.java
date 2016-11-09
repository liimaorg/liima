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
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateRequest;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateResponse;
import ch.mobi.xml.datatype.common.commons.v3.MessageSeverity;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.BusinessException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.TechnicalException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.ValidationException;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.entity.SoftlinkRelationEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.maiafederationservice.utils.BaseIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class MaiaAmwFederationServiceImportHandlerIntegrationTest extends BaseIntegrationTest {

    // usecase files
    public static final String USECASE_FUTURE_RELEASE_WS = "usecase_future_release-ws.xml";
    public static final String USECASE_SINGLE_APPLICATION_WS = "usecase_import_application-ws.xml";
    public static final String USECASE_WITH_MIK_WS = "usecase_with_mik-ws.xml";
    public static final String USECASE_SIMPLE_WS = "usecase_simple-ws.xml";
    public static final String USECASE_FIRST_FAIL_WS = "usecase_first_fail-ws.xml";
    public static final String USECASE_PARTIAL_FAIL_WS = "usecase_partial_fail-ws.xml";
    public static final String USECASE_MULTI_RELEASE_WS = "usecase_multi_release_app-ws.xml";
    public static final String USECASE_CREATE_SOFTLINK_WS = "usecase_create_softlink-ws.xml";
    public static final String USECASE_REMOVE_SOFTLINK_WS = "usecase_remove_softlink-ws.xml";
    public static final String USECASE_UPDATE_SOFTLINK_WS = "usecase_update_softlink-ws.xml";

    @Before
    public void setUp(){
        super.setUp();
        createAndAddMainRelease14_10();
    }

    @After
    public void tearDown(){
        super.tearDown();
    }

    @Test
    public void shouldNotCreateApplicationWithNotYetExistingRelease() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_FUTURE_RELEASE_WS);

        // the following parameter must match the names given in usecase file
        String applicationName = "ch_mobi_eamstuff_SnapshotComponent_v1_default";

        // assure the application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName);
        assertTrue(applications.isEmpty());

        // when
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        //then
        applications = getResourceByName(applicationName);
        assertTrue(applications.isEmpty());

    }


    @Test
    public void createNewApplication() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_SINGLE_APPLICATION_WS);

        // the following parameter must match the names given in usecase file
        String applicationName = "ch_mobi_testing_Playground_v1_default";
        String applicationTypeName = "APPLICATION";
        String externalKey = "ch.mobi.testing.Playground.v1.default";
        // &amp; must be unescaped
        String externalLink = "http://maiaurlbase.foobar?t=COMPONENT&q=ch.mobi.testing.Playground.v1";
        String releaseName = "Past";

        // consumed port
        // TODO change this, consumedPort.getProvidedPortRef().getName() is not the real name, the name of the consumed port is missing - not (yet) included in the request
        String consumedSlaveResourceName = "ch.mobi.testing.LibraryServiceComponent.v1.default#seiTestService";
        String consumedSlaveResourceTypeName = "TokyoWSCPI";

        // consumed port properties
        String technicalKey = "ch.mobi.testing.business.application.client.ejb.tokyo.seitestservice.v1.SEITestServiceClientBean_ENDPOINT_URL";
        String displayName = "endpoint";
        String exampleValue = "http://localhost:8080/___REPLACEME_context_root_ws___/SEITestService";

        // provided port
        String providedSlaveResourceName = "ch.mobi.testing.LibraryServiceComponent.v1.default#libraryService";
        String providedSlaveResourceTypeName = "TokyoWSPPI";

        // assure the application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName);
        assertTrue(applications.isEmpty());

        // when
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        applications = getResourceByName(applicationName);
        assertFalse(applications.isEmpty());
        ResourceEntity application = applications.get(0);

        assertThat(application.getName(), is(applicationName));
        assertThat(application.getResourceGroup().getName(), is(applicationName));

        assertThat(application.getOwner(), is(ForeignableOwner.MAIA));
        assertThat(application.getExternalKey(), is(externalKey));
        assertThat(application.getExternalLink(), is(externalLink));
        assertThat(application.getRelease().getName(), is(releaseName));
        assertThat(application.getResourceType().getName(), is(DefaultResourceTypeDefinition.APPLICATION.name()));

        // TODO alle importierten und erstellten resourcen/properties/relationen testen

        // consumed
        assertEquals(1, application.getConsumedMasterRelations().size());

        ConsumedResourceRelationEntity consumedResourceRelationEntity = application.getConsumedMasterRelations().iterator().next();
        assertEquals(applicationName,consumedResourceRelationEntity.getMasterResourceName());
        assertEquals(applicationTypeName, consumedResourceRelationEntity.getMasterResource().getResourceType().getName());
        assertEquals(releaseName, consumedResourceRelationEntity.getMasterResource().getRelease().getName());
        assertEquals(releaseName, consumedResourceRelationEntity.getMasterRelease());

        assertEquals(consumedSlaveResourceName,consumedResourceRelationEntity.getSlaveResource().getName());
        assertEquals(consumedSlaveResourceTypeName,consumedResourceRelationEntity.getSlaveResource().getResourceType().getName());
        assertEquals(releaseName, consumedResourceRelationEntity.getSlaveResource().getRelease().getName());

        Set<ResourceContextEntity> rce = consumedResourceRelationEntity.getSlaveResource().getContexts();
        for (ResourceContextEntity resourceContextEntity : rce) {
            Set<PropertyDescriptorEntity> propDescs = resourceContextEntity.getPropertyDescriptors();
            for (PropertyDescriptorEntity propDesc : propDescs) {
                assertEquals(technicalKey, propDesc.getPropertyName());
                assertEquals(exampleValue, propDesc.getExampleValue());
                assertEquals(displayName,propDesc.getDisplayName());
                assertTrue(propDesc.isOptional());
                // default values: false
                assertFalse(propDesc.isNullable());
                assertFalse(propDesc.isEncrypt());
                assertFalse(propDesc.isCustomType());
                assertFalse(propDesc.isTesting());
            }
        }

        // provided
        assertEquals(1, application.getProvidedMasterRelations().size());

        ProvidedResourceRelationEntity providedResourceRelationEntity = application.getProvidedMasterRelations().iterator().next();
        assertEquals(applicationName,providedResourceRelationEntity.getMasterResource().getName());
        assertEquals(applicationTypeName, providedResourceRelationEntity.getMasterResource().getResourceType().getName());
        assertEquals(releaseName, providedResourceRelationEntity.getMasterResource().getRelease().getName());

        providedResourceRelationEntity.getSlaveResource().getResourceType();
        assertEquals(providedSlaveResourceTypeName, providedResourceRelationEntity.getSlaveResource().getResourceType().getName());
        assertEquals(providedSlaveResourceName, providedResourceRelationEntity.getSlaveResource().getName());
        assertEquals(releaseName, providedResourceRelationEntity.getSlaveResource().getRelease().getName());

    }

    @Test
    public void updateOfExistingApplicationShouldSucceed() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_SINGLE_APPLICATION_WS);

        // the following parameter must match the names given in usecase file
        String applicationName = "ch_mobi_testing_Playground_v1_default";

        // assure the application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName);
        assertTrue(applications.isEmpty());

        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        assertEquals(MessageSeverity.INFO, response.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

        applications = getResourceByName(applicationName);
        assertFalse(applications.isEmpty());

        // when
        response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        assertEquals(1, response.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, response.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

    }

    @Test
    public void updateOfExistingApplicationShouldRemoveNoLongerExistingRelation() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_SINGLE_APPLICATION_WS);

        // the following parameter must match the names given in usecase file
        String applicationName = "ch_mobi_testing_Playground_v1_default";

        // assure the application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName);
        assertTrue(applications.isEmpty());

        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        assertEquals(MessageSeverity.INFO, response.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

        applications = getResourceByName(applicationName);
        assertFalse(applications.isEmpty());
        assertFalse(applications.get(0).getProvidedMasterRelations().isEmpty());

        // when
        updateRequest.getApplications().get(0).getStructure().get(0).getPayload().getProvidedPorts().remove(0);
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        applications = getResourceByName(applicationName);
        assertFalse(applications.isEmpty());

        // then
        assertTrue(applications.get(0).getProvidedMasterRelations().isEmpty());

    }

    @Test
    public void updateOfExistingApplicationShouldAddNewRelation() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_SINGLE_APPLICATION_WS);

        // the following parameter must match the names given in usecase file
        String applicationName = "ch_mobi_testing_Playground_v1_default";
        String providedPortLocalPortId = "libraryService";

        // assure the application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName);
        assertTrue(applications.isEmpty());

        updateRequest.getApplications().get(0).getStructure().get(0).getPayload().getProvidedPorts().remove(0);
        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        assertEquals(MessageSeverity.INFO, response.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

        applications = getResourceByName(applicationName);
        assertFalse(applications.isEmpty());

        // when
        updateRequest = getUpdateRequestFor(USECASE_SINGLE_APPLICATION_WS);
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        applications = getResourceByName(applicationName);
        assertFalse(applications.isEmpty());

        // then
        assertFalse(applications.get(0).getProvidedMasterRelations().isEmpty());
        assertEquals(providedPortLocalPortId, applications.get(0).getProvidedMasterRelations().iterator().next().getSlaveResource().getLocalPortId());

    }

    @Test
    public void updateOfExistingApplicationShouldUpdateItsForeignKeysAndLinks() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_SINGLE_APPLICATION_WS);

        // the following parameter must match the names given in usecase file
        String applicationName = "ch_mobi_testing_Playground_v1_default";
        String appNewFcKey = "appNewFcKey";
        String consumedNewFcKey = "consumedNewFcKey";
        String providedNewFcLink = "providedNewFcLink";

        // assure the application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName);
        assertTrue(applications.isEmpty());

        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        assertEquals(MessageSeverity.INFO, response.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

        applications = getResourceByName(applicationName);
        assertFalse(applications.isEmpty());

        assertFalse(applications.get(0).getExternalKey().equals(appNewFcKey));
        assertFalse(applications.get(0).getConsumedMasterRelations().iterator().next().getSlaveResource().getExternalKey().equals(consumedNewFcKey));
        assertFalse(applications.get(0).getProvidedMasterRelations().iterator().next().getSlaveResource().getExternalLink().equals(providedNewFcLink));

        // when
        updateRequest.getApplications().get(0).setFcKey(appNewFcKey);
        updateRequest.getApplications().get(0).getStructure().get(0).getPayload().getConsumedPorts().get(0).setFcKey(consumedNewFcKey);
        updateRequest.getApplications().get(0).getStructure().get(0).getPayload().getProvidedPorts().get(0).setFcLink(providedNewFcLink);
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        applications = getResourceByName(applicationName);
        assertFalse(applications.isEmpty());

        // then
        assertEquals(appNewFcKey, applications.get(0).getExternalKey());
        assertEquals(consumedNewFcKey, applications.get(0).getConsumedMasterRelations().iterator().next().getSlaveResource().getExternalKey());
        assertEquals(providedNewFcLink, applications.get(0).getProvidedMasterRelations().iterator().next().getSlaveResource().getExternalLink());
    }


    @Test
    public void createNewApplicationWithMik() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_WITH_MIK_WS);

        // the following parameter must match the names given in usecase file
        String applicationName = "ch_mobi_testing_LibraryServiceComponent_v3_default";
        String externalKey = "ch.mobi.testing.LibraryServiceComponent.v3.default";
        // &amp; must be unescaped
        String externalLink = "http://maiaurlbase.foobar?t=COMPONENT&q=ch.mobi.testing.LibraryServiceComponent.v3";
        String releaseName = "Past";

        String consExternalKey = "ch.mobi.testing.LibraryServiceComponent.v3.default#someMixedConsumerHornet";
        String displayName = "endpoint";
        String mik = "WS_CLIENT_URL";

        // assure the application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName);
        assertTrue(applications.isEmpty());

        // when
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        applications = getResourceByName(applicationName);
        assertFalse(applications.isEmpty());
        ResourceEntity application = applications.get(0);

        assertThat(application.getName(), is(applicationName));
        assertThat(application.getResourceGroup().getName(), is(applicationName));

        assertThat(application.getOwner(), is(ForeignableOwner.MAIA));
        assertThat(application.getExternalKey(), is(externalKey));
        assertThat(application.getExternalLink(), is(externalLink));
        assertThat(application.getRelease().getName(), is(releaseName));
        assertThat(application.getResourceType().getName(), is(DefaultResourceTypeDefinition.APPLICATION.name()));
        assertEquals(2,application.getConsumedMasterRelations().size());
        assertEquals(2,application.getProvidedMasterRelations().size());

        Set<ConsumedResourceRelationEntity> consumed = application.getConsumedMasterRelations();
        OUT:for (ConsumedResourceRelationEntity consumedResourceRelationEntity : consumed) {
            assertEquals(applicationName,consumedResourceRelationEntity.getMasterResource().getName());
            if (consumedResourceRelationEntity.getSlaveResource().getExternalKey().equals(consExternalKey)) {
                Set<ResourceContextEntity> rce = consumedResourceRelationEntity.getSlaveResource().getContexts();
                for (ResourceContextEntity resourceContextEntity : rce) {
                    Set<PropertyDescriptorEntity> propDescs = resourceContextEntity.getPropertyDescriptors();
                    assertEquals(11,propDescs.size());
                    for (PropertyDescriptorEntity propDesc : propDescs) {
                        if (propDesc.getDisplayName().equals(displayName)) {
                            assertEquals(mik,propDesc.getMachineInterpretationKey());
                            break OUT;
                        }
                    }
                }
            }
        }

    }


    @Test
    public void createTwoNewApplications() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_SIMPLE_WS);

        // first app
        // the following parameter must match the names given in usecase file
        String applicationName_1 = "ch_mobi_testing_LibraryServiceComponent_v2_default";
        String applicationExternalKey_1 = "ch.mobi.testing.LibraryServiceComponent.v2.default";

        // optional/mandatory properties
        int expectedOptional = 8;
        int expectedMandatory = 3;

        // seccond app
        String applicationName_2 = "ch_mobi_testing_Playground_v2_default";

        // assure the first application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName_1);
        assertTrue(applications.isEmpty());

        // assure the second application isn't persisted yet
        applications = getResourceByName(applicationName_2);
        assertTrue(applications.isEmpty());

        // when
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        applications = getResourceByName(applicationName_1);
        assertFalse(applications.isEmpty());
        ResourceEntity application = applications.get(0);

        assertEquals(applicationName_1,application.getName());
        assertEquals(applicationExternalKey_1,application.getExternalKey());
        assertEquals(1,application.getProvidedMasterRelations().size());
        assertEquals(1,application.getConsumedMasterRelations().size());

        Set<ConsumedResourceRelationEntity> consumed = application.getConsumedMasterRelations();
        ConsumedResourceRelationEntity consumedResourceRelationEntity = consumed.iterator().next();

        assertEquals(applicationName_1,consumedResourceRelationEntity.getMasterResource().getName());

        Set<ResourceContextEntity> rce = consumedResourceRelationEntity.getSlaveResource().getContexts();
        ResourceContextEntity resourceContextEntity = rce.iterator().next();

        Set<PropertyDescriptorEntity> propDescs = resourceContextEntity.getPropertyDescriptors();
        assertEquals(11,propDescs.size());
        int optional = 0;
        int mandatory = 0;
        for (PropertyDescriptorEntity propDesc : propDescs) {
            if(propDesc.isOptional()) {
                optional++;
            }
            else {
                mandatory++;
            }
        }

        assertEquals(expectedOptional,optional);
        assertEquals(expectedMandatory,mandatory);

        applications = getResourceByName(applicationName_2);
        assertFalse(applications.isEmpty());
        application = applications.get(0);

        assertEquals(applicationName_2,application.getName());
        assertEquals(1,application.getProvidedMasterRelations().size());

    }

    @Test
    public void createTwoNewApplicationsThenUpdateTheFirstRemovingTwoProperties() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_SIMPLE_WS);

        // first app
        // the following parameter must match the names given in usecase file
        String applicationName_1 = "ch_mobi_testing_LibraryServiceComponent_v2_default";

        // expected number of consumed port properties
        int numberOfConsumedPropertiesBefore = 11;
        int numberOfConsumedPropertiesAfter = numberOfConsumedPropertiesBefore-2;

        // seccond app
        String applicationName_2 = "ch_mobi_testing_Playground_v2_default";

        // assure the first application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName_1);
        assertTrue(applications.isEmpty());

        // assure the second application isn't persisted yet
        applications = getResourceByName(applicationName_2);
        assertTrue(applications.isEmpty());

        // when
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        applications = getResourceByName(applicationName_1);
        assertFalse(applications.isEmpty());
        ResourceEntity application = applications.get(0);

        Set<ConsumedResourceRelationEntity> consumed = application.getConsumedMasterRelations();
        ConsumedResourceRelationEntity consumedResourceRelationEntity = consumed.iterator().next();

        assertEquals(applicationName_1,consumedResourceRelationEntity.getMasterResource().getName());

        Set<ResourceContextEntity> rce = consumedResourceRelationEntity.getSlaveResource().getContexts();
        ResourceContextEntity resourceContextEntity = rce.iterator().next();

        Set<PropertyDescriptorEntity> propDescs = resourceContextEntity.getPropertyDescriptors();
        assertEquals(numberOfConsumedPropertiesBefore,propDescs.size());

        updateRequest.getApplications().get(0).getStructure().get(0).getPayload().getConsumedPorts().get(0).getProperties().remove(0);
        updateRequest.getApplications().get(0).getStructure().get(0).getPayload().getConsumedPorts().get(0).getProperties().remove(updateRequest.getApplications().get(0).getStructure().get(0).getPayload().getConsumedPorts().get(0).getProperties().size() - 1);

        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        applications = getResourceByName(applicationName_1);
        assertFalse(applications.isEmpty());
        application = applications.get(0);

        // then
        consumed = application.getConsumedMasterRelations();
        consumedResourceRelationEntity = consumed.iterator().next();
        rce = consumedResourceRelationEntity.getSlaveResource().getContexts();
        resourceContextEntity = rce.iterator().next();
        propDescs = resourceContextEntity.getPropertyDescriptors();

        assertEquals(numberOfConsumedPropertiesAfter, propDescs.size());

    }


    @Test
    public void shouldCreateSecondApplicationAfterFailureOfFirst() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_FIRST_FAIL_WS);

        // the following parameter must match the names given in usecase file
        String applicationName_1 = "ch_mobi_testing_Playground_first_default";
        String applicationName_2 = "ch_mobi_testing_Playground_second_default";

        // assure the first application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName_1);
        assertTrue(applications.isEmpty());

        // assure the second application isn't persisted yet
        applications = getResourceByName(applicationName_2);
        assertTrue(applications.isEmpty());

        // when
        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        //then
        assertEquals(2,response.getProcessedApplications().size());
        assertEquals(ProcessingState.FAILED, response.getProcessedApplications().get(0).getState());
        assertEquals(MessageSeverity.ERROR, response.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(1).getState());
        assertEquals(MessageSeverity.INFO, response.getProcessedApplications().get(1).getMessages().get(0).getSeverity());

        applications = getResourceByName(applicationName_1);
        assertTrue(applications.isEmpty());

        applications = getResourceByName(applicationName_2);
        assertFalse(applications.isEmpty());

        ResourceEntity application = applications.get(0);

        assertEquals(1,application.getProvidedMasterRelations().size());
        assertEquals(1, application.getConsumedMasterRelations().size());

    }


    @Test
    public void shouldAbortCreationOfFirstApplicationAndContinueWithSecond() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_PARTIAL_FAIL_WS);

        // the following parameter must match the names given in usecase file
        String applicationName_1 = "ch_mobi_testing_Playground_one_default";
        String applicationName_2 = "ch_mobi_testing_Playground_two_default";

        // assure the first application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName_1);
        assertTrue(applications.isEmpty());

        // assure the second application isn't persisted yet
        applications = getResourceByName(applicationName_2);
        assertTrue(applications.isEmpty());

        // when
        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        assertEquals(2,response.getProcessedApplications().size());
        assertEquals(ProcessingState.FAILED, response.getProcessedApplications().get(0).getState());
        assertEquals(MessageSeverity.ERROR, response.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(1).getState());
        assertEquals(MessageSeverity.INFO, response.getProcessedApplications().get(1).getMessages().get(0).getSeverity());

        applications = getResourceByName(applicationName_2);
        assertFalse(applications.isEmpty());

        ResourceEntity application = applications.get(0);

        assertEquals(1,application.getProvidedMasterRelations().size());
        assertEquals(1, application.getConsumedMasterRelations().size());

    }


    @Test
    public void shouldCreateMultipleReleasesOfSameApplication() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_MULTI_RELEASE_WS);

        // the following parameter must match the names given in usecase file
        String applicationName = "ch_mobi_testing_Elan_v59_default";

        // assure the application isn't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName);
        assertTrue(applications.isEmpty());

        // when
        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        assertEquals(1,response.getProcessedApplications().size());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(0).getState());
        assertEquals(MessageSeverity.INFO, response.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
        // one message per release
        assertEquals(2,response.getProcessedApplications().get(0).getMessages().size());

        applications = getResourceByName(applicationName);
        assertFalse(applications.isEmpty());

    }

    @Test
    public void shouldCreateSoftlinkRelation() throws BusinessException, TechnicalException, ValidationException{
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_CREATE_SOFTLINK_WS);

        // the following parameter must match the names given in usecase file
        String app1Name = "ch_mobi_testing_Playground_app_forCpi";
        String app2Name = "ch_mobi_testing_Playground_app_forPpi";
        String cpiName = "ch.mobi.testing.LibraryServiceComponent.cpi.default#seiTestService";
        String ppiName = "ch.mobi.testing.LibraryServiceComponent.ppi.default#libraryService";
        String softlinkRef = "ch.mobi.testing.Playground.ppi.default#libraryService_1.1";

        // assure the resources aren't persisted yet
        assertTrue(getResourceByName(app1Name).isEmpty());
        assertTrue(getResourceByName(app2Name).isEmpty());
        assertTrue(getResourceByName(cpiName).isEmpty());
        assertTrue(getResourceByName(ppiName).isEmpty());

        // when
        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        assertEquals(2,response.getProcessedApplications().size());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(0).getState());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(1).getState());

        List<ResourceEntity> cpiList = getResourceByName(cpiName);
        assertFalse(cpiList.isEmpty());
        assertEquals(1, cpiList.size());
        ResourceEntity cpi = cpiList.get(0);
        SoftlinkRelationEntity softlinkRelation = softlinkRelationService.getSoftLinkRelationByCpiAndSoftlinkRef(cpi, softlinkRef);
        assertNotNull(softlinkRelation);

        List<ResourceEntity> ppiList = getResourceByName(ppiName);
        assertFalse(ppiList.isEmpty());
        assertEquals(1, ppiList.size());
        ResourceEntity ppi = ppiList.get(0);
        assertEquals(softlinkRef, ppi.getSoftlinkId());
    }

    @Test
    public void shouldUpdateSoftlinkRelation() throws BusinessException, TechnicalException, ValidationException{
        // given
        UpdateRequest createRequest = getUpdateRequestFor(USECASE_CREATE_SOFTLINK_WS);
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_UPDATE_SOFTLINK_WS);

        // the following parameter must match the names given in usecase file
        String app1Name = "ch_mobi_testing_Playground_app_forCpi";
        String app2Name = "ch_mobi_testing_Playground_app_forPpi";
        String cpiName = "ch.mobi.testing.LibraryServiceComponent.cpi.default#seiTestService";
        String ppiName = "ch.mobi.testing.LibraryServiceComponent.ppi.default#libraryService";
        String softlinkRef = "ch.mobi.testing.Playground.ppi.default#libraryService_1.1";
        String softlinkRef2 = "ch.mobi.testing.Playground.ppi.default#libraryService_1.2";

        // execute update to create some content in database
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, createRequest);

        // assure this resources are persisted
        assertFalse(getResourceByName(app1Name).isEmpty());
        assertFalse(getResourceByName(cpiName).isEmpty());
        assertNotNull(softlinkRelationService.getSoftLinkRelationByCpiAndSoftlinkRef(getResourceByName(cpiName).get(0), softlinkRef));
        assertFalse(getResourceByName(app2Name).isEmpty());
        assertFalse(getResourceByName(ppiName).isEmpty());

        // when
        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        assertEquals(1,response.getProcessedApplications().size());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(0).getState());

        List<ResourceEntity> cpiList = getResourceByName(cpiName);
        assertFalse(cpiList.isEmpty());
        assertEquals(1, cpiList.size());
        ResourceEntity cpi = cpiList.get(0);
        SoftlinkRelationEntity softlinkRelation2 = softlinkRelationService.getSoftLinkRelationByCpiAndSoftlinkRef(cpi, softlinkRef2);
        assertNotNull(softlinkRelation2);
        SoftlinkRelationEntity softlinkRelation = softlinkRelationService.getSoftLinkRelationByCpiAndSoftlinkRef(cpi, softlinkRef);
        assertNull(softlinkRelation);

        List<ResourceEntity> ppiList = getResourceByName(ppiName);
        assertFalse(ppiList.isEmpty());
        assertEquals(1, ppiList.size());
        ResourceEntity ppi = ppiList.get(0);
        assertEquals(softlinkRef, ppi.getSoftlinkId());
    }

    @Test
    public void shouldRemoveSoftlinkRelation() throws BusinessException, TechnicalException, ValidationException{
        // given
        UpdateRequest createRequest = getUpdateRequestFor(USECASE_CREATE_SOFTLINK_WS);
        UpdateRequest removeRequest = getUpdateRequestFor(USECASE_REMOVE_SOFTLINK_WS);

        // the following parameter must match the names given in usecase file
        String app1Name = "ch_mobi_testing_Playground_app_forCpi";
        String app2Name = "ch_mobi_testing_Playground_app_forPpi";
        String cpiName = "ch.mobi.testing.LibraryServiceComponent.cpi.default#seiTestService";
        String ppiName = "ch.mobi.testing.LibraryServiceComponent.ppi.default#libraryService";

        String softlinkRef = "ch.mobi.testing.Playground.ppi.default#libraryService_1.1";

        // execute update to create some content in database
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, createRequest);

        // assure this resources are persisted
        assertFalse(getResourceByName(app1Name).isEmpty());
        assertFalse(getResourceByName(cpiName).isEmpty());
        assertNotNull(softlinkRelationService.getSoftLinkRelationByCpiAndSoftlinkRef(getResourceByName(cpiName).get(0), softlinkRef));
        assertFalse(getResourceByName(app2Name).isEmpty());
        assertFalse(getResourceByName(ppiName).isEmpty());

        // when
        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, removeRequest);

        // then
        assertEquals(1, response.getProcessedApplications().size());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(0).getState());

        List<ResourceEntity> cpiList = getResourceByName(cpiName);
        assertFalse(cpiList.isEmpty());
        assertEquals(1, cpiList.size());
        ResourceEntity cpi = cpiList.get(0);
        assertNull(cpi.getSoftlinkRelation());
        assertNull(softlinkRelationService.getSoftLinkRelationByCpiAndSoftlinkRef(getResourceByName(cpiName).get(0), softlinkRef));

        List<ResourceEntity> ppiList = getResourceByName(ppiName);
        assertFalse(ppiList.isEmpty());
        assertEquals(1, ppiList.size());
        ResourceEntity ppi = ppiList.get(0);
        assertEquals(softlinkRef, ppi.getSoftlinkId());
    }

}
