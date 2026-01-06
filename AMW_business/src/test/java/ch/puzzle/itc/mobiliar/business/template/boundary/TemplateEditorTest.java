package ch.puzzle.itc.mobiliar.business.template.boundary;

import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TemplateEditorTest {

    @InjectMocks
    @Spy
    TemplateEditor templateEditor;

    @Spy
    PermissionService permissionService;

    @Mock
    EntityManager entityManager;

    @Mock
    ResourceRelationService relationService;

    TemplateDescriptorEntity templateDescriptorEntity;

    @BeforeEach
    public void setUp() throws Exception {
        templateDescriptorEntity = new TemplateDescriptorEntity();
        templateDescriptorEntity.setName("template-name");
        templateDescriptorEntity.setName("a/valid/target-path");
        templateDescriptorEntity.getOwnerResource();
        //templateDescriptorEntity.setMaste
    }

    @Test
    public void shouldThrowExceptionEmptyTemplateName() {
        // given
        templateDescriptorEntity.setName("");

        // when
        AMWException exception = assertThrows(AMWException.class,
                                              () -> templateEditor.validateTemplate(templateDescriptorEntity));

        // then
        assertThat(exception.getMessage(), is("The template name must not be empty"));
    }

    @Test
    public void shouldThrowExceptionPathStartsWithPathTraversal() {
        // given
        templateDescriptorEntity.setTargetPath("../starts/with/path-traversal");

        // when
        AMWException exception = assertThrows(AMWException.class,
                                              () -> templateEditor.validateTemplate(templateDescriptorEntity));

        // then
        assertThat(exception.getMessage(), is("No path traversals like '../' allowed in file path"));
    }

    @Test
    public void shouldThrowExceptionPathContainsPathTraversals() {
        // given
        templateDescriptorEntity.setTargetPath("path/contains/../../../../path-traversals");

        // when
        AMWException exception = assertThrows(AMWException.class,
                                              () -> templateEditor.validateTemplate(templateDescriptorEntity));

        // then
        assertThat(exception.getMessage(), is("No path traversals like '../' allowed in file path"));
    }

    @Test
    public void shouldThrowExpectionAbsoluteTemplatePath()  {
        // given
        templateDescriptorEntity.setTargetPath("/absolute/path/is/not/allowed");

        // when
        AMWException exception = assertThrows(AMWException.class,
                                              () -> templateEditor.validateTemplate(templateDescriptorEntity));

        // then
        assertThat(exception.getMessage(), is("Absolute paths are not allowed for file path"));
    }

    @Test
    public void shouldNotThrowExceptionForTemplateWithNameAndValidPath() throws AMWException {
        // given

        // when
        templateEditor.validateTemplate(templateDescriptorEntity);

        // then
        // test fails if an exception is thrown
    }

    @Test
    public void shouldRejectSaveTemplateForResourceType_missingPermissionForActionCreate() {
        // given
        ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
        doReturn(resourceTypeEntity).when(entityManager).find(ResourceTypeEntity.class, 1);
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, null, Action.CREATE, null, resourceTypeEntity);

        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class,
                                                        () -> templateEditor.saveTemplateForResourceType(
                                                                templateDescriptorEntity,
                                                                1));

        // then
        assertThat(exception.getMessage(),
                   is("Not Authorized! You're not allowed to create/modify resource type templates!"));
    }

    @Test
    public void shouldRejectSaveTemplateForResourceType_missingPermissionForActionUpdate() {
        // given
        ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
        doReturn(resourceTypeEntity).when(entityManager).find(ResourceTypeEntity.class, 1);
        templateDescriptorEntity.setId(1);
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, null, Action.UPDATE, null, resourceTypeEntity);

        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class,
                                                        () -> templateEditor.saveTemplateForResourceType(
                                                                templateDescriptorEntity,
                                                                1));

        // then
        assertThat(exception.getMessage(),
                   is("Not Authorized! You're not allowed to create/modify resource type templates!"));
    }

    @Test
    public void shouldAllowSaveTemplateForResourceType_actionAdd() throws AMWException {
        // given
        ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, null, Action.CREATE, null, resourceTypeEntity);
        doReturn(resourceTypeEntity).when(entityManager).find(ResourceTypeEntity.class, 1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeEntity);

        // when
        templateEditor.saveTemplateForResourceType(templateDescriptorEntity, 1);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeEntity);
    }

    @Test
    public void shouldAllowSaveTemplateForResourceType_actionUpdate() throws AMWException {
        // given
        ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
        doReturn(resourceTypeEntity).when(entityManager).find(ResourceTypeEntity.class, 1);
        templateDescriptorEntity.setId(1);
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, null, Action.UPDATE, null, resourceTypeEntity);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeEntity);

        // when
        templateEditor.saveTemplateForResourceType(templateDescriptorEntity, 1);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeEntity);
    }

    @Test
    public void shouldRejectSaveTemplateForResource_missingPermissionForActionCreate() {
        // given        
        ResourceEntity resourceEntity = new ResourceEntity();
        resourceEntity.setResourceGroup(new ResourceGroupEntity());
        doReturn(resourceEntity).when(entityManager).find(ResourceEntity.class, 1);
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, null, Action.CREATE, resourceEntity.getResourceGroup(), null);

        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class,
                                                        () -> templateEditor.saveTemplateForResource(
                                                                templateDescriptorEntity,
                                                                1));

        // then
        assertThat(exception.getMessage(),
                   is("Not Authorized! You're not allowed to create/modify resource templates!"));
    }

    @Test
    public void shouldRejectSaveTemplateForResource_missingPermissionForActionUpdate() {
        // given
        ResourceEntity resourceEntity = new ResourceEntity();
        resourceEntity.setResourceGroup(new ResourceGroupEntity());
        doReturn(resourceEntity).when(entityManager).find(ResourceEntity.class, 1);
        templateDescriptorEntity.setId(1);
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, null, Action.UPDATE, resourceEntity.getResourceGroup(), null);

        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class,
                                                        () -> templateEditor.saveTemplateForResource(
                                                                templateDescriptorEntity,
                                                                1));

        // then
        assertThat(exception.getMessage(),
                   is("Not Authorized! You're not allowed to create/modify resource templates!"));
    }

    @Test
    public void shouldAllowSaveTemplateForResource_actionAdd() throws AMWException {
        // given
        ResourceEntity resourceEntity = new ResourceEntity();
        resourceEntity.setResourceGroup(new ResourceGroupEntity());
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, null, Action.CREATE, resourceEntity.getResourceGroup(), null);
        doReturn(resourceEntity).when(entityManager).find(ResourceEntity.class, 1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceEntity);

        // when
        templateEditor.saveTemplateForResource(templateDescriptorEntity, 1);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceEntity);
    }

    @Test
    public void shouldAllowSaveTemplateForResource_actionUpdate() throws AMWException {
        // given
        ResourceEntity resourceEntity = new ResourceEntity();
        resourceEntity.setResourceGroup(new ResourceGroupEntity());
        templateDescriptorEntity.setId(1);
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, null, Action.UPDATE, resourceEntity.getResourceGroup(), null);
        doReturn(resourceEntity).when(entityManager).find(ResourceEntity.class, 1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceEntity);

        // when
        templateEditor.saveTemplateForResource(templateDescriptorEntity, 1);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceEntity);
    }

    @Test
    public void shouldDoNothingSaveTemplateForRelation_relationIdIsNull() throws AMWException {
        // given
        // when
        templateEditor.saveTemplateForRelation(templateDescriptorEntity, null, true);

        // then
        verify(templateEditor, never()).saveTemplate(any(TemplateDescriptorEntity.class), any(HasContexts.class));
    }

    @Test
    public void shouldRejectSaveTemplateForRelation_noPermissionForResource() throws AMWException {
        // given
        AbstractResourceRelationEntity resourceRelation = mock(AbstractResourceRelationEntity.class);
        ResourceEntity resourceEntity = new ResourceEntity();
        resourceEntity.setResourceGroup(new ResourceGroupEntity());

        doReturn(resourceRelation).when(relationService).getResourceRelation(1);
        doReturn(resourceEntity).when(resourceRelation).getMasterResource();
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, null, Action.UPDATE, resourceEntity.getResourceGroup(), null);

        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () ->
                templateEditor.saveTemplateForRelation(templateDescriptorEntity, 1, true));

        // then
        assertThat(exception.getMessage(),
                   is("Not Authorized! You're not allowed to update templates for resource relations!"));
    }

    @Test
    public void shouldAllowSaveTemplateForRelation_isResourceEdit() throws AMWException {
        // given
        AbstractResourceRelationEntity resourceRelation = mock(AbstractResourceRelationEntity.class);
        ResourceEntity resourceEntity = new ResourceEntity();
        resourceEntity.setResourceGroup(new ResourceGroupEntity());

        doReturn(resourceRelation).when(relationService).getResourceRelation(1);
        doReturn(resourceEntity).when(resourceRelation).getMasterResource();
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, null, Action.UPDATE, resourceEntity.getResourceGroup(), null);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceRelation);

        // when
        templateEditor.saveTemplateForRelation(templateDescriptorEntity, 1, true);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceRelation);
    }

    @Test
    public void shouldRejectSaveTemplateForRelation_noPermissionForResourceType() throws AMWException {
        // given
        ResourceRelationTypeEntity resourceTypeRelation = mock(ResourceRelationTypeEntity.class);
        ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
        doReturn(resourceTypeRelation).when(relationService).getResourceTypeRelation(1);
        doReturn(resourceTypeEntity).when(resourceTypeRelation).getResourceTypeA();

        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, null, Action.UPDATE, null, resourceTypeEntity);

        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () ->
                templateEditor.saveTemplateForRelation(templateDescriptorEntity, 1, false));

        // then
        assertThat(exception.getMessage(),
                   is("Not Authorized! You're not allowed to update templates for resource type relations!"));
    }

    @Test
    public void shouldAllowSaveTemplateForRelation_notResourceEdit() throws AMWException {
        // given
        ResourceRelationTypeEntity resourceTypeRelation = mock(ResourceRelationTypeEntity.class);
        ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
        doReturn(resourceTypeRelation).when(relationService).getResourceTypeRelation(1);
        doReturn(resourceTypeEntity).when(resourceTypeRelation).getResourceTypeA();
        
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, null, Action.UPDATE, null, resourceTypeEntity);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeRelation);

        // when
        templateEditor.saveTemplateForRelation(templateDescriptorEntity, 1, false);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeRelation);
    }
}
