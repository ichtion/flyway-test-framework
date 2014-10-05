package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;

class FlywayMigrationSuiteRunner extends Runner implements Filterable {
    private final MigrationVersion migrationVersion;
    private final List<FlywayParticularMigrationTestRunner> childRunners;
    private final FlywayMigrationSuiteChildrenRunnerBuilder childrenRunnerBuilder;

    public FlywayMigrationSuiteRunner(SuiteForMigrationVersion suiteForMigrationVersion) throws InitializationError {
        this.migrationVersion = suiteForMigrationVersion.getMigrationVersion();
        childrenRunnerBuilder = new FlywayMigrationSuiteChildrenRunnerBuilder();
        childRunners = getChildRunners(suiteForMigrationVersion);
    }

    private List<FlywayParticularMigrationTestRunner> getChildRunners(SuiteForMigrationVersion suiteForMigrationVersion) throws InitializationError {
        return childrenRunnerBuilder.buildChildrenRunnersForSuite(suiteForMigrationVersion);
    }

    private String getName() {
        return "v" + migrationVersion.getVersion().replace(".", "_");
    }

    protected Description describeChild(FlywayParticularMigrationTestRunner child) {
        return child.getDescription();
    }

    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(getName());
        for (FlywayParticularMigrationTestRunner child : childRunners) {
            description.addChild(describeChild(child));
        }
        return description;
    }

    private Statement classBlock(final RunNotifier notifier) {
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                for (FlywayParticularMigrationTestRunner runner : childRunners) {
                    runner.runBeforeMigrationMethod(notifier);
                }
                for (FlywayParticularMigrationTestRunner runner : childRunners) {
                    runner.runAfterMigrationMethod(notifier);
                }
            }
        };

        for (FlywayParticularMigrationTestRunner runner : childRunners) {
            statement = runner.withBefores(statement);
            statement = runner.withAfters(statement);
        }

        return statement;
    }

    @Override
    public void run(final RunNotifier notifier) {
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, getDescription());
        try {
            Statement statement = classBlock(notifier);
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            testNotifier.fireTestIgnored();
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            testNotifier.addFailure(e);
        }
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        for (FlywayParticularMigrationTestRunner child : childRunners) {
            child.filter(filter);
        }
    }
}
