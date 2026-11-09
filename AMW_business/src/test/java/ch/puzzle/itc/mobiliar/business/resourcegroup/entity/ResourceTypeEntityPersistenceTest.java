package ch.puzzle.itc.mobiliar.business.resourcegroup.entity;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(PersistenceTestExtension.class)
public class ResourceTypeEntityPersistenceTest {

    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void shouldLoadChildrenInNameOrder() {
        ResourceTypeEntity parent = new ResourceTypeEntity();
        parent.setName("Parent");
        entityManager.persist(parent);
        for (String name : Arrays.asList("Zulu", "Alpha", "Middle")) {
            ResourceTypeEntity child = new ResourceTypeEntity();
            child.setName(name);
            child.setParentResourceType(parent);
            entityManager.persist(child);
        }
        entityManager.flush();
        Integer parentId = parent.getId();
        entityManager.clear();

        ResourceTypeEntity loaded = entityManager.find(ResourceTypeEntity.class, parentId);
        assertEquals(Arrays.asList("Alpha", "Middle", "Zulu"), loaded.getChildrenResourceTypes().stream()
                .map(ResourceTypeEntity::getName).collect(Collectors.toList()));

        entityManager.clear();
        ResourceTypeEntity fetched = entityManager.createQuery(
                "select n from ResourceTypeEntity n left join fetch n.childrenResourceTypes "
                        + "where n.id = :id order by n.name asc",
                ResourceTypeEntity.class)
                .setParameter("id", parentId)
                .getResultList().get(0);
        assertEquals(Arrays.asList("Alpha", "Middle", "Zulu"), fetched.getChildrenResourceTypes().stream()
                .map(ResourceTypeEntity::getName).collect(Collectors.toList()));
    }

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