package org.flywaydb.test.runner;

import org.flywaydb.test.runner.rule.MigrateToVersionRule;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.InitializationError;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.List;


abstract class FlywayParticularMigrationTestRunner extends BlockJUnit4ClassRunner {

    private FlywayTest flywayTest;

    FlywayParticularMigrationTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        flywayTest = FlywayTest.create(klass);
    }

    @Override
    protected String getName() {
        return flywayTest.getName();
    }

    @Override
    protected Object createTest() throws Exception {
        Object testInstance = super.createTest();
        List<FrameworkField> annotatedFields = getTestClass().getAnnotatedFields(Inject.class);
        for (FrameworkField annotatedField : annotatedFields) {
            if (annotatedField.getType().equals(DataSource.class)) {
                Field field = annotatedField.getField();
                field.setAccessible(true);
                field.set(testInstance, getDataSource());
            } else {
                throw new UnsupportedOperationException("Annotation @Inject should be used only with field of javax.sql.DataSource type");
            }
        }
        return testInstance;
    }

    private DataSource getDataSource() {
        return flywayTest.getDbMigrator().getDataSource();
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> testRules = super.getTestRules(target);
        testRules.add(new MigrateToVersionRule(flywayTest.getMigrationVersion(), flywayTest.getDbMigrator()));
        return testRules;
    }

}
