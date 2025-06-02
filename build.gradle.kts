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

fun SourceSet.extendsFrom(sourceSet: SourceSet, name: String) {
    configurations {
        maybeCreate(this@extendsFrom.apiConfigurationName).apply {
            extendsFrom(getByName(sourceSet.apiConfigurationName))
        }
        maybeCreate(this@extendsFrom.runtimeClasspathConfigurationName).apply {
            extendsFrom(getByName(sourceSet.runtimeClasspathConfigurationName))
        }
        maybeCreate(this@extendsFrom.implementationConfigurationName).apply {
            extendsFrom(getByName(sourceSet.implementationConfigurationName))
        }
    }
    dependencies {
        this@extendsFrom.apiConfigurationName("com.paulscode:$name:1.0.0")
    }
//    compileClasspath += sourceSet.compileClasspath
//    runtimeClasspath += sourceSet.runtimeClasspath
    compileClasspath += sourceSet.output
    runtimeClasspath += sourceSet.output
}

val artifactNames = mutableMapOf<SourceSet, String>(sourceSets.main.get() to "SoundSystem")

/**
 * Adds the supplied source sets and their dependencies to the source set's classpath,
 * registers a jar task for the source set,
 * and invokes the additionally supplied configuration to the created source set
 */
fun SourceSetContainer.extending(
    name: String,
    vararg sourceSets: SourceSet,
    configuration: SourceSet.() -> Unit = {}
): NamedDomainObjectContainerCreatingDelegateProvider<SourceSet> =
    this.creating {
        artifactNames[this@creating] = name
        sourceSets.forEach { this@creating.extendsFrom(it, artifactNames.getValue(it)) }
        tasks.register(this@creating.jarTaskName, Jar::class) {
            group = "build"
            archiveBaseName = name
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
fun SourceSetContainer.libExtending(
    name: String,
    vararg sourceSets: SourceSet,
    configuration: SourceSet.() -> Unit = {}
): NamedDomainObjectContainerCreatingDelegateProvider<SourceSet> =
    this.extending(name, *sourceSets) {
        tasks.register(this@extending.sourcesJarTaskName, Jar::class) {
            group = "build"
            archiveBaseName = name
            archiveClassifier = "sources"
            from(this@extending.allSource) { include("**/*.java") }
        }
        tasks.register(this@extending.javadocTaskName, Javadoc::class) {
            group = "build"
            setDestinationDir(file("${buildDir}/${name}Javadoc"))
            source = this@extending.allJava
        }
        tasks.register(this@extending.javadocJarTaskName, Jar::class) {
            group = "build"
            archiveBaseName = name
            archiveClassifier = "javadoc"
            from(this@extending.javadocTaskName)
        }
        tasks.jar.configure { dependsOn(this@extending.jarTaskName) }
        tasks.build.configure { dependsOn(this@extending.sourcesJarTaskName, this@extending.javadocJarTaskName) }
        createPublication(name)
        configuration.invoke(this@extending)
    }

/**
 * Creates a Maven publication with a complete POM file for
 */
fun SourceSet.createPublication(name: String) {
    val jarTask = tasks.named(this@createPublication.jarTaskName, Jar::class)
    configurations {
        maybeCreate(this@createPublication.apiConfigurationName)

        maybeCreate(this@createPublication.apiElementsConfigurationName).apply {
            isCanBeConsumed = true
            extendsFrom(getByName(this@createPublication.apiConfigurationName))
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
            }
        }
        maybeCreate(this@createPublication.runtimeElementsConfigurationName).apply {
            isCanBeConsumed = true
            extendsFrom(getByName(this@createPublication.runtimeClasspathConfigurationName))
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            }
        }
        maybeCreate(this@createPublication.sourcesElementsConfigurationName).apply {
            isCanBeConsumed = true
            attributes {
                attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
            }
        }
        maybeCreate(this@createPublication.javadocElementsConfigurationName).apply {
            isCanBeConsumed = true
            attributes {
                attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.JAVADOC))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
            }
        }
    }
    artifacts {
        add(this@createPublication.apiElementsConfigurationName, tasks.getByName(this@createPublication.jarTaskName)) {
            builtBy(this@createPublication.jarTaskName)
        }
        add(this@createPublication.runtimeElementsConfigurationName, tasks.getByName(this@createPublication.jarTaskName)) {
            builtBy(this@createPublication.jarTaskName)
        }
        add(this@createPublication.javadocElementsConfigurationName, tasks.getByName(this@createPublication.javadocJarTaskName)) {
            builtBy(this@createPublication.javadocJarTaskName)
            classifier = "javadoc"
        }
        add(this@createPublication.sourcesElementsConfigurationName, tasks.getByName(this@createPublication.sourcesJarTaskName)) {
            builtBy(this@createPublication.sourcesJarTaskName)
            classifier = "sources"
        }
    }
    abstract class InstrumentedJarsPlugin @Inject constructor(
        val softwareComponentFactory: SoftwareComponentFactory)
    val fac = project.objects.newInstance(InstrumentedJarsPlugin::class.java).softwareComponentFactory
    components {
        add(fac.adhoc("${name}Component"))
    }
    components {
        getByName("${name}Component", AdhocComponentWithVariants::class) {
            addVariantsFromConfiguration(configurations.getByName(this@createPublication.apiElementsConfigurationName)) {}
            addVariantsFromConfiguration(configurations.getByName(this@createPublication.runtimeElementsConfigurationName)) {
                mapToMavenScope("runtime")
            }
            addVariantsFromConfiguration(configurations.getByName(this@createPublication.sourcesElementsConfigurationName)) {}
            addVariantsFromConfiguration(configurations.getByName(this@createPublication.javadocElementsConfigurationName)) {}
        }
    }
    publishing {
        publications {
            create<MavenPublication>(name) {
                pom {
                    this@pom.name = name
                    this@pom.description = "PaulsCode SoundSystem"
                    this@pom.url = "https://web.archive.org/web/20200409022041/http://www.paulscode.com/forum/index.php?topic=4.0"
                    licenses {
                        license {
                            this@license.name = "The SoundSystem License"
                            this@license.url = "https://web.archive.org/web/20200409022057/http://www.paulscode.com/forum/index.php?topic=4.msg6#msg6"
                        }
                    }
                    developers {
                        developer {
                            this@developer.id = "paulscode"
                            this@developer.name = "Paul Lamb"
                            this@developer.url = "https://paulscode.com"
                        }
                        developer {
                            this@developer.id = "cpw"
                            this@developer.url = "https://cpw.github.io"
                            this@developer.email = "cpw@weeksfamily.ca"
                        }
                        developer {
                            this@developer.id = "halotroop2288"
                            this@developer.name = "Caroline Joy Bell"
                            this@developer.url = "https://web0.halotroop.com/caroline"
                            this@developer.email = "caroline@halotroop.com"
                        }
                    }
                    scm {
                        this@scm.connection = "scm:git:https://github.com/unimined/Paulscode-SoundSystem.git"
                        this@scm.developerConnection = "scm:git:ssh://git@github.com:unimined/Paulscode-SoundSystem.git"
                        this@scm.url = "https://github.com/unimined/Paulscode-SoundSystem"
                    }
                }
                groupId = project.group.toString()
                artifactId = name
                version = project.version.toString()

                from(components.getByName("${name}Component"))
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

/**
 * The main version of the SoundSystem
 */
val main: SourceSet by sourceSets.getting {
    createPublication("SoundSystem")
}
val test: SourceSet by sourceSets.getting

/**
 * SoundSystem loader with example XML
 */
val utils: SourceSet by sourceSets.libExtending("SoundSystem-Utils", main)

/**
 * Java's built-in audio library plugin
 */
val javaSoundPlugin: SourceSet by sourceSets.libExtending("LibraryJavaSound", main)

/**
 * LWJGL 2's OpenAL bindings plugin
 *
 * Not to be used at the same time as the [lwjgl3Plugin]!
 */
val lwjgl2Plugin: SourceSet by sourceSets.libExtending("LibraryLWJGLOpenAL", main)

/**
 * LWJGL 3's OpenAL bindings plugin
 *
 * Not to be used at the same time as the [lwjgl2Plugin]!
 */
val lwjgl3Plugin: SourceSet by sourceSets.libExtending("LibraryLWJGL3OpenAL", main)

/**
 * JogAmp's OpenAL bindings plugin
 */
val jogAmpPlugin: SourceSet by sourceSets.libExtending("LibraryJOAL", main)

/**
 * WAV codec plugin
 */
val wavPlugin: SourceSet by sourceSets.libExtending("CodecWAV", main)

/**
 * J-OGG-based OGG codec plugin
 */
val joggPlugin: SourceSet by sourceSets.libExtending("CodecJOgg", main)

/**
 * JOrbis-based OGG codec plugin
 */
val jOrbisPlugin: SourceSet by sourceSets.libExtending("CodecJOrbis", main)

/**
 * IBXM codec plugin
 */
val ibxmPlugin: SourceSet by sourceSets.libExtending("CodecIBXM", main)

/**
 * Speex codec plugin
 */
val jSpeexPlugin: SourceSet by sourceSets.libExtending("CodecJSpeex", main)

/**
 * jPCT-friendly version of the SoundSystem
 */
val jpct: SourceSet by sourceSets.libExtending("SoundSystem-jPCT", main, javaSoundPlugin, lwjgl2Plugin, joggPlugin, wavPlugin) {
    tasks.named(this.jarTaskName, Jar::class).configure {
        from(main.output)
        from(javaSoundPlugin.output)
        from(lwjgl2Plugin.output)
        from(joggPlugin.output)
        from(wavPlugin.output)
    }
}

/**
 * Sound Effect Player demo
 */
val playerDemo: SourceSet by sourceSets.extending("Player", jpct)

/**
 * Bullet / Target Collision demo
 */
val collisionDemo: SourceSet by sourceSets.extending("BulletTargetCollision", jpct)

/**
 * Holy Bouncing Helicopter Balls demo
 */
val helicopterDemo: SourceSet by sourceSets.extending("Helicopter", jpct)

dependencies {
    val jpctImplementation: Configuration = configurations.named(jpct.implementationConfigurationName).get()
    jpctImplementation(files("libs/jpct.jar"))

    val lwjglImplementation: Configuration = configurations.named(lwjgl2Plugin.implementationConfigurationName).get()
    lwjglImplementation(libs.lwjgl2)

    val lwjgl3Implementation: Configuration = configurations.named(lwjgl3Plugin.implementationConfigurationName).get()
    lwjgl3Implementation(libs.lwjgl3.openal)

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
