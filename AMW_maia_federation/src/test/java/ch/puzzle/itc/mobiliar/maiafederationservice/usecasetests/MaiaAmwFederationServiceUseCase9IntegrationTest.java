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

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateResponse;
import ch.mobi.xml.datatype.common.commons.v3.MessageSeverity;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.BusinessException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.TechnicalException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.ValidationException;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test case to verify basic predecessor Case
 */
public class MaiaAmwFederationServiceUseCase9IntegrationTest extends BaseUseCaseIntegrationTest {

    // usecase files
    private static final String REQUEST_1 = "usecase9/request1.xml";
    //
    private static final String REQUEST_2 = "usecase9/request2.xml";

    private static final String REQUEST_1_1 = "usecase9/request1_1.xml";
    //
    private static final String REQUEST_2_1 = "usecase9/request2_1.xml";


    protected static final String PUZZLE_PROPERTY = "ch.mobi.testing.business.application.server.tokyo.puzzlepaymentservice.v1_0.servicelocator.DefaultServiceLocator_JNDI";

    protected static final String PUZZLE_BANK_V_2_PPI_PAYMENT = "ch.mobi.testing.PuzzleBank.v2.default#payment";

    // verify add and remove Ppi

    // Use
    // 1 ./generateModel.sh uc_predecessor/uc_predecessor_01_init/ uc_predecessor/uc_predecessor_model_02_replace_puzzlebank/
    // <!-- altered: removed puzzleShop Application Updates, splited ch_mobi_testing_PuzzleBank_v1_default and ch_mobi_testing_PuzzleBank_v2_default to two requests --

    // to generate the Requests

    @Before
    public void setUp() {
        super.setUp();

        createAndAddMainRelease16_04();
        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_05, new Date(2016, Calendar.MAY, 1), false);
        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_06, new Date(2016, Calendar.JUNE, 1), false);

        createAndAddMainRelease16_10();

        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_11, new Date(2016, Calendar.NOVEMBER, 1), false);
        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_12, new Date(2016, Calendar.DECEMBER, 1), false);
        addResourceTypes();

        createAndAddMainRelease17_04();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void predecessorUseCaseBasic() throws Exception {
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 1);

        // add Version 2 and make predecessor handling
        executeRequest2();
        verifyNumberReleasesFor(PUZZLE_BANK_V_2, 1);
    }

    @Test
    public void predecessorUseCaseAddPropertyAmwOnMinorRelease() throws Exception {
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 1);
        createMinorReleasesForResource(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        createMinorReleasesForResource(PUZZLE_BANK_V_1_PPI_PAYMENT, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 3);

        // set propertyvalue on maia property
        String propertyValue = "puzzlevalue";
        updatePropertyValueOnProvidedRelationForRelease(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MINOR_RELEASE_16_06, PUZZLE_PROPERTY, propertyValue);


        // add Version 2 and make predecessor handling
        executeRequest2();
        verifyNumberReleasesFor(PUZZLE_BANK_V_2, 1);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_2, MAIN_RELEASE_16_10, PUZZLE_BANK_V_2_PPI_PAYMENT, MAIN_RELEASE_16_10, PUZZLE_PROPERTY);

    }

    @Test
    public void predecessorUseCaseAddPropertyAmwOnMinorReleaseWithMaiaUpdate() throws Exception {
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 1);

        createMinorReleasesForResource(PUZZLE_BANK_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 3);

        createMinorReleasesForResource(PUZZLE_BANK_V_1_PPI_PAYMENT, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_BANK_V_1_PPI_PAYMENT, 3);

        List<ResourceEntity> ppiApp = getResourceByName(PUZZLE_BANK_V_1);

        List<ResourceEntity> paymentPpis = getResourceByName(PUZZLE_BANK_V_1_PPI_PAYMENT);

        // decorate new Property on APP
        String appProperty = "appProperty";
        String appPropertyValue = "appPropertyValue";

        decorateAMWPropertyOnResourceForRelease(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, appProperty);
        updatePropertyValue(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, appProperty, appPropertyValue);
        verifyPropertyValuePresent(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, appProperty, appPropertyValue);


        // decorate new Property on PPI
        String paymentProperty = "paymentProperty";
        String paymentPropertyValue = "paymentPropertyValue";

        decorateAMWPropertyOnResourceForRelease(PUZZLE_BANK_V_1_PPI_PAYMENT,MINOR_RELEASE_16_06, paymentProperty);
        updatePropertyValue(PUZZLE_BANK_V_1_PPI_PAYMENT, MINOR_RELEASE_16_06, paymentProperty, paymentPropertyValue);
        verifyPropertyValuePresent(PUZZLE_BANK_V_1_PPI_PAYMENT, MINOR_RELEASE_16_06, paymentProperty, paymentPropertyValue);

        // set propertyvalue on maia property
        String propertyValue = "puzzlevalue";
        updatePropertyValueOnProvidedRelationForRelease(PUZZLE_BANK_V_1, MINOR_RELEASE_16_06, PUZZLE_BANK_V_1_PPI_PAYMENT, MINOR_RELEASE_16_06, PUZZLE_PROPERTY, propertyValue);

        // create new Release 16_10 should also copy AMW Property with value
        executeRequest1_1();
        verifyNumberReleasesFor(PUZZLE_BANK_V_1, 4);
        verifyNumberReleasesFor(PUZZLE_BANK_V_1_PPI_PAYMENT, 4);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_1, MAIN_RELEASE_16_10, PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_10, PUZZLE_PROPERTY, paymentProperty);
        verifyPropertyValuePresent(PUZZLE_BANK_V_1, MAIN_RELEASE_16_10, appProperty, appPropertyValue);
        verifyPropertyValuePresent(PUZZLE_BANK_V_1_PPI_PAYMENT, MAIN_RELEASE_16_10, paymentProperty, paymentPropertyValue);

        // add Version 2 and make predecessor handling
        executeRequest2_1();
        verifyNumberReleasesFor(PUZZLE_BANK_V_2, 1);
        verifyPropertiesPresentInProvidedRelation(PUZZLE_BANK_V_2, MAIN_RELEASE_17_04, PUZZLE_BANK_V_2_PPI_PAYMENT, MAIN_RELEASE_17_04, PUZZLE_PROPERTY, paymentProperty);
        verifyPropertyValuePresent(PUZZLE_BANK_V_2_PPI_PAYMENT, MAIN_RELEASE_17_04, paymentProperty, paymentPropertyValue);
    }


    private void executeRequest1() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_1);

        // then
        assertNotNull(updateResponse);
        assertEquals(1, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

    }

    private void executeRequest2() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_2);

        // then
        assertNotNull(updateResponse);

        // then
        assertEquals(1, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
    }

    private void executeRequest1_1() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_1_1);

        // then
        assertNotNull(updateResponse);
        assertEquals(1, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

    }

    private void executeRequest2_1() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_2_1);

        // then
        assertNotNull(updateResponse);

        // then
        assertEquals(1, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(1).getSeverity());
    }


}
