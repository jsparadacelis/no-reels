plugins {
    id("com.android.application")
}

android {
    namespace = "com.sinreels"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sinreels"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
    }

    // Llave fija para que cada versión nueva se instale encima de la anterior
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
