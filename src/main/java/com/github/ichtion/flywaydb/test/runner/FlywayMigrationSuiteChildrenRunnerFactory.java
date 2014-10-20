package com.github.ichtion.flywaydb.test.runner;

import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.github.ichtion.flywaydb.test.runner.TestInstanceCreator.createInstanceOf;

class FlywayMigrationSuiteChildrenRunnerFactory {
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

    private void injectDependencies(FlywayParticularMigrationTestRunner flywayParticularMigrationTestRunner, FlywayTest flywayTest) throws InitializationError {
        flywayParticularMigrationTestRunner.setTestInstance(createInstanceOf(flywayTest));
    }
}
