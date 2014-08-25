package org.flywaydb.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtils {

    private PropertiesUtils() {
    }

    public static Properties load(String resource) {
        return load(PropertiesUtils.class.getResourceAsStream(resource));
    }

    public static Properties load(InputStream inputStream) {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
