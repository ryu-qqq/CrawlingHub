package com.ryuqq.crawlinghub.integration.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database cleaner for test isolation. Truncates all tables between tests to ensure clean state.
 *
 * <p>Note: This is preferred over @Transactional on tests because TestRestTemplate runs in a
 * separate thread, making @Transactional ineffective.
 */
@Component
public class DatabaseCleaner {

    @PersistenceContext private EntityManager entityManager;

    private List<String> tableNames;

    @Transactional
    public void clean() {
        if (tableNames == null) {
            tableNames = collectTableNames();
        }

        entityManager.flush();
        disableForeignKeyChecks();
        truncateAllTables();
        enableForeignKeyChecks();
    }

    private List<String> collectTableNames() {
        return entityManager.getMetamodel().getEntities().stream()
                .filter(entity -> entity.getJavaType().isAnnotationPresent(Table.class))
                .map(entity -> entity.getJavaType().getAnnotation(Table.class).name())
                .filter(tableName -> tableName != null && !tableName.isEmpty())
                .collect(Collectors.toList());
    }

    private void disableForeignKeyChecks() {
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
    }

    private void truncateAllTables() {
        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
    }

    private void enableForeignKeyChecks() {
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }
}
