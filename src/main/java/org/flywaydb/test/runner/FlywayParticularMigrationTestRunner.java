package org.flywaydb.test.runner;

import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.db.DbMigrator;
import org.flywaydb.test.runner.rule.MigrateToVersionRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.flywaydb.test.db.DbMigrator.dbMigratorForConfiguration;

class FlywayParticularMigrationTestRunner extends BlockJUnit4ClassRunner {

    private FlywayTest flywayTest;
    private String flywayConfiguration;

    public FlywayParticularMigrationTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        flywayTest = FlywayTest.create(klass);
        flywayConfiguration = klass.getAnnotation(FlywayMigrationTest.class).flywayConfiguration();
    }

    @Override
    protected String getName() {
        return flywayTest.getName();
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> testMethodsInParticularOrder = new ArrayList<FrameworkMethod>();

        testMethodsInParticularOrder.addAll(getTestClass().getAnnotatedMethods(BeforeMigration.class));
        testMethodsInParticularOrder.addAll(getTestClass().getAnnotatedMethods(Test.class));
        testMethodsInParticularOrder.addAll(getTestClass().getAnnotatedMethods(AfterMigration.class));

        return testMethodsInParticularOrder;
    }

    // todo: improve
    @Override
    protected Object createTest() throws Exception {
        Object testInstance = super.createTest();
        List<FrameworkField> annotatedFields = getTestClass().getAnnotatedFields(Inject.class);
        for (FrameworkField annotatedField : annotatedFields) {
            if (annotatedField.getType().equals(DbMigrator.class)) {
                Field field = annotatedField.getField();
                field.setAccessible(true);
                field.set(testInstance, dbMigratorForConfiguration(flywayConfiguration));
            }
        }
        return testInstance;
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> testRules = super.getTestRules(target);
        testRules.add(new MigrateToVersionRule(flywayTest, dbMigratorForConfiguration(flywayConfiguration)));
        return testRules;
    }

}
