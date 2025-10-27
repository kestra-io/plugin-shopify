package io.kestra.plugin.shopify.products;

import io.kestra.core.http.client.HttpClient;
import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import java.io.IOException;
import java.lang.InterruptedException;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.shopify.AbstractShopifyTask;
import io.kestra.plugin.shopify.models.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Update an existing product in Shopify store",
    description = "Update an existing product with new details."
)
@Plugin(
    examples = {
        @Example(
        title = "Update product title and price",
        full = true,
        code = """
                id: shopify_update_product
                namespace: company.team
                
                tasks:
                  - id: update_product
                    type: io.kestra.plugin.shopify.products.Update
                    storeDomain: my-store.myshopify.com
                    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
                    productId: 123456789
                    title: "Updated T-Shirt Title"
                    bodyHtml: "<p>Updated description for the t-shirt</p>"
                    status: "active"
                """
        )
    }
)
public class Update extends AbstractShopifyTask implements RunnableTask<Update.Output> {

    @Schema(
        title = "Product ID",
        description = "The ID of the product to update"
    )
    private Property<Long> productId;

    @Schema(
        title = "Product title",
        description = "The title of the product"
    )
    private Property<String> title;

    @Schema(
        title = "Product description HTML",
        description = "The description of the product in HTML format"
    )
    private Property<String> bodyHtml;

    @Schema(
        title = "Vendor",
        description = "The vendor of the product"
    )
    private Property<String> vendor;

    @Schema(
        title = "Product type",
        description = "The product type"
    )
    private Property<String> productType;

    @Schema(
        title = "Tags",
        description = "Comma-separated list of tags"
    )
    private Property<String> tags;

    @Schema(
        title = "Status",
        description = "The status of the product (active, archived, draft)"
    )
    private Property<String> status;

    @Schema(
        title = "Handle",
        description = "A unique URL handle for the product"
    )
    private Property<String> handle;

    @Schema(
        title = "Template suffix",
        description = "The suffix of the Liquid template used for the product page"
    )
    private Property<String> templateSuffix;

    @Schema(
        title = "Published scope",
        description = "The scope where the product is published (web, global)"
    )
    private Property<String> publishedScope;

    @Schema(
        title = "SEO title",
        description = "The SEO title for the product"
    )
    private Property<String> seoTitle;

    @Schema(
        title = "SEO description",
        description = "The SEO description for the product"
    )
    private Property<String> seoDescription;

    @Override
    public Output run(RunContext runContext) throws Exception {
        try (HttpClient client = HttpClient.builder().runContext(runContext).build()) {
            Long rProductId = runContext.render(productId).as(Long.class)
                .orElseThrow(() -> new IllegalArgumentException("Product ID is required"));

            // Build product update object - only include fields that are provided
            Map<String, Object> productData = new java.util.HashMap<>();

            if (title != null) {
                String rTitle = runContext.render(title).as(String.class).orElse(null);
                if (rTitle != null) {
                    productData.put("title", rTitle);
                }
            }

        if (bodyHtml != null) {
        String rBodyHtml = runContext.render(bodyHtml).as(String.class).orElse(null);
        if (rBodyHtml != null) {
            productData.put("body_html", rBodyHtml);
        }
        }

        if (vendor != null) {
        String rVendor = runContext.render(vendor).as(String.class).orElse(null);
        if (rVendor != null) {
            productData.put("vendor", rVendor);
        }
        }

        if (productType != null) {
        String rProductType = runContext.render(productType).as(String.class).orElse(null);
        if (rProductType != null) {
            productData.put("product_type", rProductType);
        }
        }

        if (tags != null) {
        String rTags = runContext.render(tags).as(String.class).orElse(null);
        if (rTags != null) {
            productData.put("tags", rTags);
        }
        }

        if (status != null) {
        String rStatus = runContext.render(status).as(String.class).orElse(null);
        if (rStatus != null) {
            productData.put("status", rStatus);
        }
        }

        if (handle != null) {
        String rHandle = runContext.render(handle).as(String.class).orElse(null);
        if (rHandle != null) {
            productData.put("handle", rHandle);
        }
        }

        if (templateSuffix != null) {
        String rTemplateSuffix = runContext.render(templateSuffix).as(String.class).orElse(null);
        if (rTemplateSuffix != null) {
            productData.put("template_suffix", rTemplateSuffix);
        }
        }

        if (publishedScope != null) {
        String rPublishedScope = runContext.render(publishedScope).as(String.class).orElse(null);
        if (rPublishedScope != null) {
            productData.put("published_scope", rPublishedScope);
        }
        }

        // Add SEO fields if provided
        if (seoTitle != null || seoDescription != null) {
        Map<String, Object> seoData = new java.util.HashMap<>();
        if (seoTitle != null) {
            String rSeoTitle = runContext.render(seoTitle).as(String.class).orElse(null);
            if (rSeoTitle != null) {
                seoData.put("title", rSeoTitle);
            }
        }
        if (seoDescription != null) {
            String rSeoDescription = runContext.render(seoDescription).as(String.class).orElse(null);
            if (rSeoDescription != null) {
                seoData.put("description", rSeoDescription);
            }
        }
        if (!seoData.isEmpty()) {
            productData.put("seo", seoData);
        }
        }

            if (productData.isEmpty()) {
                throw new IllegalArgumentException("At least one field must be provided to update the product");
            }

            // Add the product ID to ensure we're updating the right product
            productData.put("id", rProductId);

            Map<String, Object> requestBody = Map.of("product", productData);

            URI uri = buildApiUrl(runContext, "/products/" + rProductId + ".json");
            HttpRequest request = buildAuthenticatedRequest(runContext, "PUT", uri, requestBody);

            runContext.logger().debug("Updating product {} in Shopify API: {}", rProductId, uri);
            
            handleRateLimit(runContext);
            HttpResponse<String> response = client.request(request, String.class);
            Map<String, Object> responseData = parseResponse(response);
        
            @SuppressWarnings("unchecked")
            Map<String, Object> updatedProductData = (Map<String, Object>) responseData.get("product");
            
            if (updatedProductData == null) {
                throw new RuntimeException("Failed to update product - no product data returned");
            }
            
            Product updatedProduct = JacksonMapper.ofJson().convertValue(updatedProductData, Product.class);

            runContext.logger().info("Updated product '{}' (ID: {}) in Shopify", 
                updatedProduct.getTitle(), updatedProduct.getId());

            return Output.builder()
                .product(updatedProduct)
                .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "Updated product",
        description = "The product that was updated in Shopify"
        )
        private final Product product;
    }
}
