#!/bin/bash

echo "Removing extra closing braces from all Java files..."

# List of files that have extra closing braces based on compilation errors
files=(
    "src/main/java/io/kestra/plugin/shopify/customers/GetCustomer.java"
    "src/main/java/io/kestra/plugin/shopify/customers/DeleteCustomer.java"
    "src/main/java/io/kestra/plugin/shopify/products/UpdateProduct.java"
    "src/main/java/io/kestra/plugin/shopify/products/CreateProduct.java"
    "src/main/java/io/kestra/plugin/shopify/products/DeleteProduct.java"
    "src/main/java/io/kestra/plugin/shopify/products/GetProduct.java"
    "src/main/java/io/kestra/plugin/shopify/orders/GetOrder.java"
    "src/main/java/io/kestra/plugin/shopify/orders/ListOrders.java"
    "src/main/java/io/kestra/plugin/shopify/orders/DeleteOrder.java"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "Fixing $file..."
        # Check if last line is just a closing brace
        if tail -1 "$file" | grep -q '^}$'; then
            # Remove the last line if it's just a closing brace
            sed -i '' '$d' "$file"
            echo "  Removed extra closing brace"
        fi
    fi
done

echo "All files fixed!"