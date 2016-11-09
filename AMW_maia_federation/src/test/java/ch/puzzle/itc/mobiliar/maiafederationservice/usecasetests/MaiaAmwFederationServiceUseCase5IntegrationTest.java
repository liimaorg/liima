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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Test case to verify creation/remove/decorate of consumed resource relation
 */
public class MaiaAmwFederationServiceUseCase5IntegrationTest extends BaseUseCaseIntegrationTest {

    // usecase files
    private static final String REQUEST_1 = "usecase5_6/request1.xml";
    //
    private static final String REQUEST_2 = "usecase5_6/request2.xml";

    private static final String RELATED_RESOURCE_NAME = "adExtern";

    // verify add and remove cpi

    // Use
    // 1 ./generateModel.sh model_small uc_dev_iterations/model_02_add_puzzleshop/
    // <!-- updaterequest calculated from difference between model 'model_small' and
    // 'uc_dev_iterations/model_02_add_puzzleshop' but then altered such that shop and bank does not have
    // any payload (no consumed and provided ports) -->
    // 2 ./generateModel.sh model_small uc_dev_iterations/model_02_add_puzzleshop/
    // <!-- updaterequest calculated from difference between model 'model_small' and
    // 'uc_dev_iterations/model_02_add_puzzleshop' but then altered such that all consumed and provided
    // ports does not have any property -->

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
    public void createMinorReleaseBasedOnMaiaMainRlWithoutCpiThenUpdateMaiaWithCpiShouldAddCpiToEachMinorRelease() throws Exception {
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1);

        createMinorReleasesForResource(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 3);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1);

        //second Maia Request adds CPI on Shop
        executeRequest2();
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);
    }

    @Test
    public void createMinorReleaseBasedOnMaiaMainRlWithCpiThenUpdateMaiaWithoutCpiShouldRemoveCpiFromEachMinorRelease() throws Exception {
        executeRequest2();

        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        createMinorReleasesForResource(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 3);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        //second Maia Request removes CPI from Shop
        executeRequest1();
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1);
    }

    @Test
    public void decorateCpiOnMainRlWithoutCpiThenCreateMinorReleaseThenUpdateMainRlWithCpiShouldAddMainRlCpiAndKeepDecoratedCpiOnEachMinorRelease() throws Exception {
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1);

        decorateConsumedRelation(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, RELATED_RESOURCE_NAME, MAIN_RELEASE_PAST);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, RELATED_RESOURCE_NAME);

        createMinorReleasesForResource(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 3);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, RELATED_RESOURCE_NAME);

        //second Maia Request adds CPI on Shop
        executeRequest2();
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF, RELATED_RESOURCE_NAME);
    }

    @Test
    public void decorateCpiOnMainRlWithCpiThenCreateMinorReleaseThenUpdateMainRlWithoutCpiShouldRemoveMainRlCpiButKeepDecoratedCpiOnEachMinorRelease() throws Exception {
        executeRequest2();

        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        decorateConsumedRelation(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, RELATED_RESOURCE_NAME, MAIN_RELEASE_PAST);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, RELATED_RESOURCE_NAME, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        createMinorReleasesForResource(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 3);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, RELATED_RESOURCE_NAME, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        //second Maia Request removes CPI from Shop
        executeRequest1();
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, RELATED_RESOURCE_NAME);
    }

    @Test
    public void createMinorReleaseBasedOnMaiaMainRlWithoutCpiThenDecorateMinorRlThenUpdateMainRlWithCpiShouldAddMainRlCpiToEachMinorReleaseAndKeepDecoration() throws Exception {
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1);

        createMinorReleasesForResource(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 3);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1);

        decorateConsumedRelation(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, RELATED_RESOURCE_NAME, MAIN_RELEASE_PAST);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, RELATED_RESOURCE_NAME);
        // verify that decoration is only on minor RL 16_05
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_06);


        //second Maia Request adds CPI on Shop
        executeRequest2();
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, RELATED_RESOURCE_NAME, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_06, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);
    }

    @Test
    public void createMinorReleaseBasedOnMaiaMainRlWithCpiThenDecorateMinorRlThenUpdateMainRlWithoutCpiShouldRemoveMainRlCpiFromEachMinorReleaseButKeepDecoration() throws Exception {
        executeRequest2();

        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        createMinorReleasesForResource(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 3);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        decorateConsumedRelation(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, RELATED_RESOURCE_NAME, MAIN_RELEASE_PAST);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, RELATED_RESOURCE_NAME, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);
        // verify that decoration is only on minor RL 16_05
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_06, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        //second Maia Request removes CPI from Shop
        executeRequest1();
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, RELATED_RESOURCE_NAME);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_06);
    }

    @Test
    public void decorateCpiOnMainRlWithoutCpiThenCreateMinorReleaseThenUpdateMainRlWithAlreadyExistingRelationShouldGenerateImportExceptionForAgregate() throws Exception {
        // create cpi resource which is further used to add as amw relation
        executeRequest2();
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1);

        decorateConsumedRelation(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        createMinorReleasesForResource(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 3);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        //second Maia Request adds CPI on Shop
        UpdateResponse updateResponse = doExecuteRequest2();
        assertEquals(2, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
        assertEquals(MessageSeverity.ERROR, updateResponse.getProcessedApplications().get(1).getMessages().get(0).getSeverity());
        assertTrue(updateResponse.getProcessedApplications().get(1).getMessages().get(0).getHumanReadableMessage().contains(PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF));
    }

    @Test
    public void createMinorReleaseThenDecorateMinorReleaseWithCpiThenUpdateMainRlWithAlreadyExistingRelationShouldGenerateImportExceptionForAgregate() throws Exception {
        // create cpi resource which is further used to add as amw relation
        executeRequest2();
        executeRequest1();

        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1);

        createMinorReleasesForResource(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, MINOR_RELEASE_16_06);
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 3);
        verifyConsumedRelationsPresentInAllReleasesFor(PUZZLE_SHOP_V_1);

        decorateConsumedRelation(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF);

        //second Maia Request adds CPI on Shop
        UpdateResponse updateResponse = doExecuteRequest2();
        assertEquals(2, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
        assertEquals(MessageSeverity.ERROR, updateResponse.getProcessedApplications().get(1).getMessages().get(0).getSeverity());
        assertTrue(updateResponse.getProcessedApplications().get(1).getMessages().get(0).getHumanReadableMessage().contains(PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF));
    }


    /**
     * small -->02: [Grundzustand] Create PuzzleShop (CPI) and PuzzleBank (PPI)
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
