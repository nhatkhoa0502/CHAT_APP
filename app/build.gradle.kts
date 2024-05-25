plugins {
    id("com.android.application")
}

android {
    namespace = "com.abc.myapplication"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.abc.myapplication"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

     implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.google.firebase:firebase-database:21.0.0")

    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")

    implementation("com.github.bumptech.glide:glide:4.16.0") //Load Image
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.opentok.android:opentok-android-sdk:2.27.1") // Video Call
    implementation("pub.devrel:easypermissions:3.0.0")

    implementation("com.journeyapps:zxing-android-embedded:4.3.0") //QR Code
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-auth") // Firebase Auth

    // Google Play services
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // Call API
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")

//    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.2")
//    implementation("androidx.multidex:multidex:2.0.1")
}