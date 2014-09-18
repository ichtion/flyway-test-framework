package org.flywaydb.test.runner;

import com.google.common.collect.ImmutableSet;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.annotation.FlywayMigrationTestSuite;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

import static org.flywaydb.test.db.DbMigrator.dbMigratorForConfiguration;

public class FlywayJUnitRunner extends ParentRunner<Runner> {
    private SortedMap<MigrationVersion, Set<Class<?>>> testClassesPerVersion = new TreeMap<MigrationVersion, Set<Class<?>>>();

    public FlywayJUnitRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        cleanDbIfNeeded(clazz);
        for (Class flywayTestClass : getFlywayTestClass(clazz)) {
            addTestClassPerMigrationVersion(flywayTestClass);
        }
    }

    private void cleanDbIfNeeded(Class<?> clazz) {
        FlywayMigrationTestSuite flywayMigrationTestSuite = clazz.getAnnotation(FlywayMigrationTestSuite.class);
        FlywayMigrationTest flywayMigrationTest = clazz.getAnnotation(FlywayMigrationTest.class);

        if (null != flywayMigrationTestSuite && flywayMigrationTestSuite.cleanDb()) {
            dbMigratorForConfiguration(flywayMigrationTestSuite.flywayConfiguration())
                    .cleanDb();
        }

        if (null != flywayMigrationTest && flywayMigrationTest.cleanDb()) {
            dbMigratorForConfiguration(flywayMigrationTest.flywayConfiguration())
                    .cleanDb();
        }
    }

    private void addTestClassPerMigrationVersion(Class<?> flywayTestClass) {
        MigrationVersion migrationVersion = getMigrationVersion(flywayTestClass);
        if (thereAreNoMigrationTestsForGivenMigrationVersion(migrationVersion)) {
            addNewSetWithMigrationTestClass(flywayTestClass, migrationVersion);
        } else {
            addAnotherFlywayMigrationTestClass(flywayTestClass, migrationVersion);
        }
    }

    private MigrationVersion getMigrationVersion(Class<?> flywayTestClass) {
        return MigrationVersion.fromVersion(flywayTestClass.getAnnotation(FlywayMigrationTest.class).migrationVersion());
    }

    private boolean thereAreNoMigrationTestsForGivenMigrationVersion(MigrationVersion migrationVersion) {
        return testClassesPerVersion.get(migrationVersion) == null;
    }

    private void addAnotherFlywayMigrationTestClass(Class<?> flywayTestClass, MigrationVersion migrationVersion) {
        testClassesPerVersion.get(migrationVersion).add(flywayTestClass);
    }

    private void addNewSetWithMigrationTestClass(Class<?> flywayTestClass, MigrationVersion migrationVersion) {
        Set<Class<?>> tests = new HashSet<Class<?>>();
        tests.add(flywayTestClass);
        testClassesPerVersion.put(migrationVersion, tests);
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
        validateProperClassAnnotations(errors);
        validateTestSuiteWhenClassIsTestSuite(errors);
        validateTestClassWhenClassIsTestClass(errors);
    }

    private void validateProperClassAnnotations(List<Throwable> errors) {
        boolean flywayMigrationTest = false;
        boolean flywayMigrationSuite = false;
        for (Annotation annotation : getTestClass().getAnnotations()) {
            if (annotation.annotationType().isAssignableFrom(FlywayMigrationTest.class)) {
                flywayMigrationTest = true;
            } else if (annotation.annotationType().isAssignableFrom(FlywayMigrationTestSuite.class)) {
                flywayMigrationSuite = true;
            }
        }

        if (flywayMigrationTest && flywayMigrationSuite) {
            errors.add(new Exception("Test class can be annotated with either @FlywayMigrationTest or @FlywayMigrationTestSuite, never both"));
        } else if (!(flywayMigrationTest || flywayMigrationSuite)) {
            errors.add(new Exception("Test class should be annotated either with @FlywayMigrationTest or @FlywayMigrationTestSuite"));
        }
    }

    private void validateTestSuiteWhenClassIsTestSuite(List<Throwable> errors) {
        if (isTestClassFlywayMigrationTestSuite()) {
            validateNoBeforeOrAfterMigrationMethods(errors);
        }
    }

    private boolean isTestClassFlywayMigrationTestSuite() {
        return null != getTestClass().getJavaClass().getAnnotation(FlywayMigrationTestSuite.class);
    }

    private void validateNoBeforeOrAfterMigrationMethods(List<Throwable> errors) {
        List<FrameworkMethod> beforeMigrationMethods = getTestClass().getAnnotatedMethods(BeforeMigration.class);
        List<FrameworkMethod> afterMigrationMethods = getTestClass().getAnnotatedMethods(AfterMigration.class);

        if (!beforeMigrationMethods.isEmpty() || !afterMigrationMethods.isEmpty()) {
            errors.add(new Exception("MigrationTestSuite should not have methods annotated as BeforeMigration or AfterMigration"));
        }
    }

    private void validateTestClassWhenClassIsTestClass(List<Throwable> errors) {
        List<FrameworkMethod> beforeMigrationMethods = getTestClass().getAnnotatedMethods(BeforeMigration.class);
        List<FrameworkMethod> afterMigrationMethods = getTestClass().getAnnotatedMethods(AfterMigration.class);

        if (beforeMigrationMethods.size() > 1 || afterMigrationMethods.size() > 1) {
            errors.add(new Exception("FlywayMigrationTest should not have more than one method annotated as BeforeMigration or AfterMigration"));
        } else if (beforeMigrationMethods.size() == 0 && afterMigrationMethods.size() == 0) {
            errors.add(new Exception("FlywayMigrationTest should have at least one method annotated as either BeforeMigration or AfterMigration"));
        } else if(beforeMigrationMethods.size() == 1) {
            beforeMigrationMethods.get(0).validatePublicVoidNoArg(false, errors);
        } else if (afterMigrationMethods.size() == 1) {
            afterMigrationMethods.get(0).validatePublicVoidNoArg(false, errors);
        }

    }

    private Set<Class> getFlywayTestClass(Class<?> clazz) {
        FlywayMigrationTestSuite flywayMigrationTestSuite = clazz.getAnnotation(FlywayMigrationTestSuite.class);
        if (null != flywayMigrationTestSuite) {
            return classesFromSuite(flywayMigrationTestSuite);
        }
        return ImmutableSet.<Class>of(clazz);
    }

    private Set<Class> classesFromSuite(FlywayMigrationTestSuite flywayMigrationTestSuite) {
        Set<Class> classes = new HashSet<Class>();

        for (String packageWithFlywayTests : flywayMigrationTestSuite.packages()) {
            Reflections reflections = new Reflections(packageWithFlywayTests);
            classes.addAll(reflections.getTypesAnnotatedWith(FlywayMigrationTest.class));
        }

        return classes;
    }

    @Override
    protected String getName() {
        return "Migration suite";
    }

    @Override
    protected List<Runner> getChildren() {
        List<Runner> childRunners = new ArrayList<Runner>();

        for (MigrationVersion migrationVersion : testClassesPerVersion.keySet()) {
            try {
                SuiteForMigrationVersion suiteForMigrationVersion = new SuiteForMigrationVersion(
                        migrationVersion,
                        testClassesPerVersion.get(migrationVersion));
                childRunners.add(new FlywayMigrationSuiteRunner(suiteForMigrationVersion));
            } catch (InitializationError initializationError) {
                throw new RuntimeException(initializationError);
            }
        }
        return childRunners;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier);
    }
}
