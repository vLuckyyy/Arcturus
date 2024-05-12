import io.papermc.paperweight.util.*
import kotlin.io.path.*

plugins {
    java
    `maven-publish`
    id("io.papermc.paperweight.patcher") version "1.6.3"
}

repositories {
    mavenCentral()
    maven("https://repo.leavesmc.top/releases") {
        content { onlyForConfigurations("paperclip") }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.10.2:fat")
    decompiler("org.vineflower:vineflower:1.10.1")
    paperclip("top.leavesmc:leavesclip:1.0.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://ci.emc.gs/nexus/content/groups/aikar/")
        maven("https://repo.aikar.co/content/groups/aikar")
        maven("https://repo.md-5.net/content/repositories/releases/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://jitpack.io")
    }
}

paperweight {
    serverProject.set(project(":leaves-server"))

    remapRepo.set("https://maven.fabricmc.net/")
    decompileRepo.set("https://files.minecraftforge.net/maven/")

    usePaperUpstream(providers.gradleProperty("paperRef")) {
        withPaperPatcher {
            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            apiOutputDir.set(layout.projectDirectory.dir("leaves-api"))

            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
            serverOutputDir.set(layout.projectDirectory.dir("leaves-server"))
        }

        patchTasks.register("generatedApi") {
            isBareDirectory = true
            upstreamDirPath = "paper-api-generator/generated"
            patchDir = layout.projectDirectory.dir("patches/generated-api")
            outputDir = layout.projectDirectory.dir("paper-api-generator/generated")
        }
    }
}

if (providers.gradleProperty("updatingMinecraft").getOrElse("false").toBoolean()) {

    tasks.withType<io.papermc.paperweight.tasks.CollectATsFromPatches>().configureEach {
        val dir = layout.projectDirectory.dir("patches/unapplied")
        if (dir.path.isDirectory()) {
            extraPatchDir = dir
        }
    }
    tasks.withType<io.papermc.paperweight.tasks.RebuildGitPatches>().configureEach {
        filterPatches = false
    }
}

tasks.register("createMojmapLeavesclipJar") {
    group = "paperweight"
    dependsOn("createMojmapPaperclipJar")
    doLast {
        file("build/libs/leaves-paperclip-${project.version}-mojmap.jar").renameTo(
            file("build/libs/leaves-leavesclip-${project.version}-mojmap.jar")
        )
    }
}

tasks.register("createReobfLeavesclipJar") {
    group = "paperweight"
    dependsOn("createReobfPaperclipJar")
    doLast {
        file("build/libs/leaves-paperclip-${project.version}-reobf.jar").renameTo(
            file("build/libs/leaves-leavesclip-${project.version}-reobf.jar")
        )
    }
}