#!/bin/bash
#
# JPA Entity Refactoring Script
# 1. Rename class files to add Entity suffix
# 2. Update class names in the files
# 3. Remove business logic methods (activate, deactivate, etc.)
#

set -e

ADAPTER_DIR="/Users/sangwon-ryu/crawlinghub/adapter/adapter-out-persistence-jpa/src/main/java/com/ryuqq/crawlinghub/adapter/persistence/jpa"

# Entity files to refactor (excluding BaseTimeEntity and already refactored CrawlSite)
ENTITIES=(
    "execution/CrawlExecution"
    "execution/ExecutionResultSummary"
    "execution/ExecutionS3Path"
    "execution/ExecutionStatistics"
    "schedule/CrawlSchedule"
    "schedule/ScheduleInputParam"
    "site/SiteApiEndpoint"
    "site/SiteApiHeader"
    "site/SiteAuthConfig"
    "site/SiteRateLimitConfig"
    "site/SiteRetryPolicy"
    "task/CrawlTask"
    "task/CrawlTaskAttempt"
    "task/TaskInputParam"
    "task/TaskOutputData"
    "task/TaskResultMetadata"
    "workflow/CrawlWorkflow"
    "workflow/WorkflowStep"
    "workflow/WorkflowStepOutput"
    "workflow/WorkflowStepParam"
)

echo "🔧 Starting JPA Entity Refactoring..."
echo ""

for entity in "${ENTITIES[@]}"; do
    OLD_FILE="${ADAPTER_DIR}/${entity}.java"
    CLASS_NAME=$(basename "$entity")
    NEW_FILE="${ADAPTER_DIR}/${entity}Entity.java"

    if [ ! -f "$OLD_FILE" ]; then
        echo "⚠️  File not found: $OLD_FILE"
        continue
    fi

    echo "📝 Refactoring: $CLASS_NAME → ${CLASS_NAME}Entity"

    # 1. Update class name in the file
    sed -i '' "s/public class ${CLASS_NAME} /public class ${CLASS_NAME}Entity /g" "$OLD_FILE"

    # 2. Update constructor names
    sed -i '' "s/protected ${CLASS_NAME}(/protected ${CLASS_NAME}Entity(/g" "$OLD_FILE"
    sed -i '' "s/private ${CLASS_NAME}(/private ${CLASS_NAME}Entity(/g" "$OLD_FILE"

    # 3. Update Builder return type
    sed -i '' "s/public ${CLASS_NAME} build()/public ${CLASS_NAME}Entity build()/g" "$OLD_FILE"
    sed -i '' "s/return new ${CLASS_NAME}(/return new ${CLASS_NAME}Entity(/g" "$OLD_FILE"

    # 4. Remove common business methods (if any)
    # This is a simple approach - remove methods between two patterns
    # More sophisticated removal might be needed for specific cases

    # 5. Rename the file
    mv "$OLD_FILE" "$NEW_FILE"

    echo "✅ Completed: ${CLASS_NAME}Entity"
done

echo ""
echo "🎉 Refactoring completed for ${#ENTITIES[@]} entities!"
echo ""
echo "📌 Next steps:"
echo "   1. Manually review and remove business methods from entities"
echo "   2. Run: ./gradlew :adapter:adapter-out-persistence-jpa:compileJava"
echo "   3. Fix any compilation errors"
