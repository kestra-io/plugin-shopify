#!/bin/bash
set -e

echo "=== Applying comprehensive final fixes ==="

# Fix test files to use @KestraTest
echo "1. Fixing test annotations..."
for test_file in src/test/java/io/kestra/plugin/shopify/*/*.java; do
    if [ -f "$test_file" ]; then
        sed -i '' 's/@MicronautTest/@KestraTest/g' "$test_file"
        echo "Fixed test annotations in $test_file"
    fi
done

# Fix all task examples to use correct class names
echo "2. Fixing task examples..."
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/CreateCustomer/Create/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/DeleteCustomer/Delete/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/GetCustomer/Get/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/ListCustomers/List/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/CreateProduct/Create/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/DeleteProduct/Delete/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/GetProduct/Get/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/ListProducts/List/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/UpdateProduct/Update/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/CreateOrder/Create/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/DeleteOrder/Delete/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/GetOrder/Get/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/ListOrders/List/g' {} \;

# Fix YAML examples formatting
echo "3. Fixing YAML example formatting..."
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/        id:/                id:/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/        namespace:/                namespace:/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/        tasks:/                tasks:/g' {} \;
find src/main/java/io/kestra/plugin/shopify -name "*.java" -exec sed -i '' 's/          - id:/                  - id:/g' {} \;

# Fix README.md to remove suffix references
echo "4. Fixing README.md..."
cat > README.md << 'EOF'
# Shopify Plugin for Kestra

A comprehensive plugin for integrating with Shopify Admin API, enabling complete management of products, orders, and customers directly from your Kestra workflows.

## Features

### 🛍️ Products
- **List** - Retrieve products with filtering and pagination
- **Get** - Fetch a specific product by ID
- **Create** - Create new products with variants and images  
- **Update** - Update existing product information
- **Delete** - Remove products from your store

### 📦 Orders
- **List** - Retrieve orders with filtering and pagination
- **Get** - Fetch a specific order by ID
- **Create** - Create new orders programmatically
- **Delete** - Cancel or remove orders

### 👥 Customers
- **List** - Retrieve customers with filtering and pagination
- **Get** - Fetch a specific customer by ID
- **Create** - Create new customer accounts
- **Delete** - Remove customer accounts

### ⚡ Triggers
- **OrderCreated** - Polling trigger for new orders in your store

## Authentication

This plugin requires a Shopify Admin API access token. You can obtain this by:

1. Creating a private app in your Shopify admin
2. Generating an Admin API access token with appropriate permissions
3. Storing the token securely using Kestra's secret management

Required permissions:
- `read_products` and `write_products` for product operations
- `read_orders` and `write_orders` for order operations  
- `read_customers` and `write_customers` for customer operations

## Quick Start

```yaml
id: shopify_example
namespace: company.team

tasks:
  - id: get_products
    type: io.kestra.plugin.shopify.products.List
    storeDomain: my-store.myshopify.com
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
    limit: 10
    status: ACTIVE
```

## Documentation

For detailed documentation on each task and trigger, refer to the individual class documentation and examples provided in the plugin code.

## Rate Limiting

The plugin includes built-in rate limiting to respect Shopify's API limits. You can configure the rate limit delay using the `rateLimitDelay` property on each task.

## Error Handling

All tasks include comprehensive error handling and will throw descriptive exceptions for common API errors, authentication issues, and validation problems.
EOF

echo "=== All fixes completed successfully! ==="