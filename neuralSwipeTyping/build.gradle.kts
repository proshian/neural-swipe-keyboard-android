plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "2.1.0"
    id("maven-publish")
}

android {
    namespace = "io.github.proshian.neuralswipetyping"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}


afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])

                groupId = "io.github.proshian"
                artifactId = "neuralswipetyping"
                version = "0.1.0-alpha-3"


                pom {
                    name.set("Neural Swipe Typing")
                    description.set("Android library for swipe typing decoding")
                    url.set("https://github.com/proshian/neural-swipe-keyboard-android")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                }


            }
        }

        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/proshian/neural-swipe-keyboard-android")
                credentials {
                    username = System.getenv("GPR_USER")
                    password = System.getenv("GPR_API_KEY")
                }
            }
        }
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":trie"))
    implementation(libs.executorch.android)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}