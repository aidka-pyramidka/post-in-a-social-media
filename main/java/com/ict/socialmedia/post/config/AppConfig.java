package com.ict.socialmedia.post.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class AppConfig {
    private final Properties props;

    public AppConfig(Properties props) {
        this.props = Objects.requireNonNull(props, "props");
    }

    public static AppConfig loadFromClasspath(String resourceName) {
        Properties p = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Config resource not found: " + resourceName);
            }
            p.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + resourceName, e);
        }
        return new AppConfig(p);
    }

    public String getRequired(String key) {
        String v = System.getenv(envKey(key));
        if (v == null || v.isBlank()) v = props.getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing required config: " + key);
        }
        return v.trim();
    }

    public String getOptional(String key, String fallback) {
        String v = System.getenv(envKey(key));
        if (v == null || v.isBlank()) v = props.getProperty(key);
        if (v == null || v.isBlank()) return fallback;
        return v.trim();
    }

    private static String envKey(String key) {
        return key.toUpperCase().replace('.', '_');
    }
}


