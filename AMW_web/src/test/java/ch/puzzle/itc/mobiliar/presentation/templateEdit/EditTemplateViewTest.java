package ch.puzzle.itc.mobiliar.presentation.templateEdit;

import ch.puzzle.itc.mobiliar.business.template.boundary.TemplateEditor;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.presentation.util.UserSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EditTemplateViewTest {

    @Mock
    UserSettings settings;

    @Mock
    TemplateEditor templateEditor;

    @Spy
    @InjectMocks
    EditTemplateView editTemplateView;

    private TemplateDescriptorEntity template;

    @Before
    public void setUp() {
        doReturn(false).when(settings).isTestingMode();
        doReturn(true).when(editTemplateView).canModifyTemplates();
        doReturn(false).when(editTemplateView).fail(any(AMWException.class));

        template = editTemplateView.getTemplate();
        template.setId(1);
    }

    @Test
    public void shouldRejectTemplateCreateIfNotAllowed() throws AMWException {
        // given
        template.setId(null);
        doReturn(false).when(editTemplateView).canModifyTemplates();

        // when
        editTemplateView.save();

        // then
        verify(editTemplateView, never()).succeed();
        verify(editTemplateView).throwError("No permission to create template!");
    }

    @Test
    public void shouldRejectTemplateModifyIfNotAllowed() throws AMWException {
        // given
        doReturn(false).when(editTemplateView).canModifyTemplates();

        //when
        editTemplateView.save();

        //then
        verify(editTemplateView, never()).succeed();
        verify(editTemplateView).throwError("No permission to modify templates!");
    }

    @Test
    public void shouldRejectAbsoluteTemplatePath() throws AMWException {
        // given
        template.setTargetPath("/absolute/path/is/not/allowed");

        // when
        editTemplateView.save();

        // then
        verify(editTemplateView).throwError("absolute paths are not allowed for file path");
        verify(editTemplateView, never()).succeed();
    }

    @Test
    public void shouldRejectTemplatePathStartsWithPathTraversals() throws AMWException {
        // given
        template.setTargetPath("../../path/traversals/not/allowed");

        // when
        editTemplateView.save();

        // then
        verify(editTemplateView).throwError("no path traversals like '../' allowed in file path");
        verify(editTemplateView, never()).succeed();
    }

    @Test
    public void shouldRejectTemplatePathWithPathTraversalsInsidePath() throws AMWException {
        // given
        template.setTargetPath("you/can/../../../../traverse/paths/like/this");

        // when
        editTemplateView.save();

        // then
        verify(editTemplateView).throwError("no path traversals like '../' allowed in file path");
        verify(editTemplateView, never()).succeed();
    }
}