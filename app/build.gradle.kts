import java.util.Properties
import java.io.FileInputStream

// 1. Load the local.properties file
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

// 2. Helper function to get a variable and strip any existing quotes
fun getEnvOrProperty(key: String, defaultValue: String): String {
    val value = System.getenv(key) ?: localProperties.getProperty(key) ?: defaultValue
    // Remove any surrounding quotes that might be in the source string
    return value.replace("\"", "")
}

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.sjfailure.kccommunityconnect"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.sjfailure.kccommunityconnect"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true 
        viewBinding = true
    }

    flavorDimensions.add("environment")

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-DEV"
            val rawUrl = getEnvOrProperty("DEBUG_API_URL", "http://10.0.2.2:8000/api/")
            // Wrap in exactly one set of escaped quotes for the Java file
            buildConfigField("String", "API_BASE_URL", "\"$rawUrl\"")
        }
        create("prod") {
            dimension = "environment"
            applicationIdSuffix = ".prod"
            versionNameSuffix = "-PROD"
            val rawUrl = getEnvOrProperty("RELEASE_API_URL", "https://api.yourproductionurl.com/api/")
            buildConfigField("String", "API_BASE_URL", "\"$rawUrl\"")
        }
    }

    buildTypes {
        getByName("debug") {
        }
        getByName("release") {
            isMinifyEnabled = true 
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.recyclerview)
    implementation(libs.impress)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation(libs.recyclerview.v120)
    implementation("com.applandeo:material-calendar-view:1.9.2")
}
