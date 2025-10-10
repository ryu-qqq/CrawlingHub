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

// Outbound Adapters (Driven)
include("adapter:adapter-out-persistence-jpa")
include("adapter:adapter-out-aws-s3")
include("adapter:adapter-out-aws-sqs")

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
project(":adapter:adapter-out-persistence-jpa").projectDir = file("adapter/adapter-out-persistence-jpa")
project(":adapter:adapter-out-aws-s3").projectDir = file("adapter/adapter-out-aws-s3")
project(":adapter:adapter-out-aws-sqs").projectDir = file("adapter/adapter-out-aws-sqs")

project(":bootstrap:bootstrap-web-api").projectDir = file("bootstrap/bootstrap-web-api")
