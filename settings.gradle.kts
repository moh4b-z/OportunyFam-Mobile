pluginManagement {
    repositories {
        // O portal de plugins do Gradle é o local principal para a maioria dos plugins
        gradlePluginPortal()

        // Repositório do Google (essencial para Android e KSP)
        google()

        // Repositório Maven Central
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "OportunyFam-Mobile"
include(":app")
 