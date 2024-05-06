import org.jetbrains.dokka.gradle.DokkaTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.dokka") version "1.9.20"
}

/*tasks.named<DokkaTask>("dokkaHtml"){
    dokkaSourceSets{
        named("main"){
            //sourceRoot("app/src/main/java")
            //sourceRoot("app/src/main/kotlin")
            sourceRoot("src")
        }
    }
}

 */

/*tasks.dokkaHtml.configure{
    outputDirectory.set(buildDir.resolve("dokka"))
    dokkaSourceSets{
        configureEach{
            sourceRoots.from(file("src/main/java/com/example/jmb_bms"))
        }
    }
}

 */

