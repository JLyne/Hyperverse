plugins {
    id("org.incendo.cloud-build-logic")
    id("org.incendo.cloud-build-logic.spotless")
}

indra {
    javaVersions {
        minimumToolchain(17)
        target(21)
        testWith().set(setOf(17))
    }
    checkstyle().set(libs.versions.checkstyle)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatypeOssSnapshots"
        mavenContent {
            snapshotsOnly()
        }
    }

    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.spongepowered.org/maven")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://ci.mg-dev.eu/plugin/repository/everything/")
}
