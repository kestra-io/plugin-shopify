package io.kestra.plugin.shopify.customers;

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
import io.kestra.plugin.shopify.models.Customer;
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
    title = "List customers from Shopify store",
    description = "Retrieve a list of customers from your Shopify store with optional filtering and pagination."
)
@Plugin(
    examples = {
        @Example(
        title = "List all customers",
        full = true,
        code = """
            id: shopify_list_customers
            namespace: company.team

            tasks:
              - id: list_customers
                type: io.kestra.plugin.shopify.customers.ListCustomers
                storeDomain: my-store.myshopify.com
                accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            """
        ),
        @Example(
        title = "List customers with filtering",
        full = true,
        code = """
            id: shopify_list_customers_filtered
            namespace: company.team

            tasks:
              - id: list_customers
                type: io.kestra.plugin.shopify.customers.ListCustomers
                storeDomain: my-store.myshopify.com
                accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                limit: 50
                createdAtMin: "2023-01-01T00:00:00Z"
            """
        )
    }
)
public class ListCustomers extends AbstractShopifyTask implements RunnableTask<ListCustomers.Output> {

    @Schema(
        title = "Number of customers to retrieve",
        description = "Maximum number of customers to return (1-250, default: 50)"
    )
    @Builder.Default
    private Property<Integer> limit = Property.of(50);

    @Schema(
        title = "Created after date",
        description = "Show customers created after this date (ISO 8601 format)"
    )
    private Property<String> createdAtMin;

    @Schema(
        title = "Created before date",
        description = "Show customers created before this date (ISO 8601 format)"
    )
    private Property<String> createdAtMax;

    @Schema(
        title = "Updated after date",
        description = "Show customers updated after this date (ISO 8601 format)"
    )
    private Property<String> updatedAtMin;

    @Schema(
        title = "Updated before date",
        description = "Show customers updated before this date (ISO 8601 format)"
    )
    private Property<String> updatedAtMax;

    @Schema(
        title = "Fields to include",
        description = "Comma-separated list of fields to include in the response"
    )
    private Property<String> fields;

    @Schema(
        title = "Page info for pagination",
        description = "Page info parameter for cursor-based pagination"
    )
    private Property<String> pageInfo;

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = buildHttpClient(runContext);
        StringBuilder pathBuilder = new StringBuilder("/customers.json");
        
        // Build query parameters
        StringBuilder queryParams = new StringBuilder();
        
        Integer limitValue = runContext.render(limit).as(Integer.class).orElse(50);
        queryParams.append("?limit=").append(Math.min(Math.max(limitValue, 1), 250));
        
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
        
        if (fields != null) {
            String fieldsValue = runContext.render(fields).as(String.class).orElse(null);
            if (fieldsValue != null) {
                queryParams.append("&fields=").append(fieldsValue);
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

        runContext.logger().debug("Listing customers from Shopify API: {}", uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> customersData = (List<Map<String, Object>>) responseData.get("customers");
        
        if (customersData == null) {
            customersData = List.of();
        }
        
        List<Customer> customers = customersData.stream()
            .map(customerData -> OBJECT_MAPPER.convertValue(customerData, Customer.class))
            .toList();

        runContext.logger().info("Retrieved {} customers from Shopify", customers.size());

        return Output.builder()
            .customers(customers)
            .count(customers.size())
            .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "List of customers",
        description = "The retrieved customers from Shopify"
        )
        private final List<Customer> customers;

        @Schema(
        title = "Number of customers",
        description = "Total number of customers retrieved"
        )
        private final Integer count;
    }
