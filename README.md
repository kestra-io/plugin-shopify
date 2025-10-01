# Shopify Plugin for Kestra

<p align="center">
    <a href="https://twitter.com/kestra_io"><img height="25" src="https://kestra.io/twitter.svg" alt="twitter" /></a> &nbsp;
    <a href="https://www.linkedin.com/company/kestra/"><img height="25" src="https://kestra.io/linkedin.svg" alt="linkedin" /></a> &nbsp;
<a href="https://www.youtube.com/@kestra-io"><img height="25" src="https://kestra.io/youtube.svg" alt="youtube" /></a> &nbsp;
</p>

<br />
<p align="center">
    <a href="https://go.kestra.io/video/product-overview" target="_blank">
        <img src="https://kestra.io/startvideo.png" alt="Get started in 4 minutes with Kestra" width="640px" />
    </a>
</p>
<p align="center" style="color:grey;"><i>Get started with Kestra in 4 minutes.</i></p>


# Shopify Plugin for Kestra

A comprehensive Kestra plugin for integrating with Shopify's Admin API. This plugin enables automation scenarios such as syncing products, managing orders, handling customers, and triggering workflows on new orders.

## Features

### 🛍️ Products
- **ListProducts** - Retrieve products with filtering and pagination
- **GetProduct** - Get detailed information about a specific product
- **CreateProduct** - Create new products with full metadata
- **UpdateProduct** - Update existing product information
- **DeleteProduct** - Remove products from your store

### 📦 Orders
- **ListOrders** - Retrieve orders with comprehensive filtering options
- **GetOrder** - Get detailed order information including line items
- **CreateOrder** - Create new orders programmatically
- **DeleteOrder** - Remove orders from your store

### 👤 Customers
- **ListCustomers** - Retrieve customer information with filtering
- **GetCustomer** - Get detailed customer profiles
- **CreateCustomer** - Add new customers with addresses and metadata
- **DeleteCustomer** - Remove customers from your store

### 🔔 Triggers
- **OrderCreated** - Automatically trigger workflows when new orders are created

## Installation

Add the plugin dependency to your Kestra installation:

```xml
<dependency>
    <groupId>io.kestra.plugin</groupId>
    <artifactId>plugin-shopify</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Configuration

### Authentication

All tasks require Shopify Admin API credentials:

- **storeDomain**: Your Shopify store domain (e.g., `my-store.myshopify.com`)
- **accessToken**: Private app access token with appropriate permissions

### Rate Limiting

The plugin automatically handles Shopify's API rate limits (2 requests per second) with built-in delays between requests.

## Quick Start Examples

### List Products
```yaml
id: list_shopify_products
namespace: company.team

tasks:
  - id: get_products
    type: io.kestra.plugin.shopify.products.ListProducts
    storeDomain: my-store.myshopify.com
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
    limit: 50
    status: active
```

### Create Product
```yaml
id: create_shopify_product
namespace: company.team

tasks:
  - id: create_product
    type: io.kestra.plugin.shopify.products.CreateProduct
    storeDomain: my-store.myshopify.com
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
    title: "New T-Shirt"
    bodyHtml: "<p>A comfortable cotton t-shirt</p>"
    vendor: "My Brand"
    productType: "Clothing"
    tags: "t-shirt,clothing,cotton"
    status: "active"
```

### Process New Orders (Trigger)
```yaml
id: process_new_orders
namespace: company.team

triggers:
  - id: order_created
    type: io.kestra.plugin.shopify.triggers.OrderCreated
    storeDomain: my-store.myshopify.com
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
    interval: PT5M
    financialStatus: paid

tasks:
  - id: process_order
    type: io.kestra.core.tasks.log.Log
    message: "New paid order: {{ trigger.order.name }} for ${{ trigger.order.totalPrice }}"
```

### Create Customer
```yaml
id: create_shopify_customer
namespace: company.team

tasks:
  - id: create_customer
    type: io.kestra.plugin.shopify.customers.CreateCustomer
    storeDomain: my-store.myshopify.com
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
    email: "customer@example.com"
    firstName: "John"
    lastName: "Doe"
    acceptsMarketing: true
    addresses:
      - firstName: "John"
        lastName: "Doe"
        address1: "123 Main St"
        city: "New York"
        province: "NY"
        country: "United States"
        zip: "10001"
        defaultAddress: true
```

## Advanced Examples

### Complete Product Management Workflow
```yaml
id: shopify_product_workflow
namespace: company.team

