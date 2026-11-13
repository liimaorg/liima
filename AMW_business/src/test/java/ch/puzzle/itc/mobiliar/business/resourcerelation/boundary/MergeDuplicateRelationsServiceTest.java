package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.TemplateUtils;
import ch.puzzle.itc.mobiliar.business.resourceactivation.entity.ResourceActivationEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.envers.AuditReaderFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, PersistenceTestExtension.class})
class MergeDuplicateRelationsServiceTest {
    @PersistenceContext EntityManager entityManager;
    @Mock PermissionBoundary permissionBoundary;
    @InjectMocks MergeDuplicateRelationsService service;
    private ResourceEntity master;
    private ResourceEntity slave;
    private ContextEntity global;

    @BeforeEach
    void setUp() {
        global = new ContextEntity();
        global.setName("Global");
        entityManager.persist(global);
        master = ResourceFactory.createNewResource("merge-master");
        slave = ResourceFactory.createNewResource("merge-slave");
        entityManager.persist(master);
        entityManager.persist(slave);
        lenient().when(permissionBoundary.hasPermission(any(), any(), any(), any(ResourceEntity.class), isNull()))
                .thenReturn(true);
    }

    private AbstractResourceRelationEntity relation(boolean provided) {
        return relation(master, provided, null);
    }

    private AbstractResourceRelationEntity relation(ResourceEntity owner, boolean provided, String identifier) {
        AbstractResourceRelationEntity relation = provided
                ? new ProvidedResourceRelationEntity() : new ConsumedResourceRelationEntity();
        relation.setMasterResource(owner);
        relation.setSlaveResource(slave);
        relation.setIdentifier(identifier);
        if (provided) {
            owner.addProvidedRelation((ProvidedResourceRelationEntity) relation);
        } else {
            owner.addConsumedRelation((ConsumedResourceRelationEntity) relation);
        }
        entityManager.persist(relation);
        return relation;
    }

    private ResourceRelationContextEntity context(AbstractResourceRelationEntity relation, ContextEntity environment) {
        ResourceRelationContextEntity context = relation.getOrCreateContext(environment);
        context.setPropertyDescriptors(new HashSet<>());
        context.setResourceActivationEntities(new HashSet<>());
        entityManager.persist(context);
        return context;
    }

    private PropertyDescriptorEntity descriptor(ResourceRelationContextEntity context, String name, boolean encrypted) {
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntity();
        descriptor.setPropertyName(name);
        descriptor.setEncrypt(encrypted);
        descriptor.setDisplayName("Display " + name);
        descriptor.setDefaultValue("default");
        descriptor.setValidationLogic(".*");
        descriptor.setPropertyComment("comment");
        context.addPropertyDescriptor(descriptor);
        entityManager.persist(descriptor);
        return descriptor;
    }

    private PropertyEntity property(ResourceRelationContextEntity context, PropertyDescriptorEntity descriptor, String value) {
        PropertyEntity property = new PropertyEntity();
        property.setDescriptor(descriptor);
        property.setValue(value);
        descriptor.addProperty(property);
        context.addProperty(property);
        entityManager.persist(property);
        return property;
    }

    private MergeDuplicateRelationsService.MigrationResult migrate(boolean dryRun) {
        entityManager.flush();
        return service.migrate(dryRun);
    }

