package org.flywaydb.test.runner;

import org.flywaydb.test.annotation.AfterMigration;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class FlywayDoParticularMigrationTestRunner extends FlywayParticularMigrationTestRunner {
    FlywayDoParticularMigrationTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> testMethodsInParticularOrder = new ArrayList<FrameworkMethod>();

        testMethodsInParticularOrder.addAll(getTestClass().getAnnotatedMethods(Test.class));

        return testMethodsInParticularOrder;
    }
}
