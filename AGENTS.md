# Kestra Shopify Plugin

## What

- Provides plugin components under `io.kestra.plugin.shopify`.
- Includes classes such as `Delete`, `List`, `Create`, `Get`.

## Why

- This plugin integrates Kestra with Shopify Customer.
- It provides tasks that create, list, fetch, or delete Shopify customers.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `shopify`

Infrastructure dependencies (Docker Compose services):

- `app`

### Key Plugin Classes

- `io.kestra.plugin.shopify.customers.Create`
- `io.kestra.plugin.shopify.customers.Delete`
- `io.kestra.plugin.shopify.customers.Get`
- `io.kestra.plugin.shopify.customers.List`
- `io.kestra.plugin.shopify.orders.Delete`
- `io.kestra.plugin.shopify.orders.List`
- `io.kestra.plugin.shopify.products.List`
- `io.kestra.plugin.shopify.products.Update`

### Project Structure

```
plugin-shopify/
├── src/main/java/io/kestra/plugin/shopify/products/
├── src/test/java/io/kestra/plugin/shopify/products/
├── build.gradle
└── README.md
```

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines
