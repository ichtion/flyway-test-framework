package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.annotation.FlywayMigrationTestSuite;
import org.flywaydb.test.db.DbMigrator;
import org.flywaydb.test.db.FlywayConfiguration;
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
import java.util.*;

import static org.flywaydb.test.db.DbMigratorProvider.dbMigratorProvider;
import static org.flywaydb.test.db.FlywayConfiguration.flywayConfiguration;

public class FlywayJUnitMigrationTestSuiteRunner extends ParentRunner<Runner> {
    private List<Runner> children;

    public FlywayJUnitMigrationTestSuiteRunner(Class<?> suiteClass) throws InitializationError {
        super(suiteClass);
        cleanDbIfNeeded(suiteClass);
        children = createChildren(testClassesSortedByMigrationVersion(suiteClass));
    }

    //TODO look for some collection that would do the 'sophisticated' logic
    private SortedMap<MigrationVersion, Set<Class<?>>> testClassesSortedByMigrationVersion(Class<?> clazz) {
        SortedMap<MigrationVersion, Set<Class<?>>> testClassesPerVersion = new TreeMap<MigrationVersion, Set<Class<?>>>();
        for (Class flywayTestClass : getFlywayTestClass(clazz)) {
            addTestClassPerMigrationVersion(testClassesPerVersion, flywayTestClass);
        }
        return testClassesPerVersion;
    }

    private List<Runner> createChildren(SortedMap<MigrationVersion, Set<Class<?>>> testClassesPerVersion) throws InitializationError {
        List<Runner> childRunners = new ArrayList<Runner>();

        for (MigrationVersion migrationVersion : testClassesPerVersion.keySet()) {
            SuiteForMigrationVersion suiteForMigrationVersion = new SuiteForMigrationVersion(
                    migrationVersion,
                    testClassesPerVersion.get(migrationVersion));
            childRunners.add(new FlywayMigrationSuiteRunner(suiteForMigrationVersion));
        }
        return childRunners;
    }

    private void cleanDbIfNeeded(Class<?> clazz) {
        FlywayMigrationTestSuite flywayMigrationTestSuite = clazz.getAnnotation(FlywayMigrationTestSuite.class);

        if (flywayMigrationTestSuite.cleanDb()) {
            cleanDataBase(flywayConfiguration(flywayMigrationTestSuite.flywayConfiguration()));
        }
    }

    private void cleanDataBase(FlywayConfiguration flywayConfiguration) {
        DbMigrator dbMigrator = dbMigratorProvider().provideDbMigratorForConfiguration(flywayConfiguration);
        dbMigrator.cleanDb();
    }

    private Set<Class> getFlywayTestClass(Class<?> clazz) {
        FlywayMigrationTestSuite flywayMigrationTestSuite = clazz.getAnnotation(FlywayMigrationTestSuite.class);
        return classesFromSuite(flywayMigrationTestSuite);
    }

    private Set<Class> classesFromSuite(FlywayMigrationTestSuite flywayMigrationTestSuite) {
        Set<Class> classes = new HashSet<Class>();

        for (String packageWithFlywayTests : flywayMigrationTestSuite.packages()) {
            Reflections reflections = new Reflections(packageWithFlywayTests);
            classes.addAll(reflections.getTypesAnnotatedWith(FlywayMigrationTest.class));
        }

        return classes;
    }

    private void addTestClassPerMigrationVersion(SortedMap<MigrationVersion, Set<Class<?>>> testClassesPerVersion, Class<?> flywayTestClass) {
        MigrationVersion migrationVersion = getMigrationVersion(flywayTestClass);
        if (thereAreNoMigrationTestsForGivenMigrationVersion(testClassesPerVersion, migrationVersion)) {
            addNewSetWithMigrationTestClass(testClassesPerVersion, flywayTestClass, migrationVersion);
        } else {
            addAnotherFlywayMigrationTestClass(testClassesPerVersion, flywayTestClass, migrationVersion);
        }
    }

    private MigrationVersion getMigrationVersion(Class<?> flywayTestClass) {
        return MigrationVersion.fromVersion(flywayTestClass.getAnnotation(FlywayMigrationTest.class).migrationVersion());
    }

    private boolean thereAreNoMigrationTestsForGivenMigrationVersion(SortedMap<MigrationVersion, Set<Class<?>>> testClassesPerVersion, MigrationVersion migrationVersion) {
        return testClassesPerVersion.get(migrationVersion) == null;
    }

    private void addAnotherFlywayMigrationTestClass(SortedMap<MigrationVersion, Set<Class<?>>> testClassesPerVersion, Class<?> flywayTestClass, MigrationVersion migrationVersion) {
        testClassesPerVersion.get(migrationVersion).add(flywayTestClass);
    }

    private void addNewSetWithMigrationTestClass(SortedMap<MigrationVersion, Set<Class<?>>> testClassesPerVersion, Class<?> flywayTestClass, MigrationVersion migrationVersion) {
        Set<Class<?>> tests = new HashSet<Class<?>>();
        tests.add(flywayTestClass);
        testClassesPerVersion.put(migrationVersion, tests);
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        return childrenInvoker(notifier);
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

    //TODO think if there should be any method at all
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
