package org.flywaydb.test.runner;

import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.MethodRule;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.runner.Description.createTestDescription;

class FlywayParticularMigrationTestRunner extends ParentRunner<FrameworkMethod> implements Filterable {

    private FlywayTest flywayTest;
    private Object testInstance;
    private List<FrameworkMethod> methodsToBeRun = new ArrayList<FrameworkMethod>();
    private CountDownLatch beforeMethodCountDownLatch;

    FlywayParticularMigrationTestRunner(FlywayTest flywayTest) throws InitializationError {
        super(flywayTest.getJavaClass());
        this.flywayTest = flywayTest;
        methodsToBeRun.add(flywayTest.getAnnotatedMethods(BeforeMigration.class).get(0));
        methodsToBeRun.add(flywayTest.getAnnotatedMethods(AfterMigration.class).get(0));
    }

    @Override
    public Description getDescription() {
        Description testDescription = createTestDescription(flywayTest.getJavaClass(), flywayTest.getName(), flywayTest.getAnnotations());

        for (FrameworkMethod methodToBeRun : methodsToBeRun) {
            testDescription.addChild(describeChild(methodToBeRun));
        }

        return testDescription;
    }

    @Override
    protected Description describeChild(FrameworkMethod methodToBeRun) {
        return createTestDescription(flywayTest.getJavaClass(), methodToBeRun.getName(), methodToBeRun.getAnnotations());
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        return methodsToBeRun;
    }

    protected Statement classBlock(final RunNotifier notifier) {
        Statement statement = childrenInvoker(notifier);
        statement = withBefores(testInstance, statement);
        statement = withAfters(testInstance, statement);
        //TODO analyze if rules are precisely meant for rather group of tests than a one test only
        statement = withClassRules(statement);

        return statement;
    }

    private Statement withBefores(Object target, Statement statement) {
        List<FrameworkMethod> befores = flywayTest.getAnnotatedMethods(Before.class);
        return befores.isEmpty() ? statement : new RunBefores(statement, befores, target);
    }

    private Statement withAfters(Object target, Statement statement) {
        List<FrameworkMethod> afters = flywayTest.getAnnotatedMethods(After.class);
        return afters.isEmpty() ? statement : new RunAfters(statement, afters, target);
    }

    private Statement withClassRules(Statement statement) {
        List<TestRule> classRules = classRules();
        return classRules.isEmpty() ? statement : new RunRules(statement, classRules, getDescription());
    }

    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        if (isIgnored(method)) {
            if (isBeforeMigration(method)) {
                beforeMethodCountDownLatch.countDown();
            }
            notifier.fireTestIgnored(description);
        } else if (isAfterMigration(method)) {
            if (isBeforeMigrationAlreadyFinished()) {
                runLeaf(methodBlock(method), description, notifier);
            } else {
                throw new RuntimeException(new TimeoutException("BeforeMethods should have been finished by now"));
            }
        } else if (isBeforeMigration(method)) {
            runLeaf(methodBlock(method), description, notifier);
            beforeMethodCountDownLatch.countDown();
        }
    }

    private boolean isBeforeMigrationAlreadyFinished() {
        try {
            System.out.println("Latch count: " + beforeMethodCountDownLatch.getCount());
            //TODO add concept of timeout to @BeforeMigration and @AfterMigration
            return beforeMethodCountDownLatch.await(10, SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isAfterMigration(FrameworkMethod method) {
        return method.getAnnotation(AfterMigration.class) != null;
    }

    private boolean isIgnored(FrameworkMethod method) {
        return method.getAnnotation(Ignore.class) != null;
    }

    private boolean isBeforeMigration(FrameworkMethod method) {
        return method.getAnnotation(BeforeMigration.class) != null;
    }

    private Statement methodBlock(FrameworkMethod method) {
        Statement statement = new InvokeMethod(method, testInstance);
        statement = withRules(method, testInstance, statement);
        return statement;
    }

    private Statement withRules(FrameworkMethod method, Object target,
                                Statement statement) {
        List<TestRule> testRules = getTestRules(target);
        statement = withMethodRules(method, testRules, target, statement);
        statement = withTestRules(method, testRules, statement);

        return statement;
    }

    private List<TestRule> getTestRules(Object target) {
        List<TestRule> result = flywayTest.getAnnotatedMethodValues(target, Rule.class, TestRule.class);

        result.addAll(flywayTest.getAnnotatedFieldValues(target, Rule.class, TestRule.class));
        result.add(new MigrateToVersionRule(flywayTest.getMigrationVersion(), flywayTest.getDbMigrator()));

        return result;
    }

    private Statement withTestRules(FrameworkMethod method, List<TestRule> testRules, Statement statement) {
        return testRules.isEmpty() ? statement : new RunRules(statement, testRules, describeChild(method));
    }

    private Statement withMethodRules(FrameworkMethod method, List<TestRule> testRules, Object target, Statement result) {
        for (MethodRule each : getMethodRules(target)) {
            if (!testRules.contains(each)) {
                result = each.apply(result, method, target);
            }
        }
        return result;
    }

    private List<MethodRule> getMethodRules(Object target) {
        return flywayTest.getAnnotatedFieldValues(target, Rule.class, MethodRule.class);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        for (Iterator<FrameworkMethod> iterator = methodsToBeRun.iterator(); iterator.hasNext();) {
            FrameworkMethod methodToBeRun = iterator.next();

            if (!shouldRun(filter, methodToBeRun)) {
                iterator.remove();
            }
        }
    }

    private boolean shouldRun(Filter filter, FrameworkMethod methodToBeRun) {
        return filter.shouldRun(createTestDescription(flywayTest.getJavaClass(), methodToBeRun.getName()));
    }

    public void setBeforeMethodCountDownLatch(CountDownLatch beforeMethodCountDownLatch) {
        this.beforeMethodCountDownLatch = beforeMethodCountDownLatch;
    }

    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }
}
