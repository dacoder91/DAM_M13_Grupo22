plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.doggo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.doggo"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // TODO: Agrega las dependencias para los productos de Firebase que deseas usar
    // Cuando uses el BoM, no especifiques versiones en las dependencias de Firebase
    implementation(libs.firebase.analytics)


    // Agrega las dependencias para cualquier otro producto de Firebase que desees
    // https://firebase.google.com/docs/android/setup#available-libraries
    // Compose
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.compose.ui:ui:1.6.1")
    implementation ("androidx.compose.material:material-icons-extended:1.6.1")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.6.1")
    implementation ("androidx.navigation:navigation-compose:2.7.7")

    // Firebase
    implementation (platform("com.google.firebase:firebase-bom:33.11.0"))
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.7.0") // Para collectAsState con Flows
    implementation (libs.firebase.firestore.ktx)

    // Maps
    implementation (libs.maps.compose)
    implementation ("com.google.android.gms:play-services-maps:19.1.0")

    // Coil para imágenes
    implementation (libs.coil.compose)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}

