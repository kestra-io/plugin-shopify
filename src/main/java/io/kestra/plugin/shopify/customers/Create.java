package io.kestra.plugin.shopify.customers;package io.kestra.plugin.shopify.customers;



import io.kestra.core.models.annotations.Example;import io.kestra.core.http.client.configurations.HttpConfiguration;

import io.kestra.core.models.annotations.Plugin;import io.kestra.core.models.annotations.Example;

import io.kestra.core.models.property.Property;import io.kestra.core.models.annotations.Plugin;

import io.kestra.core.models.tasks.RunnableTask;import io.kestra.core.models.property.Property;

import io.kestra.core.runners.RunContext;import io.kestra.core.models.tasks.RunnableTask;

import io.kestra.core.serializers.JacksonMapper;import io.kestra.core.runners.RunContext;

import io.kestra.plugin.shopify.AbstractShopifyTask;import io.kestra.core.serializers.JacksonMapper;

import io.kestra.plugin.shopify.models.Customer;import io.kestra.plugin.shopify.AbstractShopifyTask;

import io.swagger.v3.oas.annotations.media.Schema;import io.kestra.plugin.shopify.models.Customer;

import lombok.*;import io.swagger.v3.oas.annotations.media.Schema;

import lombok.experimental.SuperBuilder;import lombok.*;

import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;

import java.net.URI;import jakarta.validation.constraints.NotNull;

import java.net.http.HttpRequest;import java.net.URI;

import java.net.http.HttpResponse;import java.net.http.HttpRequest;

import java.util.HashMap;import java.net.http.HttpResponse;

import java.util.List;import java.util.HashMap;

import java.util.Map;import java.util.List;

import java.util.Map;

@SuperBuilder

@ToString@SuperBuilder

@EqualsAndHashCode@ToString

@Getter@EqualsAndHashCode

@NoArgsConstructor@Getter

