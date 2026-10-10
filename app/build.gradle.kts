plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.prasuco.gita"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.prasuco.gita"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            // Dual source: env vars (CI) take precedence, gradle.properties (local) as fallback.
            // Env: SIGNING_KEY_STORE_PATH, SIGNING_STORE_PASSWORD, SIGNING_KEY_ALIAS, SIGNING_KEY_PASSWORD
            // Props (~/.gradle/gradle.properties): SIGNING_STORE_FILE, SIGNING_STORE_PASSWORD,
            //   SIGNING_KEY_ALIAS, SIGNING_KEY_PASSWORD
            val storePath = System.getenv("SIGNING_KEY_STORE_PATH")
                ?: (project.findProperty("SIGNING_STORE_FILE") as String?)
            val storePass = System.getenv("SIGNING_STORE_PASSWORD")
                ?: (project.findProperty("SIGNING_STORE_PASSWORD") as String?)
            val aliasProp = System.getenv("SIGNING_KEY_ALIAS")
                ?: (project.findProperty("SIGNING_KEY_ALIAS") as String?)
            val keyPass = System.getenv("SIGNING_KEY_PASSWORD")
                ?: (project.findProperty("SIGNING_KEY_PASSWORD") as String?)
            if (!storePath.isNullOrBlank() && file(storePath).exists()) {
                storeFile = file(storePath)
                storePassword = storePass
                keyAlias = aliasProp
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            val requestedRelease = gradle.startParameter.taskNames.any {
                it.contains("Release", ignoreCase = true)
            }
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            } else if (requestedRelease) {
                throw GradleException(
                    "Release signing not configured. Set SIGNING_KEY_STORE_PATH/SIGNING_STORE_PASSWORD/" +
                        "SIGNING_KEY_ALIAS/SIGNING_KEY_PASSWORD env vars (CI) or SIGNING_STORE_FILE etc. " +
                        "in ~/.gradle/gradle.properties (local). See keystores/KEYSTORE_INFO.txt."
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