tasks:
  - id: list_products
    type: io.kestra.plugin.shopify.products.ListProducts
    storeDomain: "{{ secret('SHOPIFY_STORE_DOMAIN') }}"
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
    limit: 10

  - id: create_product
    type: io.kestra.plugin.shopify.products.CreateProduct
    storeDomain: "{{ secret('SHOPIFY_STORE_DOMAIN') }}"
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
    title: "Automated Product"
    bodyHtml: "<p>Created via Kestra automation</p>"
    vendor: "Kestra"
    status: "active"

  - id: update_product
    type: io.kestra.plugin.shopify.products.UpdateProduct
    storeDomain: "{{ secret('SHOPIFY_STORE_DOMAIN') }}"
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
    productId: "{{ outputs.create_product.product.id }}"
    title: "Updated Automated Product"
    tags: "updated,automation"
```

### Order Processing with Customer Data
```yaml
id: process_paid_orders
namespace: company.team

tasks:
  - id: get_recent_orders
    type: io.kestra.plugin.shopify.orders.ListOrders
    storeDomain: "{{ secret('SHOPIFY_STORE_DOMAIN') }}"
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
    financialStatus: paid
    limit: 20

  - id: process_each_order
    type: io.kestra.core.tasks.flows.ForEach
    value: "{{ outputs.get_recent_orders.orders }}"
    tasks:
      - id: get_customer
        type: io.kestra.plugin.shopify.customers.GetCustomer
        storeDomain: "{{ secret('SHOPIFY_STORE_DOMAIN') }}"
        accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
        customerId: "{{ taskrun.value.customer.id }}"

      - id: process_order
        type: io.kestra.core.tasks.log.Log
        message: |
          Processing order {{ taskrun.value.name }}:
          Customer: {{ outputs.get_customer.customer.email }}
          Total: ${{ taskrun.value.totalPrice }}
```

## Testing

The plugin includes comprehensive tests that can be run with real Shopify credentials:

```bash
export SHOPIFY_STORE_DOMAIN=your-store.myshopify.com
export SHOPIFY_ACCESS_TOKEN=your-access-token
./gradlew test
```

## Authentication Setup

1. **Create a Private App** in your Shopify Admin:
   - Go to Apps → App and sales channel settings → Develop apps
   - Create a private app
   - Configure Admin API access scopes:
     - `read_products`, `write_products`
     - `read_orders`, `write_orders`
     - `read_customers`, `write_customers`

2. **Get Access Token**:
   - Install the private app
   - Copy the Admin API access token
   - Store it securely in Kestra secrets

## Error Handling

The plugin provides comprehensive error handling:
- **Rate limiting**: Automatic delays between requests
- **Authentication errors**: Clear error messages for invalid tokens
- **API errors**: Detailed error responses from Shopify
- **Validation**: Input validation for required fields

## API Version Support

- Default API version: `2023-10`
- Configurable via the `apiVersion` parameter
- Supports all modern Shopify Admin API versions

## Shopify Webhook Integration

While this plugin provides polling-based triggers, for real-time notifications consider:
1. Setting up Shopify webhooks pointing to Kestra webhook triggers
2. Using the `OrderCreated` trigger for periodic polling
3. Combining both approaches for comprehensive order processing

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## Support

- **Documentation**: [Kestra Plugin Developer Guide](https://kestra.io/docs/plugin-developer-guide)
- **Shopify API**: [Shopify Admin API Documentation](https://shopify.dev/docs/api/admin-rest)
- **Issues**: Report issues on GitHub

## License

This plugin is licensed under the Apache License 2.0.

![Kestra orchestrator](https://kestra.io/video.gif)

## Running the project in local
### Prerequisites
- Java 21
- Docker

### Running tests
```
./gradlew check --parallel
```

### Development

`VSCode`:

Follow the README.md within the `.devcontainer` folder for a quick and easy way to get up and running with developing plugins if you are using VSCode.

`Other IDEs`:

```
./gradlew shadowJar && docker build -t kestra-custom . && docker run --rm -p 8080:8080 kestra-custom server local
```
> [!NOTE]
> You need to relaunch this whole command everytime you make a change to your plugin

go to http://localhost:8080, your plugin will be available to use

## Documentation
* Full documentation can be found under: [kestra.io/docs](https://kestra.io/docs)
* Documentation for developing a plugin is included in the [Plugin Developer Guide](https://kestra.io/docs/plugin-developer-guide/)


## License
Apache 2.0 © [Kestra Technologies](https://kestra.io)


## Stay up to date

We release new versions every month. Give the [main repository](https://github.com/kestra-io/kestra) a star to stay up to date with the latest releases and get notified about future updates.

![Star the repo](https://kestra.io/star.gif)
