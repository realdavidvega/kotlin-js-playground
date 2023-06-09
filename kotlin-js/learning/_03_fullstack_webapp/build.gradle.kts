import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlin = "1.8.10"
val serialization = "1.3.3"
val coroutines = "1.6.2"
val ktor = "2.2.4"
val logback = "1.2.11"
val wrappers = "1.0.0-pre.525"
val mongo = "4.5.0"

fun kotlinw(target: String): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target"

plugins {
    kotlin("multiplatform") version "1.8.10"
    application //to run JVM part
    kotlin("plugin.serialization") version "1.8.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
    }
    js {
        browser {
            binaries.executable()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
                implementation("io.ktor:ktor-client-core:$ktor")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-serialization:$ktor")
                implementation("io.ktor:ktor-server-content-negotiation:$ktor")
                implementation("io.ktor:ktor-server-cors:$ktor")
                implementation("io.ktor:ktor-server-compression:$ktor")
                implementation("io.ktor:ktor-server-core-jvm:$ktor")
                implementation("io.ktor:ktor-server-netty:$ktor")
                implementation("ch.qos.logback:logback-classic:$logback")
                implementation("org.litote.kmongo:kmongo-coroutine-serialization:$mongo")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktor")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor")
                implementation(enforcedPlatform(kotlinw("wrappers-bom:$wrappers")))
                implementation(kotlinw("browser"))
                implementation(kotlinw("react"))
                implementation(kotlinw("react-dom"))
                implementation(npm("uuid", "9.0.0"))
            }
        }
    }
}

application {
    mainClass.set("MainKt")
}

// include JS artifacts in any JAR we generate
tasks.getByName<Jar>("jvmJar") {
    val taskName = if (project.hasProperty("isProduction")
        || project.gradle.startParameter.taskNames.contains("installDist")
    ) {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<KotlinWebpack>(taskName)
    dependsOn(webpackTask) // make sure JS gets compiled first
    from(File(webpackTask.destinationDirectory, webpackTask.outputFileName)) // bring output file along into the JAR
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

distributions {
    main {
        contents {
            from("$buildDir/libs") {
                rename("${rootProject.name}-jvm", rootProject.name)
                into("lib")
            }
        }
    }
}

// Alias "installDist" as "stage" (for cloud providers)
tasks.create("stage") {
    dependsOn(tasks.getByName("installDist"))
}

tasks.getByName<JavaExec>("run") {
    classpath(tasks.getByName<Jar>("jvmJar")) // so that the JS artifacts generated by `jvmJar` can be found and served
}
