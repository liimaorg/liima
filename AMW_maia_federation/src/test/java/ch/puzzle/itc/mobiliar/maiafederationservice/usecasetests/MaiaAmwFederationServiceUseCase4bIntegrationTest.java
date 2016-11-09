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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Decorate with property values
 */
public class MaiaAmwFederationServiceUseCase4bIntegrationTest extends BaseUseCaseIntegrationTest {

    // usecase files
    private static final String REQUEST_1 = "usecase4/request1.xml";
    //
    private static final String REQUEST_2 = "usecase4/request2.xml";


    // Use
    // 1      ./generateModel.sh model_small uc_dev_iterations/model_02_add_puzzleshop/
//    <!-- updaterequest calculated from difference between model 'model_small' and 'uc_dev_iterations/model_02_add_puzzleshop' but then altered to have no consumed ports and added a property-->
    // 2      ./generateModel.sh uc_dev_iterations/model_02_add_puzzleshop/ uc_dev_iterations/model_03_puzzleshop_add_customer_cpi/

    // to generate the Requests


//    Use Case:
//    1. small -->02: [Grundzustand]
//    2. AMW: Information auf Puzzleshop inkl. CPI aufdekorieren. Sowohl Werte für MAIA-Props als auch eigene Propertydeskriptoren (Relations, Templates und Funktionen hinzufügen) Owner der neuen Element muss AMW sein
//    3. AMW: 2 Zwischenrelease für Puzzleshop inkl CPI erstellenAlle 3 Releases sind identisch, Owner der Elemente bleibt erhalten
//    4. 02 -->03: puzzlebank konsumiert neu auch den customerserviceErstellt 3 neue CPI (pro Release 1), alle Releases erhalten einen neuen Relation, AMW Änderungen bleiben erhalten, Softlink ist auflösbar


    @Before
    public void setUp(){
        super.setUp();

        createAndAddMainRelease16_04();
        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_05, new Date(2016, Calendar.MAY, 1), false);
        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_06, new Date(2016, Calendar.JUNE, 1), false);

        addResourceTypes();
    }

    @After
    public void tearDown(){
        super.tearDown();
    }


    @Test
    public void decorateMaiaPropertyWithValueThenUpdateRemovesPropertyShouldRemoveMaiaProperty() throws Exception {
        // create puzzleshop and bank with properties
        String propertyValue = "propertyValue";
        executeRequest2();
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyPropertiesPresentInResourceRelease(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, PUZZLE_SHOP_PROPERTY);

        // set propertyvalue on maia property
        updatePropertyValue(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, PUZZLE_SHOP_PROPERTY, propertyValue);
        verifyPropertyValuePresent(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, PUZZLE_SHOP_PROPERTY, propertyValue);

        //second Maia REquest removes maia Property from Application
        executeRequest1();

        // verify that all maia removed properties are removed from all releases
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyPropertiesPresentInAllReleasesFor(PUZZLE_SHOP_V_1);
        verifyPropertyValueNotPresent(PUZZLE_SHOP_V_1, MAIN_RELEASE_16_04, PUZZLE_SHOP_PROPERTY);
    }

    @Test
    public void createMinorReleaseAndDecorateMaiaPropertyWithValueOnMinorReleaseThenUpdateRemovesPropertyShouldRemoveMaiaProperty() throws Exception {
        // create puzzleshop and bank without properties
        String propertyValue = "propertyValue";
        executeRequest2();
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 1);
        verifyPropertiesPresentInAllReleasesFor(PUZZLE_SHOP_V_1, PUZZLE_SHOP_PROPERTY);

        // create amw minor release from maia main release
        createMinorReleasesForResource(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05);
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 2);
        verifyPropertiesPresentInAllReleasesFor(PUZZLE_SHOP_V_1, PUZZLE_SHOP_PROPERTY);

        // set propertyvalue on maia property on minor release
        updatePropertyValue(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, PUZZLE_SHOP_PROPERTY, propertyValue);
        verifyPropertyValuePresent(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, PUZZLE_SHOP_PROPERTY, propertyValue);

        //second Maia REquest adds Property on Application
        executeRequest1();

        // verify that all maia removed properties are removed from all releases
        verifyNumberReleasesFor(PUZZLE_SHOP_V_1, 2);
        verifyPropertiesPresentInAllReleasesFor(PUZZLE_SHOP_V_1);
        verifyPropertyValueNotPresent(PUZZLE_SHOP_V_1, MINOR_RELEASE_16_05, PUZZLE_SHOP_PROPERTY);
    }



    /**
     * small -->02: [Grundzustand]
     * Create PuzzleShop (CPI) and PuzzleBank (PPI)
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
