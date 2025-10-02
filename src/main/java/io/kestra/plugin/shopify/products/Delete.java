package io.kestra.plugin.shopify.products;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

import java.net.URI;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Delete a product from Shopify store",
    description = "Permanently delete a product from your Shopify store."
)
@Plugin(
    examples = {
        @Example(
        title = "Delete product by ID",
        full = true,
        code = """
                id: shopify_delete_product
                namespace: company.team

                tasks:
                  - id: delete_product
            type: io.kestra.plugin.shopify.products.Delete
            storeDomain: my-store.myshopify.com
            accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            productId: 123456789
        """
        )
    }
)
public class Delete extends AbstractShopifyTask implements RunnableTask<Delete.Output> {

    @Schema(
        title = "Product ID",
        description = "The ID of the product to delete"
    )
    private Property<Long> productId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = buildHttpClient(runContext);
        Long productIdValue = runContext.render(productId).as(Long.class)
        .orElseThrow(() -> new IllegalArgumentException("Product ID is required"));

        URI uri = buildApiUrl(runContext, "/products/" + productIdValue + ".json");
        HttpRequest request = buildAuthenticatedRequest(runContext, "DELETE", uri);

        runContext.logger().debug("Deleting product {} from Shopify API: {}", productIdValue, uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // For DELETE requests, Shopify returns 200 with empty body on success
        if (response.statusCode() != 200) {
        String errorBody = response.body() != null ? response.body() : "Unknown error";
        throw new RuntimeException(String.format("Failed to delete product with status %d: %s", 
            response.statusCode(), errorBody));
        }

        runContext.logger().info("Successfully deleted product (ID: {}) from Shopify", productIdValue);

        return Output.builder()
        .productId(productIdValue)
        .deleted(true)
        .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "Deleted product ID",
        description = "The ID of the product that was deleted"
        )
        private final Long productId;

        @Schema(
        title = "Deletion status",
        description = "Whether the product was successfully deleted"
        )
        private final Boolean deleted;
    }
