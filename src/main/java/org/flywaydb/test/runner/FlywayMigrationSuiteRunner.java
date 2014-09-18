package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

class FlywayMigrationSuiteRunner extends ParentRunner<FlywayParticularMigrationTestRunner> {
    private final MigrationVersion migrationVersion;
    private final List<FlywayParticularMigrationTestRunner> childRunners;

    public FlywayMigrationSuiteRunner(SuiteForMigrationVersion suiteForMigrationVersion) throws InitializationError {
        super(suiteForMigrationVersion.getClass());
        this.migrationVersion = suiteForMigrationVersion.getMigrationVersion();
        childRunners = getChildRunners(suiteForMigrationVersion);
    }

    private List<FlywayParticularMigrationTestRunner> getChildRunners(SuiteForMigrationVersion suiteForMigrationVersion) throws InitializationError {
        List<FlywayParticularMigrationTestRunner> childRunners = new ArrayList<FlywayParticularMigrationTestRunner>();
        List<FlywayParticularMigrationTestRunner> beforeMigrationRunners = new ArrayList<FlywayParticularMigrationTestRunner>();
        List<FlywayParticularMigrationTestRunner> afterMigrationRunners = new ArrayList<FlywayParticularMigrationTestRunner>();

        for(Class<?> testClass : suiteForMigrationVersion.getClasses()) {
                beforeMigrationRunners.add(new FlywayBeforeParticularMigrationTestRunner(testClass));
                afterMigrationRunners.add(new FlywayAfterParticularMigrationTestRunner(testClass));
        }

        childRunners.addAll(beforeMigrationRunners);
        childRunners.addAll(afterMigrationRunners);

        return childRunners;
    }

    @Override
    protected List<FlywayParticularMigrationTestRunner> getChildren() {
        return childRunners;
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
