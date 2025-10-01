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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.net.URI;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Delete a customer from Shopify store",
    description = "Permanently delete a customer from your Shopify store. Note: This action cannot be undone."
)
@Plugin(
    examples = {
        @Example(
        title = "Delete customer by ID",
        full = true,
        code = """
        id: shopify_delete_customer
        namespace: company.team

        tasks:
          - id: delete_customer
            type: io.kestra.plugin.shopify.customers.DeleteCustomer
            storeDomain: my-store.myshopify.com
            accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            customerId: 123456789
        """
        )
    }
)
public class DeleteCustomer extends AbstractShopifyTask implements RunnableTask<DeleteCustomer.Output> {

    @Schema(
        title = "Customer ID",
        description = "The ID of the customer to delete"
    )
    private Property<Long> customerId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = buildHttpClient(runContext);
        Long customerIdValue = runContext.render(customerId).as(Long.class)
        .orElseThrow(() -> new IllegalArgumentException("Customer ID is required"));

        URI uri = buildApiUrl(runContext, "/customers/" + customerIdValue + ".json");
        HttpRequest request = buildAuthenticatedRequest(runContext, "DELETE", uri);

        runContext.logger().debug("Deleting customer {} from Shopify API: {}", customerIdValue, uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // For DELETE requests, Shopify returns 200 with empty body on success
        if (response.statusCode() != 200) {
        String errorBody = response.body() != null ? response.body() : "Unknown error";
        throw new RuntimeException(String.format("Failed to delete customer with status %d: %s", 
            response.statusCode(), errorBody));
        }

        runContext.logger().info("Successfully deleted customer (ID: {}) from Shopify", customerIdValue);

        return Output.builder()
        .customerId(customerIdValue)
        .deleted(true)
        .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "Deleted customer ID",
        description = "The ID of the customer that was deleted"
        )
        private final Long customerId;

        @Schema(
        title = "Deletion status",
        description = "Whether the customer was successfully deleted"
        )
        private final Boolean deleted;
    }
