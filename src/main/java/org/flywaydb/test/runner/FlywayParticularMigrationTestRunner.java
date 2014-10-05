package org.flywaydb.test.runner;

import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
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
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.runner.Description.createTestDescription;

class FlywayParticularMigrationTestRunner implements Filterable {

    private FlywayTest flywayTest;
    private Object testInstance;
    private FrameworkMethod beforeMigrationMethodToBeRun;
    private FrameworkMethod afterMigrationMethodToBeRun;

    FlywayParticularMigrationTestRunner(FlywayTest flywayTest) throws InitializationError {
        this.flywayTest = flywayTest;
        validate();
        beforeMigrationMethodToBeRun = (flywayTest.getAnnotatedMethods(BeforeMigration.class).get(0));
        afterMigrationMethodToBeRun = (flywayTest.getAnnotatedMethods(AfterMigration.class).get(0));
    }

    private void validate() throws InitializationError {
        List<Throwable> errors = new ArrayList<Throwable>();
        validateMethods(errors);
        if (!errors.isEmpty()) {
            throw new InitializationError(errors);
        }
    }

    private void validateMethods(List<Throwable> errors) {
        validateExactlyOnePublicVoidNoArgMethod(BeforeMigration.class, errors);
        validateExactlyOnePublicVoidNoArgMethod(AfterMigration.class, errors);
        validateNoMethod(Test.class, errors);
        validateNoMethod(BeforeClass.class, errors);
        validateNoMethod(AfterClass.class, errors);
    }

    private void validateExactlyOnePublicVoidNoArgMethod(Class<? extends Annotation> annotation, List<Throwable> errors) {
        List<FrameworkMethod> annotatedMethods = flywayTest.getAnnotatedMethods(annotation);
        if (annotatedMethods.size() > 1) {
            errors.add(new Exception("There should not be more than one method annotated with @" + annotation.getSimpleName()));
        }
        annotatedMethods.get(0).validatePublicVoidNoArg(false, errors);
    }

    private void validateNoMethod(Class<? extends Annotation> notApplicableAnnotation, List<Throwable> errors) {
        List<FrameworkMethod> annotatedMethods = flywayTest.getAnnotatedMethods(notApplicableAnnotation);

        if (!annotatedMethods.isEmpty()) {
            errors.add(new Exception("Migration test should not have any method annotated with @" + notApplicableAnnotation.getSimpleName()));
        }
    }

    public Description getDescription() {
        Description testDescription = createTestDescription(flywayTest.getJavaClass(), flywayTest.getName(), flywayTest.getAnnotations());

        describeIfMethodIsNotFilteredOut(testDescription, beforeMigrationMethodToBeRun);
        describeIfMethodIsNotFilteredOut(testDescription, afterMigrationMethodToBeRun);

        return testDescription;
    }

    private void describeIfMethodIsNotFilteredOut(Description testDescription, FrameworkMethod methodToBeRun) {
        if (!isFilteredOut(methodToBeRun)) {
            testDescription.addChild(describeChild(methodToBeRun));
        }
    }

    private Description describeChild(FrameworkMethod methodToBeRun) {
        return createTestDescription(flywayTest.getJavaClass(), methodToBeRun.getName(), methodToBeRun.getAnnotations());
    }

    public Statement withBefores(Statement statement) {
        List<FrameworkMethod> befores = flywayTest.getAnnotatedMethods(Before.class);
        return befores.isEmpty() ? statement : new RunBefores(statement, befores, testInstance);
    }

    public Statement withAfters(Statement statement) {
        List<FrameworkMethod> afters = flywayTest.getAnnotatedMethods(After.class);
        return afters.isEmpty() ? statement : new RunAfters(statement, afters, testInstance);
    }

    public void runBeforeMigrationMethod(RunNotifier notifier) {
        if (!isFilteredOut(beforeMigrationMethodToBeRun)) {
            runBeforeOrAfterMigrationMethod(beforeMigrationMethodToBeRun, notifier);
        }
    }

    public void runAfterMigrationMethod(RunNotifier notifier) {
        if (!isFilteredOut(afterMigrationMethodToBeRun)) {
            runBeforeOrAfterMigrationMethod(afterMigrationMethodToBeRun, notifier);
        }
    }

    private static boolean isFilteredOut(FrameworkMethod method) {
        return method == null;
    }

    private void runBeforeOrAfterMigrationMethod(final FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        if (isIgnored(method)) {
            notifier.fireTestIgnored(description);
        } else {
            runLeaf(methodBlock(method), description, notifier);
        }
    }

    protected final void runLeaf(Statement statement, Description description,
                                 RunNotifier notifier) {
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();
        try {
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        } catch (Throwable e) {
            eachNotifier.addFailure(e);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }


    private boolean isIgnored(FrameworkMethod method) {
        return method.getAnnotation(Ignore.class) != null;
    }

    private Statement methodBlock(FrameworkMethod method) {
        Statement statement = emptyStatement();
        if (method != null) {
            statement = new InvokeMethod(method, testInstance);
            statement = withRules(method, testInstance, statement);
        }
        return statement;
    }

    private Statement emptyStatement() {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {

            }
        };
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
        result.add(new MigrateToVersionRule(flywayTest.getMigrationVersion(), flywayTest.getFlywayConfiguration()));

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
        if (!shouldRun(filter, beforeMigrationMethodToBeRun)) {
            beforeMigrationMethodToBeRun = null;
        }

        if (!shouldRun(filter, afterMigrationMethodToBeRun)) {
            afterMigrationMethodToBeRun = null;
        }
    }

    private boolean shouldRun(Filter filter, FrameworkMethod methodToBeRun) {
        return filter.shouldRun(createTestDescription(flywayTest.getJavaClass(), methodToBeRun.getName()));
    }

    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }
}
