package io.kestra.plugin.shopify.orders;

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
import io.kestra.plugin.shopify.models.Order;
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
            type: io.kestra.plugin.shopify.orders.ListOrders
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
            type: io.kestra.plugin.shopify.orders.ListOrders
            storeDomain: my-store.myshopify.com
            accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            limit: 50
            status: open
            financialStatus: paid
        """
        )
    }
)
public class ListOrders extends AbstractShopifyTask implements RunnableTask<ListOrders.Output> {

    @Schema(
        title = "Number of orders to retrieve",
        description = "Maximum number of orders to return (1-250, default: 50)"
    )
    @Builder.Default
    private Property<Integer> limit = Property.of(50);

    @Schema(
        title = "Order status filter",
        description = "Filter orders by status (open, closed, cancelled, any)"
    )
    private Property<String> status;

    @Schema(
        title = "Financial status filter",
        description = "Filter orders by financial status (authorized, pending, paid, partially_paid, refunded, voided, partially_refunded, any)"
    )
    private Property<String> financialStatus;

    @Schema(
        title = "Fulfillment status filter",
        description = "Filter orders by fulfillment status (shipped, partial, unshipped, any)"
    )
    private Property<String> fulfillmentStatus;

    @Schema(
        title = "Created after date",
        description = "Show orders created after this date (ISO 8601 format)"
    )
    private Property<String> createdAtMin;

    @Schema(
        title = "Created before date",
        description = "Show orders created before this date (ISO 8601 format)"
    )
    private Property<String> createdAtMax;

    @Schema(
        title = "Updated after date",
        description = "Show orders updated after this date (ISO 8601 format)"
    )
    private Property<String> updatedAtMin;

    @Schema(
        title = "Updated before date",
        description = "Show orders updated before this date (ISO 8601 format)"
    )
    private Property<String> updatedAtMax;

    @Schema(
        title = "Processed after date",
        description = "Show orders processed after this date (ISO 8601 format)"
    )
    private Property<String> processedAtMin;

    @Schema(
        title = "Processed before date",
        description = "Show orders processed before this date (ISO 8601 format)"
    )
    private Property<String> processedAtMax;

    @Schema(
        title = "Customer ID filter",
        description = "Filter orders by customer ID"
    )
    private Property<Long> customerId;

    @Schema(
        title = "Page info for pagination",
        description = "Page info parameter for cursor-based pagination"
    )
    private Property<String> pageInfo;

    @Schema(
        title = "Fields to include",
        description = "Comma-separated list of fields to include in the response"
    )
    private Property<String> fields;

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = buildHttpClient(runContext);
        StringBuilder pathBuilder = new StringBuilder("/orders.json");
        
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
        
        if (financialStatus != null) {
        String financialStatusValue = runContext.render(financialStatus).as(String.class).orElse(null);
        if (financialStatusValue != null) {
            queryParams.append("&financial_status=").append(financialStatusValue);
        }
        }
        
        if (fulfillmentStatus != null) {
        String fulfillmentStatusValue = runContext.render(fulfillmentStatus).as(String.class).orElse(null);
        if (fulfillmentStatusValue != null) {
            queryParams.append("&fulfillment_status=").append(fulfillmentStatusValue);
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
        
        if (processedAtMin != null) {
        String processedAtMinValue = runContext.render(processedAtMin).as(String.class).orElse(null);
        if (processedAtMinValue != null) {
            queryParams.append("&processed_at_min=").append(processedAtMinValue);
        }
        }
        
        if (processedAtMax != null) {
        String processedAtMaxValue = runContext.render(processedAtMax).as(String.class).orElse(null);
        if (processedAtMaxValue != null) {
            queryParams.append("&processed_at_max=").append(processedAtMaxValue);
        }
        }
        
        if (customerId != null) {
        Long customerIdValue = runContext.render(customerId).as(Long.class).orElse(null);
        if (customerIdValue != null) {
            queryParams.append("&customer_id=").append(customerIdValue);
        }
        }
        
        if (pageInfo != null) {
        String pageInfoValue = runContext.render(pageInfo).as(String.class).orElse(null);
        if (pageInfoValue != null) {
            queryParams.append("&page_info=").append(pageInfoValue);
        }
        }
        
        if (fields != null) {
        String fieldsValue = runContext.render(fields).as(String.class).orElse(null);
        if (fieldsValue != null) {
            queryParams.append("&fields=").append(fieldsValue);
        }
        }
        
        pathBuilder.append(queryParams.toString());

        URI uri = buildApiUrl(runContext, pathBuilder.toString());
        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri);

        runContext.logger().debug("Listing orders from Shopify API: {}", uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ordersData = (List<Map<String, Object>>) responseData.get("orders");
        
        if (ordersData == null) {
        ordersData = List.of();
        }
        
        List<Order> orders = ordersData.stream()
        .map(orderData -> OBJECT_MAPPER.convertValue(orderData, Order.class))
        .toList();

        runContext.logger().info("Retrieved {} orders from Shopify", orders.size());

        return Output.builder()
        .orders(orders)
        .count(orders.size())
        .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "List of orders",
        description = "The retrieved orders from Shopify"
        )
        private final List<Order> orders;

        @Schema(
        title = "Number of orders",
        description = "Total number of orders retrieved"
        )
        private final Integer count;
    }
