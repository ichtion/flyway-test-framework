package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;

class FlywayMigrationSuiteRunner extends ParentRunner<FlywayParticularMigrationTestRunner> {
    private final MigrationVersion migrationVersion;
    private final List<FlywayParticularMigrationTestRunner> childRunners;
    private final FlywayMigrationSuiteChildrenRunnerBuilder childrenRunnerBuilder;

    public FlywayMigrationSuiteRunner(SuiteForMigrationVersion suiteForMigrationVersion) throws InitializationError {
        super(suiteForMigrationVersion.getClass());
        this.migrationVersion = suiteForMigrationVersion.getMigrationVersion();
        childrenRunnerBuilder = new FlywayMigrationSuiteChildrenRunnerBuilder();
        childRunners = getChildRunners(suiteForMigrationVersion);
    }

    private List<FlywayParticularMigrationTestRunner> getChildRunners(SuiteForMigrationVersion suiteForMigrationVersion) throws InitializationError {
        return childrenRunnerBuilder.buildChildrenRunnersForSuite(suiteForMigrationVersion);
    }

    @Override
    protected List<FlywayParticularMigrationTestRunner> getChildren() {
        return childRunners;
    }

    @Override
    protected String getName() {
        return "v" + migrationVersion.getVersion().replace(".", "_");
    }

    @Override
    protected Description describeChild(FlywayParticularMigrationTestRunner child) {
        return child.getDescription();
    }

    //TODO detach from parent runner if it does not provide value, this empty method proves that solution is not consistent
    @Override
    protected void runChild(FlywayParticularMigrationTestRunner child, RunNotifier notifier) {

    }

    @Override
    //TODO improve
    protected Statement classBlock(final RunNotifier notifier) {
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

}
