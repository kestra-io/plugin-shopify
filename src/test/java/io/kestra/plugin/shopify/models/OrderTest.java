package io.kestra.plugin.shopify.models;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class OrderTest {

    @Test
    void testOrderCreation() {
        Order order = Order.builder()
            .id(123L)
            .name("#1001")
            .email("customer@example.com")
            .totalPrice("99.99")
            .currency("USD")
            .build();

        assertThat(order.getId(), equalTo(123L));
        assertThat(order.getName(), equalTo("#1001"));
        assertThat(order.getEmail(), equalTo("customer@example.com"));
        assertThat(order.getTotalPrice(), equalTo("99.99"));
        assertThat(order.getCurrency(), equalTo("USD"));
    }

    @Test
    void testOrderBuilder() {
        Order order = Order.builder()
            .id(456L)
            .name("#1002")
            .build();

        assertThat(order.getId(), notNullValue());
        assertThat(order.getName(), notNullValue());
        assertThat(order.getId(), equalTo(456L));
        assertThat(order.getName(), equalTo("#1002"));
    }
}