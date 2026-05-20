plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {

    namespace = "com.example.nhom5projectmobile"

    compileSdk = 36

    defaultConfig {

        applicationId = "com.example.nhom5projectmobile"

        minSdk = 24

        targetSdk = 36

        versionCode = 1

        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        release {

            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_11

        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {

        resources {

            excludes +=
                "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.appcompat)

    implementation(libs.material)

    implementation(libs.activity)

    implementation(libs.constraintlayout)

    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)

    androidTestImplementation(libs.espresso.core)

    // =========================
    // FIREBASE
    // =========================

    implementation(
        platform(
            "com.google.firebase:firebase-bom:33.1.2"
        )
    )

    implementation(
        "com.google.firebase:firebase-auth"
    )

    implementation(
        "com.google.firebase:firebase-firestore"
    )

    implementation(
        "com.google.firebase:firebase-storage"
    )

    implementation(
        "com.google.firebase:firebase-messaging"
    )

    implementation(
        "com.google.firebase:firebase-analytics"
    )

    // =========================
    // GOOGLE LOGIN
    // =========================

    implementation(
        "com.google.android.gms:play-services-auth:21.0.0"
    )

    // =========================
    // FACEBOOK LOGIN
    // =========================

    implementation(
        "com.facebook.android:facebook-login:17.0.2"
    )

    // =========================
    // GLIDE
    // =========================

    implementation(
        "com.github.bumptech.glide:glide:4.16.0"
    )

    annotationProcessor(
        "com.github.bumptech.glide:compiler:4.16.0"
    )

    // =========================
    // PDF VIEW
    // =========================

    implementation(
        "com.github.mhiew:android-pdf-viewer:3.2.0-beta.1"
    )

    // =========================
    // OKHTTP
    // =========================

    implementation(
        "com.squareup.okhttp3:okhttp:4.12.0"
    )
}