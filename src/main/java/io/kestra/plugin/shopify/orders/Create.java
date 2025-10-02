package io.kestra.plugin.shopify.orders;

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
import io.kestra.plugin.shopify.models.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Create a new order in Shopify store",
    description = "Create a new order with the specified details. Note: Creating orders through the API requires appropriate permissions."
)
@Plugin(
    examples = {
        @Example(
        title = "Create a simple order",
        full = true,
        code = """
                id: shopify_create_order
                namespace: company.team

                tasks:
                  - id: create_order
            type: io.kestra.plugin.shopify.orders.Create
            storeDomain: my-store.myshopify.com
            accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            customerEmail: "customer@example.com"
            lineItems:
              - variantId: 123456789
                quantity: 2
                price: "19.99"
            """
        )
    }
)
public class Create extends AbstractShopifyTask implements RunnableTask<Create.Output> {

    @Schema(
        title = "Customer email",
        description = "Email address of the customer"
    )
    private Property<String> customerEmail;

    @Schema(
        title = "Customer ID",
        description = "ID of existing customer"
    )
    private Property<Long> customerId;

    @Schema(
        title = "Line items",
        description = "List of line items for the order"
    )
    private Property<List<LineItemInput>> lineItems;

    @Schema(
        title = "Financial status",
        description = "Financial status of the order (pending, authorized, partially_paid, paid, partially_refunded, refunded, voided)"
    )
    @Builder.Default
    private Property<String> financialStatus = Property.of("pending");

    @Schema(
        title = "Send receipt",
        description = "Whether to send a receipt to the customer"
    )
    @Builder.Default
    private Property<Boolean> sendReceipt = Property.of(false);

    @Schema(
        title = "Send fulfillment receipt",
        description = "Whether to send a fulfillment receipt to the customer"
    )
    @Builder.Default
    private Property<Boolean> sendFulfillmentReceipt = Property.of(false);

    @Schema(
        title = "Note",
        description = "Additional note for the order"
    )
    private Property<String> note;

    @Schema(
        title = "Tags",
        description = "Comma-separated list of tags"
    )
    private Property<String> tags;

    @Schema(
        title = "Billing address",
        description = "Billing address for the order"
    )
    private Property<AddressInput> billingAddress;

    @Schema(
        title = "Shipping address",
        description = "Shipping address for the order"
    )
    private Property<AddressInput> shippingAddress;

    @Schema(
        title = "Currency",
        description = "Currency code for the order (e.g., USD, EUR)"
    )
    private Property<String> currency;

