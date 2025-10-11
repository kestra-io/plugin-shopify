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
public class Product {
    private Long id;
    private String title;
    @JsonProperty("body_html")
    private String bodyHtml;
    private String vendor;
    @JsonProperty("product_type")
    private String productType;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
    @JsonProperty("published_at")
    private Instant publishedAt;
    @JsonProperty("published_scope")
    private String publishedScope;
    private String tags;
    private String status;
    @JsonProperty("admin_graphql_api_id")
    private String adminGraphqlApiId;
    private List<ProductVariant> variants;
    private List<ProductOption> options;
    private List<ProductImage> images;
    private String handle;
    @JsonProperty("template_suffix")
    private String templateSuffix;
    private Map<String, Object> metafields;

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductVariant {
        private Long id;
        @JsonProperty("product_id")
        private Long productId;
        private String title;
        private String price;
        private String sku;
        private String position;
        @JsonProperty("inventory_policy")
        private String inventoryPolicy;
        @JsonProperty("compare_at_price")
        private String compareAtPrice;
        @JsonProperty("fulfillment_service")
        private String fulfillmentService;
        @JsonProperty("inventory_management")
        private String inventoryManagement;
        private String option1;
        private String option2;
        private String option3;
        @JsonProperty("created_at")
        private Instant createdAt;
        @JsonProperty("updated_at")
        private Instant updatedAt;
        private Boolean taxable;
        private String barcode;
        private Double grams;
        @JsonProperty("image_id")
        private Long imageId;
        private Double weight;
        @JsonProperty("weight_unit")
        private String weightUnit;
        @JsonProperty("inventory_item_id")
        private Long inventoryItemId;
        @JsonProperty("inventory_quantity")
        private Integer inventoryQuantity;
        @JsonProperty("old_inventory_quantity")
        private Integer oldInventoryQuantity;
        @JsonProperty("requires_shipping")
        private Boolean requiresShipping;
        @JsonProperty("admin_graphql_api_id")
        private String adminGraphqlApiId;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductOption {
        private Long id;
        @JsonProperty("product_id")
        private Long productId;
        private String name;
        private Integer position;
        private List<String> values;
    }

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductImage {
        private Long id;
        @JsonProperty("product_id")
        private Long productId;
        private Integer position;
        @JsonProperty("created_at")
        private Instant createdAt;
        @JsonProperty("updated_at")
        private Instant updatedAt;
        private String alt;
        private Integer width;
        private Integer height;
        private String src;
        @JsonProperty("variant_ids")
        private List<Long> variantIds;
        @JsonProperty("admin_graphql_api_id")
        private String adminGraphqlApiId;
    }
}