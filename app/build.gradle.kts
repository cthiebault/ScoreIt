/*
 * Copyright 2020 Stéphane Baiget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    id("jacoco-convention")
}

val versionMajor = 5
val versionMinor = 5
val versionPatch = 2

android {
    namespace = "com.sbgapps.scoreit"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        versionCode = versionMajor * 100 + versionMinor * 10 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            isJniDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    packaging {
        resources {
            excludes += listOf(
                "**/*.kotlin_module",
                "**/*.version",
                "**/kotlin/**",
                "**/*.txt",
                "**/*.xml",
                "**/*.properties"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":cache"))

    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.coreKtx)
    implementation(libs.material)
    implementation(libs.reviewKtx)
    implementation(libs.billingKtx)
    implementation(libs.koinAndroid)
    implementation(libs.koinCompose)
    implementation(libs.timber)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.activity)
    implementation(libs.compose.lifecycle.runtime)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)

    coreLibraryDesugaring(libs.desugaring)
}
