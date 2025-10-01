package io.kestra.plugin.shopify.triggers;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.conditions.ConditionContext;
import io.kestra.core.models.executions.Execution;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.triggers.*;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.shopify.models.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Trigger on Shopify order creation",
    description = "This trigger will fire when a new order is created in your Shopify store. " +
                  "It implements polling-based detection by checking for new orders at regular intervals. " +
                  "For real-time notifications, consider setting up Shopify webhooks separately."
)
@Plugin(
    examples = {
        @Example(
            title = "Trigger on new orders",
            full = true,
            code = """
                id: shopify_order_trigger
                namespace: company.team

                triggers:
                  - id: order_created
                    type: io.kestra.plugin.shopify.triggers.OrderCreated
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                    interval: PT5M

                tasks:
                  - id: process_order
                    type: io.kestra.core.tasks.log.Log
                    message: "New order created: {{ trigger.order.name }} for {{ trigger.order.totalPrice }}"
                """
        ),
        @Example(
            title = "Trigger with order filtering",
            full = true,
            code = """
                id: shopify_paid_order_trigger
                namespace: company.team

                triggers:
                  - id: paid_order_created
                    type: io.kestra.plugin.shopify.triggers.OrderCreated
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                    interval: PT2M
                    financialStatus: paid

                tasks:
                  - id: fulfill_order
                    type: io.kestra.core.tasks.log.Log
                    message: "Paid order ready for fulfillment: {{ trigger.order.name }}"
                """
        )
    }
)
public class OrderCreated extends AbstractTrigger implements PollingTriggerInterface, TriggerOutput<OrderCreated.Output> {

    @Schema(
        title = "Shopify store domain",
        description = "The domain of your Shopify store (e.g., 'my-store.myshopify.com')"
    )
    private Property<String> storeDomain;

    @Schema(
        title = "Admin API access token",
        description = "Private app access token for Shopify Admin API"
    )
    private Property<String> accessToken;

    @Schema(
        title = "API version",
        description = "Shopify Admin API version to use",
        example = "2023-10"
    )
    @Builder.Default
    private Property<String> apiVersion = Property.of("2023-10");

    @Schema(
        title = "Polling interval",
        description = "How often to check for new orders"
    )
    @Builder.Default
    private Property<Duration> interval = Property.of(Duration.ofMinutes(5));

    @Schema(
        title = "Financial status filter",
        description = "Only trigger for orders with this financial status (optional)"
    )
    private Property<String> financialStatus;

    @Schema(
        title = "Fulfillment status filter",
        description = "Only trigger for orders with this fulfillment status (optional)"
    )
    private Property<String> fulfillmentStatus;

    @Schema(
        title = "Maximum orders to process",
        description = "Maximum number of new orders to process in each polling cycle (1-50, default: 10)"
    )
    @Builder.Default
    private Property<Integer> maxOrders = Property.of(10);

    @Override
    public Optional<Execution> evaluate(ConditionContext conditionContext, TriggerContext context) throws Exception {
        RunContext runContext = conditionContext.getRunContext();
        Logger logger = runContext.logger();

        logger.debug("Checking for new Shopify orders");

        try {
            // Get the last execution time from trigger state
            ZonedDateTime lastExecutionTime = context.getState() != null ? 
                (ZonedDateTime) context.getState() : 
                ZonedDateTime.now().minusMinutes(10); // Default to 10 minutes ago

            // Use Shopify API to get new orders since last check
            io.kestra.plugin.shopify.orders.ListOrders listOrdersTask = io.kestra.plugin.shopify.orders.ListOrders.builder()
                .storeDomain(storeDomain)
                .accessToken(accessToken)
                .apiVersion(apiVersion)
                .createdAtMin(Property.of(lastExecutionTime.toString()))
                .financialStatus(financialStatus)
                .fulfillmentStatus(fulfillmentStatus)
                .limit(maxOrders)
                .build();

            io.kestra.plugin.shopify.orders.ListOrders.Output result = listOrdersTask.run(runContext);

            if (result.getOrders().isEmpty()) {
                logger.debug("No new orders found");
                return Optional.empty();
            }

            // Find the most recent order to update our state
            ZonedDateTime newestOrderTime = result.getOrders().stream()
                .map(order -> order.getCreatedAt())
                .filter(java.util.Objects::nonNull)
                .map(instant -> instant.atZone(java.time.ZoneId.systemDefault()))
                .max(ZonedDateTime::compareTo)
                .orElse(ZonedDateTime.now());

            // Process each new order
            for (Order order : result.getOrders()) {
                logger.info("New order detected: {} (ID: {}) - ${}", 
                    order.getName(), order.getId(), order.getTotalPrice());
            }

            // Create execution with the orders as output
            return Optional.of(TriggerService.generateExecution(
                this,
                conditionContext,
                context.withState(newestOrderTime),
                Output.builder()
                    .orders(result.getOrders())
                    .count(result.getCount())
                    .order(result.getOrders().get(0)) // First order for backward compatibility
                    .build()
            ));

        } catch (Exception e) {
            logger.error("Error checking for new Shopify orders", e);
            throw e;
        }
    }

    @Override
    public Duration getInterval() {
        return interval != null ? interval.getValue() : Duration.ofMinutes(5);
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "New orders",
            description = "List of new orders detected"
        )
        private final java.util.List<Order> orders;

        @Schema(
            title = "Number of new orders",
            description = "Total number of new orders detected"
        )
        private final Integer count;

        @Schema(
            title = "First order",
            description = "The first new order (for backward compatibility)"
        )
        private final Order order;
    }
}