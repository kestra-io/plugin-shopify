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
public class Customer {
    private Long id;
    private String email;
    @JsonProperty("accepts_marketing")
    private Boolean acceptsMarketing;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("orders_count")
    private Integer ordersCount;
    private String state;
    @JsonProperty("total_spent")
    private String totalSpent;
    @JsonProperty("last_order_id")
    private Long lastOrderId;
    private String note;
    @JsonProperty("verified_email")
    private Boolean verifiedEmail;
    @JsonProperty("multipass_identifier")
    private String multipassIdentifier;
    @JsonProperty("tax_exempt")
    private Boolean taxExempt;
    private String phone;
    private String tags;
    @JsonProperty("last_order_name")
    private String lastOrderName;
    private String currency;
    @JsonProperty("accepts_marketing_updated_at")
    private Instant acceptsMarketingUpdatedAt;
    @JsonProperty("marketing_opt_in_level")
    private String marketingOptInLevel;
    @JsonProperty("tax_exemptions")
    private List<String> taxExemptions;
    @JsonProperty("admin_graphql_api_id")
    private String adminGraphqlApiId;
    @JsonProperty("default_address")
    private CustomerAddress defaultAddress;
    private List<CustomerAddress> addresses;
    private Map<String, Object> metafields;

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomerAddress {
        private Long id;
        @JsonProperty("customer_id")
        private Long customerId;
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
        private String company;
        private String address1;
        private String address2;
        private String city;
        private String province;
        private String country;
        private String zip;
        private String phone;
        private String name;
        @JsonProperty("province_code")
        private String provinceCode;
        @JsonProperty("country_code")
        private String countryCode;
        @JsonProperty("country_name")
        private String countryName;
        @JsonProperty("default")
        private Boolean defaultAddress;
    }
}