import java.text.SimpleDateFormat
import java.util.Date

plugins {
    `java-library`
    `maven-publish`
}

val date: Date = Date()

group = project.properties["maven_group"] as String
base.archivesName = project.properties["archives_base_name"] as String

// Releases are in DateTime format to continue the tradition
// Snapshots are simply "1.0.0-SNAPSHOT" to make it easy to get the latest version
version = if (!project.hasProperty("version_release")) "1.0.0-SNAPSHOT"
else SimpleDateFormat("yyyyMMddHHmm").format(date)

repositories {
    mavenCentral()
    maven("https://maven.legacyfabric.net")
    maven("https://jogamp.org/deployment/maven")
}

fun SourceSet.extendsFrom(sourceSet: SourceSet) {
    compileClasspath += sourceSet.compileClasspath
    runtimeClasspath += sourceSet.runtimeClasspath
    compileClasspath += sourceSet.output
    runtimeClasspath += sourceSet.output
}

/**
 * Adds the supplied source sets and their dependencies to the source set's classpath,
 * registers a jar task for the source set,
 * and invokes the additionally supplied configuration to the created source set
 */
fun SourceSetContainer.extending(vararg sourceSets: SourceSet, configuration: SourceSet.() -> Unit = {}): NamedDomainObjectContainerCreatingDelegateProvider<SourceSet> =
    this.creating {
        sourceSets.forEach { this@creating.extendsFrom(it) }
        tasks.register(this@creating.jarTaskName, Jar::class) {
            group = "build"
            archiveBaseName = "${base.archivesName.get()}-${this@creating.name}"
            from(this@creating.output) { include("**/*.class") }
        }
        configuration.invoke(this@creating)
    }

/**
 * Adds the supplied source sets and their dependencies to the source set's classpath,
 * registers a jar task for the source set,
 * registers a sources jar task for the source set,
 * ensures that the jar is always built when the main jar is built,
 * ensures that the sources jar is always built when the build task runs,
 * and invokes the additionally supplied configuration to the created source set
 */
fun SourceSetContainer.libExtending(vararg sourceSets: SourceSet, configuration: SourceSet.() -> Unit = {}): NamedDomainObjectContainerCreatingDelegateProvider<SourceSet> =
    this.extending(*sourceSets) {
        tasks.register(this@extending.sourcesJarTaskName, Jar::class) {
            group = "build"
            archiveBaseName = "${base.archivesName.get()}-${this@extending.name}"
            archiveClassifier = "sources"
            from(this@extending.allSource) { include("**/*.java") }
        }
        tasks.jar.configure { dependsOn(this@extending.jarTaskName) }
        tasks.build.configure { dependsOn(this@extending.sourcesJarTaskName) }
        configuration.invoke(this@extending)
    }

/**
 * The main version of the SoundSystem
 */
val main: SourceSet by sourceSets.getting
val test: SourceSet by sourceSets.getting

/**
 * SoundSystem loader with example XML
 */
val utils: SourceSet by sourceSets.libExtending(main)

/**
 * Java's built-in audio library plugin
 */
val javaSoundPlugin: SourceSet by sourceSets.libExtending(main)

/**
 * LWJGL 2's OpenAL bindings plugin
 */
val lwjgl2Plugin: SourceSet by sourceSets.libExtending(main)

/**
 * JogAmp's OpenAL bindings plugin
 */
val jogAmpPlugin: SourceSet by sourceSets.libExtending(main)

/**
 * WAV codec plugin
 */
val wavPlugin: SourceSet by sourceSets.libExtending(main)

/**
 * J-OGG-based OGG codec plugin
 */
val joggPlugin: SourceSet by sourceSets.libExtending(main)

/**
 * JOrbis-based OGG codec plugin
 */
val jOrbisPlugin: SourceSet by sourceSets.libExtending(main)

/**
 * IBXM codec plugin
 */
val ibxmPlugin: SourceSet by sourceSets.libExtending(main)

/**
 * Speex codec plugin
 */
val jSpeexPlugin: SourceSet by sourceSets.libExtending(main)

/**
 * jPCT-friendly version of the SoundSystem
 */
val jpct: SourceSet by sourceSets.libExtending(main, javaSoundPlugin, lwjgl2Plugin, joggPlugin, wavPlugin)

/**
 * Sound Effect Player demo
 */
val playerDemo: SourceSet by sourceSets.extending(jpct)

/**
 * Bullet / Target Collision demo
 */
val collisionDemo: SourceSet by sourceSets.extending(jpct)

/**
 * Holy Bouncing Helicopter Balls demo
 */
val helicopterDemo: SourceSet by sourceSets.extending(jpct)

dependencies {
    val jpctImplementation: Configuration = configurations.named(jpct.implementationConfigurationName).get()
    jpctImplementation(files("libs/jpct.jar"))

    val lwjglImplementation: Configuration = configurations.named(lwjgl2Plugin.implementationConfigurationName).get()
    lwjglImplementation(libs.bundles.lwjgl)

    val joalImplementation: Configuration = configurations.named(jogAmpPlugin.implementationConfigurationName).get()
    joalImplementation(libs.joal)

    val joggImplementation: Configuration = configurations.named(joggPlugin.implementationConfigurationName).get()
    joggImplementation(libs.jogg.all)

    val jOrbisImplementation: Configuration = configurations.named(jOrbisPlugin.implementationConfigurationName).get()
    jOrbisImplementation(libs.jorbis)

    val jSpeexImplementation: Configuration = configurations.named(jSpeexPlugin.implementationConfigurationName).get()
    jSpeexImplementation(libs.jspeex)
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

tasks.jar {
    manifest {
        attributes(
            "Specification-Vendor" to "paulscode",
            "Specification-Title" to "SoundSystem",
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Vendor" to "Unimined",
            "Implementation-Version" to version,
            "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date)
        )
    }
}
