import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "ladysnake"
version = "2.1.0"

// https://stackoverflow.com/questions/63997525/task-run-causes-org-joor-reflectexception-java-lang-nosuchfieldexception-ja
val currentOS = DefaultNativePlatform.getCurrentOperatingSystem()
val platform = when {
    currentOS.isWindows() -> "win"
    currentOS.isLinux() -> "linux"
    currentOS.isMacOsX() -> "mac"
    else -> throw IllegalStateException()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit", "junit", "4.12")
    implementation("org.openjfx:javafx-base:15.0.1:${platform}")
    implementation("org.openjfx:javafx-controls:15.0.1:${platform}")
    implementation("org.openjfx:javafx-graphics:15.0.1:${platform}")
    implementation("org.openjfx:javafx-fxml:15.0.1:${platform}")
    implementation("no.tornado:tornadofx:1.7.20")
}

application {
    mainModule.set("ladysnake.translationhelper.app")
    mainClass.set("ladysnake.translationhelper.TranslationHelperKt")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "ladysnake.translationhelper.TranslationHelper"
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
}