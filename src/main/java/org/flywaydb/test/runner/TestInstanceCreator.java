package org.flywaydb.test.runner;

import org.junit.runners.model.FrameworkField;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.List;

class TestInstanceCreator {
    public static Object createInstanceOf(FlywayTest flywayTest) {
        Object testInstance = getBareInstance(flywayTest);
        List<FrameworkField> annotatedFields = flywayTest.getAnnotatedFields(Inject.class);
        for (FrameworkField annotatedField : annotatedFields) {
            if (annotatedField.getType().equals(DataSource.class)) {
                Field field = annotatedField.getField();
                field.setAccessible(true);
                setDataSource(flywayTest, testInstance, field);
            } else {
                throw new UnsupportedOperationException("Annotation @Inject should be used only with field of javax.sql.DataSource type");
            }
        }
        return testInstance;
    }

    private static void setDataSource(FlywayTest flywayTest, Object testInstance, Field field) {
        try {
            field.set(testInstance, aDataSource(flywayTest));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getBareInstance(FlywayTest flywayTest) {
        try {
            return flywayTest.getOnlyConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static DataSource aDataSource(FlywayTest testClass) {
        return testClass.getDbMigrator().getDataSource();
    }

}
