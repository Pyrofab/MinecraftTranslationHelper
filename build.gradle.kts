import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.11"
    id("com.github.johnrengelman.shadow") version "4.0.1"
}

group = "ladysnake"
version = "2.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")
    compile("no.tornado:tornadofx:1.7.16")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "ladysnake.translationhelper.TranslationHelper"
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}