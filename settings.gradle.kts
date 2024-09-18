pluginManagement {
    repositories {
        maven { url=uri("https://jitpack.io") }
        maven { url=uri("https://maven.aliyun.com/repository/releases") }
//        maven { url=uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url=uri("https://maven.aliyun.com/repository/google") }
        maven { url=uri("https://maven.aliyun.com/repository/central") }
        maven { url=uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url=uri("https://maven.aliyun.com/repository/public") }
//        google()
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
        maven { url=uri("https://jitpack.io") }
        maven { url=uri("https://maven.aliyun.com/repository/releases") }
//        maven { url=uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url=uri("https://maven.aliyun.com/repository/google") }
        maven { url=uri("https://maven.aliyun.com/repository/central") }
        maven { url=uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url=uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
    }
}

rootProject.name = "Piano Wizard"
include(":app")
