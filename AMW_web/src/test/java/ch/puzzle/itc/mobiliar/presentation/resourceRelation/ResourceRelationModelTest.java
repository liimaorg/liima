package ch.puzzle.itc.mobiliar.presentation.resourceRelation;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ResourceRelationModelTest {

    private ResourceRelationModel relationModel;

    @Before
    public void setUp() {
        relationModel = new ResourceRelationModel();
    }

    @Test
    public void shouldReturnNewEditRelationIfBothHaveNoIdentifierAndSameSlaveId() {
        // given
        ResourceEditRelation existingEditRelation = new ResourceEditRelation(null, 12, null, null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation newEditRelation = new ResourceEditRelation(null, 12, null, null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        // when
        ResourceEditRelation replaced = relationModel.replaceRelation(newEditRelation, existingEditRelation);

        // then
        assertThat(replaced, is(newEditRelation));
    }

    @Test
    public void shouldReturnNewEditRelationIfBothHaveSameIdentifierAndSameSlaveId() {
        // given
        ResourceEditRelation existingEditRelation = new ResourceEditRelation(null, 12, "Test", null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "PROVIDED", null);
        ResourceEditRelation newEditRelation = new ResourceEditRelation(null, 12, "Test", null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "PROVIDED", null);
        // when
        ResourceEditRelation replaced = relationModel.replaceRelation(newEditRelation, existingEditRelation);

        // then
        assertThat(replaced, is(newEditRelation));
    }

    @Test
    public void shouldReturnExistingEditRelationIfBothHaveNoIdentifierAndDifferentSlaveIds() {
        // given
        ResourceEditRelation existingEditRelation = new ResourceEditRelation(null, 12, null, null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation newEditRelation = new ResourceEditRelation(null, 13, null, null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        // when
        ResourceEditRelation replaced = relationModel.replaceRelation(newEditRelation, existingEditRelation);

        // then
        assertThat(replaced, is(existingEditRelation));
    }

    @Test
    public void shouldReturnExistingEditRelationIfBothHaveSameIdentifierButDifferentSlaveIds() {
        // given
        ResourceEditRelation existingEditRelation = new ResourceEditRelation(null, 12, "Test", null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "PROVIDED", null);
        ResourceEditRelation newEditRelation = new ResourceEditRelation(null, 13, "Test", null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "PROVIDED", null);
        // when
        ResourceEditRelation replaced = relationModel.replaceRelation(newEditRelation, existingEditRelation);

        // then
        assertThat(replaced, is(existingEditRelation));
    }

    @Test
    public void shouldReturnExistingEditRelationIfTheyHaveDifferentIdentifiers() {
        // given
        ResourceEditRelation existingEditRelation = new ResourceEditRelation(null, null, "Test", null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation newEditRelation = new ResourceEditRelation(null, null, "Another", null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        // when
        ResourceEditRelation replaced = relationModel.replaceRelation(newEditRelation, existingEditRelation);

        // then
        assertThat(replaced, is(existingEditRelation));
    }

}