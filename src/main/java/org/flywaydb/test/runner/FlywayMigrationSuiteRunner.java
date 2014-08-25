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
        List<FlywayParticularMigrationTestRunner> runners = new ArrayList<FlywayParticularMigrationTestRunner>();

        for(Class<?> testClass : flywayTestClasses) {
            //todo cleanup
            try {
                runners.add(new FlywayParticularMigrationTestRunner(testClass));
            } catch (InitializationError initializationError) {
                throw new RuntimeException(initializationError);
            }
        }
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
