pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "MATRIX-M-Remote"

include(
    ":app",
    ":core",
    ":security",
    ":devices",
    ":pairing",
    ":permissions",
    ":sessions",
    ":remote-screen",
    ":camera",
    ":file-transfer",
    ":messaging"
)
