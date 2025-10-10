package io.kestra.plugin.shopify;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
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
    @NotNull
    protected Property<String> storeDomain;

    @Schema(
        title = "Admin API access token",
        description = "Private app access token for Shopify Admin API"
    )
    @NotNull
    protected Property<String> accessToken;

    @Schema(
        title = "API version",
        description = "Shopify Admin API version to use"
    )
    @Builder.Default
    protected Property<String> apiVersion = Property.of("2024-10");

    @Schema(
        title = "Rate limit delay",
        description = "Delay between API calls to respect rate limits"
    )
    @Builder.Default
    protected Property<Duration> rateLimitDelay = Property.of(Duration.ofMillis(500));

    protected URI buildApiUrl(RunContext runContext, String path) throws Exception {
        String domain = runContext.render(storeDomain).as(String.class).orElseThrow(() -> 
            new IllegalArgumentException("Store domain is required"));
        String version = runContext.render(apiVersion).as(String.class).orElse("2024-10");
        
        return URI.create(String.format("https://%s/admin/api/%s%s", domain, version, path));
    }

    protected HttpRequest buildAuthenticatedRequest(RunContext runContext, String method, URI uri, Object body) throws Exception {
        String token = runContext.render(accessToken).as(String.class).orElseThrow(() ->
            new IllegalArgumentException("Access token is required"));

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(uri)
            .header("X-Shopify-Access-Token", token)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");

        if (body != null) {
            String jsonBody = JacksonMapper.ofJson().writeValueAsString(body);
            requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        return requestBuilder.build();
    }

    protected void handleRateLimit(RunContext runContext) throws Exception {
        Duration delay = runContext.render(rateLimitDelay).as(Duration.class).orElse(Duration.ofMillis(500));
        if (delay != null && !delay.isNegative() && !delay.isZero()) {
            Thread.sleep(delay.toMillis());
        }
    }

    protected Map<String, Object> parseResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() >= 400) {
            throw new RuntimeException(String.format("Shopify API error: %d - %s", 
                response.statusCode(), response.body()));
        }

        return JacksonMapper.ofJson().readValue(response.body(), Map.class);
    }
}
