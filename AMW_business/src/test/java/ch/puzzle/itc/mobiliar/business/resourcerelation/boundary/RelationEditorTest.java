package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class RelationEditorTest {

    @InjectMocks
    RelationEditor relationEditor;

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReturnTrueIfIdentifierMatches() {
        // given
        String identifier = "matchingIdentifier";
        ProvidedResourceRelationEntity relation = new ProvidedResourceRelationEntity();
        relation.setIdentifier(identifier);

        // when // then
        assertThat(relationEditor.isMatchingRelationName(relation, identifier), is(true));
    }

    @Test
    public void shouldReturnFalseIfIdentifierDoesNotMatch() {
        // given
        String identifier = "identifier";
        ConsumedResourceRelationEntity relation = new ConsumedResourceRelationEntity();
        relation.setIdentifier("anotherIdentifier");

        // when // then
        assertThat(relationEditor.isMatchingRelationName(relation, identifier), is(false));
    }

    @Test
    public void shouldReturnTrueIfNameOfSlaveResourceMatches() {
        // given
        String name = "name";
        ResourceEntity resource = new ResourceEntity();
        resource.setResourceGroup(new ResourceGroupEntity());
        resource.setName(name);
        ProvidedResourceRelationEntity relation = new ProvidedResourceRelationEntity();
        relation.setSlaveResource(resource);

        // when // then
        assertThat(relationEditor.isMatchingRelationName(relation, name), is(true));
    }

    @Test
    public void shouldReturnFalseIfNameOfSlaveResourceDoesNotMatch() {
        // given
        String name = "name";
        ResourceEntity resource = new ResourceEntity();
        resource.setResourceGroup(new ResourceGroupEntity());
        resource.setName("anotherName");
        ConsumedResourceRelationEntity relation = new ConsumedResourceRelationEntity();
        relation.setSlaveResource(resource);

        // when // then
        assertThat(relationEditor.isMatchingRelationName(relation, name), is(false));
    }

    @Test
    public void shouldReturnFalseIfIdentifierDoesNotMatchEvenIfNameOfSlaveResourceMatches() {
        // given
        String name = "name";
        ResourceEntity resource = new ResourceEntity();
        resource.setResourceGroup(new ResourceGroupEntity());
        resource.setName(name);
        ProvidedResourceRelationEntity relation = new ProvidedResourceRelationEntity();
        relation.setIdentifier("anotherIdentifier");
        relation.setSlaveResource(resource);

        // when // then
        assertThat(relationEditor.isMatchingRelationName(relation, name), is(false));
    }

    @Test
    public void shouldReturnFalseIfProvidedStringIsNullOnIsValidResourceRelationType() {
        // given // when // then
        assertThat(relationEditor.isValidResourceRelationType(null), is(false));
    }

    @Test
    public void shouldReturnFalseIfProvidedStringIsNotValidResourceRelationType() {
        // given
        String invalid = "invalid";

        // when // then
        assertThat(relationEditor.isValidResourceRelationType(invalid), is(false));
    }

    @Test
    public void shouldReturnIgnoreCaseOnIsValidResourceRelationType() {
        // given
        String consumed = "ConSumed";

        // when // then
        assertThat(relationEditor.isValidResourceRelationType(consumed), is(true));
    }

}