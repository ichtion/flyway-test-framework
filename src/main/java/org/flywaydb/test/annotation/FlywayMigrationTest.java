package org.flywaydb.test.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface FlywayMigrationTest {
    boolean cleanDb();

    String migrationVersion();

    String flywayConfiguration();
}
