pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = "sk.eyJ1IjoiamVjYWljZWRvIiwiYSI6ImNtaTY0YjdrYTJ2Ynkya29zYXF4dWxsZTUifQ.MwF4Jfy_GWElO54wseeV7g"
            }
        }
    }
}

rootProject.name = "UniLocal"
include(":app")