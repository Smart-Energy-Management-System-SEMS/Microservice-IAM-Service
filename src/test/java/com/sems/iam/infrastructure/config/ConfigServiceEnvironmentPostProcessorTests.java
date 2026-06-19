package com.sems.iam.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigServiceEnvironmentPostProcessorTests {

    @Test
    void removesKafkaTruststoreOverridesWhenCertificatesAreBlank() {
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("spring.kafka.properties.ssl.truststore.type", "PEM");
        overrides.put("spring.kafka.properties.ssl.truststore.certificates", "   ");

        ConfigServiceEnvironmentPostProcessor.sanitizeKafkaSslOverrides(overrides);

        assertFalse(overrides.containsKey("spring.kafka.properties.ssl.truststore.type"));
        assertFalse(overrides.containsKey("spring.kafka.properties.ssl.truststore.certificates"));
    }

    @Test
    void removesKafkaTruststoreOverridesWhenPemCertificateIsInvalid() {
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("spring.kafka.properties.ssl.truststore.type", "PEM");
        overrides.put("spring.kafka.properties.ssl.truststore.certificates", "not-a-real-pem");

        ConfigServiceEnvironmentPostProcessor.sanitizeKafkaSslOverrides(overrides);

        assertFalse(overrides.containsKey("spring.kafka.properties.ssl.truststore.type"));
        assertFalse(overrides.containsKey("spring.kafka.properties.ssl.truststore.certificates"));
    }

    @Test
    void keepsKafkaTruststoreOverridesWhenPemCertificateLooksValid() {
        Map<String, Object> overrides = new LinkedHashMap<>();
        String pem = """
                -----BEGIN CERTIFICATE-----
                MIIBszCCAVmgAwIBAgIUQ29kZXg=
                -----END CERTIFICATE-----
                """;
        overrides.put("spring.kafka.properties.ssl.truststore.type", "PEM");
        overrides.put("spring.kafka.properties.ssl.truststore.certificates", pem);

        ConfigServiceEnvironmentPostProcessor.sanitizeKafkaSslOverrides(overrides);

        assertTrue(overrides.containsKey("spring.kafka.properties.ssl.truststore.type"));
        assertEquals(pem, overrides.get("spring.kafka.properties.ssl.truststore.certificates"));
    }
}
