package io.kestra.plugin.shopify.customers;package io.kestra.plugin.shopify.customers;



import io.kestra.core.models.annotations.Example;import java.net.http.HttpClient;

import io.kestra.core.models.annotations.Plugin;import java.net.http.HttpRequest;

import io.kestra.core.models.property.Property;import java.net.http.HttpResponse;

import io.kestra.core.models.tasks.RunnableTask;import java.io.IOException;

import io.kestra.core.runners.RunContext;import java.lang.InterruptedException;

import io.kestra.core.serializers.JacksonMapper;import io.kestra.core.models.annotations.Example;

import io.kestra.plugin.shopify.AbstractShopifyTask;import io.kestra.core.models.annotations.Plugin;

import io.kestra.plugin.shopify.models.Customer;import io.kestra.core.models.property.Property;

import io.kestra.plugin.shopify.models.FetchType;import io.kestra.core.models.tasks.RunnableTask;

import io.swagger.v3.oas.annotations.media.Schema;import io.kestra.core.runners.RunContext;

import lombok.*;import io.kestra.plugin.shopify.AbstractShopifyTask;

import lombok.experimental.SuperBuilder;import io.kestra.plugin.shopify.models.Customer;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;import lombok.*;

import java.net.http.HttpRequest;import lombok.experimental.SuperBuilder;

import java.net.http.HttpResponse;

import java.util.ArrayList;import java.net.URI;

import java.util.List;import java.util.List;

import java.util.Map;import java.util.Map;



@SuperBuilder@SuperBuilder

@ToString@ToString

@EqualsAndHashCode@EqualsAndHashCode

@Getter@Getter

@NoArgsConstructor@NoArgsConstructor

@Schema(@Schema(

    title = "List customers from Shopify store",    title = "List customers from Shopify store",

    description = "Retrieve a list of customers from your Shopify store with optional filtering and pagination."    description = "Retrieve a list of customers from your Shopify store with optional filtering and pagination."

))

@Plugin(@Plugin(

    examples = {    examples = {

        @Example(        @Example(

            title = "List all customers",        title = "List all customers",

            full = true,        full = true,

            code = """        code = """

                        id: shopify_list_customers                    id: shopify_list_customers

                        namespace: company.team                    namespace: company.team

                

                        tasks:                    tasks:

                          - id: list_customers                      - id: list_customers

                    type: io.kestra.plugin.shopify.customers.List                type: io.kestra.plugin.shopify.customers.List

                    storeDomain: my-store.myshopify.com                storeDomain: my-store.myshopify.com

                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"                accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"

                """            """

        ),        ),

        @Example(        @Example(

            title = "List customers with filtering",        title = "List customers with filtering",

            full = true,        full = true,

            code = """        code = """

                        id: shopify_list_customers_filtered                    id: shopify_list_customers_filtered

                        namespace: company.team                    namespace: company.team

                

                        tasks:                    tasks:

                          - id: list_customers                      - id: list_customers

                    type: io.kestra.plugin.shopify.customers.List                type: io.kestra.plugin.shopify.customers.List

                    storeDomain: my-store.myshopify.com                storeDomain: my-store.myshopify.com

                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"                accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"

                    limit: 50                limit: 50

                    createdAtMin: "2023-01-01T00:00:00Z"                createdAtMin: "2023-01-01T00:00:00Z"

                """            """

        )        )

    }    }

))

public class List extends AbstractShopifyTask implements RunnableTask<List.Output> {public class List extends AbstractShopifyTask implements RunnableTask<List.Output> {



    @Schema(    @Schema(

        title = "Fetch type",        title = "Number of customers to retrieve",

        description = "How to fetch the data (FETCH_ONE, FETCH, STORE)"        description = "Maximum number of customers to return (1-250, default: 50)"

    )    )

    @Builder.Default    @Builder.Default

    private Property<FetchType> fetchType = Property.ofValue(FetchType.FETCH);    private Property<Integer> limit = Property.of(50);



    @Schema(    @Schema(

        title = "Limit",        title = "Created after date",

        description = "Maximum number of customers to retrieve"        description = "Show customers created after this date (ISO 8601 format)"

    )    )

    private Property<Integer> limit;    private Property<String> createdAtMin;



    @Schema(    @Schema(

        title = "Since ID",        title = "Created before date",

        description = "Retrieve customers after this ID"        description = "Show customers created before this date (ISO 8601 format)"

    )    )

    private Property<Long> sinceId;    private Property<String> createdAtMax;



    @Schema(    @Schema(

        title = "Created at min",        title = "Updated after date",

        description = "Retrieve customers created after this date"        description = "Show customers updated after this date (ISO 8601 format)"

    )    )

    private Property<String> createdAtMin;    private Property<String> updatedAtMin;



    @Schema(    @Schema(

        title = "Created at max",         title = "Updated before date",

        description = "Retrieve customers created before this date"        description = "Show customers updated before this date (ISO 8601 format)"

    )    )

    private Property<String> createdAtMax;    private Property<String> updatedAtMax;



    @Schema(    @Schema(

        title = "Updated at min",        title = "Fields to include",

        description = "Retrieve customers updated after this date"        description = "Comma-separated list of fields to include in the response"

    )    )

    private Property<String> updatedAtMin;    private Property<String> fields;



    @Schema(    @Schema(

        title = "Updated at max",        title = "Page info for pagination",

        description = "Retrieve customers updated before this date"        description = "Page info parameter for cursor-based pagination"

    )    )

    private Property<String> updatedAtMax;    private Property<String> pageInfo;



    @Override    @Override

