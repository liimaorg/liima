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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MaiaAmwFederationServiceUseCase10IntegrationTest extends BaseUseCaseIntegrationTest {

    private static final String APPSERVER = "appserver";

    // usecase files
    private static final String REQUEST_1 = "usecase10/request1.xml";
    //
    private static final String REQUEST_2 = "usecase10/request2.xml";


    @Before
    public void setUp() {
        super.setUp();

        createAndAddMainRelease16_04();
        createAndAddMainRelease16_10();
        createAndAddMainRelease17_04();
//        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_11, new Date(2016, Calendar.NOVEMBER, 1), false);

        addResourceTypes();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }



    /**
     * Import PUZZLE_SHOP_V_1 and PUZZLE_BANK_V_1 in RL 16_04 and connect to Appserver in RL 16_04 and RL 16_10
     * Update PUZZLE_BANK_V_2 where PUZZLE_BANK_V_1 is predecessor of PUZZLE_BANK_V_2 for RL 16_10
     * -> AS in RL 16_10 should have application relation to PUZZLE_BANK_V_2 and PUZZLE_SHOP_V_1
     * -> AS in RL 16_04 should have application relation to PUZZLE_BANK_V_1 and PUZZLE_SHOP_V_1
     */
    @Test
    public void updateWithPredecessorShouldUpdateExistingAppserverReleaseAndRelations() throws Exception {
        // import maia applications
        executeRequest1();

        verifyResourcePresentInRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyResourcePresentInRelease(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);

        // create appserver
        createAppserver(APPSERVER, MAIN_RELEASE_16_04, MAIN_RELEASE_16_10);
        verifyResourcePresentInRelease(APPSERVER, MAIN_RELEASE_16_04);
        verifyResourcePresentInRelease(APPSERVER, MAIN_RELEASE_16_10);

        // add maia applications to appserver
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);

        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_10, PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_10, PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_10, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);

        // execute predecessor request -> replace app with other app in successor release
        executeRequest2();

        verifyResourcePresentInRelease(PUZZLE_BANK_V_2, MAIN_RELEASE_16_10);

        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_10, PUZZLE_BANK_V_2, PUZZLE_SHOP_V_1);

    }

    /**
     * Import PUZZLE_SHOP_V_1 and PUZZLE_BANK_V_1 in RL 16_04 and connect to Appserver in RL 16_04
     * Update PUZZLE_BANK_V_2 where PUZZLE_BANK_V_1 is predecessor of PUZZLE_BANK_V_2 for RL 16_10
     * -> AS in RL 16_10 should have application relation to PUZZLE_BANK_V_2
     * -> AS in RL 16_04 should have application relation to PUZZLE_BANK_V_1 and PUZZLE_SHOP_V_1
     */
    @Test
    public void updateWithPredecessorShouldUpdateExistingAppserverWithoutRelation() throws Exception {
        // import maia applications
        executeRequest1();

        verifyResourcePresentInRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyResourcePresentInRelease(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);

        // create appserver
        createAppserver(APPSERVER, MAIN_RELEASE_16_04, MAIN_RELEASE_16_10);
        verifyResourcePresentInRelease(APPSERVER, MAIN_RELEASE_16_04);
        verifyResourcePresentInRelease(APPSERVER, MAIN_RELEASE_16_10);

        // add maia applications to appserver
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);

        // execute predecessor request -> replace app with other app in successor release
        executeRequest2();

        verifyResourcePresentInRelease(PUZZLE_BANK_V_2, MAIN_RELEASE_16_10);

        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_10, PUZZLE_BANK_V_2);

    }

    /**
     * Import PUZZLE_SHOP_V_1 and PUZZLE_BANK_V_1 in RL 16_04 and connect to Appserver in RL 16_04, 16_10 and RL 17_04
     * Update PUZZLE_BANK_V_2 where PUZZLE_BANK_V_1 is predecessor of PUZZLE_BANK_V_2 for RL 16_10
     * -> AS in RL 16_04 should have application relation to PUZZLE_BANK_V_1 and PUZZLE_SHOP_V_1
     * -> AS in RL 16_10 should have application relation to PUZZLE_BANK_V_2 and PUZZLE_SHOP_V_1
     * -> AS in RL 17_04 should have application relation to PUZZLE_BANK_V_2 and PUZZLE_SHOP_V_1 (is successor release of 16_10)
     */
    @Test
    public void updateWithPredecessorAndMultipleASRelationsShouldUpdateAllAppserverRelations() throws Exception {
        // import maia applications
        executeRequest1();

        verifyResourcePresentInRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyResourcePresentInRelease(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);

        // create appserver
        createAppserver(APPSERVER, MAIN_RELEASE_16_04, MAIN_RELEASE_16_10, MAIN_RELEASE_17_04);
        verifyResourcePresentInRelease(APPSERVER, MAIN_RELEASE_16_04);
        verifyResourcePresentInRelease(APPSERVER, MAIN_RELEASE_16_10);
        verifyResourcePresentInRelease(APPSERVER, MAIN_RELEASE_17_04);

        // add maia applications to appserver
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);

        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_10, PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_10, PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_10, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);

        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_17_04, PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_17_04, PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_17_04, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);

        // execute predecessor request -> replace app with other app in successor release
        executeRequest2();

        verifyResourcePresentInRelease(APPSERVER, MAIN_RELEASE_16_10);
        verifyResourcePresentInRelease(PUZZLE_BANK_V_2, MAIN_RELEASE_16_10);

        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_10, PUZZLE_BANK_V_2, PUZZLE_SHOP_V_1);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_17_04, PUZZLE_BANK_V_2, PUZZLE_SHOP_V_1);

    }

    /**
     * Import PUZZLE_SHOP_V_1 and PUZZLE_BANK_V_1 in RL 16_04 and connect to Appserver in RL 16_04
     * Update PUZZLE_BANK_V_2 where PUZZLE_BANK_V_1 is predecessor of PUZZLE_BANK_V_2 for RL 16_10
     * -> AS in RL 16_10 should be created and has application relation to PUZZLE_BANK_V_2 and PUZZLE_SHOP_V_1
     * -> AS in RL 16_04 should have application relation to PUZZLE_BANK_V_1 and PUZZLE_SHOP_V_1
     */
    @Test
    public void updateWithPredecessorShouldCreateNewNotYetExistingAppserverReleaseAndRelations() throws Exception {
        // import maia applications
        executeRequest1();

        verifyResourcePresentInRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyResourcePresentInRelease(PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);

        // create appserver
        createAppserver(APPSERVER, MAIN_RELEASE_16_04);
        verifyResourcePresentInRelease(APPSERVER, MAIN_RELEASE_16_04);

        // add maia applications to appserver
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, MAIN_RELEASE_16_04);
        addApplicationToAppServer(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);

        // execute predecessor request -> replace app with other app in successor release
        executeRequest2();

        verifyResourcePresentInRelease(APPSERVER, MAIN_RELEASE_16_10);
        verifyResourcePresentInRelease(PUZZLE_BANK_V_2, MAIN_RELEASE_16_10);

        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_04, PUZZLE_BANK_V_1, PUZZLE_SHOP_V_1);
        verifyConsumedRelationsPresentInResourceRelease(APPSERVER, MAIN_RELEASE_16_10, PUZZLE_BANK_V_2, PUZZLE_SHOP_V_1);

    }


    private void executeRequest1() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_1);

        // then
        assertNotNull(updateResponse);
        assertEquals(2, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(1).getMessages().get(0).getSeverity());

    }


    private void executeRequest2() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doExecuteRequest2();

        // then
        assertEquals(1, updateResponse.getProcessedApplications().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
    }

    private UpdateResponse doExecuteRequest2() throws ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.BusinessException, ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.TechnicalException, ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_2);

        // then
        assertNotNull(updateResponse);
        return updateResponse;

    }

}
