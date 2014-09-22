package org.flywaydb.test.runner;

import org.flywaydb.test.annotation.AfterMigration;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.List;

class FlywayAfterParticularMigrationTestRunner extends FlywayParticularMigrationTestRunner {
    public FlywayAfterParticularMigrationTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        return getTestClass().getAnnotatedMethods(AfterMigration.class);
    }

}