    @Test
    void mergesAllConfigurationAndKeepsOtherReleasesAndPermissionsUntouched() {
        AbstractResourceRelationEntity first = relation(false);
        AbstractResourceRelationEntity second = relation(false);
        ResourceRelationContextEntity c1 = context(first, global);
        ResourceRelationContextEntity c2 = context(second, global);
        PropertyDescriptorEntity d1 = descriptor(c1, "password", true);
        PropertyDescriptorEntity d2 = descriptor(c2, "password", true);
        property(c1, d1, "ciphertext-retained");
        property(c2, d2, "ciphertext-discarded");
        PropertyDescriptorEntity only = descriptor(c2, "only-second", false);
        PropertyTagEntity tag = new PropertyTagEntity("merge-tag", null);
        entityManager.persist(tag);
        only.addPropertyTag(tag);
        property(c2, only, "preserved");
        TemplateDescriptorEntity template = new TemplateDescriptorEntity();
        template.setName("config");
        template.setFileContent("line1\nline2");
        template.setTargetPath("/config");
        template.setTargetPlatforms(new HashSet<>(Collections.singleton(slave.getResourceGroup())));
        c2.addTemplate(template);
        entityManager.persist(template);
        ResourceActivationEntity activation = new ResourceActivationEntity();
        activation.setResourceRelationContext(c2);
        activation.setResourceGroup(slave.getResourceGroup());
        activation.setActive(false);
        c2.getResourceActivationEntities().add(activation);
        entityManager.persist(activation);
        ResourceEntity otherRelease = ResourceFactory.createNewResource(slave.getResourceGroup());
        entityManager.persist(otherRelease);
        ConsumedResourceRelationEntity other = new ConsumedResourceRelationEntity();
        other.setMasterResource(master);
        other.setSlaveResource(otherRelease);
        master.addConsumedRelation(other);
        entityManager.persist(other);
        MergeDuplicateRelationsService.MigrationResult preview = migrate(true);
        assertFalse(preview.isApplied());
        assertNotNull(entityManager.find(ConsumedResourceRelationEntity.class, second.getId()));
        assertTrue(preview.getGroups().get(0).getDiscarded().stream().anyMatch(s -> s.startsWith("property key=password")));
        assertFalse(preview.toString().contains("ciphertext"));
        MergeDuplicateRelationsService.MigrationResult applied = migrate(false);
        assertTrue(applied.isApplied());
        assertNotNull(applied.getAuditRevisionId());
        entityManager.clear();
        ConsumedResourceRelationEntity retained = entityManager.find(ConsumedResourceRelationEntity.class, first.getId());
        assertNull(entityManager.find(ConsumedResourceRelationEntity.class, second.getId()));
        assertNotNull(entityManager.find(ConsumedResourceRelationEntity.class, other.getId()));
        ResourceRelationContextEntity merged = retained.getContexts().iterator().next();
        assertEquals(2, merged.getProperties().size());
        assertEquals(2, merged.getPropertyDescriptors().size());
        assertEquals("ciphertext-retained", merged.getProperties().stream()
                .filter(p -> p.getDescriptor().getPropertyName().equals("password")).findFirst().orElseThrow().getValue());
        assertTrue(merged.getProperties().stream().anyMatch(p -> "preserved".equals(p.getValue())));
        assertTrue(merged.getPropertyDescriptors().stream()
                .anyMatch(d -> d.getPropertyTagsNameSet().contains("merge-tag")));
        TemplateDescriptorEntity restored = merged.getTemplates().iterator().next();
        assertEquals("line1\nline2", restored.getFileContent());
        assertEquals("/config", restored.getTargetPath());
        assertEquals(1, restored.getTargetPlatforms().size());
        assertFalse(merged.getResourceActivationEntities().iterator().next().isActive());
        verify(permissionBoundary, never()).removeAllRestrictionsForResourceGroup(any());
    }

    @Test
    void commitsEnversHistoryAndCanRollBackTheEntireMerge() {
        AbstractResourceRelationEntity first = relation(false);
        AbstractResourceRelationEntity second = relation(false);
        ResourceRelationContextEntity c2 = context(second, global);
        property(c2, descriptor(c2, "setting", false), "value");
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        migrate(false);
        entityManager.getTransaction().rollback();
        entityManager.clear();
        entityManager.getTransaction().begin();
        assertNotNull(entityManager.find(ConsumedResourceRelationEntity.class, second.getId()));
        MergeDuplicateRelationsService.MigrationResult applied = migrate(false);
        entityManager.getTransaction().commit();
        entityManager.clear();
        entityManager.getTransaction().begin();
        assertNull(entityManager.find(ConsumedResourceRelationEntity.class, second.getId()));
        assertTrue(AuditReaderFactory.get(entityManager)
                .getRevisions(ConsumedResourceRelationEntity.class, second.getId()).contains(applied.getAuditRevisionId()));
        assertEquals(master.getId(), entityManager.find(MyRevisionEntity.class, applied.getAuditRevisionId()).getResourceId());
    }

