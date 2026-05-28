package com.sems.iam.infrastructure.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class ConfigServiceEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final Logger log = LoggerFactory.getLogger(ConfigServiceEnvironmentPostProcessor.class);
    private static final String PROPERTY_SOURCE_NAME = "configServiceOverrides";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String configServiceUrl = environment.getProperty("CONFIG_SERVICE_URL");
        if (!StringUtils.hasText(configServiceUrl)) {
            return;
        }

        String serviceName = environment.getProperty("spring.application.name", "iam-service");
        int timeoutMs = Integer.parseInt(environment.getProperty("CONFIG_SERVICE_TIMEOUT_MS", "3000"));

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();

        Map<String, Object> overrides = new LinkedHashMap<>();
        for (String endpoint : endpoints(configServiceUrl, serviceName)) {
            fetchAndMerge(client, endpoint, timeoutMs, overrides);
        }

        if (!overrides.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, overrides));
            log.info("Loaded {} configuration overrides from Config Service", overrides.size());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private List<String> endpoints(String baseUrl, String serviceName) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        List<String> urls = new ArrayList<>();
        urls.add(normalized + "/api/v1/config/" + serviceName);
        urls.add(normalized + "/api/v1/config/kafka");
        urls.add(normalized + "/api/v1/config/services");
        return urls;
    }

    private void fetchAndMerge(HttpClient client, String url, int timeoutMs, Map<String, Object> target) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300 && StringUtils.hasText(response.body())) {
                Map<String, Object> body = OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {});
                flattenAndCollect("", body, target);
            }
        } catch (Exception ex) {
            log.debug("Config Service endpoint unavailable: {}", url);
        }
    }

    @SuppressWarnings("unchecked")
    private void flattenAndCollect(String prefix, Map<String, Object> source, Map<String, Object> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isBlank() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                flattenAndCollect(key, (Map<String, Object>) nested, target);
            } else if (value != null && isSupportedKey(key)) {
                target.put(key, value.toString());
            }
        }
    }

    private boolean isSupportedKey(String key) {
        return key.equals("server.port")
                || key.equals("service.public-base-url")
                || key.equals("spring.datasource.url")
                || key.equals("spring.datasource.username")
                || key.equals("spring.datasource.password")
                || key.equals("spring.kafka.bootstrap-servers")
                || key.equals("spring.kafka.consumer.group-id")
                || key.equals("spring.kafka.properties.security.protocol")
                || key.equals("spring.kafka.properties.sasl.mechanism")
                || key.equals("spring.kafka.properties.sasl.jaas.config")
                || key.equals("spring.kafka.properties.ssl.truststore.type")
                || key.equals("spring.kafka.properties.ssl.truststore.certificates")
                || key.startsWith("topics.")
                || key.startsWith("security.oauth2.google.")
                || key.equals("security.jwt.expiration-minutes");
    }
}
