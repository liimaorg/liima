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

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.ApplicationReleaseBinding;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.PropertyDeclaration;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.ProcessingState;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateRequest;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateResponse;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.BusinessException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.TechnicalException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.ValidationException;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntityBuilder;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.maiafederationservice.utils.BaseIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 *
 */
public class MaiaAmwFederationServicePredecessorIntegrationTest extends BaseIntegrationTest {

    // usecase files
    private static final String USECASE_PREDECESSOR_01 = "usecase_predecessor_uc01_req01.xml";
    private static final String USECASE_PREDECESSOR_02 = "usecase_predecessor_uc01_req02.xml";
    private static final String USECASE_PREDECESSOR_03 = "usecase_predecessor_uc01_req03.xml";
    private static final String USECASE_PREDECESSOR_04 = "usecase_predecessor_uc01_req04.xml";

    @Before
    public void setUp(){
        super.setUp();
        addReleases();
    }

    @After
    public void tearDown(){
        super.tearDown();
    }

    /**
     * This rather complex integration test consists of four update request
     *
     * The first of this requests creates applicationName_01 (4 releases)
     *
     * The second creates applicationName_02 (4 releases) which consumes applicationName_01
     *
     * The third creates applicationName_03 (3 releases) which replaces applicationName_01
     *
     * The last one creates applicationName_04 (3 releases) which replaces applicationName_03
     *
     * @throws BusinessException
     * @throws TechnicalException
     * @throws ValidationException
     */
    @Test
    public void theRightFunctionsShouldBeCopiedWhenReplacingPredecessorWithSuccessor() throws BusinessException, TechnicalException, ValidationException {
        // given

        // the following parameter must match the names given in usecase file
        String applicationName_01 = "ch_mobi_testing_PuzzleBank_v1_default";
        String applicationName_02 = "ch_mobi_testing_PuzzleShop_v1_default";
        String applicationName_03 = "ch_mobi_testing_PuzzleBank_v2_default";
        String applicationName_04 = "ch_mobi_testing_PuzzleBank_v3_default";

        String newFunctionName1 = "HelloWorld_1.1";
        String newFunctionName2 = "HelloWorld_1.2";
        String newFunctionName3 = "HelloWorld_1.3";

        String chuckPropName = "chuckLevel";
        String chuckValue = "Norris";

        String ppiLocalPortId = "payment";
        String relPropName = "ch.mobi.testing.PuzzleBank.dagobert";
        String relPropDisplayName = "dagobert";
        String relPropExampleValue = "findMe";
        String relPropNewValue = "overwritten";

        addResourceTypes();

        // assure the applications aren't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName_01);
        assertTrue(applications.isEmpty());
        applications = getResourceByName(applicationName_02);
        assertTrue(applications.isEmpty());
        applications = getResourceByName(applicationName_03);
        assertTrue(applications.isEmpty());

        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_PREDECESSOR_01);

        // create a fake PropertyDeclaration
        PropertyDeclaration ppiProp = new PropertyDeclaration();
        ppiProp.setTechnicalKey(relPropName);
        ppiProp.setDisplayName(relPropDisplayName);
        ppiProp.setExampleValue(relPropExampleValue);
        ppiProp.setIsKeyOptional(true);

//        <properties>
//        <technicalKey>ch.mobi.testing.PuzzleBank.dagobert</technicalKey>
//        <displayName>dagobert</displayName>
//        <exampleValue>findMe</exampleValue>
//        <isKeyOptional>true</isKeyOptional>
//        </properties>

        // alter the update request add a fake exiting property on the ppi with localPortId "payment" (RL_1610)
        ApplicationReleaseBinding applicationReleaseBinding = updateRequest.getApplications().get(0).getStructure().get(1);
        assertEquals(MAIN_RELEASE_16_10,applicationReleaseBinding.getRelease());
        assertEquals(ppiLocalPortId, applicationReleaseBinding.getPayload().getProvidedPorts().get(0).getLocalPortID());

