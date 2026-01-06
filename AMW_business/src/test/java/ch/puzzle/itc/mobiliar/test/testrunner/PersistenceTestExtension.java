package ch.puzzle.itc.mobiliar.test.testrunner;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import ch.puzzle.itc.mobiliar.business.database.control.EntityManagerProducerIntegrationTestImpl;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class PersistenceTestExtension implements BeforeEachCallback, AfterEachCallback {

    private static EntityManagerFactory entityManagerFactory;

    private static final String PERSISTENCE_UNIT = "persistence-test";
    private static final NamespaceKey STORE_KEY = new NamespaceKey();

    private static class NamespaceKey {}

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();

        EntityManagerFactory emf = createEntityManagerFactory(false);
        EntityManager em = emf.createEntityManager();

        injectPersistence(testInstance, em, emf);

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.create(PersistenceTestExtension.class, STORE_KEY));
        store.put("entityManager", em);
        store.put("transaction", tx);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.create(PersistenceTestExtension.class, STORE_KEY));
        EntityTransaction tx = store.get("transaction", EntityTransaction.class);
        EntityManager em = store.get("entityManager", EntityManager.class);

        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    private EntityManagerFactory createEntityManagerFactory(boolean singleton) throws IOException {
        if (singleton) {
            if (PersistenceTestExtension.entityManagerFactory == null) {
                EntityManagerProducerIntegrationTestImpl.copyIntegrationTestDB();
                PersistenceTestExtension.entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            }
            return PersistenceTestExtension.entityManagerFactory;
        }
        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
    }

    private void injectPersistence(Object target, EntityManager entityManager, EntityManagerFactory entityManagerFactory) {
        Field[] fields = target.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            handleClassFields(target, fields, entityManager, entityManagerFactory);
        } else {
            fields = target.getClass().getSuperclass().getDeclaredFields();
            handleClassFields(target, fields, entityManager, entityManagerFactory);
        }
    }

    private void handleClassFields(Object target, Field[] fields, EntityManager entityManager, EntityManagerFactory entityManagerFactory) {
        for (Field field : fields) {
            if (field.isAnnotationPresent(PersistenceContext.class)) {
                injectFieldValue(field, target, entityManager);
            } else if (field.isAnnotationPresent(PersistenceUnit.class)) {
                injectFieldValue(field, target, entityManagerFactory);
            }
        }
    }

    private void injectFieldValue(Field field, Object target, Object value) {
        if (field.getType().isAssignableFrom(value.getClass())) {
            boolean wasAccessible = field.canAccess(target);
            field.setAccessible(true);
            try {
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Field not accessible", e);
            } finally {
                field.setAccessible(wasAccessible);
            }
        }
    }
}
