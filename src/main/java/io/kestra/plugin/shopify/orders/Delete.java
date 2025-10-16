package io.kestra.plugin.shopify.orders;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import java.io.IOException;
import java.lang.InterruptedException;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.shopify.AbstractShopifyTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.net.URI;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Delete an order from Shopify store",
    description = "Permanently delete an order from your Shopify store. Note: This action cannot be undone."
)
@Plugin(
    examples = {
        @Example(
            title = "Delete order by ID",
            full = true,
            code = """
                id: shopify_delete_order
                namespace: company.team
                
                tasks:
                  - id: delete_order
                    type: io.kestra.plugin.shopify.orders.Delete
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                    orderId: 123456789
                """
        )
    }
)
public class Delete extends AbstractShopifyTask implements RunnableTask<Delete.Output> {

    @Schema(
        title = "Order ID",
        description = "The ID of the order to delete"
    )
    @NotNull
    private Property<Long> orderId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        try (HttpClient client = HttpClient.builder().runContext(runContext).build()) {
            Long rOrderId = runContext.render(orderId).as(Long.class)
                .orElseThrow(() -> new IllegalArgumentException("Order ID is required"));

            URI uri = buildApiUrl(runContext, "/orders/" + rOrderId + ".json");
            HttpRequest request = buildAuthenticatedRequest(runContext, "DELETE", uri, null);

            runContext.logger().debug("Deleting order {} from Shopify API: {}", rOrderId, uri);
            
            handleRateLimit(runContext);
            HttpResponse<String> response = client.request(request, String.class);
            
            // For DELETE requests, Shopify returns 200 with empty body on success
            if (response.getStatus().getCode() != 200) {
                String errorBody = response.getBody() != null ? response.getBody() : "Unknown error";
                throw new RuntimeException(String.format("Failed to delete order with status %d: %s", 
                    response.getStatus().getCode(), errorBody));
            }

            runContext.logger().info("Successfully deleted order (ID: {}) from Shopify", rOrderId);

            return Output.builder()
                .orderId(rOrderId)
                .deleted(true)
                .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "Deleted order ID",
        description = "The ID of the order that was deleted"
        )
        private final Long orderId;

        @Schema(
        title = "Deletion status",
        description = "Whether the order was successfully deleted"
        )
        private final Boolean deleted;
    }
}