    @Test
    void mergesAnUnconfiguredTripleWithoutCreatingOtherReleases() {
        AbstractResourceRelationEntity first = relation(false);
        AbstractResourceRelationEntity second = relation(false);
        AbstractResourceRelationEntity third = relation(false);
        MergeDuplicateRelationsService.MigrationResult applied = migrate(false);
        assertEquals(1, applied.getGroupCount());
        assertEquals(2, applied.getSurplusRelationCount());
        entityManager.clear();
        assertNotNull(entityManager.find(ConsumedResourceRelationEntity.class, first.getId()));
        assertNull(entityManager.find(ConsumedResourceRelationEntity.class, second.getId()));
        assertNull(entityManager.find(ConsumedResourceRelationEntity.class, third.getId()));
        assertEquals(1L, entityManager.createQuery("select count(r) from ConsumedResourceRelationEntity r", Long.class)
                .getSingleResult());
    }

    @Test
    void convertsEncryptionOnlyInsideBackendWhenDescriptorFlagsConflict() {
        AbstractResourceRelationEntity first = relation(false);
        AbstractResourceRelationEntity second = relation(false);
        descriptor(context(first, global), "setting", false);
        ResourceRelationContextEntity c2 = context(second, global);
        property(c2, descriptor(c2, "setting", true), "ciphertext");
        try (org.mockito.MockedStatic<TemplateUtils> crypto = mockStatic(TemplateUtils.class)) {
            crypto.when(() -> TemplateUtils.decrypt("ciphertext")).thenReturn("plaintext");
            MergeDuplicateRelationsService.MigrationResult preview = migrate(true);
            assertTrue(preview.getGroups().get(0).getDiscarded().stream().anyMatch(s -> s.contains("toEncrypted=false")));
            migrate(false);
        }
        entityManager.clear();
        PropertyEntity restored = entityManager.find(ConsumedResourceRelationEntity.class, first.getId())
                .getContexts().iterator().next().getProperties().iterator().next();
        assertFalse(restored.getDescriptor().isEncrypt());
        assertEquals("plaintext", restored.getValue());
    }

    @Test
    void preservesProvidedRelationsAndFillsMissingEnvironments() {
        AbstractResourceRelationEntity first = relation(true);
        AbstractResourceRelationEntity second = relation(true);
        ContextEntity test = new ContextEntity();
        test.setName("test");
        entityManager.persist(test);
        ResourceRelationContextEntity c2 = context(second, test);
        property(c2, descriptor(c2, "setting", false), "value");
        migrate(false);
        entityManager.clear();
        assertNull(entityManager.find(ProvidedResourceRelationEntity.class, second.getId()));
        assertEquals(test.getId(), entityManager.find(ProvidedResourceRelationEntity.class, first.getId())
                .getContexts().iterator().next().getContext().getId());
    }

