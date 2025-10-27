package io.kestra.plugin.shopify.models;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void testCustomerCreation() {
        Customer customer = Customer.builder()
            .id(123L)
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .phone("123-456-7890")
            .build();

        assertThat(customer.getId(), equalTo(123L));
        assertThat(customer.getEmail(), equalTo("test@example.com"));
        assertThat(customer.getFirstName(), equalTo("John"));
        assertThat(customer.getLastName(), equalTo("Doe"));
        assertThat(customer.getPhone(), equalTo("123-456-7890"));
    }

    @Test
    void testCustomerBuilder() {
        Customer customer = Customer.builder()
            .id(456L)
            .email("jane@example.com")
            .build();

        assertThat(customer.getId(), notNullValue());
        assertThat(customer.getEmail(), notNullValue());
        assertThat(customer.getId(), equalTo(456L));
        assertThat(customer.getEmail(), equalTo("jane@example.com"));
    }

    @Test
    void testCustomerEquality() {
        Customer customer1 = Customer.builder()
            .id(123L)
            .email("test@example.com")
            .build();

        Customer customer2 = Customer.builder()
            .id(123L)
            .email("test@example.com")
            .build();

        assertThat(customer1, equalTo(customer2));
        assertThat(customer1.hashCode(), equalTo(customer2.hashCode()));
    }
}