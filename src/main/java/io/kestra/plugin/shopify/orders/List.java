package io.kestra.plugin.shopify.orders;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.shopify.AbstractShopifyTask;
import io.kestra.plugin.shopify.models.Order;
import io.kestra.core.models.tasks.common.FetchType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import io.kestra.core.serializers.FileSerde;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.stream.Collectors;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "List Shopify orders",
    description = "Retrieves orders through the Shopify Admin API with optional filters (status, financial, fulfillment, date ranges). Supports fetch modes: FETCH (default), FETCH_ONE (first only), STORE (stream to storage URI). Limit honors Shopify cap."
)
    @Plugin(
        examples = {
            @Example(
                title = "List all orders",
                full = true,
            code = """
                id: shopify_list_orders
                namespace: company.team
                
                tasks:
                  - id: list_orders
                    type: io.kestra.plugin.shopify.orders.List
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                """
            ),
            @Example(
            title = "List orders with filtering",
            full = true,
            code = """
                id: shopify_list_orders_filtered
                namespace: company.team
                
                tasks:
                  - id: list_orders
                    type: io.kestra.plugin.shopify.orders.List
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                    limit: 50
                    status: "any"
                    financialStatus: "paid"
                """
        ),
        @Example(
            title = "Stream orders to storage",
            full = true,
            code = """
                id: shopify_list_orders_store
                namespace: company.team
                
                tasks:
                  - id: list_orders
                    type: io.kestra.plugin.shopify.orders.List
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                    fetchType: STORE
                    limit: 200
                """
        )
    }
)
public class List extends AbstractShopifyTask implements RunnableTask<List.Output> {

    @Schema(
        title = "Fetch type",
        description = "Controls result handling: FETCH (default), FETCH_ONE, or STORE"
    )
    @Builder.Default
    private Property<FetchType> fetchType = Property.ofValue(FetchType.FETCH);

    @Schema(
        title = "Limit",
        description = "Maximum number of orders to retrieve (1-250)"
    )
    private Property<Integer> limit;

    @Schema(
        title = "Since ID",
        description = "Retrieve orders created after this Shopify order ID"
    )
    private Property<Long> sinceId;

    @Schema(
        title = "Status filter",
        description = "Order status filter: open, closed, cancelled, or any"
    )
    private Property<String> status;

    @Schema(
        title = "Financial status filter", 
        description = "Financial status filter (e.g., paid, pending, refunded)"
    )
    private Property<String> financialStatus;

    @Schema(
        title = "Fulfillment status filter",
        description = "Fulfillment status filter (e.g., shipped, partial, unshipped)"
    )
    private Property<String> fulfillmentStatus;

    @Schema(
        title = "Created at min",
        description = "Return orders created at or after this ISO-8601 timestamp"
    )
    private Property<String> createdAtMin;

    @Schema(
        title = "Created at max",
        description = "Return orders created at or before this ISO-8601 timestamp"
    )
    private Property<String> createdAtMax;

    @Schema(
        title = "Updated at min",
        description = "Return orders updated at or after this ISO-8601 timestamp"
    )
    private Property<String> updatedAtMin;

    @Schema(
        title = "Updated at max", 
        description = "Return orders updated at or before this ISO-8601 timestamp"
    )
    private Property<String> updatedAtMax;

    @Override
    public Output run(RunContext runContext) throws Exception {
        try (HttpClient client = HttpClient.builder().runContext(runContext).build()) {
        
        // Build query parameters
        java.util.List<String> queryParams = new ArrayList<>();
        
        runContext.render(limit).as(Integer.class).ifPresent(rLimit -> 
            queryParams.add("limit=" + rLimit));
        
        runContext.render(sinceId).as(Long.class).ifPresent(rSinceId -> 
            queryParams.add("since_id=" + rSinceId));
        
        runContext.render(status).as(String.class).ifPresent(rStatus -> 
            queryParams.add("status=" + rStatus));
        
        runContext.render(financialStatus).as(String.class).ifPresent(rFinancialStatus -> 
            queryParams.add("financial_status=" + rFinancialStatus));
        
        runContext.render(fulfillmentStatus).as(String.class).ifPresent(rFulfillmentStatus -> 
            queryParams.add("fulfillment_status=" + rFulfillmentStatus));
        
        runContext.render(createdAtMin).as(String.class).ifPresent(rCreatedAtMin -> 
            queryParams.add("created_at_min=" + rCreatedAtMin));
        
        runContext.render(createdAtMax).as(String.class).ifPresent(rCreatedAtMax -> 
            queryParams.add("created_at_max=" + rCreatedAtMax));
        
        runContext.render(updatedAtMin).as(String.class).ifPresent(rUpdatedAtMin -> 
            queryParams.add("updated_at_min=" + rUpdatedAtMin));
        
        runContext.render(updatedAtMax).as(String.class).ifPresent(rUpdatedAtMax -> 
            queryParams.add("updated_at_max=" + rUpdatedAtMax));

        String path = "/orders.json";
        if (!queryParams.isEmpty()) {
            path += "?" + String.join("&", queryParams);
        }

        URI uri = buildApiUrl(runContext, path);
        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri, null);

        runContext.logger().debug("Listing orders from Shopify API: {}", uri);
        
            handleRateLimit(runContext);
            HttpResponse<String> response = client.request(request, String.class);
            Map<String, Object> responseData = parseResponse(response);
        
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> ordersData = (java.util.List<Map<String, Object>>) responseData.get("orders");
            
            if (ordersData == null) {
                ordersData = new ArrayList<>();
            }
            
            java.util.List<Order> orders = ordersData.stream()
                .map(orderData -> JacksonMapper.ofJson().convertValue(orderData, Order.class))
                .toList();

            runContext.logger().info("Retrieved {} orders from Shopify", orders.size());

            // Handle fetchType properly according to maintainer feedback
            FetchType rFetchType = runContext.render(fetchType).as(FetchType.class).orElse(FetchType.FETCH);
            
            switch (rFetchType) {
                case FETCH_ONE:
                    if (orders.isEmpty()) {
                        return Output.builder().orders(java.util.Collections.emptyList()).count(0).build();
                    }
                    return Output.builder().orders(java.util.List.of(orders.get(0))).count(1).build();
                case FETCH:
                    return Output.builder().orders(orders).count(orders.size()).build();
                case STORE:
                    java.io.File tempFile = runContext.workingDir().createTempFile(".ion").toFile();
                    try (var output = new java.io.BufferedWriter(new java.io.FileWriter(tempFile), io.kestra.core.serializers.FileSerde.BUFFER_SIZE)) {
                        reactor.core.publisher.Flux<Order> orderFlux = reactor.core.publisher.Flux.fromIterable(orders);
                        Long count = io.kestra.core.serializers.FileSerde.writeAll(output, orderFlux).block();
                        URI storedUri = runContext.storage().putFile(tempFile);
                        return Output.builder().count(count.intValue()).uri(storedUri).build();
                    }
                default:
                    return Output.builder().orders(orders).count(orders.size()).build();
            }
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Orders",
            description = "Orders retrieved when fetchType is FETCH or FETCH_ONE"
        )
        private final java.util.List<Order> orders;
        
        @Schema(
            title = "Count",
            description = "Number of orders returned or written"
        )
        private final Integer count;
        
        @Schema(
            title = "URI",
            description = "Storage URI written when fetchType is STORE"
        )
        private final URI uri;
    }
}
