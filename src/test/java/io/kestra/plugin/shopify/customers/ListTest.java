package io.kestra.plugin.shopify.customers;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.utils.TestsUtils;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.common.FetchType;
import io.kestra.plugin.shopify.models.Customer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@KestraTest
class ListTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @EnabledIfEnvironmentVariable(named = "SHOPIFY_STORE_DOMAIN", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "SHOPIFY_ACCESS_TOKEN", matches = ".*")
    void testListCustomers() throws Exception {
        String storeDomain = System.getenv("SHOPIFY_STORE_DOMAIN");
        String accessToken = System.getenv("SHOPIFY_ACCESS_TOKEN");

        List task = List.builder()
            .id("test-task")
            .type(List.class.getName())
            .storeDomain(Property.of(storeDomain))
            .accessToken(Property.of(accessToken))
            .limit(Property.of(5))
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
        List.Output output = task.run(runContext);

        assertThat(output, notNullValue());
        assertThat(output.getCount(), greaterThanOrEqualTo(0));
        assertThat(output.getCustomers(), notNullValue());
        
        if (!output.getCustomers().isEmpty()) {
            Customer firstCustomer = output.getCustomers().get(0);
            assertThat(firstCustomer.getId(), notNullValue());
            assertThat(firstCustomer.getEmail(), notNullValue());
        }
    }

    @Test
    void testListCustomersRequiredFields() {
        // Test that storeDomain is required
        assertThrows(Exception.class, () -> {
            List task = List.builder()
                .id("test-task")
                .type(List.class.getName())
                .accessToken(Property.of("test-token"))
                .build();

            RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
            task.run(runContext);
        });

        // Test that accessToken is required
        assertThrows(Exception.class, () -> {
            List task = List.builder()
                .id("test-task")
                .type(List.class.getName())
                .storeDomain(Property.of("test-store.myshopify.com"))
                .build();

            RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
            task.run(runContext);
        });
    }

    @Test
    void testTaskConfiguration() {
        // Test that task can be properly configured
        List task = List.builder()
            .id("test-task")
            .type(List.class.getName())
            .storeDomain(Property.of("test-store.myshopify.com"))
            .accessToken(Property.of("test-token"))
            .limit(Property.of(10))
            .fetchType(Property.of(FetchType.FETCH))
            .build();

        assertThat(task.getStoreDomain(), notNullValue());
        assertThat(task.getAccessToken(), notNullValue());
        assertThat(task.getLimit(), notNullValue());
        assertThat(task.getFetchType(), notNullValue());
    }

    @Test
    void testFetchTypeConfiguration() {
        // Test FetchType property configuration
        List task1 = List.builder()
            .storeDomain(Property.of("test-store.myshopify.com"))
            .accessToken(Property.of("test-token"))
            .fetchType(Property.of(FetchType.FETCH))
            .build();

        List task2 = List.builder()
            .storeDomain(Property.of("test-store.myshopify.com"))
            .accessToken(Property.of("test-token"))
            .fetchType(Property.of(FetchType.STORE))
            .build();

        assertNotNull(task1.getFetchType());
        assertNotNull(task2.getFetchType());
    }
}