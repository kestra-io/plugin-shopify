# Shopify Plugin QA Testing Guide

This document provides comprehensive testing workflows to validate the Shopify plugin functionality in Kestra.

## Prerequisites

Before running these workflows, ensure you have:
- A Shopify development store
- Shopify Admin API access token with appropriate permissions
- Kestra instance running (version 0.24.9+)

## Environment Variables Setup

Configure these secrets in Kestra:
```yaml
# In Kestra UI: Administration > Secrets
SHOPIFY_STORE_DOMAIN: your-store.myshopify.com
SHOPIFY_ACCESS_TOKEN: shpat_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

## Test Workflows

### 1. Complete Product Management Workflow

```yaml
id: shopify_product_management_qa
namespace: qa.shopify

description: |
  Comprehensive QA test for Shopify product operations:
  - List products (FETCH, FETCH_ONE, STORE)
  - Create new product
  - Update product details
  - Delete product

variables:
  store_domain: "{{ secret('SHOPIFY_STORE_DOMAIN') }}"
  access_token: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"

tasks:
  - id: test_list_products_fetch
    type: io.kestra.plugin.shopify.products.ListProducts
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    fetchType: FETCH
    limit: 5

  - id: test_list_products_fetch_one
    type: io.kestra.plugin.shopify.products.ListProducts
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    fetchType: FETCH_ONE
    limit: 1

  - id: test_list_products_store
    type: io.kestra.plugin.shopify.products.ListProducts
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    fetchType: STORE
    limit: 3

  - id: test_create_product
    type: io.kestra.plugin.shopify.products.CreateProduct
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    title: "QA Test Product {{ now() }}"
    bodyHtml: "<p>This is a test product created during QA validation</p>"
    vendor: "Kestra QA Team"
    productType: "Testing"
    status: "active"
    tags: "qa,test,kestra"

  - id: test_update_product
    type: io.kestra.plugin.shopify.products.UpdateProduct
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    productId: "{{ outputs.test_create_product.product.id }}"
    title: "Updated QA Test Product {{ now() }}"
    tags: "qa,test,kestra,updated"

  - id: test_get_product
    type: io.kestra.plugin.shopify.products.GetProduct
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    productId: "{{ outputs.test_create_product.product.id }}"

  - id: cleanup_delete_product
    type: io.kestra.plugin.shopify.products.DeleteProduct
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    productId: "{{ outputs.test_create_product.product.id }}"

  - id: qa_results_summary
    type: io.kestra.core.tasks.log.Log
    message: |
      QA Test Results Summary:
      ========================
      ✅ List Products (FETCH): {{ outputs.test_list_products_fetch.count }} products retrieved
      ✅ List Products (FETCH_ONE): {{ outputs.test_list_products_fetch_one.count }} product retrieved
      ✅ List Products (STORE): {{ outputs.test_list_products_store.count }} products stored in {{ outputs.test_list_products_store.uris | length }} files
      ✅ Create Product: {{ outputs.test_create_product.product.title }} (ID: {{ outputs.test_create_product.product.id }})
      ✅ Update Product: {{ outputs.test_update_product.product.title }}
      ✅ Get Product: {{ outputs.test_get_product.product.title }}
      ✅ Delete Product: Success ({{ outputs.cleanup_delete_product.deleted }})
      
      All product operations completed successfully! 🎉
```

### 2. Customer Management Workflow

```yaml
id: shopify_customer_management_qa
namespace: qa.shopify

variables:
  store_domain: "{{ secret('SHOPIFY_STORE_DOMAIN') }}"
  access_token: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"

