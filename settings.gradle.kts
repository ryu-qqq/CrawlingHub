rootProject.name = "crawlinghub"

// ========================================
// Core Modules (Hexagonal Architecture)
// ========================================
include("domain")
include("application")

// ========================================
// Adapter Modules (Ports & Adapters)
// ========================================
// Inbound Adapters (Driving)
include("adapter:adapter-in-admin-web")
include("adapter:adapter-in-event")

// Outbound Adapters (Driven)
include("adapter:adapter-out-persistence-jpa")
include("adapter:adapter-out-aws-s3")
include("adapter:adapter-out-aws-sqs")
include("adapter:adapter-out-aws-eventbridge")
include("adapter:adapter-out-redis")
include("adapter:adapter-out-mustit-api")

// ========================================
// Bootstrap Modules (Runnable Applications)
// ========================================
include("bootstrap:bootstrap-web-api")

// ========================================
// Project Structure
// ========================================
project(":domain").projectDir = file("domain")
project(":application").projectDir = file("application")

project(":adapter:adapter-in-admin-web").projectDir = file("adapter/adapter-in-admin-web")
project(":adapter:adapter-in-event").projectDir = file("adapter/adapter-in-event")
project(":adapter:adapter-out-persistence-jpa").projectDir = file("adapter/adapter-out-persistence-jpa")
project(":adapter:adapter-out-aws-s3").projectDir = file("adapter/adapter-out-aws-s3")
project(":adapter:adapter-out-aws-sqs").projectDir = file("adapter/adapter-out-aws-sqs")
project(":adapter:adapter-out-aws-eventbridge").projectDir = file("adapter/adapter-out-aws-eventbridge")
project(":adapter:adapter-out-redis").projectDir = file("adapter/adapter-out-redis")
project(":adapter:adapter-out-mustit-api").projectDir = file("adapter/adapter-out-mustit-api")

project(":bootstrap:bootstrap-web-api").projectDir = file("bootstrap/bootstrap-web-api")
