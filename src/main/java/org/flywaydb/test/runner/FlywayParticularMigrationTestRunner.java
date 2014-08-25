package org.flywaydb.test.runner;

import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.db.DbMigrator;
import org.flywaydb.test.runner.rule.CleanDbClassRule;
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

class FlywayParticularMigrationTestRunner extends BlockJUnit4ClassRunner {

    private FlywayTest flywayTest;

    public FlywayParticularMigrationTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        flywayTest = FlywayTest.create(klass);
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
                field.set(testInstance, flywayTest.getDbMigrator());
            }
        }
        return testInstance;
    }

    @Override
    protected List<TestRule> classRules() {
        List<TestRule> classRules = super.classRules();
        classRules.add(new CleanDbClassRule(flywayTest));
        return classRules;
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> testRules = super.getTestRules(target);
        testRules.add(new MigrateToVersionRule(flywayTest));
        return testRules;
    }

}
