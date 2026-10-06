package com.tripflow.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DotenvEnvironmentPostProcessorTest {

    @Test
    void parseDotenv_readsKeysAndIgnoresComments(@TempDir Path dir) throws Exception {
        Path env = dir.resolve(".env");
        Files.writeString(env, """
                # comment
                RAZORPAY_KEY_ID=rzp_test_abc
                RAZORPAY_KEY_SECRET=secret123
                TRIPFLOW_PAYMENT_PROVIDER=razorpay
                """);

        Map<String, Object> values = DotenvEnvironmentPostProcessor.parseDotenv(env);

        assertThat(values)
                .containsEntry("RAZORPAY_KEY_ID", "rzp_test_abc")
                .containsEntry("RAZORPAY_KEY_SECRET", "secret123")
                .containsEntry("TRIPFLOW_PAYMENT_PROVIDER", "razorpay");
    }
}
