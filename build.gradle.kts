import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.3"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }

    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.oraxen.com/releases")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation(libs.com.github.cryptomorin.xseries)
    implementation(libs.net.wesjd.anvilgui)
    implementation(libs.de.tr7zw.item.nbt.api)
    compileOnly(libs.io.th0rgal.oraxen)
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.me.clip.placeholderapi)
}

group = "de.theredend2000"
version = "3.0.4"
description = "AdvancedEggHunt"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    withType<ShadowJar> {
        relocate("com.cryptomorin.xseries", "de.theredend2000.libs.xseries")
        relocate("net.wesjd.anvilgui", "de.theredend2000.libs.anvilgui")
        relocate("de.tr7zw.changeme.nbtapi", "de.theredend2000.libs.nbtapi")
        archiveClassifier.set("")
        minimize()
    }

    withType<ProcessResources> {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    build {
        dependsOn(shadowJar)
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    runServer {
        minecraftVersion("1.20.4")
        jvmArguments.add("-Dcom.mojang.eula.agree=true")
        jvmArguments.add("-Dnet.kyori.ansi.colorLevel=truecolor")
        jvmArguments.add("-Dfile.encoding=UTF8")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)
    }
}