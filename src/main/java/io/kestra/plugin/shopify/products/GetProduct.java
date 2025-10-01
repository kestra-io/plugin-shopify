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
import io.kestra.plugin.shopify.models.Product;
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
    title = "Get a specific product from Shopify store",
    description = "Retrieve detailed information about a specific product by its ID."
)
@Plugin(
    examples = {
        @Example(
        title = "Get product by ID",
        full = true,
        code = """
        id: shopify_get_product
        namespace: company.team

        tasks:
          - id: get_product
            type: io.kestra.plugin.shopify.products.GetProduct
            storeDomain: my-store.myshopify.com
            accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            productId: 123456789
        """
        )
    }
)
public class GetProduct extends AbstractShopifyTask implements RunnableTask<GetProduct.Output> {

    @Schema(
        title = "Product ID",
        description = "The ID of the product to retrieve"
    )
    private Property<Long> productId;

    @Schema(
        title = "Fields to include",
        description = "Comma-separated list of fields to include in the response"
    )
    private Property<String> fields;

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = buildHttpClient(runContext);
        Long productIdValue = runContext.render(productId).as(Long.class)
        .orElseThrow(() -> new IllegalArgumentException("Product ID is required"));

        StringBuilder pathBuilder = new StringBuilder("/products/")
        .append(productIdValue)
        .append(".json");

        // Add fields parameter if specified
        if (fields != null) {
        String fieldsValue = runContext.render(fields).as(String.class).orElse(null);
        if (fieldsValue != null && !fieldsValue.trim().isEmpty()) {
            pathBuilder.append("?fields=").append(fieldsValue);
        }
        }

        URI uri = buildApiUrl(runContext, pathBuilder.toString());
        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri);

        runContext.logger().debug("Getting product {} from Shopify API: {}", productIdValue, uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> productData = (Map<String, Object>) responseData.get("product");
        
        if (productData == null) {
        throw new RuntimeException("Product not found with ID: " + productIdValue);
        }
        
        Product product = OBJECT_MAPPER.convertValue(productData, Product.class);

        runContext.logger().info("Retrieved product '{}' (ID: {}) from Shopify", 
        product.getTitle(), product.getId());

        return Output.builder()
        .product(product)
        .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "Product",
        description = "The retrieved product from Shopify"
        )
        private final Product product;
    }
