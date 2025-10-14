package io.kestra.plugin.shopify.customers;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.shopify.AbstractShopifyTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Delete a customer from Shopify store",
    description = "Delete a customer by their ID."
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
                    type: io.kestra.plugin.shopify.customers.Delete
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                    customerId: 12345
                """
        )
    }
)
public class Delete extends AbstractShopifyTask implements RunnableTask<VoidOutput> {

    @Schema(
        title = "Customer ID",
        description = "The ID of the customer to delete"
    )
    @NotNull
    private Property<Long> customerId;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        Long rCustomerId = runContext.render(customerId).as(Long.class).orElseThrow();
        
        URI uri = buildApiUrl(runContext, "/customers/" + rCustomerId + ".json");
        HttpRequest request = buildAuthenticatedRequest(runContext, "DELETE", uri, null);

        runContext.logger().debug("Deleting customer {} from Shopify API: {}", rCustomerId, uri);
        
        handleRateLimit(runContext);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400) {
            throw new RuntimeException(String.format("Failed to delete customer %d: %s", 
                rCustomerId, response.body()));
        }

        runContext.logger().info("Successfully deleted customer {} from Shopify", rCustomerId);
        
        return null;
    }
}
