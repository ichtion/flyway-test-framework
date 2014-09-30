package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

class FlywayMigrationSuiteRunner extends ParentRunner<FlywayParticularMigrationTestRunner> {
    private final MigrationVersion migrationVersion;
    private final List<FlywayParticularMigrationTestRunner> childRunners;
    private final FlywayMigrationSuiteChildrenRunnerBuilder childrenRunnerBuilder;

    private List<Thread> childThreads = new ArrayList<Thread>();

    public FlywayMigrationSuiteRunner(SuiteForMigrationVersion suiteForMigrationVersion) throws InitializationError {
        super(suiteForMigrationVersion.getClass());
        setScheduler(new RunnerScheduler() {
            @Override
            public void schedule(Runnable childStatement) {
                Thread thread = new Thread(childStatement);
                childThreads.add(thread);
                thread.start();
            }

            @Override
            public void finished() {
            }
        });
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

    @Override
    protected void runChild(FlywayParticularMigrationTestRunner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        return childrenInvoker(notifier);
    }

    @Override
    public void run(final RunNotifier notifier) {
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, getDescription());
        try {
            Statement statement = classBlock(notifier);
            statement.evaluate();
            waitForChildren();
        } catch (AssumptionViolatedException e) {
            testNotifier.fireTestIgnored();
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            testNotifier.addFailure(e);
        }
    }

    private void waitForChildren() {
        for (Thread childThread : childThreads) {
            try {
                //TODO wait for a "sum of all timeouts" amount of time
                childThread.join(10000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
