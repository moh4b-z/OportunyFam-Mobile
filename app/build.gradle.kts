plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "com.oportunyfam_mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.oportunyfam_mobile"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        // ...
    }

    buildTypes {
        // ...
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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Room: Implementação (runtime)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Suas bibliotecas
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // COIL
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Datastore
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    //mapa
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:4.3.3") // integração com Jetpack Compose

    implementation("com.google.android.gms:play-services-location:21.0.1")//Para obter a posição atual do usuário

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.accompanist:accompanist-permissions:0.30.1")
    implementation("com.google.android.libraries.places:places:3.5.0")

    // Remover dependências OSM
    // implementation("org.osmdroid:osmdroid-android:6.1.18")
    // implementation("org.osmdroid:osmdroid-mapsforge:6.1.18")
    // implementation("androidx.preference:preference-ktx:1.2.1")
}

room {
    schemaDirectory("$projectDir/schemas")
}