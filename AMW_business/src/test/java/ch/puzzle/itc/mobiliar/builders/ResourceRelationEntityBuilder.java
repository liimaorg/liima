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

package ch.puzzle.itc.mobiliar.builders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.mockito.Mockito;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;

public class ResourceRelationEntityBuilder extends BaseEntityBuilder {

	public ConsumedResourceRelationEntity mockConsumedResRelEntity(ResourceEntity master, ResourceEntity slave, String identifier) {
		ConsumedResourceRelationEntity mock = mock(ConsumedResourceRelationEntity.class);
		setAbstractValues(mock, master, slave, identifier);
		setConsumedMasterRelations(mock, master);
		setConsumedSlaveRelations(mock, slave);
        when(mock.getOwner()).thenReturn(ForeignableOwner.getSystemOwner()); // use systemOwner as default
		return mock;
	}

	public ConsumedResourceRelationEntity buildConsumedResRelEntity(ResourceEntity master, ResourceEntity slave, String identifier, Integer id) {
		ConsumedResourceRelationEntity relation = new ConsumedResourceRelationEntity();
		setAbstractValues(relation, master, slave, identifier);
		setConsumedMasterRelations(relation, master);
		setConsumedSlaveRelations(relation, slave);
		relation.setId(id);
		return relation;
	}

	public ProvidedResourceRelationEntity buildProvidedResRelEntity(ResourceEntity master, ResourceEntity slave, String identifier, Integer id) {
		ProvidedResourceRelationEntity relation = new ProvidedResourceRelationEntity();
		setAbstractValues(relation, master, slave, identifier);
		setProvidedMasterRelations(relation, master);
		setProvidedSlaveRelations(relation, slave);
		relation.setId(id);
		return relation;
	}

    public ConsumedResourceRelationEntity buildConsumedResRelEntity(ForeignableOwner owner, ResourceEntity master, ResourceEntity slave, String identifier) {
        ConsumedResourceRelationEntity relation = buildConsumedResRelEntity(master, slave, identifier, null);
        relation.setOwner(owner);
        return relation;
    }

	public ProvidedResourceRelationEntity mockProvidedResourceRelationEntity(ResourceEntity master, ResourceEntity slave, String identifier) {
		ProvidedResourceRelationEntity mock = mock(ProvidedResourceRelationEntity.class);
		setAbstractValues(mock, master, slave, identifier);
		setProvidedMasterRelations(mock, master);
		setProvidedSlaveRelations(mock, slave);
        when(mock.getOwner()).thenReturn(ForeignableOwner.getSystemOwner()); // use systemOwner as default
		return mock;
	}

	private void setAbstractValues(AbstractResourceRelationEntity relation, ResourceEntity master, ResourceEntity slave, String identifier) {
		if (Mockito.mockingDetails(relation).isMock()) {
			when(relation.getId()).thenReturn(getNextId());
			when(relation.getMasterResource()).thenReturn(master);
			when(relation.getSlaveResource()).thenReturn(slave);
			when(relation.getIdentifier()).thenReturn(identifier);
			ResourceRelationTypeEntity resRelType = mockResourceRelationTypeEntity(master.getResourceType(), slave.getResourceType());
			when(relation.getResourceRelationType()).thenReturn(resRelType);
		} else {
			relation.setId(getNextId());
			relation.setMasterResource(master);
			relation.setSlaveResource(slave);
			relation.setIdentifier(identifier);
			ResourceRelationTypeEntity resRelType = buildResourceRelationTypeEntity(master.getResourceType(), slave.getResourceType());
			relation.setResourceRelationType(resRelType);
		}
	}

	private ResourceRelationTypeEntity mockResourceRelationTypeEntity(ResourceTypeEntity typeA, ResourceTypeEntity typeB) {
		ResourceRelationTypeEntity mock = mock(ResourceRelationTypeEntity.class);
		when(mock.getResourceTypeA()).thenReturn(typeA);
		when(mock.getResourceTypeB()).thenReturn(typeB);
		return mock;
	}

	private ResourceRelationTypeEntity buildResourceRelationTypeEntity(ResourceTypeEntity typeA, ResourceTypeEntity typeB) {
		ResourceRelationTypeEntity relType = new ResourceRelationTypeEntity();
		relType.setResourceTypes(typeA, typeB);
		return relType;
	}

	private void setConsumedMasterRelations(ConsumedResourceRelationEntity mock, ResourceEntity master) {
		Set<ConsumedResourceRelationEntity> masterRelations = master.getConsumedMasterRelations() != null ? master.getConsumedMasterRelations()
				: new HashSet<ConsumedResourceRelationEntity>();
		masterRelations.add(mock);
		if (Mockito.mockingDetails(master).isMock()) {
			when(master.getConsumedMasterRelations()).thenReturn(masterRelations);
		} else {
			master.setConsumedMasterRelations(masterRelations);
		}
	}

	private void setConsumedSlaveRelations(ConsumedResourceRelationEntity mock, ResourceEntity slave) {
		Set<ConsumedResourceRelationEntity> slaveRelations = slave.getConsumedSlaveRelations() != null ? slave.getConsumedSlaveRelations()
				: new HashSet<ConsumedResourceRelationEntity>();
		slaveRelations.add(mock);
		if (Mockito.mockingDetails(slave).isMock()) {
			when(slave.getConsumedSlaveRelations()).thenReturn(slaveRelations);
		} else {
			slave.setConsumedMasterRelations(slaveRelations);
		}
	}

	private void setProvidedMasterRelations(ProvidedResourceRelationEntity mock, ResourceEntity master) {
		Set<ProvidedResourceRelationEntity> masterRelations = master.getProvidedMasterRelations() != null ? master.getProvidedMasterRelations()
				: new HashSet<ProvidedResourceRelationEntity>();
		masterRelations.add(mock);
		if (Mockito.mockingDetails(master).isMock()) {
			when(master.getProvidedMasterRelations()).thenReturn(masterRelations);
		} else {
			master.setProvidedMasterRelations(masterRelations);
		}
	}

	private void setProvidedSlaveRelations(ProvidedResourceRelationEntity mock, ResourceEntity slave) {
		Set<ProvidedResourceRelationEntity> slaveRelations = slave.getProvidedSlaveRelations() != null ? slave.getProvidedSlaveRelations()
				: new HashSet<ProvidedResourceRelationEntity>();
		slaveRelations.add(mock);
		if (Mockito.mockingDetails(slave).isMock()) {
			when(slave.getProvidedSlaveRelations()).thenReturn(slaveRelations);
		} else {
			slave.setProvidedMasterRelations(slaveRelations);
		}
	}
}