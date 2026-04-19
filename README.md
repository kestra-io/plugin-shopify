

# Shopify Plugin for Kestra

## Why

- What user problem does this solve? Teams need to call the Shopify Admin API to manage products, orders, and customers from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps Shopify steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on Shopify.

## What

- Provides plugin components under `io.kestra.plugin.shopify`.
- Includes classes such as `Delete`, `List`, `Create`, `Get`.

## Documentation

For detailed documentation on each task and trigger, refer to the individual class documentation and examples provided in the plugin code.

## Rate Limiting

The plugin includes built-in rate limiting to respect Shopify's API limits. You can configure the rate limit delay using the `rateLimitDelay` property on each task.

## Error Handling

All tasks include comprehensive error handling and will throw descriptive exceptions for common API errors, authentication issues, and validation problems.
