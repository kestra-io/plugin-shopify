package io.kestra.plugin.shopify.customers;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.shopify.AbstractShopifyTask;
import io.kestra.plugin.shopify.models.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get a specific customer from Shopify store",
    description = "Retrieve a customer by their ID."
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
        description = "The ID of the customer to retrieve"
    )
    @NotNull
    private Property<Long> customerId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        var client = runContext.http().client();
        
        Long customerIdValue = runContext.render(customerId).as(Long.class).orElseThrow();
        
        URI uri = buildApiUrl(runContext, "/customers/" + customerIdValue + ".json");
        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri, null);

        runContext.logger().debug("Getting customer {} from Shopify API: {}", customerIdValue, uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> customerData = (Map<String, Object>) responseData.get("customer");
        
        if (customerData == null) {
            throw new RuntimeException("Customer not found: " + customerIdValue);
        }
        
        Customer customer = JacksonMapper.ofJson().convertValue(customerData, Customer.class);

        runContext.logger().info("Retrieved customer '{}' (ID: {}) from Shopify", 
            customer.getEmail(), customer.getId());

        return Output.builder()
            .customer(customer)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Retrieved customer",
            description = "The customer retrieved from Shopify"
        )
        private final Customer customer;
    }
}