    public Output run(RunContext runContext) throws Exception {    public Output run(RunContext runContext) throws Exception {

        var client = runContext.http().client();        HttpClient client = buildHttpClient(runContext);

                StringBuilder pathBuilder = new StringBuilder("/customers.json");

        // Build query parameters        

        List<String> queryParams = new ArrayList<>();        // Build query parameters

                StringBuilder queryParams = new StringBuilder();

        if (limit != null) {        

            Integer limitValue = runContext.render(limit).as(Integer.class).orElse(null);        Integer limitValue = runContext.render(limit).as(Integer.class).orElse(50);

            if (limitValue != null) {        queryParams.append("?limit=").append(Math.min(Math.max(limitValue, 1), 250));

                queryParams.add("limit=" + limitValue);        

            }        if (createdAtMin != null) {

        }            String createdAtMinValue = runContext.render(createdAtMin).as(String.class).orElse(null);

                    if (createdAtMinValue != null) {

        if (sinceId != null) {                queryParams.append("&created_at_min=").append(createdAtMinValue);

            Long sinceIdValue = runContext.render(sinceId).as(Long.class).orElse(null);            }

            if (sinceIdValue != null) {        }

                queryParams.add("since_id=" + sinceIdValue);        

            }        if (createdAtMax != null) {

        }            String createdAtMaxValue = runContext.render(createdAtMax).as(String.class).orElse(null);

                    if (createdAtMaxValue != null) {

        if (createdAtMin != null) {                queryParams.append("&created_at_max=").append(createdAtMaxValue);

            String createdAtMinValue = runContext.render(createdAtMin).as(String.class).orElse(null);            }

            if (createdAtMinValue != null) {        }

                queryParams.add("created_at_min=" + createdAtMinValue);        

            }        if (updatedAtMin != null) {

        }            String updatedAtMinValue = runContext.render(updatedAtMin).as(String.class).orElse(null);

                    if (updatedAtMinValue != null) {

        if (createdAtMax != null) {                queryParams.append("&updated_at_min=").append(updatedAtMinValue);

            String createdAtMaxValue = runContext.render(createdAtMax).as(String.class).orElse(null);            }

            if (createdAtMaxValue != null) {        }

                queryParams.add("created_at_max=" + createdAtMaxValue);        

            }        if (updatedAtMax != null) {

        }            String updatedAtMaxValue = runContext.render(updatedAtMax).as(String.class).orElse(null);

                    if (updatedAtMaxValue != null) {

        if (updatedAtMin != null) {                queryParams.append("&updated_at_max=").append(updatedAtMaxValue);

            String updatedAtMinValue = runContext.render(updatedAtMin).as(String.class).orElse(null);            }

            if (updatedAtMinValue != null) {        }

                queryParams.add("updated_at_min=" + updatedAtMinValue);        

            }        if (fields != null) {

        }            String fieldsValue = runContext.render(fields).as(String.class).orElse(null);

                    if (fieldsValue != null) {

        if (updatedAtMax != null) {                queryParams.append("&fields=").append(fieldsValue);

            String updatedAtMaxValue = runContext.render(updatedAtMax).as(String.class).orElse(null);            }

            if (updatedAtMaxValue != null) {        }

                queryParams.add("updated_at_max=" + updatedAtMaxValue);        

            }        if (pageInfo != null) {

        }            String pageInfoValue = runContext.render(pageInfo).as(String.class).orElse(null);

            if (pageInfoValue != null) {

        String path = "/customers.json";                queryParams.append("&page_info=").append(pageInfoValue);

        if (!queryParams.isEmpty()) {            }

            path += "?" + String.join("&", queryParams);        }

        }        

        pathBuilder.append(queryParams.toString());

        URI uri = buildApiUrl(runContext, path);

        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri, null);        URI uri = buildApiUrl(runContext, pathBuilder.toString());

        HttpRequest request = buildAuthenticatedRequest(runContext, "GET", uri);

        runContext.logger().debug("Listing customers from Shopify API: {}", uri);

                runContext.logger().debug("Listing customers from Shopify API: {}", uri);

        handleRateLimit();        

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());        handleRateLimit();

        Map<String, Object> responseData = parseResponse(response);        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                Map<String, Object> responseData = parseResponse(response);

        @SuppressWarnings("unchecked")        

        List<Map<String, Object>> customersData = (List<Map<String, Object>>) responseData.get("customers");        @SuppressWarnings("unchecked")

                List<Map<String, Object>> customersData = (List<Map<String, Object>>) responseData.get("customers");

        if (customersData == null) {        

            customersData = new ArrayList<>();        if (customersData == null) {

        }            customersData = List.of();

                }

        List<Customer> customers = customersData.stream()        

            .map(customerData -> JacksonMapper.ofJson().convertValue(customerData, Customer.class))        List<Customer> customers = customersData.stream()

            .toList();            .map(customerData -> OBJECT_MAPPER.convertValue(customerData, Customer.class))

            .toList();

        runContext.logger().info("Retrieved {} customers from Shopify", customers.size());

        runContext.logger().info("Retrieved {} customers from Shopify", customers.size());

        return Output.builder()

            .customers(customers)        return Output.builder()

            .build();            .customers(customers)

    }            .count(customers.size())

            .build();

    @Builder        }

    @Getter    }

    public static class Output implements io.kestra.core.models.tasks.Output {

        @Schema(    @Builder

            title = "Customers",    @Getter

            description = "List of customers retrieved from Shopify"    public static class Output implements io.kestra.core.models.tasks.Output {

        )        @Schema(

        private final List<Customer> customers;        title = "List of customers",

    }        description = "The retrieved customers from Shopify"

}        )
        private final List<Customer> customers;

        @Schema(
        title = "Number of customers",
        description = "Total number of customers retrieved"
        )
        private final Integer count;
    }
