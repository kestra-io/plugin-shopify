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
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "List products from Shopify store",
    description = "Retrieve a list of products from your Shopify store with optional filtering and pagination."
)
@Plugin(
    examples = {
        @Example(
        title = "List all products",
        full = true,
        code = """
            id: shopify_list_products
            namespace: company.team

            tasks:
              - id: list_products
                type: io.kestra.plugin.shopify.products.ListProducts
                storeDomain: my-store.myshopify.com
                accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            """
        ),
        @Example(
        title = "List products with filtering",
        full = true,
        code = """
            id: shopify_list_products_filtered
            namespace: company.team

            tasks:
              - id: list_products
                type: io.kestra.plugin.shopify.products.ListProducts
                storeDomain: my-store.myshopify.com
                accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                limit: 50
                status: active
                productType: clothing
            """
        )
    }
)
public class ListProducts extends AbstractShopifyTask implements RunnableTask<ListProducts.Output> {

    @Schema(
        title = "Number of products to retrieve",
        description = "Maximum number of products to return (1-250, default: 50)"
    )
    @Builder.Default
    private Property<Integer> limit = Property.of(50);

    @Schema(
        title = "Product status filter",
        description = "Filter products by status"
    )
    private Property<String> status;

    @Schema(
        title = "Product type filter",
        description = "Filter products by product type"
    )
    private Property<String> productType;

    @Schema(
        title = "Vendor filter",
        description = "Filter products by vendor"
    )
    private Property<String> vendor;

    @Schema(
        title = "Collection ID filter",
        description = "Filter products by collection ID"
    )
    private Property<Long> collectionId;

    @Schema(
        title = "Product handle filter",
        description = "Filter products by handle"
    )
    private Property<String> handle;

    @Schema(
        title = "Published status filter",
        description = "Filter by published status (published, unpublished, any)"
    )
    private Property<String> publishedStatus;

    @Schema(
        title = "Created after date",
        description = "Show products created after this date (ISO 8601 format)"
    )
    private Property<String> createdAtMin;

    @Schema(
        title = "Created before date",
        description = "Show products created before this date (ISO 8601 format)"
    )
    private Property<String> createdAtMax;

    @Schema(
        title = "Updated after date",
        description = "Show products updated after this date (ISO 8601 format)"
    )
    private Property<String> updatedAtMin;

    @Schema(
        title = "Updated before date",
        description = "Show products updated before this date (ISO 8601 format)"
    )
    private Property<String> updatedAtMax;

    @Schema(
        title = "Page info for pagination",
        description = "Page info parameter for cursor-based pagination"
    )
    private Property<String> pageInfo;

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = buildHttpClient(runContext);
        StringBuilder pathBuilder = new StringBuilder("/products.json");
        
        // Build query parameters
        StringBuilder queryParams = new StringBuilder();
        
        Integer limitValue = runContext.render(limit).as(Integer.class).orElse(50);
        queryParams.append("?limit=").append(Math.min(Math.max(limitValue, 1), 250));
        
        if (status != null) {
            String statusValue = runContext.render(status).as(String.class).orElse(null);
            if (statusValue != null) {
                queryParams.append("&status=").append(statusValue);
            }
        }
        
        if (productType != null) {
            String productTypeValue = runContext.render(productType).as(String.class).orElse(null);
            if (productTypeValue != null) {
                queryParams.append("&product_type=").append(productTypeValue);
            }
        }
        
        if (vendor != null) {
            String vendorValue = runContext.render(vendor).as(String.class).orElse(null);
            if (vendorValue != null) {
                queryParams.append("&vendor=").append(vendorValue);
            }
        }
        
        if (collectionId != null) {
            Long collectionIdValue = runContext.render(collectionId).as(Long.class).orElse(null);
            if (collectionIdValue != null) {
                queryParams.append("&collection_id=").append(collectionIdValue);
            }
        }
        
        if (handle != null) {
            String handleValue = runContext.render(handle).as(String.class).orElse(null);
            if (handleValue != null) {
                queryParams.append("&handle=").append(handleValue);
            }
        }
        
        if (publishedStatus != null) {
            String publishedStatusValue = runContext.render(publishedStatus).as(String.class).orElse(null);
            if (publishedStatusValue != null) {
                queryParams.append("&published_status=").append(publishedStatusValue);
            }
        }
        
        if (createdAtMin != null) {
            String createdAtMinValue = runContext.render(createdAtMin).as(String.class).orElse(null);
            if (createdAtMinValue != null) {
                queryParams.append("&created_at_min=").append(createdAtMinValue);
            }
        }
        
        if (createdAtMax != null) {
            String createdAtMaxValue = runContext.render(createdAtMax).as(String.class).orElse(null);
            if (createdAtMaxValue != null) {
                queryParams.append("&created_at_max=").append(createdAtMaxValue);
            }
        }
        
        if (updatedAtMin != null) {
            String updatedAtMinValue = runContext.render(updatedAtMin).as(String.class).orElse(null);
            if (updatedAtMinValue != null) {
                queryParams.append("&updated_at_min=").append(updatedAtMinValue);
            }
        }
        
        if (updatedAtMax != null) {
            String updatedAtMaxValue = runContext.render(updatedAtMax).as(String.class).orElse(null);
            if (updatedAtMaxValue != null) {
                queryParams.append("&updated_at_max=").append(updatedAtMaxValue);
            }
        }
        
        if (pageInfo != null) {
            String pageInfoValue = runContext.render(pageInfo).as(String.class).orElse(null);
            if (pageInfoValue != null) {
                queryParams.append("&page_info=").append(pageInfoValue);
            }
        }
        
        pathBuilder.append(queryParams.toString());

        URI uri = buildApiUrl(runContext, pathBuilder.toString());
        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri);

        runContext.logger().debug("Listing products from Shopify API: {}", uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> productsData = (List<Map<String, Object>>) responseData.get("products");
        
        if (productsData == null) {
            productsData = List.of();
        }
        
        List<Product> products = productsData.stream()
            .map(productData -> OBJECT_MAPPER.convertValue(productData, Product.class))
            .toList();

        runContext.logger().info("Retrieved {} products from Shopify", products.size());

        return Output.builder()
            .products(products)
            .count(products.size())
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "List of products",
        description = "The retrieved products from Shopify"
        )
        private final List<Product> products;

        @Schema(
        title = "Number of products",
        description = "Total number of products retrieved"
        )
        private final Integer count;
    }
}