tasks:
  - id: test_list_customers
    type: io.kestra.plugin.shopify.customers.ListCustomers
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    fetchType: FETCH
    limit: 10

  - id: test_create_customer
    type: io.kestra.plugin.shopify.customers.CreateCustomer
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    email: "qa-test-{{ now() | date('yyyyMMddHHmmss') }}@kestra.io"
    firstName: "QA"
    lastName: "Tester"
    acceptsMarketing: false
    tags: "qa,test,automated"

  - id: test_get_customer
    type: io.kestra.plugin.shopify.customers.GetCustomer
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    customerId: "{{ outputs.test_create_customer.customer.id }}"

  - id: cleanup_delete_customer
    type: io.kestra.plugin.shopify.customers.DeleteCustomer
    storeDomain: "{{ vars.store_domain }}"
    accessToken: "{{ vars.access_token }}"
    customerId: "{{ outputs.test_create_customer.customer.id }}"

  - id: customer_qa_summary
    type: io.kestra.core.tasks.log.Log
    message: |
      Customer QA Results:
      ===================
      ✅ Listed {{ outputs.test_list_customers.count }} existing customers
      ✅ Created customer: {{ outputs.test_create_customer.customer.email }}
      ✅ Retrieved customer: {{ outputs.test_get_customer.customer.firstName }} {{ outputs.test_get_customer.customer.lastName }}
      ✅ Deleted customer: {{ outputs.cleanup_delete_customer.deleted }}
```

### 3. Order Management Workflow

```yaml
id: shopify_order_management_qa  
namespace: qa.shopify

variables:
  store_domain: "{{ secret('SHOPIFY_STORE_DOMAIN') }}"
  access_token: "{{ secret('SHOPIFY_ACCESS_TOKEN') }}"

tasks:
  - id: test_list_orders_all_types
    type: io.kestra.core.tasks.flows.Parallel
    tasks:
      - id: list_orders_fetch
        type: io.kestra.plugin.shopify.orders.ListOrders
        storeDomain: "{{ vars.store_domain }}"
        accessToken: "{{ vars.access_token }}"
        fetchType: FETCH
        limit: 5
        
      - id: list_orders_store
        type: io.kestra.plugin.shopify.orders.ListOrders
        storeDomain: "{{ vars.store_domain }}"
        accessToken: "{{ vars.access_token }}"
        fetchType: STORE
        limit: 3

  - id: order_qa_summary
    type: io.kestra.core.tasks.log.Log
    message: |
      Order QA Results:
      ================
      ✅ Listed orders (FETCH): {{ outputs.test_list_orders_all_types.list_orders_fetch.count }} orders
      ✅ Listed orders (STORE): {{ outputs.test_list_orders_all_types.list_orders_store.count }} orders stored
      ✅ Storage URIs: {{ outputs.test_list_orders_all_types.list_orders_store.uris | length }} files created
```

## Expected QA Results

When these workflows execute successfully, you should see:

### ✅ Success Indicators:
- All tasks complete with status `SUCCESS`
- No compilation or runtime errors
- Proper data returned in outputs
- Storage functionality creates files (for STORE fetchType)
- Create/Update/Delete operations modify Shopify data appropriately

### 📊 Key Metrics to Validate:
- **Response Times**: API calls should complete within reasonable time (< 30s each)
- **Data Integrity**: Retrieved data matches Shopify Admin panel
- **Error Handling**: Invalid inputs produce clear error messages
- **Storage**: STORE fetchType creates accessible files in Kestra storage
- **Logging**: Informative logs at appropriate levels (DEBUG, INFO)

## QA Screenshots Checklist

When running QA, capture these screenshots for the PR:

1. **Kestra Flow Execution Overview** - Showing all tasks completed successfully
2. **Product Management Results** - Output logs showing successful operations
3. **Customer Management Results** - Verification of CRUD operations
4. **Order Listing Results** - Different fetchType outputs
5. **Storage Files Created** - Files generated by STORE fetchType
6. **Error Handling Example** - Invalid API token or missing required field

## Troubleshooting

### Common Issues:
- **Rate Limiting**: Shopify API has rate limits - the plugin includes automatic delays
- **Permissions**: Ensure API token has required scopes
- **Store Domain**: Use format `your-store.myshopify.com` (not custom domain)

### Debug Steps:
1. Check Kestra logs for detailed error messages
2. Verify secrets are properly configured
3. Test API token manually with curl
4. Check Shopify Admin for created/modified resources