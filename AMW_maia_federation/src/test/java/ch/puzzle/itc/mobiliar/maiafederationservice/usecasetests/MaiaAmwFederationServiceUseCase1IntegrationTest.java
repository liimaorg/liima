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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MaiaAmwFederationServiceUseCase1IntegrationTest extends BaseUseCaseIntegrationTest {


    // Use
    // 1      ./generateModel.sh model_small uc_dev_iterations/model_01_add_puzzlebank/
    // 2     ./generateModel.sh uc_dev_iterations/model_01_add_puzzlebank/ uc_dev_iterations/model_02_add_puzzleshop/
    // 3     ./generateModel.sh uc_dev_iterations/model_02_add_puzzleshop/ uc_dev_iterations/model_03_puzzleshop_add_customer_cpi/
    // 4     ./generateModel.sh uc_dev_iterations/model_03_puzzleshop_add_customer_cpi/ uc_dev_iterations/model_04_puzzleshop_remove_customer_cpi/
    // 5     ./generateModel.sh uc_dev_iterations/model_04_puzzleshop_remove_customer_cpi/ uc_dev_iterations/model_01_add_puzzlebank/
    // 6     ./generateModel.sh uc_dev_iterations/model_01_add_puzzlebank/ model_small/
    // to generate the Requests

    // usecase files
    public static final String REQUEST_1 = "usecase1/request1.xml";
    public static final String REQUEST_2 = "usecase1/request2.xml";
    public static final String REQUEST_3 = "usecase1/request3.xml";
    public static final String REQUEST_4 = "usecase1/request4.xml";
    public static final String REQUEST_5 = "usecase1/request5.xml";
    public static final String REQUEST_6 = "usecase1/request6.xml";


    @Before
    public void setUp(){
        super.setUp();

        createAndAddMainRelease16_04();

        addResourceTypes();


    }

    @After
    public void tearDown(){
        super.tearDown();
    }

    @Test
    public void useCase1shouldNotFail() throws BusinessException, TechnicalException, ValidationException {
        executeRequest1();
        executeRequest2();
        executeRequest3();
        executeRequest4();
        executeRequest5();
        executeRequest6();
    }

    /**
     * Add Puzzlebank
     * @throws BusinessException
     * @throws TechnicalException
     * @throws ValidationException
     */
    private void executeRequest1() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_1);

        // then
        assertNotNull(updateResponse);
        assertEquals(1, updateResponse.getProcessedApplications().size());
        assertEquals(1, updateResponse.getProcessedApplications().get(0).getMessages().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

        List<ResourceEntity> apps = getResourceByName(PUZZLE_BANK_V_1);
        assertNotNull(apps);
        assertEquals(1, apps.size());

    }

    /**
     * Add PuzzleShop
     * @throws BusinessException
     * @throws TechnicalException
     * @throws ValidationException
     */
    private void executeRequest2() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_2);

        // then
        assertNotNull(updateResponse);
        assertEquals(1, updateResponse.getProcessedApplications().size());
        assertEquals(1, updateResponse.getProcessedApplications().get(0).getMessages().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

        List<ResourceEntity> apps = getResourceByName(PUZZLE_SHOP_V_1);
        assertNotNull(apps);
        assertEquals(1, apps.size());
    }

    private void executeRequest3() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_3);

        // then
        assertNotNull(updateResponse);
        // TODO Implement checks
    }

    private void executeRequest4() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_4);

        // then
        assertNotNull(updateResponse);
        // TODO Implement checks
    }

    private void executeRequest5() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_5);

        // then
        assertNotNull(updateResponse);
        assertEquals(1, updateResponse.getProcessedApplications().size());
        assertEquals(1, updateResponse.getProcessedApplications().get(0).getMessages().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

        // TODO Transactional Problem during Test
//        List<ResourceEntity> apps = getResourceByName("PUZZLE_SHOP_V_1");
//        assertNotNull(apps);
//        assertEquals(0, apps.size());
    }

    private void executeRequest6() throws BusinessException, TechnicalException, ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(REQUEST_6);

        // then
        assertNotNull(updateResponse);
        assertEquals(1, updateResponse.getProcessedApplications().size());
        assertEquals(1, updateResponse.getProcessedApplications().get(0).getMessages().size());
        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());

        // TODO Transactional Problem during Test
//        List<ResourceEntity> apps = getResourceByName(PUZZLE_BANK_V_1);
//        assertNotNull(apps);
//        assertEquals(0, apps.size());

    }
}
