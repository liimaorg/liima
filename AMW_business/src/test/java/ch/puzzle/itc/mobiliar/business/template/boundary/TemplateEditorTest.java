package ch.puzzle.itc.mobiliar.business.template.boundary;

import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

public class TemplateEditorTest {

    TemplateEditor templateEditor;
    TemplateDescriptorEntity templateDescriptorEntity;

    @Before
    public void setUp() throws Exception {
         templateEditor = new TemplateEditor();
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
}