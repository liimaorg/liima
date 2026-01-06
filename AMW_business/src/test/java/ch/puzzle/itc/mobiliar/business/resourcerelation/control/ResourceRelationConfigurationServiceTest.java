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

package ch.puzzle.itc.mobiliar.business.resourcerelation.control;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;


@ExtendWith(MockitoExtension.class)
public class ResourceRelationConfigurationServiceTest {

    @InjectMocks
    ResourceRelationConfigurationService service;

    @Test
    public void suspectOwnerCombinationShouldNotBePlausibleTest() {
        assertFalse(service.isPlausibleOwnerCombination(ForeignableOwner.MAIA,ForeignableOwner.AMW,ForeignableOwner.MAIA));
    }

    @Test
    public void plausibleOwnerCombinationsShouldNotBeSuspectTest() {
        // when then
        assertFalse(service.isSuspectOwnerCombination(ForeignableOwner.getSystemOwner(), ForeignableOwner.getSystemOwner(), ForeignableOwner.getSystemOwner()));

        // when then
        assertFalse(service.isSuspectOwnerCombination(ForeignableOwner.AMW, ForeignableOwner.AMW, ForeignableOwner.AMW));

        // when then
        assertFalse(service.isSuspectOwnerCombination(ForeignableOwner.AMW,ForeignableOwner.AMW,ForeignableOwner.MAIA));

        // when then
        assertFalse(service.isSuspectOwnerCombination(ForeignableOwner.MAIA, ForeignableOwner.AMW, ForeignableOwner.AMW));

        // when then
        assertFalse(service.isSuspectOwnerCombination(null, null, null));

    }

    @Test
    public void suspectRelationShouldNotBePlausibleTest() {
        // given
        ResourceEntity master = ResourceFactory.createNewResource("master");
        ResourceEntity slave = ResourceFactory.createNewResource("slave");

        master.setOwner(ForeignableOwner.MAIA);
        slave.setOwner(ForeignableOwner.MAIA);

        ConsumedResourceRelationEntity cRelation = new ConsumedResourceRelationEntity();
        cRelation.setOwner(ForeignableOwner.AMW);
        cRelation.setMasterResource(master);
        cRelation.setSlaveResource(slave);

        // when then
        assertFalse(service.isPlausibleRelation(cRelation));
    }

    @Test
    public void plausibleRelationShouldNotBeSuspectTest() {
        // given
        ResourceEntity master = ResourceFactory.createNewResource("master");
        ResourceEntity slave = ResourceFactory.createNewResource("slave");

        master.setOwner(ForeignableOwner.MAIA);
        slave.setOwner(ForeignableOwner.AMW);

        ProvidedResourceRelationEntity pRelation = new ProvidedResourceRelationEntity();
        pRelation.setOwner(ForeignableOwner.AMW);
        pRelation.setMasterResource(master);
        pRelation.setSlaveResource(slave);

        // when then
        assertFalse(service.isSuspectRelation(pRelation));
    }

}
