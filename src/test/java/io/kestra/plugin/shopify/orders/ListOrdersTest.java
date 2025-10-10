package io.kestra.plugin.shopify.orders;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.utils.TestsUtils;
import io.kestra.core.models.property.Property;
import io.kestra.plugin.shopify.models.Order;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class ListOrdersTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @EnabledIfEnvironmentVariable(named = "SHOPIFY_STORE_DOMAIN", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "SHOPIFY_ACCESS_TOKEN", matches = ".*")
    void testListOrders() throws Exception {
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
        assertThat(output.getOrders(), notNullValue());
        
        if (!output.getOrders().isEmpty()) {
            Order firstOrder = output.getOrders().get(0);
            assertThat(firstOrder.getId(), notNullValue());
            assertThat(firstOrder.getName(), notNullValue());
        }
    }

    @Test
    void testListOrdersRequiredFields() {
        // Test task validation - storeDomain is required
        List task = List.builder()
            .id("test-task")
            .type(List.class.getName())
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
        
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            task.run(runContext);
        });
    }
}