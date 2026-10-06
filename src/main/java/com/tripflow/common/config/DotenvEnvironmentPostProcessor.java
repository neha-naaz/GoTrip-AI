package com.tripflow.common.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;

/**
 * Loads a project {@code .env} file before beans are created.
 * {@code spring.config.import=optional:file:.env} is CWD-sensitive and resolves
 * placeholders in {@code application.properties} too early for IntelliJ runs.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String PROPERTY_SOURCE_NAME = "tripflowDotenv";

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.acceptsProfiles(Profiles.of("test"))) {
            return;
        }
        if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
            return;
        }

        Path envFile = findEnvFile();
        if (envFile == null) {
            return;
        }

        Map<String, Object> values = parseDotenv(envFile);
        alias(values, "RAZORPAY_KEY_ID", "tripflow.payment.razorpay.key-id");
        alias(values, "TRIPFLOW_RAZORPAY_KEY_ID", "tripflow.payment.razorpay.key-id");
        alias(values, "RAZORPAY_KEY_SECRET", "tripflow.payment.razorpay.key-secret");
        alias(values, "TRIPFLOW_RAZORPAY_KEY_SECRET", "tripflow.payment.razorpay.key-secret");
        alias(values, "TRIPFLOW_RAZORPAY_WEBHOOK_SECRET", "tripflow.payment.razorpay.webhook-secret");
        alias(values, "TRIPFLOW_PAYMENT_PROVIDER", "tripflow.payment.provider");

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, values));
    }

    private static void alias(Map<String, Object> values, String from, String to) {
        Object value = values.get(from);
        if (value instanceof String text && !text.isBlank() && !values.containsKey(to)) {
            values.put(to, text);
        }
    }

    static Path findEnvFile() {
        Path cwd = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path current = cwd;
        for (int i = 0; i < 6 && current != null; i++) {
            Path candidate = current.resolve(".env");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return Files.isRegularFile(cwd.resolve(".env")) ? cwd.resolve(".env") : null;
    }

    static Map<String, Object> parseDotenv(Path envFile) {
        Map<String, Object> values = new LinkedHashMap<>();
        try {
            for (String raw : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = unquote(line.substring(eq + 1).trim());
                values.put(key, value);
            }
        } catch (IOException ignored) {
            return Map.of();
        }
        return values;
    }

    private static String unquote(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