        updateRequest.getApplications().get(0).getStructure().get(1).getPayload().getProvidedPorts().get(0).getProperties().add(ppiProp);

        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        applications = getResourceByName(applicationName_01);
        assertFalse(applications.isEmpty());

        // we need the second resource, the first one has release RL_1604 which is only existing in PuzzleBank_v1
        ResourceEntity application = applications.get(1);
        assertEquals(MAIN_RELEASE_16_10, application.getRelease().getName());

        assertThat(application.getOwner(), is(ForeignableOwner.MAIA));
        assertThat(application.getResourceType().getName(), is(DefaultResourceTypeDefinition.APPLICATION.name()));

        // add a property (simulate the addition of an additional property created by AMW)
        PropertyDescriptorEntity propDesc = new PropertyDescriptorEntity();
        propDesc.setPropertyName(chuckPropName);
        propDesc.setDisplayName(chuckPropName);
        propDesc.setOptional(true);
        propDesc.setNullable(true);
        propDesc.setOwner(ForeignableOwner.AMW);

        propDesc = entityManager.merge(propDesc);
        entityManager.persist(propDesc);

        PropertyEntity prop = new PropertyEntity();
        prop.setDescriptor(propDesc);
        prop.setValue(chuckValue);

        Set<ResourceContextEntity> contexts = application.getContexts();
        for (ResourceContextEntity context : contexts) {
            if (context.getContext().getName().equals("Global")) {
                prop.setOwningResource(context.getContext());
                context.addPropertyDescriptor(propDesc);
                context.addProperty(prop);
                prop = entityManager.merge(prop);
                entityManager.persist(prop);
                context = entityManager.merge(context);
                entityManager.persist(context);
            }
        }

        Set<PropertyEntity> props = new HashSet<>();
        props.add(prop);
        propDesc.setProperties(props);
        entityManager.persist(propDesc);

        // add a property to a relation
        PropertyEntity relProp = new PropertyEntity();
        PropertyDescriptorEntity relPropDesc = descriptorService.findPropertyDescriptorByName(relPropName);
        relProp.setDescriptor(relPropDesc);
        relProp.setValue(relPropNewValue);
        entityManager.persist(relProp);
        relPropDesc.addProperty(relProp);
        entityManager.persist(relPropDesc);

        Set<ProvidedResourceRelationEntity> providedMasterRelations = application.getProvidedMasterRelations();
        for (ProvidedResourceRelationEntity providedMasterRelation : providedMasterRelations) {

            ResourceRelationContextEntity resRelConEnt =  new ResourceRelationContextEntity();
            resRelConEnt.setContext(contextService.getGlobalResourceContextEntity());
            providedMasterRelation.addContext(resRelConEnt);
            Set<ResourceRelationContextEntity> resourceRelationContextEntities = providedMasterRelation.getContexts();

            for (ResourceRelationContextEntity resourceRelationContextEntity : resourceRelationContextEntities) {
                resourceRelationContextEntity.addProperty(relProp);
            }
        }

        // add a function to release RL_1610
        AmwFunctionEntityBuilder functionBuilder = new AmwFunctionEntityBuilder(newFunctionName1, 1111);
        functionBuilder.forResource(application);
        functionBuilder.withImplementation("GoodbyeWorld_1.0");
        AmwFunctionEntity function = functionBuilder.build();
        function = entityManager.merge(function);
        Set<AmwFunctionEntity> functions = new HashSet<>();
        functions.add(function);
        application.setFunctions(functions);
        entityManager.persist(application);

