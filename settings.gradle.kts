rootProject.name = "crawlinghub"

// ========================================
// Plugin Management (for JitPack)
// ========================================
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// ========================================
// Dependency Resolution Management
// ========================================
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
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
include("adapter-out:persistence-mysql")
include("adapter-out:aws-eventbridge")


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
