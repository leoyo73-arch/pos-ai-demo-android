plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.posaidemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.posaidemo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // 서버 주소. 에뮬레이터는 10.0.2.2가 PC의 localhost입니다.
        // 실제 폰으로 테스트할 때는 PC의 IP(예: "http://192.168.0.10:8000/")로 바꾸세요.
        buildConfigField("String", "SERVER_URL", "\"http://10.101.225.31:8000/\"")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.8.5")       // 탭마다 Fragment + by viewModels()
    implementation("androidx.viewpager2:viewpager2:1.1.0")       // 상담 / 비서 탭 전환

    // MVVM
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // 서버 통신
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
