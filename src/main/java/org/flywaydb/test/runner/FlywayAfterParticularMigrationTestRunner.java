package org.flywaydb.test.runner;

import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.runner.FlywayParticularMigrationTestRunner;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

class FlywayAfterParticularMigrationTestRunner extends FlywayParticularMigrationTestRunner {
    public FlywayAfterParticularMigrationTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> testMethodsInParticularOrder = new ArrayList<FrameworkMethod>();

        testMethodsInParticularOrder.addAll(getTestClass().getAnnotatedMethods(AfterMigration.class));

        return testMethodsInParticularOrder;
    }

}
