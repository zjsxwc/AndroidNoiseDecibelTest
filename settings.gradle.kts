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
    }

    // 添加版本目录配置
    versionCatalogs {
        create("libs") {
            // Android Gradle 插件（应用模块）
            plugin("androidApp", "com.android.application").version("8.4.0") // 唯一别名
            // Android Gradle 库插件（可选）
            plugin("androidLib", "com.android.library").version("8.4.0")
            // Kotlin Android 插件
            plugin("kotlinAndroid", "org.jetbrains.kotlin.android").version("1.9.20")
        }
    }
}

rootProject.name = "My Application"
include(":app")
