package io.kestra.plugin.shopify.orders;

import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.utils.TestsUtils;
import io.kestra.plugin.shopify.models.Order;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@MicronautTest
class ListOrdersTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @EnabledIfEnvironmentVariable(named = "SHOPIFY_STORE_DOMAIN", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "SHOPIFY_ACCESS_TOKEN", matches = ".*")
    void testListOrders() throws Exception {
        String storeDomain = System.getenv("SHOPIFY_STORE_DOMAIN");
        String accessToken = System.getenv("SHOPIFY_ACCESS_TOKEN");

        ListOrders task = ListOrders.builder()
            .storeDomain(storeDomain)
            .accessToken(accessToken)
            .limit(5)
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
        ListOrders.Output output = task.run(runContext);

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
        ListOrders task = ListOrders.builder()
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            task.run(runContext);
        });
    }
}