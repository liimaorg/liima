package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

import static ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType.GLOBAL;
import static ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType.LOCAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


@ExtendWith(PersistenceTestExtension.class)
public class ListTagsUseCaseTest {

    @PersistenceContext
    private EntityManager em;

    private ListTagsUseCase useCase;

    @BeforeEach
    public void setUp() {
        PropertyTagEditingService propertyTagEditingService = new PropertyTagEditingService(em, Logger.getLogger(
                ListTagsUseCase.class.getName()));
        useCase = new ListTagsUseCase(propertyTagEditingService);
    }

    @Test
    public void shouldGetAllGlobalTagsWhenNoneIsPersisted() {
        // given

        // when
        List<PropertyTagEntity> propertyTagEntities = useCase.get();

        // then
        assertEquals(0, propertyTagEntities.size());
    }

    @Test
    public void shouldGetAllGlobalTags() {
        // given
        em.persist(new PropertyTagEntity("local-property-tag", LOCAL));
        em.persist(new PropertyTagEntity("tag1", GLOBAL));
        em.persist(new PropertyTagEntity("tag2", GLOBAL));

        // when
        List<PropertyTagEntity> result = useCase.get();

        // then
        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(propertyTagEntity -> LOCAL.equals(propertyTagEntity.getTagType())));
    }
}