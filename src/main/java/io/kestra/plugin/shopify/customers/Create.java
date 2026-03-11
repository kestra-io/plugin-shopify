package io.kestra.plugin.shopify.customers;

import java.net.URI;
import java.util.HashMap;
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

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Create Shopify customer record",
    description = "Creates a customer through the Shopify Admin API using the provided store domain and access token. Email is required; name and phone fields are optional."
)
@Plugin(
    examples = {
        @Example(
            title = "Create a customer",
            full = true,
            code = """
                id: shopify_create_customer
                namespace: company.team

                tasks:
                  - id: create_customer
                    type: io.kestra.plugin.shopify.customers.Create
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                    email: customer@example.com
                    firstName: John
                    lastName: Doe
                """
        )
    }
)
public class Create extends AbstractShopifyTask implements RunnableTask<Create.Output> {

    @Schema(
        title = "Customer email",
        description = "Email address for the new customer (required)"
    )
    @NotNull
    protected Property<String> email;

    @Schema(
        title = "First name",
        description = "Optional given name for the customer"
    )
    protected Property<String> firstName;

    @Schema(
        title = "Last name",
        description = "Optional family name for the customer"
    )
    protected Property<String> lastName;

    @Schema(
        title = "Phone number",
        description = "Optional phone number sent to Shopify"
    )
    protected Property<String> phone;

    @Override
    public Output run(RunContext runContext) throws Exception {
        try (HttpClient client = HttpClient.builder().runContext(runContext).build()) {

            String rEmail = runContext.render(email).as(String.class).orElseThrow();
            String rFirstName = runContext.render(firstName).as(String.class).orElse(null);
            String rLastName = runContext.render(lastName).as(String.class).orElse(null);
            String rPhone = runContext.render(phone).as(String.class).orElse(null);

            Map<String, Object> customerData = new HashMap<>();
            customerData.put("email", rEmail);
            if (rFirstName != null)
                customerData.put("first_name", rFirstName);
            if (rLastName != null)
                customerData.put("last_name", rLastName);
            if (rPhone != null)
                customerData.put("phone", rPhone);

            Map<String, Object> requestBody = Map.of("customer", customerData);

            URI uri = buildApiUrl(runContext, "/customers.json");
            HttpRequest request = buildAuthenticatedRequest(runContext, "POST", uri, requestBody);

            runContext.logger().debug("Creating customer in Shopify API: {}", uri);

            handleRateLimit(runContext);
            HttpResponse<String> response = client.request(request, String.class);
            Map<String, Object> responseData = parseResponse(response);

            @SuppressWarnings("unchecked")
            Map<String, Object> createdCustomerData = (Map<String, Object>) responseData.get("customer");
            Customer customer = JacksonMapper.ofJson().convertValue(createdCustomerData, Customer.class);

            runContext.logger().info("Created customer with ID: {}", customer.getId());

            return Output.builder()
                .customer(customer)
                .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Created customer",
            description = "Customer object returned by Shopify for the created record"
        )
        private final Customer customer;
    }
}
