package org.flywaydb.test.runner;

import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestInstanceProvider {
    private static TestInstanceProvider testInstanceProvider = new TestInstanceProvider();
    private Map<TestClass, Object> testInstances = new HashMap<TestClass, Object>();

    public static TestInstanceProvider testInstanceProvider() {
        return testInstanceProvider;
    }

    public Object provideInstanceFor(FlywayTest flywayTest) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (!testInstances.containsKey(flywayTest)) {
            Object instance = createInstanceOf(flywayTest);
            testInstances.put(flywayTest, instance);
        }
        return testInstances.get(flywayTest);
    }

    private Object createInstanceOf(FlywayTest flywayTest) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object testInstance = flywayTest.getOnlyConstructor().newInstance();
        List<FrameworkField> annotatedFields = flywayTest.getAnnotatedFields(Inject.class);
        for (FrameworkField annotatedField : annotatedFields) {
            if (annotatedField.getType().equals(DataSource.class)) {
                Field field = annotatedField.getField();
                field.setAccessible(true);
                field.set(testInstance, aDataSource(flywayTest));
            } else {
                throw new UnsupportedOperationException("Annotation @Inject should be used only with field of javax.sql.DataSource type");
            }
        }
        return testInstance;
    }

    private DataSource aDataSource(FlywayTest testClass) {
        return testClass.getDbMigrator().getDataSource();
    }

}
