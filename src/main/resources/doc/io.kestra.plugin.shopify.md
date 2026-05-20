# How to use the Shopify plugin

Manage customers, orders, and products in Shopify from Kestra flows.

## Authentication

Set `storeDomain` to your Shopify store domain (e.g. `my-store.myshopify.com`) and `accessToken` to your Admin API access token. Store secrets in [secrets](https://kestra.io/docs/concepts/secret) and apply connection properties globally with [plugin defaults](https://kestra.io/docs/workflow-components/plugin-defaults).

## Tasks

**Customers** — `customers.Create` creates a customer with `email` (required) plus optional `firstName`, `lastName`, and `phone`. `customers.Get` retrieves a customer by `customerId`. `customers.List` returns customers — control result handling with `fetchType` (default `FETCH`) and bound results with `limit`. `customers.Delete` removes a customer by `customerId`.

**Orders** — `orders.List` returns orders filtered by `status`, `financialStatus`, `fulfillmentStatus`, date ranges (`createdAtMin`/`createdAtMax`, `updatedAtMin`/`updatedAtMax`), or `sinceId`. Control result handling with `fetchType` (default `FETCH`). `orders.Delete` removes an order by `orderId`.

**Products** — `products.List` returns products filtered by `status`, `publishedStatus`, `productType`, `vendor`, `handle`, or date ranges. `products.Update` updates a product by `productId` — set fields such as `title`, `bodyHtml`, `vendor`, `tags`, `status`, and `seoTitle`/`seoDescription`.
