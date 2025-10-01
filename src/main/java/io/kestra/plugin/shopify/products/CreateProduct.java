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
    title = "Create a new product in Shopify store",
    description = "Create a new product with the specified details."
)
@Plugin(
    examples = {
        @Example(
        title = "Create a simple product",
        full = true,
        code = """
        id: shopify_create_product
        namespace: company.team

        tasks:
          - id: create_product
            type: io.kestra.plugin.shopify.products.CreateProduct
            storeDomain: my-store.myshopify.com
            accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
            title: "New T-Shirt"
            bodyHtml: "<p>A comfortable cotton t-shirt</p>"
            vendor: "My Brand"
            productType: "Clothing"
            tags: "t-shirt,clothing,cotton"
            status: "active"
        """
        )
    }
)
public class CreateProduct extends AbstractShopifyTask implements RunnableTask<CreateProduct.Output> {

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
    @Builder.Default
    private Property<String> status = Property.of("draft");

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
        // Build product object
        Map<String, Object> productData = new java.util.HashMap<>();
        
        String titleValue = runContext.render(title).as(String.class).orElse(null);
        if (titleValue == null || titleValue.trim().isEmpty()) {
        throw new IllegalArgumentException("Product title is required");
        }
        productData.put("title", titleValue);

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

        String statusValue = runContext.render(status).as(String.class).orElse("draft");
        productData.put("status", statusValue);

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

        Map<String, Object> requestBody = Map.of("product", productData);

        URI uri = buildApiUrl(runContext, "/products.json");
        HttpRequest request = buildAuthenticatedRequest(runContext, "POST", uri, requestBody);

        runContext.logger().debug("Creating product '{}' in Shopify API: {}", titleValue, uri);
        
        handleRateLimit();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = parseResponse(response);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> createdProductData = (Map<String, Object>) responseData.get("product");
        
        if (createdProductData == null) {
        throw new RuntimeException("Failed to create product - no product data returned");
        }
        
        Product createdProduct = OBJECT_MAPPER.convertValue(createdProductData, Product.class);

        runContext.logger().info("Created product '{}' (ID: {}) in Shopify", 
        createdProduct.getTitle(), createdProduct.getId());

        return Output.builder()
        .product(createdProduct)
        .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
        title = "Created product",
        description = "The product that was created in Shopify"
        )
        private final Product product;
    }
