package org.flywaydb.test.db;

import org.flywaydb.test.util.PropertiesUtils;

import java.util.Properties;

public class FlywayConfiguration extends Properties{
    private static final String DB_URL_KEY = "flyway.url";
    private static final String DB_USER_KEY = "flyway.user";
    private static final String DB_PASSWORD_KEY = "flyway.password";

    public static FlywayConfiguration flywayConfiguration(String configFilePath) {
        return new FlywayConfiguration(PropertiesUtils.load(configFilePath));
    }

    private FlywayConfiguration(Properties flywayProperties) {
        super(flywayProperties);
        validateProperties();
    }

    private void validateProperties() {
        if (getProperty(DB_URL_KEY) == null ||
                getProperty(DB_USER_KEY) == null ||
                getProperty(DB_PASSWORD_KEY) == null) {
            StringBuilder errorMessageBuilder = new StringBuilder()
                    .append("It is mandatory to provide following data in the configuration file: ")
                    .append(DB_URL_KEY).append(", ")
                    .append(DB_USER_KEY).append(", ")
                    .append(DB_PASSWORD_KEY);
            throw new IllegalArgumentException(errorMessageBuilder.toString());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FlywayConfiguration)) {
            return false;
        }
        FlywayConfiguration otherConfiguration = (FlywayConfiguration) obj;
        return this.getProperty(DB_URL_KEY)
                .equals(otherConfiguration.getProperty(DB_URL_KEY));
    }

    @Override
    public int hashCode() {
        return getProperty(DB_URL_KEY).hashCode();
    }
}
