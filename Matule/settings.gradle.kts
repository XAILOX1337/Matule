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
}

rootProject.name = "Matule"
include(":app")

// Библиотеки — отдельные проекты. Подключаются исходниками (composite build):
// приложение всегда собирается с актуальной версией UIKit и сетевого слоя.
includeBuild("../MatuleUIKit") {
    dependencySubstitution {
        substitute(module("ru.matule:uikit")).using(project(":uikit"))
    }
}
includeBuild("../MatuleNetwork") {
    dependencySubstitution {
        substitute(module("ru.matule:network")).using(project(":network"))
    }
}
