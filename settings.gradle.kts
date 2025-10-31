rootProject.name = "crawlinghub"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

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
// New Hexagonal Architecture Adapters
include("adapter-out:persistence-mysql")
include("adapter-out:persistence-redis")
include("adapter-out:aws-eventbridge")

// ========================================
// Bootstrap Modules (Runnable Applications)
// ========================================
include("bootstrap:bootstrap-web-api")
