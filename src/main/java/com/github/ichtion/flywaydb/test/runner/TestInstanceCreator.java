package com.github.ichtion.flywaydb.test.runner;

import com.github.ichtion.flywaydb.test.db.DbUtilities;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.InitializationError;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.github.ichtion.flywaydb.test.db.DbUtilities.getDataSource;

class TestInstanceCreator {
    public static Object createInstanceOf(FlywayTest flywayTest) throws InitializationError {
        Object testInstance = getBareInstance(flywayTest);
        FrameworkField dataSourceField = getDataSourceField(flywayTest);

        Field field = dataSourceField.getField();
        field.setAccessible(true);
        setDataSource(flywayTest, testInstance, field);

        return testInstance;
    }

    private static FrameworkField getDataSourceField(FlywayTest flywayTest) throws InitializationError {
        List<FrameworkField> dataSourceFields = new ArrayList<FrameworkField>();
        for (FrameworkField field : flywayTest.getAnnotatedFields(Inject.class)) {
            if (field.getType().equals(DataSource.class)) {
                dataSourceFields.add(field);
            }
        }

        if (dataSourceFields.size() != 1) {
            throw new InitializationError("There should be exactly one DataSource type field annotated with @Inject");
        }

        return dataSourceFields.get(0);
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
        return getDataSource(testClass.getFlywayConfiguration());
    }

}
