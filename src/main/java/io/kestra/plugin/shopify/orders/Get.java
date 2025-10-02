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
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get a specific order from Shopify store",
    description = "Retrieve detailed information about a specific order by its ID."
)
@Plugin(
    examples = {
        @Example(
        title = "Get order by ID",
        full = true,
        code = """
                id: shopify_get_order
                namespace: company.team

                tasks:
                  - id: get_order
            type: io.kestra.plugin.shopify.orders.Get
            storeDomain: my-store.myshopify.com
            accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            orderId: 123456789
        """
        )
    }
)
public class Get extends AbstractShopifyTask implements RunnableTask<Get.Output> {

    @Schema(
        title = "Order ID",
        description = "The ID of the order to retrieve"
    )
    private Property<Long> orderId;

    @Schema(
        title = "Fields to include",
        description = "Comma-separated list of fields to include in the response"
    )
    private Property<String> fields;

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = buildHttpClient(runContext);
        Long orderIdValue = runContext.render(orderId).as(Long.class)
        .orElseThrow(() -> new IllegalArgumentException("Order ID is required"));

        StringBuilder pathBuilder = new StringBuilder("/orders/")
        .append(orderIdValue)
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

        runContext.logger().debug("Getting order {} from Shopify API: {}", orderIdValue, uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> orderData = (Map<String, Object>) responseData.get("order");
        
        if (orderData == null) {
        throw new RuntimeException("Order not found with ID: " + orderIdValue);
        }
        
        Order order = OBJECT_MAPPER.convertValue(orderData, Order.class);

        runContext.logger().info("Retrieved order '{}' (ID: {}) from Shopify", 
        order.getName(), order.getId());

        return Output.builder()
        .order(order)
        .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "Order",
        description = "The retrieved order from Shopify"
        )
        private final Order order;
    }
