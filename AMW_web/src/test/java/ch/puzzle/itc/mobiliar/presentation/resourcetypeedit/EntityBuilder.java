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

package ch.puzzle.itc.mobiliar.presentation.resourcetypeedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;

public class EntityBuilder {

	public static final String APP = "ch_puzzle_itc_mobi_amw";
	public static final String AS = "amw";

	private List<ResourceTypeEntity> types;
	private List<ResourceEntity> resources;
	private List<ConsumedResourceRelationEntity> relations;
	public List<ResourceRelationTypeEntity> typeRelations;

	private int typeCounter;
	private int resourceCounter;
	private int relationCounter;
	private int relationTypeCounter;


	public EntityBuilder() {
		this.typeCounter = 0;
		this.resourceCounter = 0;
		this.relationCounter = 0;
		this.relationTypeCounter = 0;

		this.types = new ArrayList<ResourceTypeEntity>();
		for (String typeName : amwTypes()) {
			this.types.add(buildResourceType(typeName));
		}

		this.resources = new ArrayList<ResourceEntity>();
		this.resources.add(buildResource(typeFor("APPLICATIONSERVER"), AS));
		this.resources.add(buildResource(typeFor("APPLICATION"), APP));
		this.resources.add(buildResource(typeFor("NODE"), "node1"));
		this.resources.add(buildResource(typeFor("NODE"), "node2"));

		this.resources.add(buildResource(typeFor("ActiveDirectory"), "adIntern"));
		this.resources.add(buildResource(typeFor("CertLoginModule"), "certAdIntern"));
		this.resources.add(buildResource(typeFor("DB2"), "db2Host"));
		this.resources.add(buildResource(typeFor("JBoss7Management"), "jboss7Management_Ldap"));
		this.resources.add(buildResource(typeFor("Keystore"), "jspCertJKS"));
		this.resources.add(buildResource(typeFor("Mail"), "mailrelay"));
		this.resources.add(buildResource(typeFor("ModCluster"), "proxy01"));
		this.resources.add(buildResource(typeFor("Truststore"), "mobiTrustJKS"));

		this.relations = new ArrayList<ConsumedResourceRelationEntity>();
		this.typeRelations = new ArrayList<ResourceRelationTypeEntity>();
		this.relations.add(buildConsumedRelation(resourceFor(AS), resourceFor("node1"), "node1"));
		this.relations.add(buildConsumedRelation(resourceFor(AS), resourceFor("node2"), "node2"));
		this.relations.add(buildConsumedRelation(resourceFor(AS), resourceFor(APP), "APP"));
		this.relations.add(buildConsumedRelation(resourceFor(APP), resourceFor("adIntern"), "adIntern"));
		this.relations.add(buildConsumedRelation(resourceFor(APP), resourceFor("certAdIntern"), "certAdIntern"));
		this.relations.add(buildConsumedRelation(resourceFor(APP), resourceFor("db2Host"), "db2Host"));
		this.relations.add(buildConsumedRelation(resourceFor(APP), resourceFor("jboss7Management_Ldap"), "jboss7Management_Ldap"));
		this.relations.add(buildConsumedRelation(resourceFor(APP), resourceFor("jspCertJKS"), "jspCertJKS"));
		this.relations.add(buildConsumedRelation(resourceFor(APP), resourceFor("mailrelay"), "mailrelay"));
		this.relations.add(buildConsumedRelation(resourceFor(APP), resourceFor("proxy01"), "proxy01"));
		this.relations.add(buildConsumedRelation(resourceFor(APP), resourceFor("mobiTrustJKS"), "mobiTrustJKS"));

		// inheritance
		ResourceTypeEntity cert = buildResourceType("Certificate");
		buildTypeInheritance(cert, typeFor("Keystore"));
		buildTypeInheritance(cert, typeFor("Truststore"));
		this.types.add(cert);
	}

	private void buildTypeInheritance(ResourceTypeEntity base, ResourceTypeEntity child) {
		base.getChildrenResourceTypes().add(child);
		child.setParentResourceType(base);
	}

	private List<String> amwTypes() {
		return Arrays.asList("APPLICATIONSERVER", "NODE", "APPLICATION", "ActiveDirectory", "CertLoginModule", "DB2",
				"JBoss7Management", "Keystore", "Mail", "ModCluster", "Truststore");
	}

	public ConsumedResourceRelationEntity buildConsumedRelation(ResourceEntity master, ResourceEntity slave, String identifier) {
		ConsumedResourceRelationEntity relation = new ConsumedResourceRelationEntity();
		relation.setId(relationCounter++);
		relation.setMasterResource(master);
		relation.setSlaveResource(slave);
		master.getConsumedMasterRelations().add(relation);

		ResourceRelationTypeEntity typeRelation = buildTypeRelation(master.getResourceType(), slave.getResourceType(), identifier);
		relation.setResourceRelationType(typeRelation);
		this.typeRelations.add(typeRelation);

		return relation;
	}

	public ResourceRelationTypeEntity buildTypeRelation(ResourceTypeEntity master, ResourceTypeEntity slave, String identifier) {
		ResourceRelationTypeEntity typeRelation = new ResourceRelationTypeEntity();
		typeRelation.setId(relationTypeCounter++);
		typeRelation.setIdentifier(identifier);
		typeRelation.setResourceTypes(master, slave);

		master.getResourceRelationTypesA().add(typeRelation);
		slave.getResourceRelationTypesB().add(typeRelation);

		return typeRelation;
	}

	public ResourceEntity resourceFor(String name) {
		for (ResourceEntity type : resources) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		throw new RuntimeException("resource: " + name + " not found.");
	}

	public ResourceTypeEntity typeFor(String name) {
		for (ResourceTypeEntity type : types) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		throw new RuntimeException("type: " + name + " not found.");
	}

	public ConsumedResourceRelationEntity relationFor(String master, String slave) {
		for (ConsumedResourceRelationEntity relation : relations) {
			if (relation.getMasterResource().getName().equals(master) && relation.getSlaveResource().getName().equals(slave)) {
				return relation;
			}
		}
		throw new RuntimeException("master: " + master + ", slave: " + slave + " not found.");
	}

	public ConsumedResourceRelationEntity buildRelation(ResourceEntity resource, Integer id) {
		ConsumedResourceRelationEntity relation = new ConsumedResourceRelationEntity();
		relation.setId(id);
		relation.setSlaveResource(resource);
		return relation;
	}

	private ResourceEntity buildResource(ResourceTypeEntity type, String name) {
		return buildResource(type, name, resourceCounter++);
	}

	public ResourceEntity buildResource(ResourceTypeEntity type, String name, Integer id) {
		ResourceEntity entity = ResourceFactory.createNewResource(name);
		entity.setConsumedMasterRelations(new HashSet<ConsumedResourceRelationEntity>());
		entity.setResourceType(type);
		entity.setId(id);
		return entity;
	}

	public ResourceTypeEntity buildResourceType(String name) {
		return buildResourceType(name, ++typeCounter);
	}

	public ResourceTypeEntity buildResourceType(String name, Integer id) {

		ResourceTypeEntity type = new ResourceTypeEntity();
		type.setResourceRelationTypesA(new HashSet<ResourceRelationTypeEntity>());
		type.setResourceRelationTypesB(new HashSet<ResourceRelationTypeEntity>());
		type.setChildrenResourceTypes(new HashSet<ResourceTypeEntity>());
		type.setId(id);
		type.setName(name);

		return type;
	}

	public List<ResourceEntity> getResources() {
		return resources;
	}

}
