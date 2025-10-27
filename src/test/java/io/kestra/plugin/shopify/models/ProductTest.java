package io.kestra.plugin.shopify.models;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ProductTest {

    @Test
    void testProductCreation() {
        Product product = Product.builder()
            .id(123L)
            .title("Test Product")
            .handle("test-product")
            .bodyHtml("<p>Product description</p>")
            .vendor("Test Vendor")
            .productType("Test Type")
            .build();

        assertThat(product.getId(), equalTo(123L));
        assertThat(product.getTitle(), equalTo("Test Product"));
        assertThat(product.getHandle(), equalTo("test-product"));
        assertThat(product.getBodyHtml(), equalTo("<p>Product description</p>"));
        assertThat(product.getVendor(), equalTo("Test Vendor"));
        assertThat(product.getProductType(), equalTo("Test Type"));
    }

    @Test
    void testProductBuilder() {
        Product product = Product.builder()
            .id(456L)
            .title("Another Product")
            .build();

        assertThat(product.getId(), notNullValue());
        assertThat(product.getTitle(), notNullValue());
        assertThat(product.getId(), equalTo(456L));
        assertThat(product.getTitle(), equalTo("Another Product"));
    }
}