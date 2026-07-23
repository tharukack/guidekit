import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("com.android.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "io.github.tharukack"
version = "1.1.0"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "GuideKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.material.icons.extended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "io.github.tharukack.guidekit"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = "io.github.tharukack",
        artifactId = "guidekit",
        version = version.toString(),
    )

    pom {
        name.set("GuideKit")
        description.set(
            "A Compose Multiplatform coachmark overlay library for " +
                    "product tours, onboarding hints, and guided feature discovery."
        )

        inceptionYear.set("2026")
        url.set("https://github.com/tharukack/guidekit")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("tharukack")
                name.set("Tharuka Chathura")
                url.set("https://github.com/tharukack")
            }
        }

        scm {
            url.set("https://github.com/tharukack/guidekit")
            connection.set(
                "scm:git:git://github.com/tharukack/guidekit.git"
            )
            developerConnection.set(
                "scm:git:ssh://git@github.com/tharukack/guidekit.git"
            )
        }
    }
}