    @Test
    void previewsEveryGroupWithoutWritesAndAppliesWithoutAPreviousDryRun() {
        AbstractResourceRelationEntity first = relation(false);
        AbstractResourceRelationEntity second = relation(false);
        relation(master, true, "named");
        relation(master, true, "named");
        ResourceEntity otherMaster = ResourceFactory.createNewResource("other-master");
        entityManager.persist(otherMaster);
        relation(otherMaster, false, "named");
        AbstractResourceRelationEntity later = relation(otherMaster, false, "named");
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        Long revisionsBefore = entityManager.createQuery("select count(r) from MyRevisionEntity r", Long.class).getSingleResult();
        MergeDuplicateRelationsService.MigrationResult preview = migrate(true);
        assertEquals(3, preview.getGroupCount());
        assertEquals(3, preview.getSurplusRelationCount());
        assertFalse(preview.isApplied());
        assertNull(preview.getAuditRevisionId());
        assertTrue(preview.getGroups().stream().noneMatch(MergeDuplicateRelationsService.Result::isApplied));
        entityManager.flush();
        assertEquals(revisionsBefore, entityManager.createQuery("select count(r) from MyRevisionEntity r", Long.class).getSingleResult());
        assertNotNull(entityManager.find(ConsumedResourceRelationEntity.class, second.getId()));
        assertNotNull(entityManager.find(ConsumedResourceRelationEntity.class, later.getId()));
        entityManager.getTransaction().rollback();
        entityManager.clear();
        entityManager.getTransaction().begin();
        MergeDuplicateRelationsService.MigrationResult applied = migrate(false);
        assertEquals(3, applied.getGroupCount());
        assertTrue(applied.getGroups().stream().allMatch(MergeDuplicateRelationsService.Result::isApplied));
        entityManager.getTransaction().commit();
        entityManager.clear();
        entityManager.getTransaction().begin();
        assertNotNull(entityManager.find(ConsumedResourceRelationEntity.class, first.getId()));
        assertNull(entityManager.find(ConsumedResourceRelationEntity.class, second.getId()));
        assertNull(entityManager.find(ConsumedResourceRelationEntity.class, later.getId()));
        assertTrue(AuditReaderFactory.get(entityManager).getRevisions(ConsumedResourceRelationEntity.class, second.getId())
                .contains(applied.getAuditRevisionId()));
        assertTrue(AuditReaderFactory.get(entityManager).getRevisions(ConsumedResourceRelationEntity.class, later.getId())
                .contains(applied.getAuditRevisionId()));
        assertNull(entityManager.find(MyRevisionEntity.class, applied.getAuditRevisionId()).getResourceId());
        MergeDuplicateRelationsService.MigrationResult repeated = migrate(false);
        assertTrue(repeated.isApplied());
        assertEquals(0, repeated.getGroupCount());
        assertNull(repeated.getAuditRevisionId());
    }

    @Test
    void aFailureInALaterGroupRollsBackEarlierGroupsToo() {
        AbstractResourceRelationEntity first = relation(false);
        AbstractResourceRelationEntity second = relation(false);
        AbstractResourceRelationEntity laterFirst = relation(master, true, null);
        AbstractResourceRelationEntity laterSecond = relation(master, true, null);
        ResourceRelationContextEntity laterContext = context(laterSecond, global);
        PropertyDescriptorEntity descriptor = descriptor(laterContext, "setting", false);
        PropertyEntity outside = new PropertyEntity();
        outside.setDescriptor(descriptor);
        outside.setValue("outside");
        descriptor.addProperty(outside);
        entityManager.persist(outside);
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        assertThrows(MergeDuplicateRelationsService.MergeException.class, () -> migrate(false));
        assertNull(entityManager.find(ConsumedResourceRelationEntity.class, second.getId()));
        entityManager.getTransaction().rollback();
        entityManager.clear();
        entityManager.getTransaction().begin();
        assertNotNull(entityManager.find(ConsumedResourceRelationEntity.class, first.getId()));
        assertNotNull(entityManager.find(ConsumedResourceRelationEntity.class, second.getId()));
        assertNotNull(entityManager.find(ProvidedResourceRelationEntity.class, laterFirst.getId()));
        assertNotNull(entityManager.find(ProvidedResourceRelationEntity.class, laterSecond.getId()));
    }

    @Test
    void rejectsMissingContextPermissionAndSharedLocalDescriptors() {
        AbstractResourceRelationEntity first = relation(false);
        AbstractResourceRelationEntity second = relation(false);
        ResourceRelationContextEntity c2 = context(second, global);
        PropertyDescriptorEntity descriptor = descriptor(c2, "setting", false);
        PropertyEntity external = new PropertyEntity();
        external.setDescriptor(descriptor);
        external.setValue("outside");
        descriptor.addProperty(external);
        entityManager.persist(external);
        assertThrows(MergeDuplicateRelationsService.MergeException.class, () -> migrate(true));
        when(permissionBoundary.hasPermission(any(), any(), any(), any(ResourceEntity.class), isNull())).thenReturn(false);
        assertThrows(NotAuthorizedException.class, () -> migrate(true));
        assertNotNull(entityManager.find(ConsumedResourceRelationEntity.class, second.getId()));
    }
}
