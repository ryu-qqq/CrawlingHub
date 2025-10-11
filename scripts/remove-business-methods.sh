#!/bin/bash
#
# Remove business methods from JPA entities
# Keep only getters, Builder, and static factory methods
#

set -e

ADAPTER_DIR="/Users/sangwon-ryu/crawlinghub/adapter/adapter-out-persistence-jpa/src/main/java/com/ryuqq/crawlinghub/adapter/persistence/jpa"

# Files with business methods
FILES_TO_CLEAN=(
    "schedule/CrawlScheduleEntity.java"
    "schedule/ScheduleInputParamEntity.java"
    "execution/ExecutionResultSummaryEntity.java"
    "execution/ExecutionS3PathEntity.java"
    "execution/CrawlExecutionEntity.java"
    "execution/ExecutionStatisticsEntity.java"
    "workflow/WorkflowStepOutputEntity.java"
    "workflow/WorkflowStepEntity.java"
)

echo "🧹 Removing business methods from JPA entities..."
echo ""

for file in "${FILES_TO_CLEAN[@]}"; do
    FULL_PATH="${ADAPTER_DIR}/${file}"

    if [ ! -f "$FULL_PATH" ]; then
        echo "⚠️  File not found: $FULL_PATH"
        continue
    fi

    echo "📝 Processing: $file"

    # Create a temporary file
    TMP_FILE="${FULL_PATH}.tmp"

    # Use awk to remove business methods (public void methods that are not getters/setters)
    awk '
    BEGIN { in_method = 0; brace_count = 0; skip = 0; }

    # Detect start of business method (public void methods, not getters)
    /^[[:space:]]*public void [a-z][a-zA-Z]*\(/ {
        if ($0 !~ /get|set/) {
            in_method = 1
            skip = 1
            next
        }
    }

    # Track braces when inside a method
    {
        if (in_method) {
            for (i = 1; i <= length($0); i++) {
                char = substr($0, i, 1)
                if (char == "{") brace_count++
                if (char == "}") {
                    brace_count--
                    if (brace_count == 0) {
                        in_method = 0
                        skip = 0
                        next
                    }
                }
            }
        }
    }

    # Print lines that are not part of business methods
    {
        if (!skip) {
            print
        }
    }
    ' "$FULL_PATH" > "$TMP_FILE"

    # Replace original file
    mv "$TMP_FILE" "$FULL_PATH"

    echo "✅ Cleaned: $file"
done

echo ""
echo "🎉 Business method removal completed!"
echo ""
echo "📌 Next step: ./gradlew :adapter:adapter-out-persistence-jpa:compileJava"
