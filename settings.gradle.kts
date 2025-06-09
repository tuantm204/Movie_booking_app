pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Cách khai báo flatDir trong .kts
        flatDir {
            dirs("libs", "../app/libs")
        }
    }
}
// ...phần còn lại giữ nguyên...

rootProject.name = "Movie-booking-app"
include(":app")
