package org.flywaydb.test.runner;

import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.flywaydb.test.runner.TestInstanceCreator.createInstanceOf;

class FlywayMigrationSuiteChildrenRunnerBuilder {
    public List<FlywayParticularMigrationTestRunner> buildChildrenRunnersForSuite(SuiteForMigrationVersion suite) throws InitializationError {
        List<FlywayParticularMigrationTestRunner> childRunners = new ArrayList<FlywayParticularMigrationTestRunner>();
        Set<Class<?>> suiteForMigrationVersionClasses = suite.getClasses();

        for (Class<?> testClass : suiteForMigrationVersionClasses) {
            FlywayTest flywayTest = new FlywayTest(testClass);

            FlywayParticularMigrationTestRunner flywayParticularMigrationTestRunner = new FlywayParticularMigrationTestRunner(flywayTest);
            injectDependencies(flywayParticularMigrationTestRunner, flywayTest);

            childRunners.add(flywayParticularMigrationTestRunner);
        }

        return childRunners;
    }

    private void injectDependencies(FlywayParticularMigrationTestRunner flywayParticularMigrationTestRunner, FlywayTest flywayTest) {
        flywayParticularMigrationTestRunner.setTestInstance(createInstanceOf(flywayTest));
    }
}
