package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

import static ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType.GLOBAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(PersistenceTestRunner.class)
public class AddTagUseCaseTest {

    @PersistenceContext
    EntityManager em;

    AddTagUseCase useCase;

    @Before
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

    @Test(expected = ValidationException.class)
    public void shouldThrowExceptionWhenTagWithSameNameAlreadyExists() throws ValidationException {
        // given
        em.persist(new PropertyTagEntity("test-tag", GLOBAL));

        // when
        useCase.addTag(new TagCommand("test-tag"));

        // then
        fail("should have thrown exception");
    }
}