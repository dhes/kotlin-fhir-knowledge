plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.maven.publish)
}

val mavenGroupId: String by project
val mavenArtifactId: String by project
val mavenVersion: String by project

group = mavenGroupId

version = mavenVersion

kotlin {
  jvmToolchain(21)

  androidLibrary {
    namespace = "dev.ohs.fhir.knowledge"
    compileSdk = 36
    minSdk = 26
    withHostTestBuilder {}
  }

  jvm("desktop")

  // iosX64 is omitted because androidx.room3 does not publish it; including it breaks dependency
  // resolution. kotlin-fhir-engine drops it for the same reason.
  iosArm64()
  iosSimulatorArm64()

  // The library caches FHIR NPM packages on disk, and okio publishes neither FileSystem.SYSTEM nor
  // GzipSource for js/wasmJs, so there are no web targets.

  targets.configureEach {
    compilations.configureEach {
      compilerOptions.configure {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        optIn.add("kotlin.time.ExperimentalTime")
      }
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.androidx.room3.runtime)
        implementation(libs.kermit)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.client.core)
        implementation(libs.ohs.fhir.model)
        api(libs.okio)
      }
    }
    commonTest {
      dependencies {
        implementation(libs.kotest.assertions.core)
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.ktor.client.mock)
        implementation(libs.okio.fakefilesystem)
      }
    }
    androidMain.dependencies {
      implementation(libs.androidx.sqlite.bundled)
      implementation(libs.ktor.client.okhttp)
    }
    val desktopMain by getting {
      dependencies {
        implementation(libs.androidx.sqlite.bundled)
        implementation(libs.ktor.client.java)
      }
    }
    iosMain.dependencies {
      implementation(libs.androidx.sqlite.bundled)
      implementation(libs.ktor.client.darwin)
    }
  }
}

dependencies {
  listOf("kspAndroid", "kspDesktop", "kspIosArm64", "kspIosSimulatorArm64").forEach {
    add(it, libs.androidx.room3.compiler)
  }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

// androidx.sqlite:sqlite-bundled ships Android ABI binaries only, so the database-backed tests
// cannot open a connection on a host JVM. They run against the desktop and iOS targets, compiled
// from the same common source set.
tasks.withType<Test>().configureEach {
  if (name == "testAndroidHostTest") {
    listOf("*Knowledge*Test").forEach(filter::excludeTestsMatching)
  }
}

mavenPublishing {
  publishToMavenCentral()
  signAllPublications()
  coordinates(mavenGroupId, mavenArtifactId, mavenVersion)

  pom {
    name = "Kotlin FHIR Knowledge Manager"
    description =
      "A Kotlin Multiplatform library for installing FHIR implementation guides from NPM " +
        "packages and resolving the knowledge artifacts they hold by canonical URL"
    inceptionYear = "2026"
    url = "https://github.com/ohs-foundation/kotlin-fhir-knowledge"
    licenses {
      license {
        name = "The Apache License, Version 2.0"
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
        distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }
    developers {
      developer {
        id = "ohs-foundation"
        name = "Open Health Stack Foundation"
        url = "https://ohs.dev/"
      }
    }
    scm {
      url = "https://github.com/ohs-foundation/kotlin-fhir-knowledge/"
      connection = "scm:git:git://github.com/ohs-foundation/kotlin-fhir-knowledge.git"
      developerConnection = "scm:git:ssh://git@github.com/ohs-foundation/kotlin-fhir-knowledge.git"
    }
  }
}