        updateRequest = getUpdateRequestFor(USECASE_PREDECESSOR_02);
        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(0).getState());

        // when (PuzzleBank v1 gets replaced by PuzzleBank v2)
        updateRequest = getUpdateRequestFor(USECASE_PREDECESSOR_03);

        // alter the update request add a fake exiting property on the ppi with localPortId "payment" (RL_1610)
        applicationReleaseBinding = updateRequest.getApplications().get(1).getStructure().get(0);
        assertEquals(MAIN_RELEASE_16_10,applicationReleaseBinding.getRelease());
        assertEquals(ppiLocalPortId, applicationReleaseBinding.getPayload().getProvidedPorts().get(0).getLocalPortID());

        updateRequest.getApplications().get(1).getStructure().get(0).getPayload().getProvidedPorts().get(0).getProperties().add(ppiProp);

        // alter the update request add a fake exiting property on the ppi with localPortId "payment" (RL_1704)
        applicationReleaseBinding = updateRequest.getApplications().get(1).getStructure().get(1);
        assertEquals(MAIN_RELEASE_17_04,applicationReleaseBinding.getRelease());
        assertEquals(ppiLocalPortId, applicationReleaseBinding.getPayload().getProvidedPorts().get(0).getLocalPortID());

        updateRequest.getApplications().get(1).getStructure().get(1).getPayload().getProvidedPorts().get(0).getProperties().add(ppiProp);

