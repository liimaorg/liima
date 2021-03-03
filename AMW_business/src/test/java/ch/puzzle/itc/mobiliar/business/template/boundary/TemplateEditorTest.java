package ch.puzzle.itc.mobiliar.business.template.boundary;

import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void setUp() throws Exception {
        templateDescriptorEntity = new TemplateDescriptorEntity();
        templateDescriptorEntity.setName("template-name");
        templateDescriptorEntity.setName("a/valid/target-path");
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
    public void shouldRejectSaveTemplateForResourceTypeInTestingMode_missingPermission() {
        // given
        doReturn(false).when(permissionService).hasPermission(Permission.SHAKEDOWN_TEST_MODE);
        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class,
                                                        () -> templateEditor.saveTemplateForResourceType(
                                                                templateDescriptorEntity,
                                                                1,
                                                                true));
        // then
        assertThat(exception.getMessage(), is("Not Authorized! You're not allowed to execute shakedown test mode!"));
    }

    @Test
    public void shouldAllowSaveTemplateForResourceTypeInTestingMode_withPermission() throws AMWException {
        // given
        doReturn(true).when(permissionService).hasPermission(Permission.SHAKEDOWN_TEST_MODE);
        ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
        doReturn(resourceTypeEntity).when(entityManager).find(ResourceTypeEntity.class, 1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeEntity);

        // when
        templateEditor.saveTemplateForResourceType(templateDescriptorEntity, 1, true);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeEntity);
    }

    @Test
    public void shouldRejectSaveTemplateForResourceType_missingPermissionForActionCreate() {
        // given
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, Action.CREATE);

        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class,
                                                        () -> templateEditor.saveTemplateForResourceType(
                                                                templateDescriptorEntity,
                                                                1,
                                                                false));

        // then
        assertThat(exception.getMessage(),
                   is("Not Authorized! You're not allowed to create/ modify resource type templates!"));
    }

    @Test
    public void shouldRejectSaveTemplateForResourceType_missingPermissionForActionUpdate() {
        // given
        templateDescriptorEntity.setId(1);
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, Action.UPDATE);

        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class,
                                                        () -> templateEditor.saveTemplateForResourceType(
                                                                templateDescriptorEntity,
                                                                1,
                                                                false));

        // then
        assertThat(exception.getMessage(),
                   is("Not Authorized! You're not allowed to create/ modify resource type templates!"));
    }

    @Test
    public void shouldAllowSaveTemplateForResourceType_actionAdd() throws AMWException {
        // given
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, Action.CREATE);
        ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
        doReturn(resourceTypeEntity).when(entityManager).find(ResourceTypeEntity.class, 1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeEntity);

        // when
        templateEditor.saveTemplateForResourceType(templateDescriptorEntity, 1, false);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeEntity);
    }

    @Test
    public void shouldAllowSaveTemplateForResourceType_actionUpdate() throws AMWException {
        // given
        templateDescriptorEntity.setId(1);
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, Action.UPDATE);
        ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
        doReturn(resourceTypeEntity).when(entityManager).find(ResourceTypeEntity.class, 1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeEntity);

        // when
        templateEditor.saveTemplateForResourceType(templateDescriptorEntity, 1, false);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeEntity);
    }

    @Test
    public void shouldRejectSaveTemplateForResourceInTestingMode_missingPermission() {
        // given
        doReturn(false).when(permissionService).hasPermission(Permission.SHAKEDOWN_TEST_MODE);
        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class,
                                                        () -> templateEditor.saveTemplateForResource(
                                                                templateDescriptorEntity,
                                                                1,
                                                                true));
        // then
        assertThat(exception.getMessage(), is("Not Authorized! You're not allowed to execute shakedown test mode!"));
    }

    @Test
    public void shouldAllowSaveTemplateForResourceInTestingMode_withPermission() throws AMWException {
        // given
        doReturn(true).when(permissionService).hasPermission(Permission.SHAKEDOWN_TEST_MODE);
        ResourceEntity resourceEntity = new ResourceEntity();
        doReturn(resourceEntity).when(entityManager).find(ResourceEntity.class, 1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceEntity);

        // when
        templateEditor.saveTemplateForResource(templateDescriptorEntity, 1, true);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceEntity);
    }

    @Test
    public void shouldRejectSaveTemplateForResource_missingPermissionForActionCreate() {
        // given
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, Action.CREATE);

        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class,
                                                        () -> templateEditor.saveTemplateForResource(
                                                                templateDescriptorEntity,
                                                                1,
                                                                false));

        // then
        assertThat(exception.getMessage(),
                   is("Not Authorized! You're not allowed to create/ modify resource templates!"));
    }

    @Test
    public void shouldRejectSaveTemplateForResource_missingPermissionForActionUpdate() {
        // given
        templateDescriptorEntity.setId(1);
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, Action.UPDATE);

        // when
        NotAuthorizedException exception = assertThrows(NotAuthorizedException.class,
                                                        () -> templateEditor.saveTemplateForResource(
                                                                templateDescriptorEntity,
                                                                1,
                                                                false));

        // then
        assertThat(exception.getMessage(),
                   is("Not Authorized! You're not allowed to create/ modify resource templates!"));
    }

    @Test
    public void shouldAllowSaveTemplateForResource_actionAdd() throws AMWException {
        // given
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, Action.CREATE);
        ResourceEntity resourceEntity = new ResourceEntity();
        doReturn(resourceEntity).when(entityManager).find(ResourceEntity.class, 1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceEntity);

        // when
        templateEditor.saveTemplateForResource(templateDescriptorEntity, 1, false);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceEntity);
    }

    @Test
    public void shouldAllowSaveTemplateForResource_actionUpdate() throws AMWException {
        // given
        templateDescriptorEntity.setId(1);
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, Action.UPDATE);
        ResourceEntity resourceEntity = new ResourceEntity();
        doReturn(resourceEntity).when(entityManager).find(ResourceEntity.class, 1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceEntity);

        // when
        templateEditor.saveTemplateForResource(templateDescriptorEntity, 1, false);

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
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, Action.UPDATE);

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
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCE_TEMPLATE, Action.UPDATE);
        AbstractResourceRelationEntity resourceRelation = mock(AbstractResourceRelationEntity.class);

        doReturn(resourceRelation).when(relationService).getResourceRelation(1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceRelation);

        // when
        templateEditor.saveTemplateForRelation(templateDescriptorEntity, 1, true);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceRelation);
    }

    @Test
    public void shouldRejectSaveTemplateForRelation_noPermissionForResourceType() throws AMWException {
        // given
        doReturn(false).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, Action.UPDATE);

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
        doReturn(true).when(permissionService).hasPermission(Permission.RESOURCETYPE_TEMPLATE, Action.UPDATE);
        ResourceRelationTypeEntity resourceTypeRelation = new ResourceRelationTypeEntity();
        doReturn(resourceTypeRelation).when(relationService).getResourceTypeRelation(1);
        doNothing().when(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeRelation);

        // when
        templateEditor.saveTemplateForRelation(templateDescriptorEntity, 1, false);

        // then
        verify(templateEditor).saveTemplate(templateDescriptorEntity, resourceTypeRelation);
    }
}