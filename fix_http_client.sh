#!/bin/bash

# Update HttpClientInterface usage to HttpClient
find src/main/java -name "*.java" -exec sed -i '' 's/try (HttpClientInterface client = buildHttpClient(runContext))/HttpClient client = buildHttpClient(runContext)/g' {} \;

# Update HttpResponse method calls
find src/main/java -name "*.java" -exec sed -i '' 's/response\.getStatusCode()/response.statusCode()/g' {} \;
find src/main/java -name "*.java" -exec sed -i '' 's/response\.getBody()/response.body()/g' {} \;

# Update client.send() calls
find src/main/java -name "*.java" -exec sed -i '' 's/HttpResponse response = client\.send(request);/HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());/g' {} \;

echo "Updated HTTP client usage in all Java files"