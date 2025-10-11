package io.kestra.plugin.shopify.customers;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.utils.TestsUtils;
import io.kestra.core.models.property.Property;
import io.kestra.plugin.shopify.models.Customer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class ListCustomersTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @EnabledIfEnvironmentVariable(named = "SHOPIFY_STORE_DOMAIN", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "SHOPIFY_ACCESS_TOKEN", matches = ".*")
    void testListCustomers() throws Exception {
        String storeDomain = System.getenv("SHOPIFY_STORE_DOMAIN");
        String accessToken = System.getenv("SHOPIFY_ACCESS_TOKEN");

        ListCustomers task = ListCustomers.builder()
            .id("test-task")
            .type(ListCustomers.class.getName())
            .storeDomain(Property.of(storeDomain))
            .accessToken(Property.of(accessToken))
            .limit(Property.of(5))
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
        ListCustomers.Output output = task.run(runContext);

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
        // Test task validation - storeDomain is required
        ListCustomers task = ListCustomers.builder()
            .id("test-task")
            .type(ListCustomers.class.getName())
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
        
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            task.run(runContext);
        });
    }
}