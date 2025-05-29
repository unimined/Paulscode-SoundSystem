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
version = if (project.hasProperty("version_snapshot")) "1.0.0-SNAPSHOT"
else SimpleDateFormat("yyyyMMddHHmm").format(Date())

repositories {
    mavenCentral()
    maven("https://maven.legacyfabric.net")
}

val main: SourceSet by sourceSets.getting

dependencies {
    implementation(libs.bundles.lwjgl)
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
    withJavadocJar()
}
