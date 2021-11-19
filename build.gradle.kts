import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    kotlin("multiplatform") version "1.6.0"
    id("maven-publish")
}

group = "ro.jwt"
version = getCurrentVersion()

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        nodejs()
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}

publishing {
    repositories {
        maven {
            name = "DummyLocal"
            url = uri(layout.buildDirectory.dir("localPublishRepo"))
        }
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/mirceanis/komuta")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

fun getCurrentVersion(): String {
    val props = loadProperties("version.properties")
    return props.getProperty("version") ?: "0.0.42-dev"
}
