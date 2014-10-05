package org.flywaydb.test.runner;

import com.google.common.collect.ImmutableSet;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.db.DbMigrator;
import org.flywaydb.test.db.FlywayConfiguration;
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

import static org.flywaydb.test.db.DbMigratorProvider.dbMigratorProvider;
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

    private void cleanDataBase(FlywayConfiguration flywayConfiguration) {
        DbMigrator dbMigrator = dbMigratorProvider().provideDbMigratorForConfiguration(flywayConfiguration);
        dbMigrator.cleanDb();
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
            cleanDataBase(flywayConfiguration(flywayMigrationTest.flywayConfiguration()));
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
        validateMethods(errors);
    }

    private void validateClass(List<Throwable> errors) {
        validateProperClassAnnotations(errors);
        validateTestClassWhenApplicable(errors);
    }

    private void validateMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(BeforeMigration.class, false, errors);
        validatePublicVoidNoArgMethods(AfterMigration.class, false, errors);
        validateExactlyOneMethod(BeforeMigration.class, errors);
        validateExactlyOneMethod(AfterMigration.class, errors);
        validateNoMethod(Test.class, errors);
        validateNoMethod(BeforeClass.class, errors);
        validateNoMethod(AfterClass.class, errors);
    }

    private void validateExactlyOneMethod(Class<? extends Annotation> annotation, List<Throwable> errors) {
        if (getTestClass().getAnnotatedMethods(annotation).size() > 1) {
            errors.add(new Exception("There should not more than one method annotated with @" + annotation.getSimpleName()));
        }
    }

    private void validateNoMethod(Class<? extends Annotation> notApplicableAnnotation, List<Throwable> errors) {
        List<FrameworkMethod> annotatedMethods = getTestClass().getAnnotatedMethods(notApplicableAnnotation);

        if (!annotatedMethods.isEmpty()) {
            errors.add(new Exception("Migration test should not have any method annotated with @" + notApplicableAnnotation.getSimpleName()));
        }
    }

    private void validateProperClassAnnotations(List<Throwable> errors) {
        for (Annotation annotation : getTestClass().getAnnotations()) {
            if (annotation.annotationType().isAssignableFrom(FlywayMigrationTest.class)) {
                return;
            }
        }
        errors.add(new Exception("Test class run with FlywayJUnitMigrationTestRunner must be annotated with @FlywayMigrationTest"));
    }

    private void validateTestClassWhenApplicable(List<Throwable> errors) {
        List<FrameworkMethod> beforeMigrationMethods = getTestClass().getAnnotatedMethods(BeforeMigration.class);
        List<FrameworkMethod> afterMigrationMethods = getTestClass().getAnnotatedMethods(AfterMigration.class);

        if (beforeMigrationMethods.size() > 1 || afterMigrationMethods.size() > 1) {
            errors.add(new Exception("FlywayMigrationTest should not have more than one method annotated as BeforeMigration or AfterMigration"));
        } else if (beforeMigrationMethods.size() == 0 && afterMigrationMethods.size() == 0) {
            errors.add(new Exception("FlywayMigrationTest should have at least one method annotated as either BeforeMigration or AfterMigration"));
        } else if (beforeMigrationMethods.size() == 1) {
            beforeMigrationMethods.get(0).validatePublicVoidNoArg(false, errors);
        } else if (afterMigrationMethods.size() == 1) {
            afterMigrationMethods.get(0).validatePublicVoidNoArg(false, errors);
        }
    }
}