@Schema(@NoArgsConstructor

    title = "Create a new customer in Shopify store",@Schema(

    description = "Create a new customer with the specified details."    title = "Create a new customer in Shopify store",

)    description = "Create a new customer with the specified details."

@Plugin()

    examples = {@Plugin(

        @Example(    examples = {

            title = "Create a simple customer",        @Example(

            full = true,            title = "Create a simple customer",

            code = """            full = true,

                        id: shopify_create_customer            code = """

                        namespace: company.team                        id: shopify_create_customer

                                        namespace: company.team

                        tasks:                

                          - id: create_customer                        tasks:

                    type: io.kestra.plugin.shopify.customers.Create                          - id: create_customer

                    storeDomain: my-store.myshopify.com                    type: io.kestra.plugin.shopify.customers.Create

                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"                    storeDomain: my-store.myshopify.com

                    email: "customer@example.com"                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"

                    firstName: "John"                    email: "customer@example.com"

                    lastName: "Doe"                    firstName: "John"

                    acceptsMarketing: true                    lastName: "Doe"

                """                    acceptsMarketing: true

        )                """

    }        )

)    }

public class Create extends AbstractShopifyTask implements RunnableTask<Create.Output> {)

public class Create extends AbstractShopifyTask implements RunnableTask<Create.Output> {

    @Schema(

        title = "Customer email",    @Schema(

        description = "Email address of the customer (required)"        title = "Customer email",

    )        description = "Email address of the customer (required)"

    @NotNull    )

    private Property<String> email;    @NotNull

    private Property<String> email;

    @Schema(

        title = "First name",    @Schema(

        description = "First name of the customer"        title = "First name",

    )        description = "First name of the customer"

    private Property<String> firstName;    )

    private Property<String> firstName;

    @Schema(

        title = "Last name",    @Schema(

        description = "Last name of the customer"        title = "Last name",

    )        description = "Last name of the customer"

    private Property<String> lastName;    )

    private Property<String> lastName;

    @Schema(

        title = "Phone number",    @Schema(

        description = "Phone number of the customer"        title = "Phone number",

    )        description = "Phone number of the customer"

    private Property<String> phone;    )

    private Property<String> phone;

    @Schema(

        title = "Accepts marketing",    @Schema(

        description = "Whether the customer accepts marketing emails"        title = "Accepts marketing",

    )        description = "Whether the customer accepts marketing emails"

    @Builder.Default    )

    private Property<Boolean> acceptsMarketing = Property.ofValue(false);    @Builder.Default

    private Property<Boolean> acceptsMarketing = Property.ofValue(false);

    @Schema(

        title = "Verified email",    @Schema(

        description = "Whether the customer's email has been verified"        title = "Verified email",

    )        description = "Whether the customer's email has been verified"

    @Builder.Default    )

    private Property<Boolean> verifiedEmail = Property.ofValue(false);    @Builder.Default

    private Property<Boolean> verifiedEmail = Property.ofValue(false);

    @Schema(

        title = "Tax exempt",    @Schema(

        description = "Whether the customer is tax exempt"        title = "Tax exempt",

    )        description = "Whether the customer is tax exempt"

    @Builder.Default    )

    private Property<Boolean> taxExempt = Property.ofValue(false);    @Builder.Default

    private Property<Boolean> taxExempt = Property.ofValue(false);

    @Schema(

        title = "Tags",    @Schema(

        description = "Comma-separated list of tags"        title = "Tags",

    )        description = "Comma-separated list of tags"

    private Property<String> tags;    )

    private Property<String> tags;

    @Schema(

        title = "Note",    @Schema(

        description = "Additional note about the customer"        title = "Note",

    )        description = "Additional note about the customer"

    private Property<String> note;    )

    private Property<String> note;

    @Schema(

        title = "Password",    @Schema(

        description = "Password for the customer account"        title = "Password",

    )        description = "Password for the customer account"

    private Property<String> password;    )

    private Property<String> password;

    @Schema(

        title = "Password confirmation",    @Schema(

        description = "Password confirmation for the customer account"        title = "Password confirmation",

    )        description = "Password confirmation for the customer account"

    private Property<String> passwordConfirmation;    )

    private Property<String> passwordConfirmation;

    @Schema(

        title = "Send email invite",    @Schema(

        description = "Whether to send an email invitation to the customer"        title = "Send email invite",

    )        description = "Whether to send an email invite to the customer"

    @Builder.Default    )

    private Property<Boolean> sendEmailInvite = Property.ofValue(false);    @Builder.Default

    private Property<Boolean> sendEmailInvite = Property.of(false);

    @Schema(

        title = "Addresses",    @Schema(

        description = "List of addresses for the customer"        title = "Addresses",

    )        description = "List of addresses for the customer"

    private Property<List<AddressInput>> addresses;    )

    private Property<List<AddressInput>> addresses;

    @Schema(

        title = "Metafields",    @Schema(

        description = "Metafields for the customer"        title = "Metafields",

    )        description = "Metafields for the customer"

    private Property<Map<String, Object>> metafields;    )

    private Property<Map<String, Object>> metafields;

    @Data

    @Builder    @Data

    @NoArgsConstructor    @Builder

    @AllArgsConstructor    @NoArgsConstructor

    public static class AddressInput {    @AllArgsConstructor

        private String firstName;    public static class AddressInput {

        private String lastName;        private String firstName;

        private String company;        private String lastName;

        private String address1;        private String company;

        private String address2;        private String address1;

        private String city;        private String address2;

        private String province;        private String city;

        private String country;        private String province;

        private String zip;        private String country;

        private String phone;        private String zip;

        private String provinceCode;        private String phone;

        private String countryCode;        private String provinceCode;

        private Boolean defaultAddress;        private String countryCode;

    }        private Boolean defaultAddress;

    }

    @Override

    public Output run(RunContext runContext) throws Exception {    @Override

        var client = runContext.http().client();    public Output run(RunContext runContext) throws Exception {

                var client = runContext.http().client();

        // Build customer object        // Build customer object

        Map<String, Object> customerData = new HashMap<>();        Map<String, Object> customerData = new HashMap<>();

                

        String emailValue = runContext.render(email).as(String.class).orElseThrow();        String emailValue = runContext.render(email).as(String.class).orElseThrow();

        customerData.put("email", emailValue);        customerData.put("email", emailValue);



        putIfNotNull(customerData, "first_name",         if (firstName != null) {

            firstName != null ? runContext.render(firstName).as(String.class).orElse(null) : null);        String firstNameValue = runContext.render(firstName).as(String.class).orElse(null);

        putIfNotNull(customerData, "last_name",         if (firstNameValue != null) {

            lastName != null ? runContext.render(lastName).as(String.class).orElse(null) : null);            customerData.put("first_name", firstNameValue);

        putIfNotNull(customerData, "phone",         }

            phone != null ? runContext.render(phone).as(String.class).orElse(null) : null);        }



        Boolean acceptsMarketingValue = runContext.render(acceptsMarketing).as(Boolean.class).orElse(false);        if (lastName != null) {

        customerData.put("accepts_marketing", acceptsMarketingValue);        String lastNameValue = runContext.render(lastName).as(String.class).orElse(null);

        if (lastNameValue != null) {

        Boolean verifiedEmailValue = runContext.render(verifiedEmail).as(Boolean.class).orElse(false);            customerData.put("last_name", lastNameValue);

        customerData.put("verified_email", verifiedEmailValue);        }

        }

        Boolean taxExemptValue = runContext.render(taxExempt).as(Boolean.class).orElse(false);

        customerData.put("tax_exempt", taxExemptValue);        if (phone != null) {

        String phoneValue = runContext.render(phone).as(String.class).orElse(null);

        putIfNotNull(customerData, "tags",         if (phoneValue != null) {

            tags != null ? runContext.render(tags).as(String.class).orElse(null) : null);            customerData.put("phone", phoneValue);

        putIfNotNull(customerData, "note",         }

            note != null ? runContext.render(note).as(String.class).orElse(null) : null);        }

        putIfNotNull(customerData, "password", 

            password != null ? runContext.render(password).as(String.class).orElse(null) : null);        Boolean acceptsMarketingValue = runContext.render(acceptsMarketing).as(Boolean.class).orElse(false);

        putIfNotNull(customerData, "password_confirmation",         customerData.put("accepts_marketing", acceptsMarketingValue);

            passwordConfirmation != null ? runContext.render(passwordConfirmation).as(String.class).orElse(null) : null);

        Boolean verifiedEmailValue = runContext.render(verifiedEmail).as(Boolean.class).orElse(false);

        Boolean sendEmailInviteValue = runContext.render(sendEmailInvite).as(Boolean.class).orElse(false);        customerData.put("verified_email", verifiedEmailValue);

        customerData.put("send_email_invite", sendEmailInviteValue);

        Boolean taxExemptValue = runContext.render(taxExempt).as(Boolean.class).orElse(false);

        // Add addresses if provided        customerData.put("tax_exempt", taxExemptValue);

        if (addresses != null) {

            List<AddressInput> addressesValue = runContext.render(addresses).asList(AddressInput.class);        if (tags != null) {

            if (addressesValue != null && !addressesValue.isEmpty()) {        String tagsValue = runContext.render(tags).as(String.class).orElse(null);

                List<Map<String, Object>> addressesData = addressesValue.stream()        if (tagsValue != null) {

                    .map(this::convertAddressToMap)            customerData.put("tags", tagsValue);

                    .toList();        }

                customerData.put("addresses", addressesData);        }

            }

        }        if (note != null) {

        String noteValue = runContext.render(note).as(String.class).orElse(null);

        // Add metafields if provided        if (noteValue != null) {

        if (metafields != null) {            customerData.put("note", noteValue);

            Map<String, Object> metafieldsValue = runContext.render(metafields).asMap(String.class, Object.class);        }

            if (metafieldsValue != null && !metafieldsValue.isEmpty()) {        }

                customerData.put("metafields", metafieldsValue);

            }        if (password != null) {

        }        String passwordValue = runContext.render(password).as(String.class).orElse(null);

        if (passwordValue != null) {

        Map<String, Object> requestBody = Map.of("customer", customerData);            customerData.put("password", passwordValue);

        }

        URI uri = buildApiUrl(runContext, "/customers.json");        }

        HttpRequest request = buildAuthenticatedRequest(runContext, "POST", uri, requestBody);

        if (passwordConfirmation != null) {

        runContext.logger().debug("Creating customer '{}' in Shopify API: {}", emailValue, uri);        String passwordConfirmationValue = runContext.render(passwordConfirmation).as(String.class).orElse(null);

                if (passwordConfirmationValue != null) {

        handleRateLimit();            customerData.put("password_confirmation", passwordConfirmationValue);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());        }

        Map<String, Object> responseData = parseResponse(response);        }

        

        @SuppressWarnings("unchecked")        Boolean sendEmailInviteValue = runContext.render(sendEmailInvite).as(Boolean.class).orElse(false);

        Map<String, Object> createdCustomerData = (Map<String, Object>) responseData.get("customer");        customerData.put("send_email_invite", sendEmailInviteValue);

        

        if (createdCustomerData == null) {        // Add addresses if provided

            throw new RuntimeException("Failed to create customer - no customer data returned");        if (addresses != null) {

        }        @SuppressWarnings("unchecked")

                List<AddressInput> addressesValue = runContext.render(addresses).asList(AddressInput.class);

        Customer createdCustomer = JacksonMapper.ofJson().convertValue(createdCustomerData, Customer.class);        if (addressesValue != null && !addressesValue.isEmpty()) {

            List<Map<String, Object>> addressesData = addressesValue.stream()

        runContext.logger().info("Created customer '{}' (ID: {}) in Shopify",                 .map(this:: convertAddressToMap)

            createdCustomer.getEmail(), createdCustomer.getId());                .toList();

            customerData.put("addresses", addressesData);

        return Output.builder()        }

            .customer(createdCustomer)        }

            .build();

    }        // Add metafields if provided

        if (metafields != null) {

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {        @SuppressWarnings("unchecked")

        if (value != null) {        Map<String, Object> metafieldsValue = runContext.render(metafields).asMap(String.class, Object.class);

            map.put(key, value);        if (metafieldsValue != null && !metafieldsValue.isEmpty()) {

        }            customerData.put("metafields", metafieldsValue);

    }        }

        }

    private Map<String, Object> convertAddressToMap(AddressInput address) {

        Map<String, Object> addressMap = new HashMap<>();        Map<String, Object> requestBody = Map.of("customer", customerData);

        

        putIfNotNull(addressMap, "first_name", address.getFirstName());        URI uri = buildApiUrl(runContext, "/customers.json");

        putIfNotNull(addressMap, "last_name", address.getLastName());        HttpRequest request = buildAuthenticatedRequest(runContext, "POST", uri, requestBody);

        putIfNotNull(addressMap, "company", address.getCompany());

        putIfNotNull(addressMap, "address1", address.getAddress1());        runContext.logger().debug("Creating customer '{}' in Shopify API: {}", emailValue, uri);

        putIfNotNull(addressMap, "address2", address.getAddress2());        

        putIfNotNull(addressMap, "city", address.getCity());        handleRateLimit();

        putIfNotNull(addressMap, "province", address.getProvince());        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        putIfNotNull(addressMap, "country", address.getCountry());        Map<String, Object> responseData = parseResponse(response);

        putIfNotNull(addressMap, "zip", address.getZip());        

        putIfNotNull(addressMap, "phone", address.getPhone());        @SuppressWarnings("unchecked")

        putIfNotNull(addressMap, "province_code", address.getProvinceCode());        Map<String, Object> createdCustomerData = (Map<String, Object>) responseData.get("customer");

        putIfNotNull(addressMap, "country_code", address.getCountryCode());        

        putIfNotNull(addressMap, "default", address.getDefaultAddress());        if (createdCustomerData == null) {

                throw new RuntimeException("Failed to create customer - no customer data returned");

        return addressMap;        }

    }        

        Customer createdCustomer = OBJECT_MAPPER.convertValue(createdCustomerData, Customer.class);

    @Builder

    @Getter        runContext.logger().info("Created customer '{}' (ID: {}) in Shopify", 

    public static class Output implements io.kestra.core.models.tasks.Output {            createdCustomer.getEmail(), createdCustomer.getId());

        @Schema(

            title = "Created customer",        return Output.builder()

            description = "The customer that was created in Shopify"            .customer(createdCustomer)

        )            .build();

        private final Customer customer;    }

    }

}    private Map<String, Object> convertAddressToMap(AddressInput address) {
        Map<String, Object> addressMap = new HashMap<>();
        
        if (address.getFirstName() != null) addressMap.put("first_name", address.getFirstName());
        if (address.getLastName() != null) addressMap.put("last_name", address.getLastName());
        if (address.getCompany() != null) addressMap.put("company", address.getCompany());
        if (address.getAddress1() != null) addressMap.put("address1", address.getAddress1());
        if (address.getAddress2() != null) addressMap.put("address2", address.getAddress2());
        if (address.getCity() != null) addressMap.put("city", address.getCity());
        if (address.getProvince() != null) addressMap.put("province", address.getProvince());
        if (address.getCountry() != null) addressMap.put("country", address.getCountry());
        if (address.getZip() != null) addressMap.put("zip", address.getZip());
        if (address.getPhone() != null) addressMap.put("phone", address.getPhone());
        if (address.getProvinceCode() != null) addressMap.put("province_code", address.getProvinceCode());
        if (address.getCountryCode() != null) addressMap.put("country_code", address.getCountryCode());
        if (address.getDefaultAddress() != null) addressMap.put("default", address.getDefaultAddress());
        
        return addressMap;
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "Created customer",
        description = "The customer that was created in Shopify"
        )
        private final Customer customer;
    }
}