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

package ch.puzzle.itc.mobiliar.maiafederationservice.usecasetests;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.ApplicationUpdateResult;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateResponse;
import ch.mobi.xml.datatype.common.commons.v3.MessageSeverity;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.BusinessException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.TechnicalException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Test case to verify creation/remove/decorate of properties on provided resource relation
 */
public class MaiaAmwFederationServiceUseCase8IntegrationTest extends BaseUseCaseIntegrationTest {

    // usecase files
    private static final String REQUEST_1 = "usecase7_8/request1.xml";
    //
    private static final String REQUEST_2 = "usecase7_8/request2.xml";

    private static final String RELATED_RESOURCE_NAME = "adExtern";

    // verify add and remove Ppi

    // Use
    // 1 ./generateModel.sh model_small uc_dev_iterations/model_02_add_puzzleshop/
    // <!-- updaterequest calculated from difference between model 'model_small' and
    // 'uc_dev_iterations/model_02_add_puzzleshop' but then altered such that shop and bank does not have
    // any properties on consumed/provided relations-->
    // 2 ./generateModel.sh model_small uc_dev_iterations/model_02_add_puzzleshop/
    // <!-- updaterequest calculated from difference between model 'model_small' and
    // 'uc_dev_iterations/model_02_add_puzzleshop' but then altered such that all consumed and provided
    // ports does have property 'puzzleBankPaymentProperty', 'puzzleBankCustomerProperty' and 'puzzleShopPaymentStuffProperty' -->

    // to generate the Requests

    @Before
    public void setUp() {
        super.setUp();

        createAndAddMainRelease16_04();
        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_05, new Date(2016, Calendar.MAY, 1), false);
        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_06, new Date(2016, Calendar.JUNE, 1), false);

        addResourceTypes();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void createMinorReleaseBasedOnMaiaMainRlWithPpiWithoutPropertiesThenUpdateMaiaWithPpiWithPropertiesShouldAddPpiPropertiesToEachMinorRelease() throws Exception {
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 1);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);

        createMinorReleasesForResource(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 3);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);

        //second Maia Request adds properties to Ppi on Shop
        executeRequest2();
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);
    }

    @Test
    public void createMinorReleaseBasedOnMaiaMainRlWithPpiWithPropertiesThenUpdateMaiaWithPpiWithoutPropertiesShouldRemovePpiPropertiesFromEachMinorRelease() throws Exception {
        executeRequest2();

        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 1);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);

        createMinorReleasesForResource(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 3);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);

        //second Maia Request removes properties from Ppi from Shop
        executeRequest1();
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
    }

    @Test
    public void decorateProvidedRelationPropertyOnMainRlWithPpiWithoutPropertiesThenCreateMinorReleaseThenUpdateMainRlWithPpiWithPropertiesShouldAddMainRlPpiPropertiesAndKeepDecoratedRelationPropertyOnEachMinorRelease() throws Exception {
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 1);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);

        decorateAMWPropertyOnResourceForRelease(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);

        createMinorReleasesForResource(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 3);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);

        //second Maia Request adds properties to Ppi on Shop
        executeRequest2();
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);
    }

    @Test
    public void decorateProvidedRelationPropertiesOnMainRlWithPpiWithPropertiesThenCreateMinorReleaseThenUpdateMainRlWithPpiWithoutPropertiesPpiShouldRemoveMainRlPpiPropertiesButKeepDecoratedRelationPropertiesOnEachMinorRelease() throws Exception {
        executeRequest2();

        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 1);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);

        decorateAMWPropertyOnResourceForRelease(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);

        createMinorReleasesForResource(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 3);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04, PUZZLE_BANK_PAYMENT_PROPERTY);


        //second Maia Request removes properties from Ppi from Shop
        executeRequest1();
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, buildPropertyNameFor(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04));
    }

    @Test
    public void decorateProvidedRelationWithAmwPropertyOnMainRlWithPpiWithoutPropertiesThenCreateMinorReleaseThenUpdateMainRlWithPpiWithAlreadyExistingPropertyShouldGenerateImportExceptionForAgregate() throws Exception {
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 1);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);

        decorateAMWPropertyOnResourceForRelease(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);

        createMinorReleasesForResource(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 3);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);


        UpdateResponse updateResponse = doExecuteRequest2();
        assertEquals(2, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(1).getMessages().get(0).getSeverity());
        ApplicationUpdateResult updateResult = updateResponse.getProcessedApplications().get(0);
        assertEquals(MessageSeverity.ERROR, updateResult.getMessages().get(0).getSeverity());
        assertEquals(PUZZLE_BANK_V_1, updateResult.getId().getName());
        assertTrue(updateResult.getMessages().get(0).getHumanReadableMessage().contains(PUZZLE_BANK_CUSTOMER_PROPERTY));
    }

    @Test
    public void createMinorReleaseThenDecorateProvidedRelationWithAmwPropertyThenUpdateMainRlWithPpiWithAlreadyExistingPropertyShouldGenerateImportExceptionForAgregate() throws Exception {
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 1);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);

        createMinorReleasesForResource(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 3);

        decorateAMWPropertyOnResourceForRelease(PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_CUSTOMER, MAIN_RELEASE_16_04, PUZZLE_BANK_CUSTOMER_PROPERTY);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);

        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_04);


        UpdateResponse updateResponse = doExecuteRequest2();
        assertEquals(2, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(1).getMessages().get(0).getSeverity());
        ApplicationUpdateResult updateResult = updateResponse.getProcessedApplications().get(0);
        assertEquals(MessageSeverity.ERROR, updateResult.getMessages().get(0).getSeverity());
        assertEquals(PUZZLE_BANK_V_1, updateResult.getId().getName());
        assertTrue(updateResult.getMessages().get(0).getHumanReadableMessage().contains(PUZZLE_BANK_CUSTOMER_PROPERTY));
    }

    /**
     * small -->02: [Grundzustand] Create PuzzleShop (Ppi) and PuzzleBank (PPI)
     */
    private void executeRequest1() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_1);

        // then
        assertNotNull(updateResponse);
        assertEquals(2, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(1).getMessages().get(0).getSeverity());

    }

    /**
     * small -->02: [Grundzustand] additional Property
     */
    private void executeRequest2() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doExecuteRequest2();

        // then
        assertEquals(2, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(1).getMessages().get(0).getSeverity());
    }

    private UpdateResponse doExecuteRequest2() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_2);

        // then
        assertNotNull(updateResponse);
        return updateResponse;

    }


}