    @Schema(
        title = "Inventory behaviour",
        description = "How to handle inventory (bypass, decrement_ignoring_policy, decrement_obeying_policy)"
    )
    @Builder.Default
    private Property<String> inventoryBehaviour = Property.of("bypass");

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItemInput {
        private Long variantId;
        private Long productId;
        private Integer quantity;
        private String price;
        private String title;
        private String sku;
        private Boolean giftCard;
        private Boolean requiresShipping;
        private Boolean taxable;
        private String fulfillmentService;
        private Map<String, Object> properties;
    }

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
    }

    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpClient client = buildHttpClient(runContext);
        // Build order object
        Map<String, Object> orderData = new HashMap<>();

        // Customer information
        if (customerEmail != null) {
        String customerEmailValue = runContext.render(customerEmail).as(String.class).orElse(null);
        if (customerEmailValue != null) {
            orderData.put("email", customerEmailValue);
            orderData.put("customer", Map.of("email", customerEmailValue));
        }
        }

        if (customerId != null) {
        Long customerIdValue = runContext.render(customerId).as(Long.class).orElse(null);
        if (customerIdValue != null) {
            orderData.put("customer", Map.of("id", customerIdValue));
        }
        }

        // Line items
        @SuppressWarnings("unchecked")
        List<LineItemInput> lineItemsValue = runContext.render(lineItems).asList(LineItemInput.class);
        if (lineItemsValue == null || lineItemsValue.isEmpty()) {
        throw new IllegalArgumentException("At least one line item is required");
        }

        List<Map<String, Object>> lineItemsData = new ArrayList<>();
        for (LineItemInput lineItem : lineItemsValue) {
        Map<String, Object> lineItemData = new HashMap<>();
        
        if (lineItem.getVariantId() != null) {
            lineItemData.put("variant_id", lineItem.getVariantId());
        }
        if (lineItem.getProductId() != null) {
            lineItemData.put("product_id", lineItem.getProductId());
        }
        if (lineItem.getQuantity() != null) {
            lineItemData.put("quantity", lineItem.getQuantity());
        }
        if (lineItem.getPrice() != null) {
            lineItemData.put("price", lineItem.getPrice());
        }
        if (lineItem.getTitle() != null) {
            lineItemData.put("title", lineItem.getTitle());
        }
        if (lineItem.getSku() != null) {
            lineItemData.put("sku", lineItem.getSku());
        }
        if (lineItem.getGiftCard() != null) {
            lineItemData.put("gift_card", lineItem.getGiftCard());
        }
        if (lineItem.getRequiresShipping() != null) {
            lineItemData.put("requires_shipping", lineItem.getRequiresShipping());
        }
        if (lineItem.getTaxable() != null) {
            lineItemData.put("taxable", lineItem.getTaxable());
        }
        if (lineItem.getFulfillmentService() != null) {
            lineItemData.put("fulfillment_service", lineItem.getFulfillmentService());
        }
        if (lineItem.getProperties() != null) {
            lineItemData.put("properties", lineItem.getProperties());
        }
        
        lineItemsData.add(lineItemData);
        }
        orderData.put("line_items", lineItemsData);

        // Financial status
        String financialStatusValue = runContext.render(financialStatus).as(String.class).orElse("pending");
        orderData.put("financial_status", financialStatusValue);

        // Additional properties
        if (note != null) {
        String noteValue = runContext.render(note).as(String.class).orElse(null);
        if (noteValue != null) {
            orderData.put("note", noteValue);
        }
        }

        if (tags != null) {
        String tagsValue = runContext.render(tags).as(String.class).orElse(null);
        if (tagsValue != null) {
            orderData.put("tags", tagsValue);
        }
        }

        if (currency != null) {
        String currencyValue = runContext.render(currency).as(String.class).orElse(null);
        if (currencyValue != null) {
            orderData.put("currency", currencyValue);
        }
        }

        // Addresses
        if (billingAddress != null) {
        AddressInput billingAddressValue = runContext.render(billingAddress).as(AddressInput.class).orElse(null);
        if (billingAddressValue != null) {
            orderData.put("billing_address", convertAddressToMap(billingAddressValue));
        }
        }

        if (shippingAddress != null) {
        AddressInput shippingAddressValue = runContext.render(shippingAddress).as(AddressInput.class).orElse(null);
        if (shippingAddressValue != null) {
            orderData.put("shipping_address", convertAddressToMap(shippingAddressValue));
        }
        }

        // Inventory behaviour
        String inventoryBehaviourValue = runContext.render(inventoryBehaviour).as(String.class).orElse("bypass");
        orderData.put("inventory_behaviour", inventoryBehaviourValue);

        // Send receipt flags
        Boolean sendReceiptValue = runContext.render(sendReceipt).as(Boolean.class).orElse(false);
        Boolean sendFulfillmentReceiptValue = runContext.render(sendFulfillmentReceipt).as(Boolean.class).orElse(false);
        orderData.put("send_receipt", sendReceiptValue);
        orderData.put("send_fulfillment_receipt", sendFulfillmentReceiptValue);

        Map<String, Object> requestBody = Map.of("order", orderData);

        URI uri = buildApiUrl(runContext, "/orders.json");
        HttpRequest request = buildAuthenticatedRequest(runContext, "POST", uri, requestBody);

        runContext.logger().debug("Creating order in Shopify API: {}", uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> createdOrderData = (Map<String, Object>) responseData.get("order");
        
        if (createdOrderData == null) {
        throw new RuntimeException("Failed to create order - no order data returned");
        }
        
        Order createdOrder = OBJECT_MAPPER.convertValue(createdOrderData, Order.class);

        runContext.logger().info("Created order '{}' (ID: {}) in Shopify", 
            createdOrder.getName(), createdOrder.getId());

        return Output.builder()
            .order(createdOrder)
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
        
        return addressMap;
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "Created order",
        description = "The order that was created in Shopify"
        )
        private final Order order;
    }
}