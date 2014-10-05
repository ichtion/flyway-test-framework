package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.annotation.FlywayMigrationTestSuite;
import org.flywaydb.test.util.SortedSetMultiMap;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.flywaydb.test.db.DbUtilities.cleanDb;
import static org.flywaydb.test.db.FlywayConfiguration.flywayConfiguration;

public class FlywayJUnitMigrationTestSuiteRunner extends ParentRunner<Runner> {
    private List<Runner> children;

    public FlywayJUnitMigrationTestSuiteRunner(Class<?> suiteClass) throws InitializationError {
        super(suiteClass);
        children = createChildren(testClassesSortedByMigrationVersion(suiteClass));
    }

    private static SortedSetMultiMap<MigrationVersion, Class<?>> testClassesSortedByMigrationVersion(Class<?> clazz) {
        SortedSetMultiMap<MigrationVersion, Class<?>> testClassesPerVersion = new SortedSetMultiMap<MigrationVersion, Class<?>>();
        for (Class flywayTestClass : getFlywayTestClass(clazz)) {
            testClassesPerVersion.put(migrationVersionFrom(flywayTestClass), flywayTestClass);
        }
        return testClassesPerVersion;
    }

    private static List<Runner> createChildren(SortedSetMultiMap<MigrationVersion, Class<?>> testClassesPerVersion) throws InitializationError {
        List<Runner> childRunners = new ArrayList<Runner>();

        for (MigrationVersion migrationVersion : testClassesPerVersion.keySet()) {
            SuiteForMigrationVersion suiteForMigrationVersion = new SuiteForMigrationVersion(
                    migrationVersion,
                    testClassesPerVersion.get(migrationVersion));
            childRunners.add(new FlywayMigrationSuiteRunner(suiteForMigrationVersion));
        }
        return childRunners;
    }

    private static Set<Class> getFlywayTestClass(Class<?> clazz) {
        FlywayMigrationTestSuite flywayMigrationTestSuite = clazz.getAnnotation(FlywayMigrationTestSuite.class);
        return classesFromSuite(flywayMigrationTestSuite);
    }

    private static Set<Class> classesFromSuite(FlywayMigrationTestSuite flywayMigrationTestSuite) {
        Set<Class> classes = new HashSet<Class>();

        for (String packageWithFlywayTests : flywayMigrationTestSuite.packages()) {
            Reflections reflections = new Reflections(packageWithFlywayTests);
            classes.addAll(reflections.getTypesAnnotatedWith(FlywayMigrationTest.class));
        }

        return classes;
    }

    private static MigrationVersion migrationVersionFrom(Class<?> flywayTestClass) {
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
        FlywayMigrationTestSuite flywayMigrationTestSuite = getTestClass().getJavaClass().getAnnotation(FlywayMigrationTestSuite.class);

        if (flywayMigrationTestSuite.cleanDb()) {
            cleanDb(flywayConfiguration(flywayMigrationTestSuite.flywayConfiguration()));
        }
    }


    @Override
    protected String getName() {
        return "Migration suite";
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
        validateProperClassAnnotations(errors);
        validateMethods(errors);
    }

    private void validateMethods(List<Throwable> errors) {
        validateNoMethod(Test.class, errors);
        validateNoMethod(BeforeMigration.class, errors);
        validateNoMethod(AfterMigration.class, errors);
    }

    private void validateNoMethod(Class<? extends Annotation> notApplicableAnnotation, List<Throwable> errors) {
        List<FrameworkMethod> annotatedMethods = getTestClass().getAnnotatedMethods(notApplicableAnnotation);

        if (!annotatedMethods.isEmpty()) {
            errors.add(new Exception("Migration test should not have any method annotated with @" + notApplicableAnnotation.getSimpleName()));
        }
    }

    private void validateProperClassAnnotations(List<Throwable> errors) {
        for (Annotation annotation : getTestClass().getAnnotations()) {
            if (annotation.annotationType().isAssignableFrom(FlywayMigrationTestSuite.class)) {
                return;
            }
        }
        errors.add(new Exception("Tests run with FlywayJUnitMigrationTestSuiteRunner must be annotated with @FlywayMigrationTestSuite"));
    }
}
