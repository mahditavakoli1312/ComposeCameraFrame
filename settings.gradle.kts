pluginManagement {
    repositories {
        maven {
            url = uri("https://inexus.samentic.com/repository/samentic-android/")
            artifactUrls("https://inexus.samentic.com/repository/samentic-android/")
            credentials {
                username = "signal"
                password = "mR,A7,Na@s4&37@"
            }
        }
        jcenter()

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://inexus.samentic.com/repository/samentic-android/")
            artifactUrls("https://inexus.samentic.com/repository/samentic-android/")
            credentials {
                username = "signal"
                password = "mR,A7,Na@s4&37@"
            }
        }
        jcenter()

    }
}

rootProject.name = "ComposeCameraFrame"
include(":app")
 