package io.kestra.plugin.shopify.models;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ProductStatusTest {

    @Test
    void testProductStatusValues() {
        ProductStatus[] values = ProductStatus.values();
        
        assertThat(values, notNullValue());
        assertThat(values.length, greaterThan(0));
        
        // Test specific values exist
        assertThat(ProductStatus.valueOf("ACTIVE"), equalTo(ProductStatus.ACTIVE));
        assertThat(ProductStatus.valueOf("ARCHIVED"), equalTo(ProductStatus.ARCHIVED));
        assertThat(ProductStatus.valueOf("DRAFT"), equalTo(ProductStatus.DRAFT));
    }

    @Test
    void testProductStatusToString() {
        assertThat(ProductStatus.ACTIVE.toString(), equalTo("ACTIVE"));
        assertThat(ProductStatus.ARCHIVED.toString(), equalTo("ARCHIVED"));
        assertThat(ProductStatus.DRAFT.toString(), equalTo("DRAFT"));
    }
}

class PublishedStatusTest {

    @Test
    void testPublishedStatusValues() {
        PublishedStatus[] values = PublishedStatus.values();
        
        assertThat(values, notNullValue());
        assertThat(values.length, greaterThan(0));
        
        // Test specific values exist
        assertThat(PublishedStatus.valueOf("PUBLISHED"), equalTo(PublishedStatus.PUBLISHED));
        assertThat(PublishedStatus.valueOf("UNPUBLISHED"), equalTo(PublishedStatus.UNPUBLISHED));
        assertThat(PublishedStatus.valueOf("ANY"), equalTo(PublishedStatus.ANY));
    }

    @Test
    void testPublishedStatusToString() {
        assertThat(PublishedStatus.PUBLISHED.toString(), equalTo("PUBLISHED"));
        assertThat(PublishedStatus.UNPUBLISHED.toString(), equalTo("UNPUBLISHED"));
        assertThat(PublishedStatus.ANY.toString(), equalTo("ANY"));
    }
}