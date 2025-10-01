package io.kestra.plugin.shopify;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class AbstractShopifyTask extends Task {
    
    @Schema(
        title = "Shopify store domain",
        description = "The domain of your Shopify store (e.g., 'my-store.myshopify.com')"
    )
    protected Property<String> storeDomain;

    @Schema(
        title = "Admin API access token",
        description = "Private app access token for Shopify Admin API"
    )
    protected Property<String> accessToken;

    @Schema(
        title = "API version",
        description = "Shopify Admin API version to use",
        example = "2023-10"
    )
    @lombok.Builder.Default
    protected Property<String> apiVersion = Property.of("2023-10");

    @Schema(
        title = "Request timeout",
        description = "Timeout for HTTP requests"
    )
    @lombok.Builder.Default
    protected Property<Duration> timeout = Property.of(Duration.ofSeconds(30));

    protected static final ObjectMapper OBJECT_MAPPER = JacksonMapper.ofJson();
    
    // Rate limiting constants (Shopify allows 2 requests per second)
    protected static final Duration RATE_LIMIT_DELAY = Duration.ofMillis(500);
    
    protected HttpClient buildHttpClient(RunContext runContext) {
        return HttpClient.newBuilder()
            .connectTimeout(runContext.render(timeout).as(Duration.class).orElse(Duration.ofSeconds(30)))
            .build();
    }
    
    protected URI buildApiUrl(RunContext runContext, String path) throws Exception {
        String domain = runContext.render(storeDomain).as(String.class).orElseThrow(() -> 
            new IllegalArgumentException("Store domain is required"));
        String version = runContext.render(apiVersion).as(String.class).orElse("2023-10");
        
        return URI.create(String.format("https://%s/admin/api/%s%s", domain, version, path));
    }
    
    protected HttpRequest buildAuthenticatedRequest(RunContext runContext, String method, URI uri) throws Exception {
        String token = runContext.render(accessToken).as(String.class).orElseThrow(() -> 
            new IllegalArgumentException("Access token is required"));
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .header("X-Shopify-Access-Token", token)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
            
        switch (method.toUpperCase()) {
            case "GET":
                builder.GET();
                break;
            case "DELETE":
                builder.DELETE();
                break;
            default:
                throw new IllegalArgumentException("Unsupported method for request without body: " + method);
        }
        
        return builder.build();
    }
    
    protected HttpRequest buildAuthenticatedRequest(RunContext runContext, String method, URI uri, Object body) throws Exception {
        String token = runContext.render(accessToken).as(String.class).orElseThrow(() -> 
            new IllegalArgumentException("Access token is required"));
        
        String jsonBody = OBJECT_MAPPER.writeValueAsString(body);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .header("X-Shopify-Access-Token", token)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
            
        switch (method.toUpperCase()) {
            case "POST":
                builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
                break;
            case "PUT":
                builder.PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
                break;
            default:
                throw new IllegalArgumentException("Unsupported method for request with body: " + method);
        }
        
        return builder.build();
    }
    
    protected void handleRateLimit() {
        try {
            Thread.sleep(RATE_LIMIT_DELAY.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    protected void validateResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() >= 400) {
            String errorBody = response.body() != null ? response.body() : "Unknown error";
            throw new RuntimeException(String.format("Shopify API request failed with status %d: %s", 
                response.statusCode(), errorBody));
        }
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> parseResponse(HttpResponse<String> response) throws Exception {
        validateResponse(response);
        if (response.body() == null || response.body().trim().isEmpty()) {
            return Map.of();
        }
        return OBJECT_MAPPER.readValue(response.body(), Map.class);
    }
}