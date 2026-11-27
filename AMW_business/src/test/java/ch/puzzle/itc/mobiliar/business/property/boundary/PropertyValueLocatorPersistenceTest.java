package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyValueLocation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.puzzle.itc.mobiliar.business.property.entity.PropertyValueLocation.DefinedOn.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, PersistenceTestExtension.class})
class PropertyValueLocatorPersistenceTest {

    @PersistenceContext
    EntityManager entityManager;

    @InjectMocks
    PropertyValueLocator locator;

    private ContextEntity context;
    private ResourceEntity master;
    private ResourceEntity slave;
    private ResourceTypeEntity masterType;
    private ResourceTypeEntity slaveType;

    @BeforeEach
    void setUp() {
        context = new ContextEntity();
        context.setName("Global");
        entityManager.persist(context);
        masterType = new ResourceTypeEntity();
        masterType.setName("Application");
        entityManager.persist(masterType);
        slaveType = new ResourceTypeEntity();
        slaveType.setName("Database");
        entityManager.persist(slaveType);
        master = ResourceFactory.createNewResource("app");
        master.setResourceType(masterType);
        entityManager.persist(master);
        slave = ResourceFactory.createNewResource("db");
        slave.setResourceType(slaveType);
        entityManager.persist(slave);
    }

    @Test
    void findsAllFiveDefinitionKindsWithTheirUpdateIds() {
        String value = "hostsys1.mobi.ch";
        PropertyEntity resourceProperty = addProperty(master.getOrCreateContext(context), value, false);
        addProperty(masterType.getOrCreateContext(context), value, false);
        ResourceRelationTypeEntity relationType = new ResourceRelationTypeEntity();
        relationType.setResourceTypes(masterType, slaveType);
        entityManager.persist(relationType);
        addProperty(relationType.getOrCreateContext(context), value, false);
        ConsumedResourceRelationEntity consumed = new ConsumedResourceRelationEntity();
        consumed.setMasterResource(master);
        consumed.setSlaveResource(slave);
        consumed.setResourceRelationType(relationType);
        entityManager.persist(consumed);
        addProperty(consumed.getOrCreateContext(context), value, false);
        ProvidedResourceRelationEntity provided = new ProvidedResourceRelationEntity();
        provided.setMasterResource(master);
        provided.setSlaveResource(slave);
        provided.setResourceRelationType(relationType);
        entityManager.persist(provided);
        addProperty(provided.getOrCreateContext(context), value, false);
        entityManager.flush();
        entityManager.clear();

        List<PropertyValueLocation> locations = locator.findByValue(value);

        assertEquals(5, locations.size());
        Map<PropertyValueLocation.DefinedOn, PropertyValueLocation> byKind = locations.stream()
                .collect(Collectors.toMap(PropertyValueLocation::getDefinedOn, location -> location));
        PropertyValueLocation resource = byKind.get(RESOURCE);
        assertEquals(resourceProperty.getId(), resource.getPropertyId());
        assertEquals(resourceProperty.getDescriptor().getId(), resource.getDescriptorId());
        assertEquals("host", resource.getPropertyName());
        assertEquals(master.getId(), resource.getOwnerId());
        assertEquals("app", resource.getOwnerName());
        assertNull(resource.getRelationId());
        assertNull(resource.getTargetId());
        assertEquals(masterType.getId(), byKind.get(RESOURCE_TYPE).getOwnerId());
        assertEquals(consumed.getId(), byKind.get(CONSUMED_RELATION).getRelationId());
        assertEquals(provided.getId(), byKind.get(PROVIDED_RELATION).getRelationId());
        assertEquals(relationType.getId(), byKind.get(RESOURCE_TYPE_RELATION).getRelationId());
        assertEquals(masterType.getId(), byKind.get(RESOURCE_TYPE_RELATION).getOwnerId());
        assertEquals(slaveType.getId(), byKind.get(RESOURCE_TYPE_RELATION).getTargetId());
        for (PropertyValueLocation.DefinedOn kind : List.of(CONSUMED_RELATION, PROVIDED_RELATION)) {
            assertEquals(master.getId(), byKind.get(kind).getOwnerId());
            assertEquals(slave.getId(), byKind.get(kind).getTargetId());
            assertEquals("db", byKind.get(kind).getTargetName());
        }
        for (PropertyValueLocation location : locations) {
            assertEquals(value, location.getPropertyValue());
            assertEquals(context.getId(), location.getContextId());
            assertEquals("Global", location.getContextName());
        }
    }

    @Test
    void matchesFullValuesLiterallyWithoutWildcardsOrTruncation() {
        String value = " %_\\!'host" + "x".repeat(250) + " ";
        PropertyEntity expected = addProperty(master.getOrCreateContext(context), value, false);
        addProperty(master.getOrCreateContext(context), "a" + value + "suffix", false);
        addProperty(master.getOrCreateContext(context), value.replace("%_", "ab"), false);
        entityManager.flush();
        entityManager.clear();

        List<PropertyValueLocation> locations = locator.findByValue(value);

        assertEquals(1, locations.size());
        assertEquals(expected.getId(), locations.get(0).getPropertyId());
        assertEquals(value, locations.get(0).getPropertyValue());
    }

    @Test
    void asteriskIsAWildcardAndDisablesImplicitContains() {
        PropertyEntity prefixed = addProperty(master.getOrCreateContext(context), "jdbc\\:db2\\://hostsys1.mobi.ch\\:5057/DB", false);
        addProperty(master.getOrCreateContext(context), "xjdbc\\:db2\\://hostsys1.mobi.ch\\:5057/DB", false);
        entityManager.flush();
        entityManager.clear();

        List<PropertyValueLocation> locations = locator.findByValue("jdbc\\:db2\\://host*:5057*");
        assertEquals(1, locations.size());
        assertEquals(prefixed.getId(), locations.get(0).getPropertyId());
        assertEquals(2, locator.findByValue("*jdbc\\:db2\\://host*").size());

        assertEquals(1, locations.size());
        assertEquals(prefixed.getId(), locations.get(0).getPropertyId());
    }

    @Test
    void excludesEncryptedPropertiesDefaultsAndUnownedValues() {
        String value = "hostsys1.mobi.ch";
        addProperty(master.getOrCreateContext(context), value, true);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntity();
        descriptor.setPropertyName("defaultHost");
        descriptor.setDefaultValue(value);
        entityManager.persist(descriptor);
        PropertyEntity unowned = new PropertyEntity();
        unowned.setDescriptor(descriptor);
        unowned.setValue(value);
        entityManager.persist(unowned);
        master.getOrCreateContext(context).addPropertyDescriptor(descriptor);
        entityManager.flush();
        entityManager.clear();

        assertTrue(locator.findByValue(value).isEmpty());
        assertTrue(locator.findByValue("missing").isEmpty());
    }

    private PropertyEntity addProperty(ContextDependency<?> definition, String value, boolean encrypted) {
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntity();
        descriptor.setPropertyName("host");
        descriptor.setEncrypt(encrypted);
        entityManager.persist(descriptor);
        PropertyEntity property = new PropertyEntity();
        property.setDescriptor(descriptor);
        property.setValue(value);
        definition.addProperty(property);
        entityManager.persist(definition);
        return property;
    }
}
