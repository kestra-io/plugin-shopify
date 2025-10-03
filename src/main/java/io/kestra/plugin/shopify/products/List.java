package io.kestra.plugin.shopify.products;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.shopify.AbstractShopifyTask;
import io.kestra.plugin.shopify.models.Product;
import io.kestra.plugin.shopify.models.FetchType;
import io.kestra.plugin.shopify.models.ProductStatus;
import io.kestra.plugin.shopify.models.PublishedStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
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
                    type: io.kestra.plugin.shopify.products.List
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
                    type: io.kestra.plugin.shopify.products.List
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                    limit: 50
                    status: ACTIVE
                    publishedStatus: PUBLISHED
                """
        )
    }
)
public class List extends AbstractShopifyTask implements RunnableTask<List.Output> {

    @Schema(
        title = "Fetch type",
        description = "How to fetch the data (FETCH_ONE, FETCH, STORE)"
    )
    @Builder.Default
    private Property<FetchType> fetchType = Property.ofValue(FetchType.FETCH);

    @Schema(
        title = "Limit",
        description = "Maximum number of products to retrieve"
    )
    private Property<Integer> limit;

    @Schema(
        title = "Since ID", 
        description = "Retrieve products after this ID"
    )
    private Property<Long> sinceId;

    @Schema(
        title = "Product status filter",
        description = "Filter products by status"
    )
    private Property<ProductStatus> status;

    @Schema(
        title = "Published status filter",
        description = "Filter by published status"
    )
    private Property<PublishedStatus> publishedStatus;

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
        title = "Handle filter",
        description = "Filter products by handle"
    )
    private Property<String> handle;

    @Schema(
        title = "Created at min",
        description = "Retrieve products created after this date"
    )
    private Property<String> createdAtMin;

    @Schema(
        title = "Created at max",
        description = "Retrieve products created before this date"
    )
    private Property<String> createdAtMax;

    @Schema(
        title = "Updated at min",
        description = "Retrieve products updated after this date"
    )
    private Property<String> updatedAtMin;

    @Schema(
        title = "Updated at max",
        description = "Retrieve products updated before this date"
    )
    private Property<String> updatedAtMax;

    @Override
    public Output run(RunContext runContext) throws Exception {
        var client = runContext.http().client();
        
        // Build query parameters
        List<String> queryParams = new ArrayList<>();
        
        if (limit != null) {
            Integer limitValue = runContext.render(limit).as(Integer.class).orElse(null);
            if (limitValue != null) {
                queryParams.add("limit=" + limitValue);
            }
        }
        
        if (sinceId != null) {
            Long sinceIdValue = runContext.render(sinceId).as(Long.class).orElse(null);
            if (sinceIdValue != null) {
                queryParams.add("since_id=" + sinceIdValue);
            }
        }
        
        if (status != null) {
            ProductStatus statusValue = runContext.render(status).as(ProductStatus.class).orElse(null);
            if (statusValue != null) {
                queryParams.add("status=" + statusValue.name().toLowerCase());
            }
        }
        
        if (publishedStatus != null) {
            PublishedStatus publishedValue = runContext.render(publishedStatus).as(PublishedStatus.class).orElse(null);
            if (publishedValue != null) {
                queryParams.add("published_status=" + publishedValue.name().toLowerCase());
            }
        }
        
        if (productType != null) {
            String productTypeValue = runContext.render(productType).as(String.class).orElse(null);
            if (productTypeValue != null) {
                queryParams.add("product_type=" + productTypeValue);
            }
        }
        
        if (vendor != null) {
            String vendorValue = runContext.render(vendor).as(String.class).orElse(null);
            if (vendorValue != null) {
                queryParams.add("vendor=" + vendorValue);
            }
        }
        
        if (handle != null) {
            String handleValue = runContext.render(handle).as(String.class).orElse(null);
            if (handleValue != null) {
                queryParams.add("handle=" + handleValue);
            }
        }
        
        if (createdAtMin != null) {
            String createdAtMinValue = runContext.render(createdAtMin).as(String.class).orElse(null);
            if (createdAtMinValue != null) {
                queryParams.add("created_at_min=" + createdAtMinValue);
            }
        }
        
        if (createdAtMax != null) {
            String createdAtMaxValue = runContext.render(createdAtMax).as(String.class).orElse(null);
            if (createdAtMaxValue != null) {
                queryParams.add("created_at_max=" + createdAtMaxValue);
            }
        }
        
        if (updatedAtMin != null) {
            String updatedAtMinValue = runContext.render(updatedAtMin).as(String.class).orElse(null);
            if (updatedAtMinValue != null) {
                queryParams.add("updated_at_min=" + updatedAtMinValue);
            }
        }
        
        if (updatedAtMax != null) {
            String updatedAtMaxValue = runContext.render(updatedAtMax).as(String.class).orElse(null);
            if (updatedAtMaxValue != null) {
                queryParams.add("updated_at_max=" + updatedAtMaxValue);
            }
        }

        String path = "/products.json";
        if (!queryParams.isEmpty()) {
            path += "?" + String.join("&", queryParams);
        }

        URI uri = buildApiUrl(runContext, path);
        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri, null);

        runContext.logger().debug("Listing products from Shopify API: {}", uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> productsData = (List<Map<String, Object>>) responseData.get("products");
        
        if (productsData == null) {
            productsData = new ArrayList<>();
        }
        
        List<Product> products = productsData.stream()
            .map(productData -> JacksonMapper.ofJson().convertValue(productData, Product.class))
            .toList();

        runContext.logger().info("Retrieved {} products from Shopify", products.size());

        // Handle fetchType properly according to maintainer feedback
        FetchType fetchTypeValue = runContext.render(fetchType).as(FetchType.class).orElse(FetchType.FETCH);
        
        switch (fetchTypeValue) {
            case FETCH_ONE:
                if (products.isEmpty()) {
                    return Output.builder().products(List.of()).count(0).build();
                }
                return Output.builder().products(List.of(products.get(0))).count(1).build();
            case FETCH:
                return Output.builder().products(products).count(products.size()).build();
            case STORE:
                // TODO: Implement storage functionality when needed
                // For now, return as FETCH
                return Output.builder().products(products).count(products.size()).build();
            default:
                return Output.builder().products(products).count(products.size()).build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Products",
            description = "List of products retrieved from Shopify"
        )
        private final List<Product> products;
        
        @Schema(
            title = "Count",
            description = "Number of products retrieved"
        )
        private final Integer count;
    }
}