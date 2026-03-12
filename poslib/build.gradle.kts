plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.daou.kwpsdk"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    lint {
        abortOnError = false
    }
}

dependencies {
    // Flutter 의존성 추가
    compileOnly("io.flutter:flutter_embedding_release:1.0.0-0fddccd3109d1fd3f6022cd1a741b3531047f1e6")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(files("libs/d2xx.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}