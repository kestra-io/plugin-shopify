package io.kestra.plugin.shopify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private Long id;
    @JsonProperty("admin_graphql_api_id")
    private String adminGraphqlApiId;
    @JsonProperty("app_id")
    private Long appId;
    @JsonProperty("browser_ip")
    private String browserIp;
    @JsonProperty("buyer_accepts_marketing")
    private Boolean buyerAcceptsMarketing;
    @JsonProperty("cancel_reason")
    private String cancelReason;
    @JsonProperty("cancelled_at")
    private Instant cancelledAt;
    @JsonProperty("cart_token")
    private String cartToken;
    @JsonProperty("checkout_id")
    private Long checkoutId;
    @JsonProperty("checkout_token")
    private String checkoutToken;
    @JsonProperty("closed_at")
    private Instant closedAt;
    private Boolean confirmed;
    @JsonProperty("contact_email")
    private String contactEmail;
    @JsonProperty("created_at")
    private Instant createdAt;
    private String currency;
    @JsonProperty("current_subtotal_price")
    private String currentSubtotalPrice;
    @JsonProperty("current_subtotal_price_set")
    private PriceSet currentSubtotalPriceSet;
    @JsonProperty("current_total_discounts")
    private String currentTotalDiscounts;
    @JsonProperty("current_total_discounts_set")
    private PriceSet currentTotalDiscountsSet;
    @JsonProperty("current_total_duties_set")
    private PriceSet currentTotalDutiesSet;
    @JsonProperty("current_total_price")
    private String currentTotalPrice;
    @JsonProperty("current_total_price_set")
    private PriceSet currentTotalPriceSet;
    @JsonProperty("current_total_tax")
    private String currentTotalTax;
    @JsonProperty("current_total_tax_set")
    private PriceSet currentTotalTaxSet;
    @JsonProperty("customer_locale")
    private String customerLocale;
    @JsonProperty("device_id")
    private String deviceId;
    @JsonProperty("discount_codes")
    private List<DiscountCode> discountCodes;
    private String email;
    @JsonProperty("estimated_taxes")
    private Boolean estimatedTaxes;
    @JsonProperty("financial_status")
    private String financialStatus;
    @JsonProperty("fulfillment_status")
    private String fulfillmentStatus;
    private String gateway;
    @JsonProperty("landing_site")
    private String landingSite;
    @JsonProperty("landing_site_ref")
    private String landingSiteRef;
    @JsonProperty("location_id")
    private Long locationId;
    private String name;
    private String note;
    @JsonProperty("note_attributes")
    private List<NoteAttribute> noteAttributes;
    private Long number;
    @JsonProperty("order_number")
    private Long orderNumber;
    @JsonProperty("order_status_url")
    private String orderStatusUrl;
    @JsonProperty("original_total_duties_set")
    private PriceSet originalTotalDutiesSet;
    @JsonProperty("payment_gateway_names")
    private List<String> paymentGatewayNames;
    private String phone;
    @JsonProperty("presentment_currency")
    private String presentmentCurrency;
    @JsonProperty("processed_at")
    private Instant processedAt;
    @JsonProperty("processing_method")
    private String processingMethod;
    private String reference;
    @JsonProperty("referring_site")
    private String referringSite;
    @JsonProperty("source_identifier")
    private String sourceIdentifier;
    @JsonProperty("source_name")
    private String sourceName;
    @JsonProperty("source_url")
    private String sourceUrl;
    @JsonProperty("subtotal_price")
    private String subtotalPrice;
    @JsonProperty("subtotal_price_set")
    private PriceSet subtotalPriceSet;
    private String tags;
    @JsonProperty("tax_lines")
    private List<TaxLine> taxLines;
    @JsonProperty("taxes_included")
    private Boolean taxesIncluded;
    private Boolean test;
    private String token;
    @JsonProperty("total_discounts")
    private String totalDiscounts;
    @JsonProperty("total_discounts_set")
    private PriceSet totalDiscountsSet;
    @JsonProperty("total_line_items_price")
    private String totalLineItemsPrice;
    @JsonProperty("total_line_items_price_set")
    private PriceSet totalLineItemsPriceSet;
    @JsonProperty("total_outstanding")
    private String totalOutstanding;
    @JsonProperty("total_price")
    private String totalPrice;
    @JsonProperty("total_price_set")
    private PriceSet totalPriceSet;
    @JsonProperty("total_price_usd")
    private String totalPriceUsd;
    @JsonProperty("total_shipping_price_set")
    private PriceSet totalShippingPriceSet;
    @JsonProperty("total_tax")
    private String totalTax;
    @JsonProperty("total_tax_set")
    private PriceSet totalTaxSet;
    @JsonProperty("total_tip_received")
    private String totalTipReceived;
    @JsonProperty("total_weight")
    private Integer totalWeight;
    @JsonProperty("updated_at")
    private Instant updatedAt;
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("billing_address")
    private Address billingAddress;
    private Customer customer;
    @JsonProperty("discount_applications")
    private List<DiscountApplication> discountApplications;
    private List<Fulfillment> fulfillments;
    @JsonProperty("line_items")
    private List<LineItem> lineItems;
    @JsonProperty("payment_terms")
    private PaymentTerms paymentTerms;
    private List<Refund> refunds;
    @JsonProperty("shipping_address")
    private Address shippingAddress;
    @JsonProperty("shipping_lines")
    private List<ShippingLine> shippingLines;

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PriceSet {
        @JsonProperty("shop_money")
        private Money shopMoney;
        @JsonProperty("presentment_money")
        private Money presentmentMoney;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Money {
        private String amount;
        @JsonProperty("currency_code")
        private String currencyCode;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiscountCode {
        private String code;
        private String amount;
        private String type;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NoteAttribute {
        private String name;
        private String value;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaxLine {
        private String price;
        private Double rate;
        private String title;
        @JsonProperty("price_set")
        private PriceSet priceSet;
        @JsonProperty("channel_liable")
        private Boolean channelLiable;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        @JsonProperty("first_name")
        private String firstName;
        private String address1;
        private String phone;
        private String city;
        private String zip;
        private String province;
        private String country;
        @JsonProperty("last_name")
        private String lastName;
        private String address2;
        private String company;
        private Double latitude;
        private Double longitude;
        private String name;
        @JsonProperty("country_code")
        private String countryCode;
        @JsonProperty("province_code")
        private String provinceCode;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiscountApplication {
        @JsonProperty("target_type")
        private String targetType;
        private String type;
        private String value;
        @JsonProperty("value_type")
        private String valueType;
        @JsonProperty("allocation_method")
        private String allocationMethod;
        @JsonProperty("target_selection")
        private String targetSelection;
        private String title;
        private String description;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fulfillment {
        private Long id;
        @JsonProperty("order_id")
        private Long orderId;
        private String status;
        @JsonProperty("created_at")
        private Instant createdAt;
        private String service;
        @JsonProperty("updated_at")
        private Instant updatedAt;
        @JsonProperty("tracking_company")
        private String trackingCompany;
        @JsonProperty("tracking_number")
        private String trackingNumber;
        @JsonProperty("tracking_numbers")
        private List<String> trackingNumbers;
        @JsonProperty("tracking_url")
        private String trackingUrl;
        @JsonProperty("tracking_urls")
        private List<String> trackingUrls;
        private Receipt receipt;
        @JsonProperty("line_items")
        private List<LineItem> lineItems;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Receipt {
        @JsonProperty("testcase")
        private Boolean testcase;
        private String authorization;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineItem {
        private Long id;
        @JsonProperty("admin_graphql_api_id")
        private String adminGraphqlApiId;
        @JsonProperty("fulfillable_quantity")
        private Integer fulfillableQuantity;
        @JsonProperty("fulfillment_service")
        private String fulfillmentService;
        @JsonProperty("fulfillment_status")
        private String fulfillmentStatus;
        @JsonProperty("gift_card")
        private Boolean giftCard;
        private Integer grams;
        private String name;
        private String price;
        @JsonProperty("price_set")
        private PriceSet priceSet;
        @JsonProperty("product_exists")
        private Boolean productExists;
        @JsonProperty("product_id")
        private Long productId;
        private Map<String, Object> properties;
        private Integer quantity;
        @JsonProperty("requires_shipping")
        private Boolean requiresShipping;
        private String sku;
        private Boolean taxable;
        private String title;
        @JsonProperty("total_discount")
        private String totalDiscount;
        @JsonProperty("total_discount_set")
        private PriceSet totalDiscountSet;
        @JsonProperty("variant_id")
        private Long variantId;
        @JsonProperty("variant_inventory_management")
        private String variantInventoryManagement;
        @JsonProperty("variant_title")
        private String variantTitle;
        private String vendor;
        @JsonProperty("tax_lines")
        private List<TaxLine> taxLines;
        private List<Map<String, Object>> duties;
        @JsonProperty("discount_allocations")
        private List<DiscountAllocation> discountAllocations;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiscountAllocation {
        private String amount;
        @JsonProperty("amount_set")
        private PriceSet amountSet;
        @JsonProperty("discount_application_index")
        private Integer discountApplicationIndex;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentTerms {
        private Integer amount;
        private String currency;
        @JsonProperty("payment_terms_name")
        private String paymentTermsName;
        @JsonProperty("payment_terms_type")
        private String paymentTermsType;
        @JsonProperty("due_in_days")
        private Integer dueInDays;
        @JsonProperty("payment_schedules")
        private List<PaymentSchedule> paymentSchedules;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentSchedule {
        private Integer amount;
        private String currency;
        @JsonProperty("issued_at")
        private Instant issuedAt;
        @JsonProperty("due_at")
        private Instant dueAt;
        @JsonProperty("completed_at")
        private Instant completedAt;
        @JsonProperty("expected_payment_method")
        private String expectedPaymentMethod;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Refund {
        private Long id;
        @JsonProperty("admin_graphql_api_id")
        private String adminGraphqlApiId;
        @JsonProperty("created_at")
        private Instant createdAt;
        private String note;
        @JsonProperty("order_id")
        private Long orderId;
        @JsonProperty("processed_at")
        private Instant processedAt;
        private Boolean restock;
        @JsonProperty("total_duties_set")
        private PriceSet totalDutiesSet;
        @JsonProperty("user_id")
        private Long userId;
        @JsonProperty("order_adjustments")
        private List<OrderAdjustment> orderAdjustments;
        private List<Transaction> transactions;
        @JsonProperty("refund_line_items")
        private List<RefundLineItem> refundLineItems;
        private List<Map<String, Object>> duties;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderAdjustment {
        private Long id;
        @JsonProperty("order_id")
        private Long orderId;
        @JsonProperty("refund_id")
        private Long refundId;
        private String amount;
        @JsonProperty("tax_amount")
        private String taxAmount;
        private String kind;
        private String reason;
        @JsonProperty("amount_set")
        private PriceSet amountSet;
        @JsonProperty("tax_amount_set")
        private PriceSet taxAmountSet;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transaction {
        private Long id;
        @JsonProperty("admin_graphql_api_id")
        private String adminGraphqlApiId;
        private String amount;
        private String authorization;
        @JsonProperty("created_at")
        private Instant createdAt;
        private String currency;
        @JsonProperty("device_id")
        private String deviceId;
        @JsonProperty("error_code")
        private String errorCode;
        private String gateway;
        private String kind;
        @JsonProperty("location_id")
        private Long locationId;
        private String message;
        @JsonProperty("order_id")
        private Long orderId;
        @JsonProperty("parent_id")
        private Long parentId;
        @JsonProperty("processed_at")
        private Instant processedAt;
        private Receipt receipt;
        @JsonProperty("source_name")
        private String sourceName;
        private String status;
        private Boolean test;
        @JsonProperty("user_id")
        private Long userId;
        @JsonProperty("currency_exchange_adjustment")
        private CurrencyExchangeAdjustment currencyExchangeAdjustment;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrencyExchangeAdjustment {
        private String adjustment;
        @JsonProperty("original_amount")
        private String originalAmount;
        @JsonProperty("final_amount")
        private String finalAmount;
        private String currency;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RefundLineItem {
        private Long id;
        @JsonProperty("line_item_id")
        private Long lineItemId;
        @JsonProperty("location_id")
        private Long locationId;
        private Integer quantity;
        @JsonProperty("restock_type")
        private String restockType;
        private String subtotal;
        @JsonProperty("subtotal_set")
        private PriceSet subtotalSet;
        @JsonProperty("total_tax")
        private String totalTax;
        @JsonProperty("total_tax_set")
        private PriceSet totalTaxSet;
        @JsonProperty("line_item")
        private LineItem lineItem;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShippingLine {
        private Long id;
        @JsonProperty("carrier_identifier")
        private String carrierIdentifier;
        private String code;
        @JsonProperty("delivery_category")
        private String deliveryCategory;
        @JsonProperty("discounted_price")
        private String discountedPrice;
        @JsonProperty("discounted_price_set")
        private PriceSet discountedPriceSet;
        private String phone;
        private String price;
        @JsonProperty("price_set")
        private PriceSet priceSet;
        @JsonProperty("requested_fulfillment_service_id")
        private String requestedFulfillmentServiceId;
        private String source;
        private String title;
        @JsonProperty("tax_lines")
        private List<TaxLine> taxLines;
        @JsonProperty("discount_allocations")
        private List<DiscountAllocation> discountAllocations;
    }
}