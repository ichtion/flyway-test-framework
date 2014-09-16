package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class FlywayMigrationSuiteRunner extends ParentRunner<FlywayParticularMigrationTestRunner> {
    private Set<Class<?>> flywayTestClasses;
    private MigrationVersion migrationVersion;

    public FlywayMigrationSuiteRunner(SuiteForMigrationVersion suiteForMigrationVersion) throws InitializationError {
        super(suiteForMigrationVersion.getClass());
        this.migrationVersion = suiteForMigrationVersion.getMigrationVersion();
        this.flywayTestClasses = suiteForMigrationVersion.getClasses();
    }

    @Override
    protected List<FlywayParticularMigrationTestRunner> getChildren() {
        //todo cleanup
        List<FlywayParticularMigrationTestRunner> runners = new ArrayList<FlywayParticularMigrationTestRunner>();
        List<FlywayParticularMigrationTestRunner> beforeMigrationRunners = new ArrayList<FlywayParticularMigrationTestRunner>();
        List<FlywayParticularMigrationTestRunner> afterMigrationRunners = new ArrayList<FlywayParticularMigrationTestRunner>();

        for(Class<?> testClass : flywayTestClasses) {
            try {
                beforeMigrationRunners.add(new FlywayBeforeParticularMigrationTestRunner(testClass));
                afterMigrationRunners.add(new FlywayAfterParticularMigrationTestRunner(testClass));
            } catch (InitializationError initializationError) {
                throw new RuntimeException(initializationError);
            }
        }

        runners.addAll(beforeMigrationRunners);
        runners.addAll(afterMigrationRunners);

        return runners;
    }

    @Override
    protected String getName() {
        return "v" + migrationVersion.getVersion();
    }

    @Override
    protected Description describeChild(FlywayParticularMigrationTestRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(FlywayParticularMigrationTestRunner child, RunNotifier notifier) {
        child.run(notifier);
    }
}
