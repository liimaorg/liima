package ch.puzzle.itc.mobiliar.business.property.boundary;

import static ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType.GLOBAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;

@ExtendWith(PersistenceTestExtension.class)
public class AddTagUseCaseTest {

    @PersistenceContext
    EntityManager em;

    AddTagUseCase useCase;

    @BeforeEach
    public void setUp() throws Exception {
        useCase = new AddTagUseCase(new PropertyTagEditingService(em, Logger.getLogger(AddTagUseCase.class.getName())));
    }

    @Test
    public void shouldAddTag() throws ValidationException {
        // given

        // when
        PropertyTagEntity tag = useCase.addTag(new TagCommand("test-tag"));

        // then
        assertEquals(tag, em.find(PropertyTagEntity.class, tag.getId()));
    }

    @Test
    public void shouldThrowExceptionWhenTagWithSameNameAlreadyExists() throws ValidationException {
        // given
        em.persist(new PropertyTagEntity("test-tag", GLOBAL));

        // when
        assertThrows(ValidationException.class, () -> {
            useCase.addTag(new TagCommand("test-tag"));
        });
    }
}