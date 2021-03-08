package ch.puzzle.itc.mobiliar.presentation.templateEdit;

import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownStpEntity;
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

    @Spy
    TemplateEditor templateEditor;

    @Mock
    ShakedownStpEntity selectedStp;

    @Spy
    @InjectMocks
    EditTemplateView editTemplateView;

    @Spy
    private TemplateDescriptorEntity template;

    @Before
    public void setUp() {
        doReturn(false).when(settings).isTestingMode();
        doReturn(false).when(editTemplateView).fail(any(AMWException.class));
        doNothing().when(editTemplateView).succeed();

        template = editTemplateView.getTemplate();
        template.setId(1);
    }

    @Test
    public void shouldRejectTemplateInTestingModeWihtoutSelectedSTP() throws AMWException {
        // given
        doReturn(true).when(settings).isTestingMode();
        editTemplateView.setSelectedStpId(9999); // clear selectedStp in the view

        // when
        editTemplateView.save();

        // then
        verify(editTemplateView).throwError("No STP-name selected!");
        verify(editTemplateView, never()).succeed();
    }

    @Test
    public void shouldSetTemplateNameFromSelectedStp() throws AMWException {
        // given
        doReturn(true).when(settings).isTestingMode();
        doReturn("name-of-selected-stp").when(selectedStp).getStpName();

        editTemplateView.setResourceTypeId(10);
        doNothing().when(templateEditor).saveTemplateForResourceType(template, 10, true);

        // when
        editTemplateView.save();

        // then
        verify(template).setName("name-of-selected-stp");
        verify(editTemplateView).succeed();
    }

    @Test
    public void shouldSaveTemplateForRelationWithResourceIdNull() throws AMWException {
        // given
        editTemplateView.setRelationIdForTemplate(1);
        editTemplateView.setResourceId(null);
        doNothing().when(templateEditor).saveTemplateForRelation(template, 1, false);

        // when
        editTemplateView.save();

        // then
        verify(templateEditor).saveTemplateForRelation(template, 1, false);
        verify(editTemplateView).succeed();

    }

    @Test
    public void shouldSaveTemplateForRelationWithResourceId() throws AMWException {
        editTemplateView.setRelationIdForTemplate(1);
        editTemplateView.setResourceId(123);
        doNothing().when(templateEditor).saveTemplateForRelation(template, 1, true);

        // when
        editTemplateView.save();

        // then
        verify(templateEditor).saveTemplateForRelation(template, 1, true);
        verify(editTemplateView).succeed();
    }

    @Test
    public void shouldSaveTemplateForResourceType() throws AMWException {
        // given
        editTemplateView.setRelationIdForTemplate(null);
        editTemplateView.setResourceId(null);
        editTemplateView.setResourceTypeId(99);
        doNothing().when(templateEditor).saveTemplateForResourceType(template, 99, false);

        // when
        editTemplateView.save();

        // then
        verify(templateEditor).saveTemplateForResourceType(template, 99, settings.isTestingMode());
        verify(editTemplateView).succeed();
    }

    @Test
    public void shouldSaveTemplateForResource() throws AMWException {
        // given
        editTemplateView.setResourceTypeId(null);
        editTemplateView.setRelationIdForTemplate(null);
        editTemplateView.setResourceId(10);
        doNothing().when(templateEditor).saveTemplateForResource(template, 10, false);

        // when
        editTemplateView.save();

        // then
        verify(templateEditor).saveTemplateForResource(template, 10, settings.isTestingMode());
        verify(editTemplateView).succeed();
    }
}
