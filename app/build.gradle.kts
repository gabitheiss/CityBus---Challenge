plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.android.citybus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.citybus"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    viewBinding {
        enable = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
//    implementation (libs.androidx.activity.ktx)
//    implementation (libs.androidx.fragment.ktx)
    implementation (libs.androidx.navigation.fragment.ktx)

    //LottieFiles
    implementation(libs.lottie)

    //Material
    implementation(libs.material)

    //Retrofit
    implementation (libs.retrofit)
    implementation (libs.logging.interceptor)

    //Gson
    implementation (libs.converter.gson)

    //Koin
    implementation(libs.koin.android)

    //OpenStreetMap
    implementation (libs.osmdroid.android)
}