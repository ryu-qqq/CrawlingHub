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
include("adapter-in:rest-api")

// Outbound Adapters (Driven)
include("adapter-out:persistence-mysql")


// ========================================
// Bootstrap Modules (Runnable Applications)
// ========================================
include("bootstrap:bootstrap-web-api")

// ========================================
// Project Structure
// ========================================
project(":domain").projectDir = file("domain")
project(":application").projectDir = file("application")


project(":bootstrap:bootstrap-web-api").projectDir = file("bootstrap/bootstrap-web-api")
