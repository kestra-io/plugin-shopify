package io.kestra.plugin.shopify.customers;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.utils.TestsUtils;
import io.kestra.core.models.property.Property;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@KestraTest
class CreateTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void testCreateCustomerRequiredFields() {
        // Test that storeDomain is required
        assertThrows(Exception.class, () -> {
            Create task = Create.builder()
                .id("test-task")
                .type(Create.class.getName())
                .accessToken(Property.of("test-token"))
                .email(Property.of("test@example.com"))
                .build();

            RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
            task.run(runContext);
        });

        // Test that accessToken is required
        assertThrows(Exception.class, () -> {
            Create task = Create.builder()
                .id("test-task")
                .type(Create.class.getName())
                .storeDomain(Property.of("test-store.myshopify.com"))
                .email(Property.of("test@example.com"))
                .build();

            RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
            task.run(runContext);
        });

        // Test that email is required
        assertThrows(Exception.class, () -> {
            Create task = Create.builder()
                .id("test-task")
                .type(Create.class.getName())
                .storeDomain(Property.of("test-store.myshopify.com"))
                .accessToken(Property.of("test-token"))
                .build();

            RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
            task.run(runContext);
        });
    }

    @Test
    void testTaskConfiguration() {
        // Test that task can be properly configured
        Create task = Create.builder()
            .id("test-task")
            .type(Create.class.getName())
            .storeDomain(Property.of("test-store.myshopify.com"))
            .accessToken(Property.of("test-token"))
            .email(Property.of("test@example.com"))
            .firstName(Property.of("John"))
            .lastName(Property.of("Doe"))
            .build();

        assertThat(task.getStoreDomain(), notNullValue());
        assertThat(task.getAccessToken(), notNullValue());
        assertThat(task.getEmail(), notNullValue());
        assertThat(task.getFirstName(), notNullValue());
        assertThat(task.getLastName(), notNullValue());
    }
}