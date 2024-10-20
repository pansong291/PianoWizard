plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "pansong291.piano.wizard"
    compileSdk = 34

    defaultConfig {
        applicationId = "pansong291.piano.wizard"
        minSdk = 24
        targetSdk = 34
        versionCode = 20241020
        versionName = "1.2.0-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            resValue("string", "build_app_name", "@string/app_name")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // 调试版本，添加一个后缀来区分
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
            // 这个函数只能添加资源，无法覆盖原有的同名资源
            resValue("string", "build_app_name", "PianoWizard Debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.getactivity.xxpermissions)
    implementation(libs.getactivity.easywindow)
    implementation(libs.google.gson)
    implementation(libs.simplecityapps.recyclerview.fastscroll)
    implementation(libs.googlecode.juniversalchardet)
    implementation(libs.xw.repo.bubbleseekbar)
    implementation(libs.androidx.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
