package io.kestra.plugin.shopify.products;

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
        HttpClient client = buildHttpClient(runContext);
        Long productIdValue = runContext.render(productId).as(Long.class)
        .orElseThrow(() -> new IllegalArgumentException("Product ID is required"));

        // Build product update object - only include fields that are provided
        Map<String, Object> productData = new java.util.HashMap<>();

        if (title != null) {
        String titleValue = runContext.render(title).as(String.class).orElse(null);
        if (titleValue != null) {
            productData.put("title", titleValue);
        }
        }

        if (bodyHtml != null) {
        String bodyHtmlValue = runContext.render(bodyHtml).as(String.class).orElse(null);
        if (bodyHtmlValue != null) {
            productData.put("body_html", bodyHtmlValue);
        }
        }

        if (vendor != null) {
        String vendorValue = runContext.render(vendor).as(String.class).orElse(null);
        if (vendorValue != null) {
            productData.put("vendor", vendorValue);
        }
        }

        if (productType != null) {
        String productTypeValue = runContext.render(productType).as(String.class).orElse(null);
        if (productTypeValue != null) {
            productData.put("product_type", productTypeValue);
        }
        }

        if (tags != null) {
        String tagsValue = runContext.render(tags).as(String.class).orElse(null);
        if (tagsValue != null) {
            productData.put("tags", tagsValue);
        }
        }

        if (status != null) {
        String statusValue = runContext.render(status).as(String.class).orElse(null);
        if (statusValue != null) {
            productData.put("status", statusValue);
        }
        }

        if (handle != null) {
        String handleValue = runContext.render(handle).as(String.class).orElse(null);
        if (handleValue != null) {
            productData.put("handle", handleValue);
        }
        }

        if (templateSuffix != null) {
        String templateSuffixValue = runContext.render(templateSuffix).as(String.class).orElse(null);
        if (templateSuffixValue != null) {
            productData.put("template_suffix", templateSuffixValue);
        }
        }

        if (publishedScope != null) {
        String publishedScopeValue = runContext.render(publishedScope).as(String.class).orElse(null);
        if (publishedScopeValue != null) {
            productData.put("published_scope", publishedScopeValue);
        }
        }

        // Add SEO fields if provided
        if (seoTitle != null || seoDescription != null) {
        Map<String, Object> seoData = new java.util.HashMap<>();
        if (seoTitle != null) {
            String seoTitleValue = runContext.render(seoTitle).as(String.class).orElse(null);
            if (seoTitleValue != null) {
                seoData.put("title", seoTitleValue);
            }
        }
        if (seoDescription != null) {
            String seoDescriptionValue = runContext.render(seoDescription).as(String.class).orElse(null);
            if (seoDescriptionValue != null) {
                seoData.put("description", seoDescriptionValue);
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
        productData.put("id", productIdValue);

        Map<String, Object> requestBody = Map.of("product", productData);

        URI uri = buildApiUrl(runContext, "/products/" + productIdValue + ".json");
        HttpRequest request = buildAuthenticatedRequest(runContext, "PUT", uri, requestBody);

        runContext.logger().debug("Updating product {} in Shopify API: {}", productIdValue, uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> updatedProductData = (Map<String, Object>) responseData.get("product");
        
        if (updatedProductData == null) {
        throw new RuntimeException("Failed to update product - no product data returned");
        }
        
        Product updatedProduct = OBJECT_MAPPER.convertValue(updatedProductData, Product.class);

        runContext.logger().info("Updated product '{}' (ID: {}) in Shopify", 
        updatedProduct.getTitle(), updatedProduct.getId());

        return Output.builder()
        .product(updatedProduct)
        .build();
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
