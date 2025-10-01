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
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get a specific customer from Shopify store",
    description = "Retrieve detailed information about a specific customer by their ID."
)
@Plugin(
    examples = {
        @Example(
        title = "Get customer by ID",
        full = true,
        code = """
        id: shopify_get_customer
        namespace: company.team

        tasks:
          - id: get_customer
            type: io.kestra.plugin.shopify.customers.GetCustomer
            storeDomain: my-store.myshopify.com
            accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            customerId: 123456789
        """
        )
    }
)
public class GetCustomer extends AbstractShopifyTask implements RunnableTask<GetCustomer.Output> {

    @Schema(
        title = "Customer ID",
        description = "The ID of the customer to retrieve"
    )
    private Property<Long> customerId;

    @Schema(
        title = "Fields to include",
        description = "Comma-separated list of fields to include in the response"
    )
    private Property<String> fields;

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = buildHttpClient(runContext);
        Long customerIdValue = runContext.render(customerId).as(Long.class)
        .orElseThrow(() -> new IllegalArgumentException("Customer ID is required"));

        StringBuilder pathBuilder = new StringBuilder("/customers/")
        .append(customerIdValue)
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

        runContext.logger().debug("Getting customer {} from Shopify API: {}", customerIdValue, uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> customerData = (Map<String, Object>) responseData.get("customer");
        
        if (customerData == null) {
        throw new RuntimeException("Customer not found with ID: " + customerIdValue);
        }
        
        Customer customer = OBJECT_MAPPER.convertValue(customerData, Customer.class);

        runContext.logger().info("Retrieved customer '{}' (ID: {}) from Shopify", 
        customer.getEmail(), customer.getId());

        return Output.builder()
        .customer(customer)
        .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "Customer",
        description = "The retrieved customer from Shopify"
        )
        private final Customer customer;
    }
