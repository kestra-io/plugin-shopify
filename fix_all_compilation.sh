#!/bin/bash

echo "Fixing all compilation issues..."

# List of all Java task files that need fixing
files=(
    "src/main/java/io/kestra/plugin/shopify/customers/CreateCustomer.java"
    "src/main/java/io/kestra/plugin/shopify/customers/GetCustomer.java"
    "src/main/java/io/kestra/plugin/shopify/customers/DeleteCustomer.java"
    "src/main/java/io/kestra/plugin/shopify/products/UpdateProduct.java"
    "src/main/java/io/kestra/plugin/shopify/products/CreateProduct.java"
    "src/main/java/io/kestra/plugin/shopify/products/DeleteProduct.java"
    "src/main/java/io/kestra/plugin/shopify/products/GetProduct.java"
    "src/main/java/io/kestra/plugin/shopify/orders/GetOrder.java"
    "src/main/java/io/kestra/plugin/shopify/orders/CreateOrder.java"
    "src/main/java/io/kestra/plugin/shopify/orders/ListOrders.java"
    "src/main/java/io/kestra/plugin/shopify/orders/DeleteOrder.java"
)

# Fix each file by removing orphaned try blocks
for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "Fixing $file..."
        
        # Remove lines that contain only "try {" with whitespace
        sed -i '' '/^[[:space:]]*try[[:space:]]*{[[:space:]]*$/d' "$file"
        
        # Fix any remaining indentation issues - standardize to 8 spaces for method content
        sed -i '' 's/^            /        /g' "$file"
    fi
done

echo "All files fixed!"