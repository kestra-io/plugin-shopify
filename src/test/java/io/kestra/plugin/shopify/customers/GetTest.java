package io.kestra.plugin.shopify.customers;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.utils.TestsUtils;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@KestraTest
class GetTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void testGetCustomerRequiredFields() {
        // Test that storeDomain is required
        assertThrows(Exception.class, () ->
        {
            Get task = Get.builder()
                .id("test-task")
                .type(Get.class.getName())
                .accessToken(Property.ofValue("test-token"))
                .customerId(Property.ofValue(123L))
                .build();

            RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
            task.run(runContext);
        });

        // Test that accessToken is required
        assertThrows(Exception.class, () ->
        {
            Get task = Get.builder()
                .id("test-task")
                .type(Get.class.getName())
                .storeDomain(Property.ofValue("test-store.myshopify.com"))
                .customerId(Property.ofValue(123L))
                .build();

            RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
            task.run(runContext);
        });

        // Test that customerId is required
        assertThrows(Exception.class, () ->
        {
            Get task = Get.builder()
                .id("test-task")
                .type(Get.class.getName())
                .storeDomain(Property.ofValue("test-store.myshopify.com"))
                .accessToken(Property.ofValue("test-token"))
                .build();

            RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
            task.run(runContext);
        });
    }

    @Test
    void testTaskConfiguration() {
        // Test that task can be properly configured
        Get task = Get.builder()
            .id("test-task")
            .type(Get.class.getName())
            .storeDomain(Property.ofValue("test-store.myshopify.com"))
            .accessToken(Property.ofValue("test-token"))
            .customerId(Property.ofValue(123L))
            .build();

        assertThat(task.getStoreDomain(), notNullValue());
        assertThat(task.getAccessToken(), notNullValue());
        assertThat(task.getCustomerId(), notNullValue());
    }
}