#!/bin/bash

# Remove the unnecessary try blocks that were added incorrectly
for file in $(find src/main/java -name "*.java"); do
    # Check if this is a task implementation file (not the abstract base class)
    if [[ "$file" == *"tasks/"* ]] || [[ "$file" == *"products/"* ]] || [[ "$file" == *"orders/"* ]] || [[ "$file" == *"customers/"* ]]; then
        # Replace the pattern: HttpClient client = buildHttpClient(runContext);\n        try { with just the first line
        sed -i '' 's/HttpClient client = buildHttpClient(runContext);[[:space:]]*try {/HttpClient client = buildHttpClient(runContext);/g' "$file"
        
        # Also handle the case where they're on the same line
        sed -i '' 's/HttpClient client = buildHttpClient(runContext); try {/HttpClient client = buildHttpClient(runContext);/g' "$file"
    fi
done

echo "Removed unnecessary try blocks from all task files"