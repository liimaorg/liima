package ch.mobi.itc.mobiliar.rest.tags.usecases;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(PersistenceTestRunner.class)
public class RemoveTagUseCaseTest {

    @PersistenceContext
    private EntityManager em;

    private RemoveTagUseCase useCase;

    @Before
    public void setUp() {
        PropertyTagEditingService propertyTagEditingService = new PropertyTagEditingService(em,
                                                                                            Logger.getLogger(
                                                                                                    RemoveTagUseCase.class.getName()));
        useCase = new RemoveTagUseCase(propertyTagEditingService);
    }

    @Test(expected = NoResultException.class)
    public void shouldThrowNoResultException_noTagsInDB() {
        // given

        // when
        useCase.removeTag(-1);

        // then
        fail("should have thrown exception");
    }

    @Test(expected = NoResultException.class)
    public void shouldThrowNoResultException_wrongId() {
        // given
        PropertyTagEntity tag = new PropertyTagEntity("test", PropertyTagType.GLOBAL);
        em.persist(tag);

        // when
        useCase.removeTag(tag.getId() + 999);

        // then
        fail("should have thrown exception");
    }

    @Test
    public void shouldRemoveTag() {
        // given
        PropertyTagEntity tag = new PropertyTagEntity("test", PropertyTagType.GLOBAL);
        em.persist(tag);

        // when
        useCase.removeTag(tag.getId());

        // then
        assertNull(em.find(PropertyTagEntity.class, tag.getId()));
    }
}