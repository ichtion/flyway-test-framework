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
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.flywaydb.test.runner.TestInstanceProvider.testInstanceProvider;

class FlywayMigrationSuiteRunner extends ParentRunner<FlywayParticularMigrationTestRunner> {
    private final MigrationVersion migrationVersion;
    private final List<FlywayParticularMigrationTestRunner> childRunners;

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
            public void finished() { }
        });
        this.migrationVersion = suiteForMigrationVersion.getMigrationVersion();
        childRunners = getChildRunners(suiteForMigrationVersion);
    }

    //TODO introduce a concept of RunnerBuilder so this logic is encapsulated there
    //TODO remove concept of testInstanceProvider - this should also be created by the builder
    private List<FlywayParticularMigrationTestRunner> getChildRunners(SuiteForMigrationVersion suiteForMigrationVersion) throws InitializationError {
        List<FlywayParticularMigrationTestRunner> childRunners = new ArrayList<FlywayParticularMigrationTestRunner>();
        Set<Class<?>> suiteForMigrationVersionClasses = suiteForMigrationVersion.getClasses();
        CountDownLatch beforeMigrationMethodCountDownLatch = new CountDownLatch(suiteForMigrationVersionClasses.size());

        for (Class<?> testClass : suiteForMigrationVersionClasses) {
            FlywayTest flywayTest = new FlywayTest(testClass);
            testInstanceProvider().createInstanceOf(flywayTest);

            FlywayParticularMigrationTestRunner flywayParticularMigrationTestRunner = new FlywayParticularMigrationTestRunner(flywayTest);
            flywayParticularMigrationTestRunner.setBeforeMethodCountDownLatch(beforeMigrationMethodCountDownLatch);
            childRunners.add(flywayParticularMigrationTestRunner);
        }

        return childRunners;
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
