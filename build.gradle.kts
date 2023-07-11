version = "0.1.0"

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    processResources {
        val apiVersion = rootProject.providers.gradleProperty("mcVersion").get()
            .split(".", "-").take(2).joinToString(".")
        val props = mapOf(
            "version" to project.version,
            "apiversion" to "\"$apiVersion\"",
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    base {
        archivesName.set("doBeanGriefing")
    }

    shadowJar {
        archiveClassifier.set("")
    }
}