package ch.puzzle.itc.mobiliar.business.resourcegroup.entity;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(PersistenceTestRunner.class)
public class ResourceEntityPersistenceTest {
    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void shouldDeleteResourceWithFunction() {
        // given
        ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
        AmwFunctionEntity function = new AmwFunctionEntity();
        function.setName("function");
        ResourceEntity resourceEntity = new ResourceEntity();
        resourceEntity.setResourceGroup(resourceGroup);
        resourceEntity.setName("entity");
        resourceEntity.addFunction(function);
        entityManager.persist(resourceEntity);
        entityManager.flush();
        Integer resourceEntityId = resourceEntity.getId();
        Integer functionId = function.getId();
        assertNotNull(entityManager.find(ResourceEntity.class, resourceEntityId));
        assertNotNull(entityManager.find(AmwFunctionEntity.class, functionId));

        // when
        entityManager.remove(resourceEntity);
        entityManager.flush();

        // then
        assertNull(entityManager.find(ResourceEntity.class, resourceEntityId));
        assertNull(entityManager.find(AmwFunctionEntity.class, functionId));

    }

}