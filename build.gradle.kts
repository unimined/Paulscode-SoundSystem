import java.text.SimpleDateFormat
import java.util.Date

plugins {
    `java-library`
    `maven-publish`
}

group = project.properties["maven_group"] as String
base.archivesName = project.properties["archives_base_name"] as String

// Releases are in DateTime format to continue the tradition
// Snapshots are simply "1.0.0-SNAPSHOT" to make it easy to get the latest version
version = if (!project.hasProperty("version_release")) "1.0.0-SNAPSHOT"
else SimpleDateFormat("yyyyMMddHHmm").format(Date())

repositories {
    mavenCentral()
    maven("https://maven.legacyfabric.net")
    maven("https://jogamp.org/deployment/maven")
}

fun SourceSetContainer.extending(sourceSet: SourceSet): NamedDomainObjectContainerCreatingDelegateProvider<SourceSet> =
    this.creating {
        compileClasspath += sourceSet.compileClasspath
        runtimeClasspath += sourceSet.runtimeClasspath
        compileClasspath += sourceSet.output
        runtimeClasspath += sourceSet.output
        tasks.register(this@creating.jarTaskName, Jar::class) {
            group = "build"
            archiveBaseName = "${base.archivesName.get()}-${this@creating.name}"
            from(this@creating.output) { include("**/*.class") }
        }
        tasks.register(this@creating.sourcesJarTaskName, Jar::class) {
            group = "build"
            archiveBaseName = "${base.archivesName.get()}-${this@creating.name}"
            archiveClassifier = "sources"
            from(this@creating.allSource) { include("**/*.java") }
        }
        tasks.jar.configure { dependsOn(this@creating.jarTaskName) }
        tasks.build.configure { dependsOn(this@creating.sourcesJarTaskName) }
    }

val main: SourceSet by sourceSets.getting
val test: SourceSet by sourceSets.getting

/**
 * Java's built-in audio library
 */
val javaSoundPlugin: SourceSet by sourceSets.extending(main)

/**
 * LWJGL 2's OpenAL bindings
 */
val lwjgl2Plugin: SourceSet by sourceSets.extending(main)

/**
 * JogAmp's OpenAL bindings
 */
val jogAmpPlugin: SourceSet by sourceSets.extending(main)

dependencies {
    val lwjglImplementation: Configuration = configurations.named(lwjgl2Plugin.implementationConfigurationName).get()
    lwjglImplementation(libs.bundles.lwjgl)

    val joalImplementation: Configuration = configurations.named(jogAmpPlugin.implementationConfigurationName).get()
    joalImplementation(libs.joal)
}

publishing {
    repositories {
        maven {
            name = "WagYourMaven"
            url = if (project.hasProperty("version_release")) {
                uri("https://maven.wagyourtail.xyz/releases")
            } else {
                uri("https://maven.wagyourtail.xyz/snapshots")
            }
            credentials {
                username = project.findProperty("mvn.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("mvn.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}
