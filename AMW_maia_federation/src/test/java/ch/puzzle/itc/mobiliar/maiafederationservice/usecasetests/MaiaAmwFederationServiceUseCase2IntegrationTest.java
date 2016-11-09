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
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MaiaAmwFederationServiceUseCase2IntegrationTest extends BaseUseCaseIntegrationTest {


    // Use
    // 1      ./generateModel.sh model_small uc_dev_iterations/model_01_add_puzzlebank/
    // to generate the Requests

    // usecase files
    private static final String MANUAL_REQUEST_1 = "usecase2/manual-yves-request1.xml";
    private static final String REQUEST_1 = "usecase2/request1.xml";


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
    public void useCase1shouldNotFail() throws Exception {
        step1();
        executeRequest1();
        step3();

    }

    /**
     * Add Puzzlebank
     * @throws BusinessException
     * @throws TechnicalException
     * @throws ValidationException
     */
    private void step1() throws BusinessException, TechnicalException, ValidationException, ch.puzzle.itc.mobiliar.business.utils.ValidationException {
        // when
        UpdateResponse updateResponse = doUpdate(MANUAL_REQUEST_1);
        List<ResourceEntity> apps = getResourceByName("yves_ch_mobi_testing_PuzzleBank_v1_default");
        assertEquals(1, apps.size());
        ResourceEntity app = apps.get(0);
        // update owner
        app.setOwner(ForeignableOwner.getSystemOwner());

        // add Property values
        updatePropertyValue(app, "testproperty", "value");

        entityManager.persist(app);

    }

    /**
     * Add PuzzleShop
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

    private void step3() throws ForeignableOwnerViolationException, AMWException {
        // when
        List<ResourceEntity> appsOrigin = getResourceByName("yves_ch_mobi_testing_PuzzleBank_v1_default");
        List<ResourceEntity> appsTarget = getResourceByName(PUZZLE_BANK_V_1);

        CopyResourceResult result = copyResourceDomainService.copyFromOriginToTargetResource(appsOrigin.get(0), appsTarget.get(0), ForeignableOwner.getSystemOwner());


        ResourceContextEntity context = appsTarget.get(0).getOrCreateContext(contextService.getGlobalResourceContextEntity());

        // then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, context.getProperties().size());
        // is the value of the Property which was on the origin App still set
        assertEquals("value", context.getProperties().iterator().next().getValue());

    }
}