//        // alter the update request add a fake exiting property on the ppi with localPortId "payment" (future)
//        applicationReleaseBinding = updateRequest.getApplications().get(1).getStructure().get(2);
//        assertEquals(RELEASE_NAMES[3],applicationReleaseBinding.getRelease());
//        assertEquals(ppiLocalPortId, applicationReleaseBinding.getPayload().getProvidedPorts().get(0).getLocalPortID());
//
//        updateRequest.getApplications().get(1).getStructure().get(2).getPayload().getProvidedPorts().get(0).getProperties().add(ppiProp);

        response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        assertEquals(3,response.getProcessedApplications().size());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(0).getState());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(1).getState());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(2).getState());

        applications = getResourceByName(applicationName_03);

        application = applications.get(0);
        assertEquals(MAIN_RELEASE_16_10, application.getRelease().getName());

        // check if the function has been copied to the successor
        List<String> functionNames = new ArrayList<>();
        for (ResourceEntity resourceEntity : applications) {
            Set<AmwFunctionEntity> funs = resourceEntity.getFunctions();
            for (AmwFunctionEntity fun : funs) {
                functionNames.add(fun.getName());
            }
        }
        // function from predecessor with release RL_1610 should have been copied
        assertTrue(functionNames.contains(newFunctionName1));

        // add a second function to release RL_1610
        functionBuilder = new AmwFunctionEntityBuilder(newFunctionName2, 2222);
        functionBuilder.forResource(application);
        functionBuilder.withImplementation("GoodbyeWorld_1.1");
        function = functionBuilder.build();
        function = entityManager.merge(function);
        functions = new HashSet<>();
        functions.addAll(application.getFunctions());
        functions.add(function);
        application.setFunctions(functions);
        entityManager.persist(application);

        // check if the property has been copied
        boolean foundChuck = false;
        contexts = application.getContexts();
        for (ResourceContextEntity context : contexts) {
            if (context.getContext().getName().equals("Global")) {
                Set<PropertyEntity> properties = context.getProperties();
                if (properties != null) {
                    for (PropertyEntity property : properties) {
                        if (property.getValue().equals(chuckValue)) {
                            foundChuck = true;
                            break;
                        }
                    }
                }
                assertTrue(foundChuck);
                foundChuck = false;
                Set<PropertyDescriptorEntity> propertyDescriptors = context.getPropertyDescriptors();
                if (propertyDescriptors != null) {
                    for (PropertyDescriptorEntity propertyDescriptor : propertyDescriptors) {
                        if (propertyDescriptor.getDisplayName().equals(chuckPropName)) {
                            foundChuck = true;
                            break;
                        }
                    }
                }
                assertTrue(foundChuck);
                foundChuck = false;
            }
        }

        Set<PropertyDescriptorEntity> propertyDescriptors = application.getContexts().iterator().next().getPropertyDescriptors();
        if (propertyDescriptors != null) {
            for (PropertyDescriptorEntity propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getDisplayName().equals(chuckPropName)) {
                    foundChuck = true;
                    break;
                }
            }
        }
        assertTrue(foundChuck);

        // check if the property on the provided relation has been copied
        // TODO spezielles property
        boolean valueOverwritten = false;
        for (ProvidedResourceRelationEntity providedResourceRelationEntity : application.getProvidedMasterRelations()) {
            assertEquals(2,application.getProvidedMasterRelations().size());
            if (providedResourceRelationEntity.getContexts() != null) {
                ResourceRelationContextEntity resourceRelationContextEntity = providedResourceRelationEntity.getContexts().iterator().next();
                assertNotNull(resourceRelationContextEntity);
                assertNotNull(resourceRelationContextEntity.getProperties());
                for (PropertyEntity propertyEntity : resourceRelationContextEntity.getProperties()) {
                    if (propertyEntity.getValue().equals(relPropNewValue)) {
                        valueOverwritten = true;
                        break;
                    }
                }
            }
        }
        assertTrue(valueOverwritten);

        application = applications.get(1);
        assertEquals(MAIN_RELEASE_17_04, application.getRelease().getName());

        // add a first function to release RL_1704
        functionBuilder = new AmwFunctionEntityBuilder(newFunctionName3, 3333);
        functionBuilder.forResource(application);
        functionBuilder.withImplementation("GoodbyeWorld_1.2");
        function = functionBuilder.build();
        function = entityManager.merge(function);
        functions = new HashSet<>();
        functions.add(function);
        application.setFunctions(functions);
        entityManager.persist(application);

        // when (PuzzleBank v2 gets replaced by PuzzleBank v3, PuzzleBank v2 gets deleted)
        updateRequest = getUpdateRequestFor(USECASE_PREDECESSOR_04);
        response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        assertEquals(3, response.getProcessedApplications().size());

        applications = getResourceByName(applicationName_04);

        application = applications.get(0);
        assertEquals(MAIN_RELEASE_16_10, application.getRelease().getName());

        // check if both functions have been copied to the new successor
        functionNames = new ArrayList<>();
        for (ResourceEntity resourceEntity : applications) {
            Set<AmwFunctionEntity> funs = resourceEntity.getFunctions();
            for (AmwFunctionEntity fun : funs) {
                functionNames.add(fun.getName());
            }
        }
        // functions from predecessor with release RL_1610 should have been copied
        assertTrue(functionNames.contains(newFunctionName1));
        assertTrue(functionNames.contains(newFunctionName2));
        // function from predecessor with release RL_1704 should not have been copied
        assertFalse(functionNames.contains(newFunctionName3));

    }

    @Test
    public void replacementWithPreExistingSuccessorShouldFail() throws BusinessException, TechnicalException, ValidationException {
        // given
        UpdateRequest updateRequest = getUpdateRequestFor(USECASE_PREDECESSOR_01);

        // the following parameter must match the names given in usecase file
        String applicationName_01 = "ch_mobi_testing_PuzzleBank_v1_default";
        String applicationName_02 = "ch_mobi_testing_PuzzleShop_v1_default";
        String applicationName_03 = "ch_mobi_testing_PuzzleBank_v2_default";

        addResourceTypes();

        // assure the applications aren't persisted yet
        List<ResourceEntity> applications = getResourceByName(applicationName_01);
        assertTrue(applications.isEmpty());
        applications = getResourceByName(applicationName_02);
        assertTrue(applications.isEmpty());
        applications = getResourceByName(applicationName_03);
        assertTrue(applications.isEmpty());

        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        updateRequest = getUpdateRequestFor(USECASE_PREDECESSOR_02);
        UpdateResponse response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(0).getState());

        updateRequest = getUpdateRequestFor(USECASE_PREDECESSOR_03);
        updateRequest.getApplicationPredecessors().clear();
        maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // when
        updateRequest = getUpdateRequestFor(USECASE_PREDECESSOR_03);
        response = maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);

        // then
        assertEquals(3, response.getProcessedApplications().size());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(0).getState());
        assertEquals(ProcessingState.FAILED, response.getProcessedApplications().get(1).getState());
        assertEquals(ProcessingState.OK, response.getProcessedApplications().get(2).getState());
    }

}
