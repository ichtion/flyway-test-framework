package org.flywaydb.test.runner;

import com.google.common.collect.ImmutableSet;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.flywaydb.test.db.DbUtilities.cleanDb;
import static org.flywaydb.test.db.FlywayConfiguration.flywayConfiguration;

public class FlywayJUnitMigrationTestRunner extends ParentRunner<Runner> {
    private List<Runner> children;

    public FlywayJUnitMigrationTestRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        setChildren(clazz);
    }

    private void setChildren(Class<?> clazz) throws InitializationError {
        children = new ArrayList<Runner>();

        SuiteForMigrationVersion suiteForMigrationVersion = new SuiteForMigrationVersion(
                getMigrationVersion(clazz), ImmutableSet.<Class<?>>of(clazz));

        children.add(new FlywayMigrationSuiteRunner(suiteForMigrationVersion));
    }

    private MigrationVersion getMigrationVersion(Class<?> flywayTestClass) {
        return MigrationVersion.fromVersion(flywayTestClass.getAnnotation(FlywayMigrationTest.class).migrationVersion());
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                cleanDbIfNeeded();
                childrenInvoker(notifier).evaluate();
            }
        };
    }

    private void cleanDbIfNeeded() {
        FlywayMigrationTest flywayMigrationTest = getTestClass().getJavaClass().getAnnotation(FlywayMigrationTest.class);

        if (flywayMigrationTest.cleanDb()) {
            cleanDb(flywayConfiguration(flywayMigrationTest.flywayConfiguration()));
        }
    }

    @Override
    protected String getName() {
        return getTestClass().getName();
    }

    @Override
    protected List<Runner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
        validateClass(errors);
    }

    private void validateClass(List<Throwable> errors) {
        validateProperClassAnnotations(errors);
    }

    private void validateProperClassAnnotations(List<Throwable> errors) {
        for (Annotation annotation : getTestClass().getAnnotations()) {
            if (annotation.annotationType().isAssignableFrom(FlywayMigrationTest.class)) {
                return;
            }
        }
        errors.add(new Exception("Test class run with FlywayJUnitMigrationTestRunner must be annotated with @FlywayMigrationTest"));
    }

}
