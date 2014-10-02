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
        CountDownLatch beforeMigrationMethodCountDownLatch = new CountDownLatch(suiteForMigrationVersionClasses.size());

        for (Class<?> testClass : suiteForMigrationVersionClasses) {
            FlywayTest flywayTest = new FlywayTest(testClass);

            FlywayParticularMigrationTestRunner flywayParticularMigrationTestRunner = new FlywayParticularMigrationTestRunner(flywayTest);
            injectDependencies(flywayParticularMigrationTestRunner, beforeMigrationMethodCountDownLatch, flywayTest);

            childRunners.add(flywayParticularMigrationTestRunner);
        }

        return childRunners;
    }

    private void injectDependencies(FlywayParticularMigrationTestRunner flywayParticularMigrationTestRunner, CountDownLatch beforeMigrationMethodCountDownLatch, FlywayTest flywayTest) {
        flywayParticularMigrationTestRunner.setBeforeMethodCountDownLatch(beforeMigrationMethodCountDownLatch);
        flywayParticularMigrationTestRunner.setTestInstance(createInstanceOf(flywayTest));
    }
}
