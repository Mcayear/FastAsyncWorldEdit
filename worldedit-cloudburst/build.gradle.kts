import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

project.description = "CloudBurst"

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven {
        name = "Cloudburst Releases"
        url = uri("https://repo.opencollab.dev/maven-releases")
    }
    maven {
        name = "Cloudburst Snapshots"
        url = uri("https://repo.opencollab.dev/maven-snapshots")
    }
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
    }
    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "ltname"
        url = uri("https://repo.lanink.cn/repository/maven-public/")
    }
    mavenLocal()
    // 如果需要，可以添加更多仓库
}

val localImplementation by configurations.creating {
    description = "Dependencies used locally, but provided by the runtime CloudBurst implementation"
    isCanBeConsumed = false
    isCanBeResolved = false
}

dependencies {
    // 模块
    api(project(":worldedit-core"))
    api(project(":worldedit-libs:cloudburst"))

    // Minecraft 相关
    implementation(libs.fastutil)

    // 平台相关
    compileOnly(libs.cloudburstmc) {
        exclude("junit", "junit")
        isTransitive = false
        isChanging = true
    }

    // 日志
    localImplementation(libs.log4jApi)
    localImplementation(libs.log4jBom) {
        because("CloudBurst 提供了 Log4J（不是通过 API，而是服务器隐式包含）")
    }

    // 插件
    compileOnly(libs.annotations)
    compileOnly(libs.voxelsniper) {
        isTransitive = false
    }

    // 第三方依赖
    implementation(libs.gson)
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.extra["internalVersion"] as String
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
                "Class-Path" to CLASSPATH,
                "WorldEdit-Version" to project.version
        )
    }
}

addJarManifest(WorldEditKind.Plugin, includeClasspath = true)

tasks.register<ShadowJar>("reobfShadowJar") {
    archiveFileName.set("${rootProject.name}-CloudBurst-${project.version}.${archiveExtension.getOrElse("jar")}")
    configurations = listOf(
            project.configurations.runtimeClasspath.get()
    )
    from(sourceSets.main.get().output)
    manifest.inheritFrom(tasks.jar.get().manifest)
    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")

    manifest {
        attributes(
                "FAWE-Plugin-Jar-Type" to "cloudburst-mcayear"
        )
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("${rootProject.name}-CloudBurst-${project.version}.${archiveExtension.getOrElse("jar")}")
    configurations = listOf(project.configurations.runtimeClasspath.get())
    manifest {
        attributes(
                "cloudburst-mappings-namespace" to "mojang",
                "FAWE-Plugin-Jar-Type" to "mojang"
        )
    }
}

tasks.withType<ShadowJar>().configureEach {
    dependencies {
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")

        include(dependency(":worldedit-core"))
        include(dependency(":worldedit-libs:cloudburst"))
        include(dependency("org.antlr:antlr4-runtime"))
        include(dependency("com.google.code.gson:gson"))
        relocate("it.unimi.dsi.fastutil", "com.sk89q.worldedit.bukkit.fastutil") {
            include(dependency("it.unimi.dsi:fastutil"))
        }

        relocate("org.lz4", "com.fastasyncworldedit.core.lz4") {
            include(dependency("org.lz4:lz4-java:1.8.0"))
        }
    }

    // 如果有其他需要排除或包含的依赖项，可以在此添加
    minimize {
        // 示例：排除某些依赖项
        // exclude(dependency("com.example:example-lib"))
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
    dependsOn("reobfShadowJar")
}
