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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
    type: io.kestra.plugin.shopify.products.ListProducts
    storeDomain: my-store.myshopify.com
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
"""
        )
    }
)
public class ListProducts extends AbstractShopifyTask implements RunnableTask<ListProducts.Output> {
    
    @Schema(
        title = "Fetch type",
        description = "How to fetch the products"
    )
    @Builder.Default
    @NotNull
    protected Property<FetchType> fetchType = Property.of(FetchType.FETCH);
    
    @Schema(
        title = "Product limit",
        description = "Maximum number of products to return (1-250)"
    )
    protected Property<Integer> limit;

    @Override
    public Output run(RunContext runContext) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        
        java.util.List<String> queryParams = new ArrayList<>();
        
        if (limit != null) {
            Integer limitValue = runContext.render(limit).as(Integer.class).orElse(null);
            if (limitValue != null) {
                queryParams.add("limit=" + limitValue);
            }
        }
        
        String path = "/products.json";
        if (!queryParams.isEmpty()) {
            path += "?" + String.join("&", queryParams);
        }

        URI uri = buildApiUrl(runContext, path);
        java.net.http.HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri, null);

        runContext.logger().debug("Listing products from Shopify API: {}", uri);
        
        handleRateLimit(runContext);
        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> productsData = (java.util.List<Map<String, Object>>) responseData.get("products");
        
        java.util.List<Product> products = productsData.stream()
            .map(productData -> JacksonMapper.ofJson().convertValue(productData, Product.class))
            .collect(Collectors.toList());
            
        FetchType rFetchType = runContext.render(fetchType).as(FetchType.class).orElse(FetchType.FETCH);
        
        runContext.logger().info("Retrieved {} products from Shopify", products.size());
        
        switch (rFetchType) {
            case FETCH_ONE:
                if (products.isEmpty()) {
                    return Output.builder().products(java.util.List.of()).count(0).build();
                }
                return Output.builder().products(java.util.List.of(products.get(0))).count(1).build();
            case STORE:
                java.util.List<String> uris = new ArrayList<>();
                for (Product product : products) {
                    URI storedUri = runContext.storage().putFile(
                        new ByteArrayInputStream(
                            JacksonMapper.ofJson().writeValueAsString(product).getBytes(StandardCharsets.UTF_8)
                        ),
                        "product_" + product.getId() + ".json"
                    );
                    uris.add(storedUri.toString());
                }
                return Output.builder().products(products).count(products.size()).uris(uris).build();
            case FETCH:
            default:
                return Output.builder().products(products).count(products.size()).build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "List of products",
            description = "The retrieved products from Shopify"
        )
        private final java.util.List<Product> products;
        
        @Schema(
            title = "Count",
            description = "Number of products retrieved"
        )
        private final Integer count;
        
        @Schema(
            title = "URIs",
            description = "URIs of stored product files when fetchType is STORE"
        )
        private final java.util.List<String> uris;
    }
}