#!/bin/bash

# Fix indentation issues in all Java files after removing try blocks
for file in $(find src/main/java -name "*.java"); do
    if [[ "$file" == *"products/"* ]] || [[ "$file" == *"orders/"* ]] || [[ "$file" == *"customers/"* ]]; then
        # Fix indentation: lines that have 12 spaces should have 8 spaces
        sed -i '' 's/^            /        /g' "$file"
    fi
done

echo "Fixed indentation in all task files"