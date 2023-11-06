package ch.puzzle.itc.mobiliar.business.resourcegroup.entity;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.*;

@RunWith(PersistenceTestRunner.class)
public class ResourceTypeEntityPersistenceTest {

    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void shouldDeleteResourceTypeWithFunction() {
        // given
        ResourceTypeEntity appType = new ResourceTypeEntity();
        appType.setName("resourcetype");
        AmwFunctionEntity function = new AmwFunctionEntity();
        function.setName("function1");
        function.setImplementation("test");
        function.setResourceType(appType);
        appType.addFunction(function);
        entityManager.persist(appType);
        Integer appTypeId = appType.getId();
        Integer functionId = function.getId();
        assertNotNull(entityManager.find(ResourceTypeEntity.class, appTypeId));
        assertNotNull(entityManager.find(AmwFunctionEntity.class, functionId));

        // when
        entityManager.remove(appType);
        entityManager.flush();

        // then
        assertNull(entityManager.find(ResourceTypeEntity.class, appTypeId));
        assertNull(entityManager.find(AmwFunctionEntity.class, functionId));

    }

}