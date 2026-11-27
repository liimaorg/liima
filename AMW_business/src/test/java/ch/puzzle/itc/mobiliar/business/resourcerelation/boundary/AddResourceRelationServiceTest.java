package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddResourceRelationServiceTest {

    @InjectMocks
    AddResourceRelationService service;

    @Mock
    ResourceRelationService resourceRelationService;

    @Test
    void resolvesTheSelectedTypeRelationInsteadOfAnArbitraryRelationOfTheSameType() throws Exception {
        service.addRelation(new AddResourceRelationCommand(1, 2, false, "secondary", "secondary"));

        verify(resourceRelationService).addRelationByGroup(1, 2, false, "secondary", "secondary");
    }

    @Test
    void preservesAddingRelationsWithoutAnExplicitTypeIdentifier() throws Exception {
        service.addRelation(new AddResourceRelationCommand(1, 2, false, null));

        verify(resourceRelationService).addRelationByGroup(1, 2, false, null, null);
    }
}
