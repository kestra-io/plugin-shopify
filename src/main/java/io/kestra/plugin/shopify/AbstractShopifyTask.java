package io.kestra.plugin.shopify;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
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
        String rStoreDomain = runContext.render(storeDomain).as(String.class).orElseThrow(() -> 
            new IllegalArgumentException("Store domain is required"));
        String rApiVersion = runContext.render(apiVersion).as(String.class).orElse("2024-10");
        
        return URI.create(String.format("https://%s/admin/api/%s%s", rStoreDomain, rApiVersion, path));
    }

    protected HttpRequest buildAuthenticatedRequest(RunContext runContext, String method, URI uri, Object body) throws Exception {
        String rAccessToken = runContext.render(accessToken).as(String.class).orElseThrow(() ->
            new IllegalArgumentException("Access token is required"));

        HttpRequest.HttpRequestBuilder requestBuilder = HttpRequest.builder()
            .uri(uri)
            .method(method)
            .addHeader("X-Shopify-Access-Token", rAccessToken)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json");

        if (body != null) {
            String jsonBody = JacksonMapper.ofJson().writeValueAsString(body);
            requestBuilder.body(HttpRequest.StringRequestBody.builder()
                .content(jsonBody)
                .contentType("application/json")
                .build());
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
        if (response.getStatus().getCode() >= 400) {
            throw new RuntimeException(String.format("Shopify API error: %d - %s", 
                response.getStatus().getCode(), response.getBody()));
        }

        return JacksonMapper.ofJson().readValue(response.getBody(), Map.class);
    }
}
