package org.flywaydb.test.runner;

import org.flywaydb.test.annotation.BeforeMigration;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.List;

class FlywayBeforeParticularMigrationTestRunner extends FlywayParticularMigrationTestRunner {

    public FlywayBeforeParticularMigrationTestRunner(FlywayTest flywayTest) throws InitializationError {
        super(flywayTest);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        return getTestClass().getAnnotatedMethods(BeforeMigration.class);
    }
}
