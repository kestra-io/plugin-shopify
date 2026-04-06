package io.kestra.plugin.shopify.customers;

import java.net.URI;
import java.util.Map;

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
import io.kestra.plugin.shopify.models.Customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import io.kestra.core.models.annotations.PluginProperty;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Fetch Shopify customer by ID",
    description = "Retrieves a customer through the Shopify Admin API using the store domain and access token. Throws an error if the ID is not found."
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
                    type: io.kestra.plugin.shopify.customers.Get
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                    customerId: 12345
                """
        )
    }
)
public class Get extends AbstractShopifyTask implements RunnableTask<Get.Output> {

    @Schema(
        title = "Customer ID",
        description = "Shopify customer ID to retrieve"
    )
    @NotNull
    @PluginProperty(group = "main")
    private Property<Long> customerId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        try (HttpClient client = HttpClient.builder().runContext(runContext).build()) {
            Long rCustomerId = runContext.render(customerId).as(Long.class).orElseThrow();

            URI uri = buildApiUrl(runContext, "/customers/" + rCustomerId + ".json");
            HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri, null);

            runContext.logger().debug("Getting customer {} from Shopify API: {}", rCustomerId, uri);

            handleRateLimit(runContext);
            HttpResponse<String> response = client.request(request, String.class);
            Map<String, Object> responseData = parseResponse(response);

            @SuppressWarnings("unchecked")
            Map<String, Object> customerData = (Map<String, Object>) responseData.get("customer");

            if (customerData == null) {
                throw new RuntimeException("Customer not found: " + rCustomerId);
            }

            Customer customer = JacksonMapper.ofJson().convertValue(customerData, Customer.class);

            runContext.logger().info(
                "Retrieved customer '{}' (ID: {}) from Shopify",
                customer.getEmail(), customer.getId()
            );

            return Output.builder()
                .customer(customer)
                .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Retrieved customer",
            description = "Customer object returned by Shopify"
        )
        private final Customer customer;
    }
}
