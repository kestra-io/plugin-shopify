package io.kestra.plugin.shopify.orders;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.shopify.AbstractShopifyTask;
import io.kestra.plugin.shopify.models.Order;
import io.kestra.plugin.shopify.models.FetchType;
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
    title = "List orders from Shopify store",
    description = "Retrieve a list of orders from your Shopify store with optional filtering and pagination."
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
        description = "Maximum number of orders to retrieve"
    )
    private Property<Integer> limit;

    @Schema(
        title = "Since ID",
        description = "Retrieve orders after this ID"
    )
    private Property<Long> sinceId;

    @Schema(
        title = "Status filter",
        description = "Filter orders by status (open, closed, cancelled, any)"
    )
    private Property<String> status;

    @Schema(
        title = "Financial status filter", 
        description = "Filter orders by financial status"
    )
    private Property<String> financialStatus;

    @Schema(
        title = "Fulfillment status filter",
        description = "Filter orders by fulfillment status"
    )
    private Property<String> fulfillmentStatus;

    @Schema(
        title = "Created at min",
        description = "Retrieve orders created after this date"
    )
    private Property<String> createdAtMin;

    @Schema(
        title = "Created at max",
        description = "Retrieve orders created before this date"
    )
    private Property<String> createdAtMax;

    @Schema(
        title = "Updated at min",
        description = "Retrieve orders updated after this date"
    )
    private Property<String> updatedAtMin;

    @Schema(
        title = "Updated at max", 
        description = "Retrieve orders updated before this date"
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
            String statusValue = runContext.render(status).as(String.class).orElse(null);
            if (statusValue != null) {
                queryParams.add("status=" + statusValue);
            }
        }
        
        if (financialStatus != null) {
            String financialStatusValue = runContext.render(financialStatus).as(String.class).orElse(null);
            if (financialStatusValue != null) {
                queryParams.add("financial_status=" + financialStatusValue);
            }
        }
        
        if (fulfillmentStatus != null) {
            String fulfillmentStatusValue = runContext.render(fulfillmentStatus).as(String.class).orElse(null);
            if (fulfillmentStatusValue != null) {
                queryParams.add("fulfillment_status=" + fulfillmentStatusValue);
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

        String path = "/orders.json";
        if (!queryParams.isEmpty()) {
            path += "?" + String.join("&", queryParams);
        }

        URI uri = buildApiUrl(runContext, path);
        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri, null);

        runContext.logger().debug("Listing orders from Shopify API: {}", uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ordersData = (List<Map<String, Object>>) responseData.get("orders");
        
        if (ordersData == null) {
            ordersData = new ArrayList<>();
        }
        
        List<Order> orders = ordersData.stream()
            .map(orderData -> JacksonMapper.ofJson().convertValue(orderData, Order.class))
            .toList();

        runContext.logger().info("Retrieved {} orders from Shopify", orders.size());

        // Handle fetchType properly according to maintainer feedback
        FetchType fetchTypeValue = runContext.render(fetchType).as(FetchType.class).orElse(FetchType.FETCH);
        
        switch (fetchTypeValue) {
            case FETCH_ONE:
                if (orders.isEmpty()) {
                    return Output.builder().orders(List.of()).count(0).build();
                }
                return Output.builder().orders(List.of(orders.get(0))).count(1).build();
            case FETCH:
                return Output.builder().orders(orders).count(orders.size()).build();
            case STORE:
                // TODO: Implement storage functionality when needed
                // For now, return as FETCH
                return Output.builder().orders(orders).count(orders.size()).build();
            default:
                return Output.builder().orders(orders).count(orders.size()).build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Orders",
            description = "List of orders retrieved from Shopify"
        )
        private final List<Order> orders;
        
        @Schema(
            title = "Count",
            description = "Number of orders retrieved"
        )
        private final Integer count;
    }
}