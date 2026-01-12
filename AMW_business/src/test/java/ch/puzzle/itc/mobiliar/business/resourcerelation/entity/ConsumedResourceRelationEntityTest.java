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

package ch.puzzle.itc.mobiliar.business.resourcerelation.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainServiceTestHelper;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService.CopyMode;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;

public class ConsumedResourceRelationEntityTest {

	public ResourceRelationEntityBuilder relationEntityBuilder = new ResourceRelationEntityBuilder();

    @ParameterizedTest
    @EnumSource(CopyMode.class)
	public void shouldCopyMasterRelations_emptyTarget(CopyResourceDomainService.CopyMode mode) throws AMWException {
		// given
		ResourceEntity originMaster = CopyResourceDomainServiceTestHelper.mockOriginResource();
		ResourceEntity targetMaster = new ResourceEntityBuilder().buildAppServerEntity("targetResource",
				null, null, true);

		String identifier = "fooBar";
		ResourceEntity originSlave = new ResourceEntityBuilder().mockResourceEntity("wsOrigin", null,
				"webservice", null);
		ConsumedResourceRelationEntity origin = relationEntityBuilder.buildConsumedResRelEntity(
				originMaster, originSlave, identifier, 1);
		CopyUnit copyUnit = new CopyUnit(originMaster, targetMaster, mode);

		// when
		ConsumedResourceRelationEntity copy = origin.getCopy(null, copyUnit);

		// then
		assertTrue(copyUnit.getResult().isSuccess());
		assertEquals(targetMaster, copy.getMasterResource());
		assertEquals(originSlave, copy.getSlaveResource());
		assertNotEquals(origin.getId(), copy.getId());
	}

    @ParameterizedTest
    @EnumSource(CopyMode.class)
	public void shouldCopyMasterRelations_existingTarget(CopyResourceDomainService.CopyMode mode) throws AMWException {
		// given
		ResourceEntity originMaster = CopyResourceDomainServiceTestHelper.mockOriginResource();
		ResourceEntity targetMaster = new ResourceEntityBuilder().buildAppServerEntity("targetResource",
				null, null, true);

		String identifier = "fooBar";
		ResourceEntity originSlave = new ResourceEntityBuilder().mockResourceEntity("wsOrigin", null,
				"webservice", null);
		ConsumedResourceRelationEntity origin = relationEntityBuilder.buildConsumedResRelEntity(
				originMaster, originSlave, identifier, 1);

		ResourceEntity targetSlave = new ResourceEntityBuilder().mockResourceEntity("wsTarget", null,
				"webservice", null);
		ConsumedResourceRelationEntity target = relationEntityBuilder.buildConsumedResRelEntity(
				targetMaster, targetSlave, identifier, 2);

		CopyUnit copyUnit = new CopyUnit(originMaster, targetMaster, mode);

		// when
		ConsumedResourceRelationEntity copy = origin.getCopy(target, copyUnit);

		// then
		assertTrue(copyUnit.getResult().isSuccess());
		assertEquals(targetMaster, copy.getMasterResource());
		assertEquals(originSlave, copy.getSlaveResource());
		assertNotEquals(origin.getId(), copy.getId());
	}

    @ParameterizedTest
    @EnumSource(CopyMode.class)
	public void shouldCopySlaveRelations_emptyTarget(CopyResourceDomainService.CopyMode mode) throws AMWException {
		// given
		ResourceEntity originMaster = CopyResourceDomainServiceTestHelper.mockOriginResource();

		String identifier = "fooBar";
		ResourceEntity originSlave = new ResourceEntityBuilder().mockResourceEntity("wsOrigin", null,
				"webservice", null);
		ConsumedResourceRelationEntity origin = relationEntityBuilder.buildConsumedResRelEntity(
				originMaster, originSlave, identifier, 1);

		ResourceEntity targetSlave = new ResourceEntityBuilder().buildResourceEntity("wsTarget", null,
				"webservice", null, true);

		CopyUnit copyUnit = new CopyUnit(originSlave, targetSlave, mode);

		// when
		ConsumedResourceRelationEntity copy = origin.getCopy(null, copyUnit);

		// then
		assertTrue(copyUnit.getResult().isSuccess());
		if (copyUnit.getMode() == CopyResourceDomainService.CopyMode.RELEASE) {
			assertEquals(originMaster, copy.getMasterResource());
			assertEquals(targetSlave, copy.getSlaveResource());
			assertNotEquals(origin.getId(), copy.getId());
		}
		else {
			assertNull(copy);
		}
	}

    @ParameterizedTest
    @EnumSource(CopyMode.class)
	public void shouldCopySlaveRelations_existingTarget(CopyResourceDomainService.CopyMode mode) throws AMWException {
		// given
		ResourceEntity originMaster = CopyResourceDomainServiceTestHelper.mockOriginResource();
		ResourceEntity targetMaster = new ResourceEntityBuilder().buildAppServerEntity("targetResource",
				null, null, true);

		String identifier = "foo";
		ResourceEntity originSlave = new ResourceEntityBuilder().mockResourceEntity("wsOrigin", null,
				"webservice", null);
		ConsumedResourceRelationEntity origin = relationEntityBuilder.buildConsumedResRelEntity(
				originMaster, originSlave, identifier, 1);

		ResourceEntity targetSlave = new ResourceEntityBuilder().buildResourceEntity("wsTarget", null,
				"webservice", null, true);
		ConsumedResourceRelationEntity target = relationEntityBuilder.buildConsumedResRelEntity(
				targetMaster, targetSlave, identifier, 2);

		CopyUnit copyUnit = new CopyUnit(originSlave, targetSlave, mode);

		// when
		ConsumedResourceRelationEntity copy = origin.getCopy(target, copyUnit);

		// then
		assertTrue(copyUnit.getResult().isSuccess());
		if (copyUnit.getMode() == CopyResourceDomainService.CopyMode.RELEASE) {
			assertEquals(originMaster, copy.getMasterResource());
			assertEquals(targetSlave, copy.getSlaveResource());
			assertNotEquals(origin.getId(), copy.getId());
		}
		else {
			assertNull(copy);
		}
	}

    @ParameterizedTest
    @EnumSource(CopyMode.class)
	public void shouldCopyApplicationRelations(CopyResourceDomainService.CopyMode mode) throws AMWException {
		// given
		ResourceEntity originResource = CopyResourceDomainServiceTestHelper.mockOriginResource();
		ResourceEntity targetResource = new ResourceEntityBuilder().buildAppServerEntity("targetResource",
				null, null, true);

		ResourceEntity app = new ResourceEntityBuilder().mockApplicationEntity("app", null, null);
		ConsumedResourceRelationEntity origin = relationEntityBuilder.buildConsumedResRelEntity(
				originResource, app, app.getName(), null);

		CopyUnit copyUnit = new CopyUnit(originResource, targetResource, mode);

		// when
		ConsumedResourceRelationEntity copy = origin.getCopy(null, copyUnit);

		// then
		assertTrue(copyUnit.getResult().isSuccess());
		if (mode == CopyResourceDomainService.CopyMode.COPY) {
			assertNull(copy);
			assertFalse(copyUnit.getResult().getSkippedConsumedRelations().isEmpty());
		}
		else if (mode == CopyResourceDomainService.CopyMode.RELEASE) {
			assertNotNull(copy);
			assertTrue(copyUnit.getResult().getSkippedConsumedRelations().isEmpty());
		}
	}
}