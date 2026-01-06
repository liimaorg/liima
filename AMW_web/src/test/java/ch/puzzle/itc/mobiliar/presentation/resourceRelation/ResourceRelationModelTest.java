package ch.puzzle.itc.mobiliar.presentation.resourceRelation;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResourceRelationModelTest {

    private ResourceRelationModel relationModel;

    @BeforeEach
    public void setUp() {
        relationModel = new ResourceRelationModel();
    }

    @Test
    public void shouldReturnNewEditRelationIfBothHaveNoIdentifierAndSameSlaveGroupId() {
        // given
        ResourceEditRelation existingEditRelation = new ResourceEditRelation(null, null, null, null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation newEditRelation = new ResourceEditRelation(null, null, null, null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        // when
        ResourceEditRelation replaced = relationModel.replaceRelation(newEditRelation, existingEditRelation);

        // then
        assertThat(replaced, is(newEditRelation));
    }

    @Test
    public void shouldReturnNewEditRelationIfBothHaveSameIdentifierAndSameSlaveGroupId() {
        // given
        ResourceEditRelation existingEditRelation = new ResourceEditRelation(null, null, "Test", null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "PROVIDED", null);
        ResourceEditRelation newEditRelation = new ResourceEditRelation(null, null, "Test", null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "PROVIDED", null);
        // when
        ResourceEditRelation replaced = relationModel.replaceRelation(newEditRelation, existingEditRelation);

        // then
        assertThat(replaced, is(newEditRelation));
    }

    @Test
    public void shouldReturnExistingEditRelationIfBothHaveNoIdentifierAndDifferentSlaveGroupIds() {
        // given
        ResourceEditRelation existingEditRelation = new ResourceEditRelation(null, null, null, null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation newEditRelation = new ResourceEditRelation(null, null, null, null, null, null, null, null,
                null, null, 2, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        // when
        ResourceEditRelation replaced = relationModel.replaceRelation(newEditRelation, existingEditRelation);

        // then
        assertThat(replaced, is(existingEditRelation));
    }

    @Test
    public void shouldReturnExistingEditRelationIfBothHaveSameIdentifierButDifferentSlaveGroupIds() {
        // given
        ResourceEditRelation existingEditRelation = new ResourceEditRelation(null, null, "Test", null, null, null, null, null,
                null, null, 1, null, null, null, null, null, null, null, null, null, null, "PROVIDED", null);
        ResourceEditRelation newEditRelation = new ResourceEditRelation(null, null, "Test", null, null, null, null, null,
                null, null, 2, null, null, null, null, null, null, null, null, null, null, "PROVIDED", null);
        // when
        ResourceEditRelation replaced = relationModel.replaceRelation(newEditRelation, existingEditRelation);

        // then
        assertThat(replaced, is(existingEditRelation));
    }

    @Test
    public void shouldReturnExistingEditRelationIfTheyHaveDifferentIdentifiers() {
        // given
        ResourceEditRelation existingEditRelation = new ResourceEditRelation(null, null, "Test", null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation newEditRelation = new ResourceEditRelation(null, null, "Another", null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, "CONSUMED", null);
        // when
        ResourceEditRelation replaced = relationModel.replaceRelation(newEditRelation, existingEditRelation);

        // then
        assertThat(replaced, is(existingEditRelation));
    }

}