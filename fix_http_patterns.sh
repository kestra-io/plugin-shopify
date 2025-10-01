#!/bin/bash

# Fix all Java files to properly handle HttpClient usage
for file in $(find src/main/java -name "*.java"); do
    # Fix the try-with-resources pattern that was incorrectly updated
    sed -i '' 's/HttpClient client = buildHttpClient(runContext) {/HttpClient client = buildHttpClient(runContext);\
        try {/g' "$file"
    
    # Add missing import for IOException
    if grep -q "HttpResponse<String>" "$file" && ! grep -q "import java.io.IOException;" "$file"; then
        sed -i '' '/import java.net.http.HttpResponse;/a\
import java.io.IOException;\
import java.lang.InterruptedException;
' "$file"
    fi
done

echo "Fixed all HTTP client usage patterns"