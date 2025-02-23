import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("hyperverse.base-conventions")
    id("hyperverse.publishing-conventions")
    alias(libs.plugins.run.paper)
    alias(libs.plugins.pluginyml)
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    compileOnly(libs.paper)
    compileOnly(libs.placeholderapi)

    // TODO: Remove, because yuck.
    paperLibrary("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    paperLibrary(libs.guice) {
        exclude("com.google.guava", "guava")
    }
    paperLibrary(libs.assistedInject) {
        exclude("com.google.guava", "guava")
    }
    paperLibrary(libs.configurateHocon)
    paperLibrary(libs.cloudPaper)
    paperLibrary(libs.cloudMinecraftExtras)
}

paper {
    name = "Hyperverse"
    website = "https://github.com/incendo/Hyperverse"
    authors = listOf("Citymonstret", "andrewandy")
    main = "org.incendo.hyperverse.Hyperverse"
    loader = "org.incendo.hyperverse.HyperverseLoader"
    generateLibrariesJson = true
    apiVersion = libs.versions.minecraft.get().replace(Regex("\\-R\\d.\\d-SNAPSHOT"), "")

    serverDependencies {
        register("PlaceholderAPI") {
            required = false
        }
    }

    permissions {
        mapOf(
            "worlds" to "Allows players to use the Hyperverse command",
            "reload" to "Allows players to reload the configuration and messages used by Hyperverse",
            "create" to "Allows players to create new worlds",
            "list" to "Allows players to list worlds",
            "teleport" to "Allows players to teleport between worlds",
            "teleport.other" to "Allows players to teleport other players between worlds",
            "teleportgroup" to "Allows players to teleport to their last location in a given profile group",
            "info" to "Allows players to view world info",
            "unload" to "Allows players to unload worlds",
            "load" to "Allows players to load worlds",
            "import" to "Allows players to import worlds",
            "find" to "Allows players to find the current world of another player",
            "flag.list" to "Allows player to list available flags",
            "flag.set" to "Allows players to set world flags",
            "flag.info" to "Allows players to view information regarding a flag in a given world",
            "gamerule.set" to "Allows players to world set gamerules",
            "delete" to "Allows players to delete worlds",
            "who" to "Allows players to list players",
            "plugin.import" to "Allows players to import configurations from external plugins",
            "regenerate" to "Allows players to regenerate worlds"
        ).forEach { (permission, description) ->
            register("hyperverse.$permission") {
                this.description = description
                this.default = BukkitPluginDescription.Permission.Default.OP
            }
        }
        register("hyperverse.override.gamemode") {
            description = "Allows players to override world gamemode"
            default = BukkitPluginDescription.Permission.Default.FALSE
        }
    }
}

tasks {
    runServer {
        java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        minecraftVersion("1.21.4")
    }
}
