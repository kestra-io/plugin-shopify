# Kestra Shopify Plugin

## What

- Provides plugin components under `io.kestra.plugin.shopify`.
- Includes classes such as `Delete`, `List`, `Create`, `Get`.

## Why

- What user problem does this solve? Teams need to call the Shopify Admin API to manage products, orders, and customers from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps Shopify steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on Shopify.

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
