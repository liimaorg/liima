package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(PersistenceTestExtension.class)
public class RemoveTagUseCaseTest {

    @PersistenceContext
    private EntityManager em;

    private RemoveTagUseCase useCase;

    @BeforeEach
    public void setUp() {
        PropertyTagEditingService propertyTagEditingService = new PropertyTagEditingService(em,
                Logger.getLogger(
                        RemoveTagUseCase.class.getName()));
        useCase = new RemoveTagUseCase(propertyTagEditingService);
    }

    @Test
    public void shouldThrowNoResultException_noTagsInDB() {
        assertThrows(NoResultException.class, () -> {
            useCase.removeTag(-1);
        });
    }

    @Test
    public void shouldThrowNoResultException_wrongId() {
        // given
        PropertyTagEntity tag = new PropertyTagEntity("test", PropertyTagType.GLOBAL);
        em.persist(tag);

        // when / then
        assertThrows(NoResultException.class, () -> {
            useCase.removeTag(tag.getId() + 999);
        });
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