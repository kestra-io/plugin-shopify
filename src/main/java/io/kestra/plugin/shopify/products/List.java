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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import java.util.Map;
import java.util.stream.Collectors;

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
    private Property<FetchType> fetchType = Property.of(FetchType.FETCH);

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
        HttpClient client = HttpClient.newHttpClient();
        
        // Build query parameters
        java.util.List<String> queryParams = new ArrayList<>();
        
        runContext.render(limit).as(Integer.class).ifPresent(rLimit -> 
            queryParams.add("limit=" + rLimit));
        
        runContext.render(sinceId).as(Long.class).ifPresent(rSinceId -> 
            queryParams.add("since_id=" + rSinceId));
        
        runContext.render(status).as(ProductStatus.class).ifPresent(rStatus -> 
            queryParams.add("status=" + rStatus.name().toLowerCase()));
        
        runContext.render(publishedStatus).as(PublishedStatus.class).ifPresent(rPublished -> 
            queryParams.add("published_status=" + rPublished.name().toLowerCase()));
        
        if (productType != null) {
            String rProductType = runContext.render(productType).as(String.class).orElse(null);
            if (rProductType != null) {
                queryParams.add("product_type=" + rProductType);
            }
        }
        
        runContext.render(vendor).as(String.class).ifPresent(rVendor -> 
            queryParams.add("vendor=" + rVendor));
        
        runContext.render(handle).as(String.class).ifPresent(rHandle -> 
            queryParams.add("handle=" + rHandle));
        
        runContext.render(createdAtMin).as(String.class).ifPresent(rCreatedAtMin -> 
            queryParams.add("created_at_min=" + rCreatedAtMin));
        
        runContext.render(createdAtMax).as(String.class).ifPresent(rCreatedAtMax -> 
            queryParams.add("created_at_max=" + rCreatedAtMax));
        
        runContext.render(updatedAtMin).as(String.class).ifPresent(rUpdatedAtMin -> 
            queryParams.add("updated_at_min=" + rUpdatedAtMin));
        
        runContext.render(updatedAtMax).as(String.class).ifPresent(rUpdatedAtMax -> 
            queryParams.add("updated_at_max=" + rUpdatedAtMax));

        String path = "/products.json";
        if (!queryParams.isEmpty()) {
            path += "?" + String.join("&", queryParams);
        }

        URI uri = buildApiUrl(runContext, path);
        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri, null);

        runContext.logger().debug("Listing products from Shopify API: {}", uri);
        
        handleRateLimit(runContext);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> productsData = (java.util.List<Map<String, Object>>) responseData.get("products");
        
        if (productsData == null) {
            productsData = new ArrayList<>();
        }
        
        java.util.List<Product> products = productsData.stream()
            .map(productData -> JacksonMapper.ofJson().convertValue(productData, Product.class))
            .toList();

        runContext.logger().info("Retrieved {} products from Shopify", products.size());

        // Handle fetchType properly according to maintainer feedback
        FetchType rFetchType = runContext.render(fetchType).as(FetchType.class).orElse(FetchType.FETCH);
        
        switch (rFetchType) {
            case FETCH_ONE:
                if (products.isEmpty()) {
                    return Output.builder().products(java.util.Collections.emptyList()).count(0).build();
                }
                return Output.builder().products(java.util.List.of(products.get(0))).count(1).build();
            case FETCH:
                return Output.builder().products(products).count(products.size()).build();
            case STORE:
                java.io.File tempFile = runContext.workingDir().createTempFile(".ion").toFile();
                try (var output = new java.io.BufferedWriter(new java.io.FileWriter(tempFile), io.kestra.core.serializers.FileSerde.BUFFER_SIZE)) {
                    reactor.core.publisher.Flux<Product> productFlux = reactor.core.publisher.Flux.fromIterable(products);
                    Long count = io.kestra.core.serializers.FileSerde.writeAll(output, productFlux).block();
                    URI storedUri = runContext.storage().putFile(tempFile);
                    return Output.builder().count(count.intValue()).uri(storedUri).build();
                }
            default:
                return Output.builder().products(products).count(products.size()).build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Products",
            description = "List of products retrieved from Shopify. Only populated if using fetchType=FETCH or FETCH_ONE."
        )
        private final java.util.List<Product> products;
        
        @Schema(
            title = "Count",
            description = "Number of products retrieved"
        )
        private final Integer count;
        
        @Schema(
            title = "URI",
            description = "URI of the stored data. Only populated if using fetchType=STORE."
        )
        private final URI uri;
    }
}