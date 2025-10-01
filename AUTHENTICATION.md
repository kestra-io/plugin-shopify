# Shopify Authentication Setup Guide

This guide walks you through setting up authentication for the Kestra Shopify plugin.

## Prerequisites

- A Shopify store (any plan)
- Admin access to your Shopify store
- Kestra installation with secrets management

## Step 1: Create a Private App

1. **Log into your Shopify Admin**
2. **Navigate to Apps**:
   - Go to `Apps` in the left sidebar
   - Click on `App and sales channel settings`
   - Click on `Develop apps`

3. **Create a Private App**:
   - Click `Create an app`
   - Enter app name: `Kestra Integration`
   - Enter app developer: Your name or company

4. **Configure App Overview** (Optional):
   - Add app description
   - Set app URL (optional)

## Step 2: Configure Admin API Access

1. **Click on "Configuration" tab**
2. **Configure Admin API access scopes**:

   For **Products**:
   - `read_products` - Required for ListProducts, GetProduct
   - `write_products` - Required for CreateProduct, UpdateProduct, DeleteProduct

   For **Orders**:
   - `read_orders` - Required for ListOrders, GetOrder
   - `write_orders` - Required for CreateOrder, DeleteOrder

   For **Customers**:
   - `read_customers` - Required for ListCustomers, GetCustomer
   - `write_customers` - Required for CreateCustomer, DeleteCustomer

   **Minimal Setup** (for testing):
   ```
   read_products
   read_orders
   read_customers
   ```

   **Full Access** (for production):
   ```
   read_products, write_products
   read_orders, write_orders
   read_customers, write_customers
   ```

3. **Save the configuration**

## Step 3: Install the Private App

1. **Click "Install app"**
2. **Review permissions** and click "Install"
3. **Copy the Admin API access token**:
   - This will be shown only once
   - Store it securely (you'll need it for Kestra)

## Step 4: Configure Kestra Secrets

Add your Shopify credentials to Kestra secrets:

### Via Kestra UI:
1. Go to Administration → Secrets
2. Add secrets:
   - Key: `SHOPIFY_STORE_DOMAIN`
   - Value: `your-store.myshopify.com`
   
   - Key: `SHOPIFY_ACCESS_TOKEN`  
   - Value: `shpat_xxxxxxxxxxxxxxxxxx` (your access token)

### Via Kestra CLI:
```bash
kestra secrets update SHOPIFY_STORE_DOMAIN your-store.myshopify.com
kestra secrets update SHOPIFY_ACCESS_TOKEN shpat_xxxxxxxxxxxxxxxxxx
```

### Via Environment Variables:
```bash
export KESTRA_SECRET_SHOPIFY_STORE_DOMAIN=your-store.myshopify.com
export KESTRA_SECRET_SHOPIFY_ACCESS_TOKEN=shpat_xxxxxxxxxxxxxxxxxx
```

## Step 5: Test the Connection

Create a simple test flow:

```yaml
id: test_shopify_connection
namespace: test

tasks:
  - id: test_products
    type: io.kestra.plugin.shopify.products.ListProducts
    storeDomain: "{{ secret('SHOPIFY_STORE_DOMAIN') }}"
    accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
    limit: 1

  - id: log_result
    type: io.kestra.core.tasks.log.Log
    message: "✅ Shopify connection successful! Found {{ outputs.test_products.count }} products."
```

## Security Best Practices

1. **Use Secrets Management**: Never hardcode credentials in flows
2. **Limit Permissions**: Only grant necessary API scopes
3. **Rotate Tokens**: Regularly regenerate access tokens
4. **Monitor Usage**: Review API usage in Shopify Admin
5. **Use HTTPS**: Ensure all API calls use HTTPS (automatic with this plugin)

## Troubleshooting

### Common Errors

**`401 Unauthorized`**:
- Check that your access token is correct
- Verify the token hasn't expired
- Ensure the private app is installed

**`403 Forbidden`**:
- Check that you have the required API scopes
- Verify the store domain is correct
- Ensure your Shopify plan supports the API features

**`404 Not Found`**:
- Verify the store domain format: `store-name.myshopify.com`
- Check that the store exists and is active

**Rate Limiting (`429 Too Many Requests`)**:
- The plugin handles this automatically with delays
- If persistent, reduce request frequency

### Testing Credentials

Test your credentials with curl:

```bash
curl -H "X-Shopify-Access-Token: YOUR_ACCESS_TOKEN" \
     "https://your-store.myshopify.com/admin/api/2023-10/products.json?limit=1"
```

### Getting Help

- Check Shopify's [Admin API documentation](https://shopify.dev/docs/api/admin-rest)
- Review [Shopify's authentication guide](https://shopify.dev/docs/api/admin-rest#authentication)
- Consult Kestra's [secrets documentation](https://kestra.io/docs/concepts/secrets)

## API Version Compatibility

The plugin supports modern Shopify API versions:
- `2023-10` (default)
- `2023-07`
- `2023-04`
- `2023-01`

Configure via the `apiVersion` parameter:

```yaml
- id: list_products
  type: io.kestra.plugin.shopify.products.ListProducts
  storeDomain: "{{ secret('SHOPIFY_STORE_DOMAIN') }}"
  accessToken: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"
  apiVersion: "2023-10"
```