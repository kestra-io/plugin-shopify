package io.kestra.plugin.shopify.products;

import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.utils.TestsUtils;
import io.kestra.plugin.shopify.models.Product;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@MicronautTest
class ListProductsTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @EnabledIfEnvironmentVariable(named = "SHOPIFY_STORE_DOMAIN", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "SHOPIFY_ACCESS_TOKEN", matches = ".*")
    void testListProducts() throws Exception {
        String storeDomain = System.getenv("SHOPIFY_STORE_DOMAIN");
        String accessToken = System.getenv("SHOPIFY_ACCESS_TOKEN");

        ListProducts task = ListProducts.builder()
            .storeDomain(storeDomain)
            .accessToken(accessToken)
            .limit(5)
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
        ListProducts.Output output = task.run(runContext);

        assertThat(output, notNullValue());
        assertThat(output.getCount(), greaterThanOrEqualTo(0));
        assertThat(output.getProducts(), notNullValue());
        
        if (!output.getProducts().isEmpty()) {
            Product firstProduct = output.getProducts().get(0);
            assertThat(firstProduct.getId(), notNullValue());
            assertThat(firstProduct.getTitle(), notNullValue());
        }
    }

    @Test
    void testListProductsRequiredFields() {
        ListProducts task = ListProducts.builder()
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, Map.of());
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            task.run(runContext);
        });
    }
}