import java.security.MessageDigest

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.docscanner"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.docscanner"
        minSdk = 26
        targetSdk = 36
        versionCode = 10
        versionName = "1.9"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        // ---- a build you can actually judge the app by -----------------------
        // `assembleDebug` produces a *debuggable* APK: ART interprets and JITs
        // instead of running optimised code, and Compose's own runtime keeps its
        // debug hooks live. That penalty is multiplicative and it lands hardest on
        // the screen with the most work per frame, which is why a photo grid can
        // look janky next to a list of text in a debug build and be perfectly
        // smooth in the same code built for release.
        //
        // `release` is not installable here (no release keystore), so this type is
        // identical to it — no minification, no shrinking — except that it is
        // non-debuggable and signed with the debug key. That makes it the honest
        // answer to "is this the code or the build": install it over the debug app
        // (same application id, same key, data kept) and compare.
        //
        //     ./gradlew assembleBenchmark
        //     adb install -r app/build/outputs/apk/benchmark/app-benchmark.apk
        create("benchmark") {
            // Qualified deliberately: inside this block the receiver is the build
            // type, not the container.
            initWith(android.buildTypes.getByName("release"))
            isDebuggable = false
            matchingFallbacks += listOf("release")
            // `findByName`, not `getByName`: this must never be able to break
            // `assembleDebug` on a machine without a debug keystore configured yet.
            signingConfig = android.signingConfigs.findByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }

    // `.tflite` must not be deflated.
    //
    // `MlBorderDetector` memory-maps the model with `AssetManager.openFd()` and
    // `FileChannel.map()`, which is the only way to load a 4.7 MB model without
    // copying it through the heap. `openFd` throws
    // `FileNotFoundException: This file can not be opened as a file descriptor;
    // it is probably compressed` for anything aapt2 deflated — and `.tflite` is
    // not in aapt2's default no-compress list. Without this line the model never
    // loads and the detector silently falls back to the Kotlin geometry pass,
    // which looks exactly like "the ML feature does nothing". See
    // docs/AGENT_COORDINATION.md §6i, which asked for this when the asset was
    // first proposed.
    androidResources {
      noCompress += "tflite"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.maxParallelForks = 1
            // 512m, not 256m. Each test worker is its own JVM, so this heap has
            // nothing to do with the Gradle daemon's — shrinking it saves no build
            // memory, it only moves an OutOfMemoryError from the daemon (which
            // reports it clearly) into the forked test JVM (which reports it as a
            // crash in whichever test happened to be running). The suite decodes
            // nothing, but several thousand RANSAC iterations across the detector
            // tests allocate more than 256m of garbage between collections.
            it.jvmArgs("-Xmx512m", "-XX:+UseParallelGC")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

// ---------------------------------------------------------------------------
// Rust pixel kernels (app/src/main/rust)
// ---------------------------------------------------------------------------
// Optional by design. `util/NativeFilters` and `DocumentFilters` are written so
// that the app is complete and correct without this task ever running: if the
// toolchain is missing, the .so is simply absent and every filter uses the Kotlin
// reference implementation it always used.
//
// To build them you need the Android NDK plus:
//     cargo install cargo-ndk
//     rustup target add aarch64-linux-android x86_64-linux-android
// and ANDROID_NDK_HOME pointing at an installed NDK.
//
// The output goes to src/main/jniLibs/, which is git-ignored: binaries are build
// artifacts here (AGENTS.md §2.5), so they are produced, never committed.

fun hasExecutableOnPath(executable: String): Boolean {
    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val candidates = if (isWindows) listOf("$executable.exe", "$executable.cmd", "$executable.bat", executable) else listOf(executable)
    val pathDirs = System.getenv("PATH")?.split(File.pathSeparator).orEmpty()
    return pathDirs.any { dir ->
        candidates.any { name -> File(dir, name).isFile }
    }
}

val rustDir = layout.projectDirectory.dir("src/main/rust").asFile
val jniLibsDir = layout.projectDirectory.dir("src/main/jniLibs").asFile
val rustToolchainReady = hasExecutableOnPath("cargo") && hasExecutableOnPath("cargo-ndk")

val buildRustKernels = tasks.register<Exec>("buildRustKernels") {
    group = "build"
    description = "Cross-compiles the Rust pixel kernels into src/main/jniLibs (skipped without cargo-ndk)."
    workingDir = rustDir
    commandLine(
        "cargo", "ndk",
        "-t", "arm64-v8a",
        "-t", "x86_64",
        "-o", jniLibsDir.absolutePath,
        "build", "--release"
    )
    enabled = rustToolchainReady
}

tasks.matching { it.name == "preBuild" }.configureEach { dependsOn(buildRustKernels) }

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // CameraX — preview + still capture for the scanner
  implementation(libs.androidx.camera.core)
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)

  // On-device text recognition: turns a saved page into searchable text.
  // See util/OcrEngine.kt and docs/AGENT_COORDINATION.md §6i.
  implementation(libs.mlkit.text.recognition)

  // JSON persistence for the document library
  implementation(libs.kotlinx.serialization.json)

  // Material Icons Extended
  implementation("androidx.compose.material:material-icons-extended")

  // TensorFlow Lite (matches original app ML border detection)
  implementation(libs.tensorflow.lite)
}

/**
 * Fails the build when the border model is gone.
 *
 * `MlBorderDetector` degrades to the Kotlin geometry pass when
 * `assets/border_detect_224.tflite` is missing — correct at runtime, invisible at
 * build time. That is how a build ends up shipped without the feature it advertises,
 * and it is exactly the failure a device test cannot diagnose from the outside. The
 * check is cheap (one 4.7 MB read) and runs before every build's `preBuild`.
 *
 * The hash is *reported*, not enforced: replacing the model with a re-exported one is
 * legitimate, and a hard failure there would be someone else's Tuesday. A mismatch is
 * a warning plus the new hash, so the pinned value below can be updated deliberately.
 *
 * Note for `.gitignore` / AGENTS.md §2.5: this asset is a binary and is supposed to be
 * provisioned rather than committed. It is committed today (see
 * docs/AGENT_COORDINATION.md §6i); if it moves to the ignored `assets/models/` path,
 * this task is what keeps the omission loud.
 */
abstract class VerifyBorderModelTask : DefaultTask() {
    @get:InputFile
    abstract val modelFile: RegularFileProperty

    @get:Input
    abstract val expectedSha256: Property<String>

    @TaskAction
    fun verify() {
        val file = modelFile.get().asFile
        if (!file.isFile || file.length() == 0L) {
            throw GradleException(
                "Missing border model: ${file.path}. The app will silently fall back to " +
                    "the geometry detector. Provision it (docs/AGENT_COORDINATION.md §6i) " +
                    "or drop the ML path deliberately."
            )
        }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(file.readBytes())
            .joinToString("") { "%02x".format(it) }
        val pinned = expectedSha256.get()
        if (digest != pinned) {
            logger.warn(
                "border model hash changed (${digest.take(16)}…, pinned " +
                    "${pinned.take(16)}…). If the model was re-sourced on " +
                    "purpose, update `borderModelSha256` in app/build.gradle.kts."
            )
        }
    }
}

val verifyBorderModel = tasks.register<VerifyBorderModelTask>("verifyBorderModel") {
    group = "verification"
    description = "Checks that the TFLite border model is present and reports its SHA-256."
    modelFile.set(layout.projectDirectory.file("src/main/assets/border_detect_224.tflite"))
    expectedSha256.set("578f53b6c90ee9bc14b6ba350c3d261ffd6001d3e9b716a7ad571b6a4bdd1972")
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(verifyBorderModel)
}
