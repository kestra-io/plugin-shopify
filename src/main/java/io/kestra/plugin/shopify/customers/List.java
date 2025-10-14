package io.kestra.plugin.shopify.customers;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.shopify.AbstractShopifyTask;
import io.kestra.plugin.shopify.models.Customer;
import io.kestra.plugin.shopify.models.FetchType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

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
                    type: io.kestra.plugin.shopify.customers.List
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                """
        )
    }
)
public class List extends AbstractShopifyTask implements RunnableTask<List.Output> {
    
    @Schema(
        title = "Fetch type",
        description = "How to fetch the customers"
    )
    @Builder.Default
    @NotNull
    protected Property<FetchType> fetchType = Property.of(FetchType.FETCH);
    
    @Schema(
        title = "Customer limit",
        description = "Maximum number of customers to return (1-250)"
    )
    protected Property<Integer> limit;

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        java.util.List<String> queryParams = new ArrayList<>();
        
        runContext.render(limit).as(Integer.class).ifPresent(rLimit -> 
            queryParams.add("limit=" + rLimit));
        
        String path = "/customers.json";
        if (!queryParams.isEmpty()) {
            path += "?" + String.join("&", queryParams);
        }

        URI uri = buildApiUrl(runContext, path);
        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri, null);

        runContext.logger().debug("Listing customers from Shopify API: {}", uri);
        
        handleRateLimit(runContext);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> customersData = (java.util.List<Map<String, Object>>) responseData.get("customers");
        
        java.util.List<Customer> customers = customersData.stream()
            .map(Customer::fromMap)
            .collect(Collectors.toList());
            
        FetchType rFetchType = Property.as(fetchType, runContext, FetchType.class);
        
        switch (rFetchType) {
            case FETCH_ONE:
                if (customers.isEmpty()) {
                    return Output.builder().customers(java.util.List.of()).count(0).build();
                }
                return Output.builder().customers(java.util.List.of(customers.get(0))).count(1).build();
            case STORE:
                java.io.File tempFile = runContext.workingDir().createTempFile(".ion").toFile();
                try (var output = new java.io.BufferedWriter(new java.io.FileWriter(tempFile), io.kestra.core.serializers.FileSerde.BUFFER_SIZE)) {
                    reactor.core.publisher.Flux<Customer> customerFlux = reactor.core.publisher.Flux.fromIterable(customers);
                    Long count = io.kestra.core.serializers.FileSerde.writeAll(output, customerFlux).block();
                    URI storedUri = runContext.storage().putFile(tempFile);
                    return Output.builder().count(count.intValue()).uri(storedUri).build();
                }
            case FETCH:
            default:
                return Output.builder().customers(customers).count(customers.size()).build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Customers",
            description = "List of customers retrieved from Shopify. Only populated if using fetchType=FETCH or FETCH_ONE."
        )
        private final java.util.List<Customer> customers;
        
        @Schema(
            title = "Count",
            description = "Number of customers retrieved"
        )
        private final Integer count;
        
        @Schema(
            title = "URI",
            description = "URI of the stored data. Only populated if using fetchType=STORE."
        )
        private final URI uri;
    }
}