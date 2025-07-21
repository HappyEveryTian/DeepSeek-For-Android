import com.android.build.gradle.internal.utils.createPublishingInfoForLibrary
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.caiyu.deepseekdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.caiyu.deepseekdemo"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        buildFeatures {
            buildConfig = true
            viewBinding = true
        }

        // 配置deepseek api key在local.properties中对应的属性
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"${properties.getProperty("deepseek.api.key")}\"")
    }

    buildTypes {
        getByName("release") {
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
}
//
//publishing {
//    publications {
//        register<MavenPublication>("release") {
//            groupId = "com..cameraxmanager"
//            artifactId = "cameraxmanager"
//            version = "1.0.0"
//
//            afterEvaluate {
//                from(components["release"])
//            }
//        }
//    }
//}

dependencies {
    implementation(project(":deepseek-for-android"))
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.github.li-xiaojun:XPopup:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}