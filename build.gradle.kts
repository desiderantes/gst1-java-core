plugins {
    `java-library-distribution`
    `maven-publish`
    alias(libs.plugins.ben.manes.versions)
    `jvm-test-suite`
    alias(libs.plugins.spotless)
}

group = "com.desiderantes"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = libs.versions.java.map(JavaVersion::toVersion).get()
    targetCompatibility = libs.versions.java.map(JavaVersion::toVersion).get()
}

tasks.javadoc {
    options {
        exclude("**/org/freedesktop/gstreamer/lowlevel/**")
    }
}

dependencies {
    implementation(libs.jna)
}
@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            val nativeTestForks = providers.gradleProperty("nativeTestForks")
                .map(String::toInt)
                .orElse(32)

            useJUnitJupiter(libs.versions.junit.jupiter)
            dependencies {
                implementation(platform(libs.junit.bom))
                implementation(libs.junit.jupiter)
                runtimeOnly(libs.junit.platform.launcher)
            }

            targets.all {
                testTask.configure {
                    this.jvmArgs("-XX:+IgnoreUnrecognizedVMOptions", "--enable-native-access=ALL-UNNAMED", "-Djna.nosys=true")
                    // Native GStreamer tests are unstable when many JVM test forks run concurrently.
                    this.maxParallelForks = nativeTestForks.get()
                    // Also isolate class-level native lifecycle by using a fresh JVM per test class.
                    //this.forkEvery = 1
                }
            }
        }
    }
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("src/main/java/org/freedesktop/gstreamer/Gst.java"), "package")
        eclipse().configFile(rootProject.file("eclipse-formatter.xml"))
        importOrder()
        removeUnusedImports()
    }
}

publishing {
    repositories {
        mavenLocal()
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
            pom {

                licenses {
                    license {
                        name.set("LGPL-3.0-only")
                        url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
                        distribution.set("repo")
                    }
                }

                scm {
                    url.set("https://github.com/gstreamer-java/gst1-java-core")
                    connection.set("scm:git:https://github.com/gstreamer-java/gst1-java-core.git")
                    developerConnection.set("scm:git:https://github.com/gstreamer-java/gst1-java-core.git")
                }

                developers {
                    developer {
                        id.set("neilcsmith-net")
                        name.set("Neil C. Smith")
                        organization.set("Codelerity Ltd.")
                        email.set("neil@codelerity.com")
                        roles.addAll(
                            "Lead maintainer",
                            "Developer"
                        )
                        url.set("https://www.codelerity.com")
                    }
                    developer {
                        id.set("wmeissner")
                        name.set("Wayne Meissner")
                        roles.addAll(
                            "Founder of GStreamer 0.10 bindings"
                        )
                    }

                }

            }
        }
    }
}
