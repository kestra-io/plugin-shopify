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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Create a new customer in Shopify store",
    description = "Create a new customer with the specified details."
)
@Plugin(
    examples = {
        @Example(
        title = "Create a simple customer",
        full = true,
        code = """
        id: shopify_create_customer
        namespace: company.team

        tasks:
          - id: create_customer
            type: io.kestra.plugin.shopify.customers.CreateCustomer
            storeDomain: my-store.myshopify.com
            accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            email: "customer@example.com"
            firstName: "John"
            lastName: "Doe"
            acceptsMarketing: true
        """
        )
    }
)
public class CreateCustomer extends AbstractShopifyTask implements RunnableTask<CreateCustomer.Output> {

    @Schema(
        title = "Customer email",
        description = "Email address of the customer (required)"
    )
    private Property<String> email;

    @Schema(
        title = "First name",
        description = "First name of the customer"
    )
    private Property<String> firstName;

    @Schema(
        title = "Last name",
        description = "Last name of the customer"
    )
    private Property<String> lastName;

    @Schema(
        title = "Phone number",
        description = "Phone number of the customer"
    )
    private Property<String> phone;

    @Schema(
        title = "Accepts marketing",
        description = "Whether the customer accepts marketing emails"
    )
    @Builder.Default
    private Property<Boolean> acceptsMarketing = Property.of(false);

    @Schema(
        title = "Verified email",
        description = "Whether the customer's email has been verified"
    )
    @Builder.Default
    private Property<Boolean> verifiedEmail = Property.of(false);

    @Schema(
        title = "Tax exempt",
        description = "Whether the customer is tax exempt"
    )
    @Builder.Default
    private Property<Boolean> taxExempt = Property.of(false);

    @Schema(
        title = "Tags",
        description = "Comma-separated list of tags"
    )
    private Property<String> tags;

    @Schema(
        title = "Note",
        description = "Additional note about the customer"
    )
    private Property<String> note;

    @Schema(
        title = "Password",
        description = "Password for the customer account"
    )
    private Property<String> password;

    @Schema(
        title = "Password confirmation",
        description = "Password confirmation for the customer account"
    )
    private Property<String> passwordConfirmation;

    @Schema(
        title = "Send email invite",
        description = "Whether to send an email invite to the customer"
    )
    @Builder.Default
    private Property<Boolean> sendEmailInvite = Property.of(false);

    @Schema(
        title = "Addresses",
        description = "List of addresses for the customer"
    )
    private Property<List<AddressInput>> addresses;

    @Schema(
        title = "Metafields",
        description = "Metafields for the customer"
    )
    private Property<Map<String, Object>> metafields;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInput {
        private String firstName;
        private String lastName;
        private String company;
        private String address1;
        private String address2;
        private String city;
        private String province;
        private String country;
        private String zip;
        private String phone;
        private String provinceCode;
        private String countryCode;
        private Boolean defaultAddress;
    }

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = buildHttpClient(runContext);
        // Build customer object
        Map<String, Object> customerData = new HashMap<>();
        
        String emailValue = runContext.render(email).as(String.class).orElse(null);
        if (emailValue == null || emailValue.trim().isEmpty()) {
        throw new IllegalArgumentException("Customer email is required");
        }
        customerData.put("email", emailValue);

        if (firstName != null) {
        String firstNameValue = runContext.render(firstName).as(String.class).orElse(null);
        if (firstNameValue != null) {
            customerData.put("first_name", firstNameValue);
        }
        }

        if (lastName != null) {
        String lastNameValue = runContext.render(lastName).as(String.class).orElse(null);
        if (lastNameValue != null) {
            customerData.put("last_name", lastNameValue);
        }
        }

        if (phone != null) {
        String phoneValue = runContext.render(phone).as(String.class).orElse(null);
        if (phoneValue != null) {
            customerData.put("phone", phoneValue);
        }
        }

        Boolean acceptsMarketingValue = runContext.render(acceptsMarketing).as(Boolean.class).orElse(false);
        customerData.put("accepts_marketing", acceptsMarketingValue);

        Boolean verifiedEmailValue = runContext.render(verifiedEmail).as(Boolean.class).orElse(false);
        customerData.put("verified_email", verifiedEmailValue);

        Boolean taxExemptValue = runContext.render(taxExempt).as(Boolean.class).orElse(false);
        customerData.put("tax_exempt", taxExemptValue);

        if (tags != null) {
        String tagsValue = runContext.render(tags).as(String.class).orElse(null);
        if (tagsValue != null) {
            customerData.put("tags", tagsValue);
        }
        }

        if (note != null) {
        String noteValue = runContext.render(note).as(String.class).orElse(null);
        if (noteValue != null) {
            customerData.put("note", noteValue);
        }
        }

        if (password != null) {
        String passwordValue = runContext.render(password).as(String.class).orElse(null);
        if (passwordValue != null) {
            customerData.put("password", passwordValue);
        }
        }

        if (passwordConfirmation != null) {
        String passwordConfirmationValue = runContext.render(passwordConfirmation).as(String.class).orElse(null);
        if (passwordConfirmationValue != null) {
            customerData.put("password_confirmation", passwordConfirmationValue);
        }
        }

        Boolean sendEmailInviteValue = runContext.render(sendEmailInvite).as(Boolean.class).orElse(false);
        customerData.put("send_email_invite", sendEmailInviteValue);

        // Add addresses if provided
        if (addresses != null) {
        @SuppressWarnings("unchecked")
        List<AddressInput> addressesValue = runContext.render(addresses).asList(AddressInput.class);
        if (addressesValue != null && !addressesValue.isEmpty()) {
            List<Map<String, Object>> addressesData = addressesValue.stream()
                .map(this:: convertAddressToMap)
                .toList();
            customerData.put("addresses", addressesData);
        }
        }

        // Add metafields if provided
        if (metafields != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> metafieldsValue = runContext.render(metafields).asMap(String.class, Object.class);
        if (metafieldsValue != null && !metafieldsValue.isEmpty()) {
            customerData.put("metafields", metafieldsValue);
        }
        }

        Map<String, Object> requestBody = Map.of("customer", customerData);

        URI uri = buildApiUrl(runContext, "/customers.json");
        HttpRequest request = buildAuthenticatedRequest(runContext, "POST", uri, requestBody);

        runContext.logger().debug("Creating customer '{}' in Shopify API: {}", emailValue, uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> createdCustomerData = (Map<String, Object>) responseData.get("customer");
        
        if (createdCustomerData == null) {
        throw new RuntimeException("Failed to create customer - no customer data returned");
        }
        
        Customer createdCustomer = OBJECT_MAPPER.convertValue(createdCustomerData, Customer.class);

        runContext.logger().info("Created customer '{}' (ID: {}) in Shopify", 
            createdCustomer.getEmail(), createdCustomer.getId());

        return Output.builder()
            .customer(createdCustomer)
            .build();
    }

    private Map<String, Object> convertAddressToMap(AddressInput address) {
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