plugins {
    `java-library-distribution`
    `maven-publish`
}

group = "com.desiderantes"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.java.dev.jna:jna:5.13.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
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
                        url.set("http://www.gnu.org/licenses/lgpl.html")
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
