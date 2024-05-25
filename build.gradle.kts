plugins {
    `java-library`
    alias(libs.plugins.gradleJavaConventions)
}

repositories { mavenCentral() }

group = "com.opencastsoftware"

description = "A Prettier Printer for Java"

java { toolchain.languageVersion.set(JavaLanguageVersion.of(11)) }

dependencies {
    testImplementation(libs.junitJupiter)
    testImplementation(libs.jqwik)
    testImplementation(libs.hamcrest)
    testImplementation(libs.equalsVerifier)
    testImplementation(libs.toStringVerifier)
}

mavenPublishing {
    coordinates("com.opencastsoftware", "prettier4j", project.version.toString())

    pom {
        name.set("prettier4j")
        description.set(project.description)
        url.set("https://github.com/opencastsoftware/prettier4j")
        inceptionYear.set("2022")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        organization {
            name.set("Opencast Software Europe Ltd")
            url.set("https://opencastsoftware.com")
        }
        developers {
            developer {
                id.set("DavidGregory084")
                name.set("David Gregory")
                organization.set("Opencast Software Europe Ltd")
                organizationUrl.set("https://opencastsoftware.com/")
                timezone.set("Europe/London")
                url.set("https://github.com/DavidGregory084")
            }
        }
        ciManagement {
            system.set("Github Actions")
            url.set("https://github.com/opencastsoftware/prettier4j/actions")
        }
        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/opencastsoftware/prettier4j/issues")
        }
        scm {
            connection.set("scm:git:https://github.com/opencastsoftware/prettier4j.git")
            developerConnection.set("scm:git:git@github.com:opencastsoftware/prettier4j.git")
            url.set("https://github.com/opencastsoftware/prettier4j")
        }
    }
}

tasks.withType<JavaCompile> {
    // Target Java 8
    options.release.set(8)
}

tasks.withType<Javadoc> {
    options {
        this as StandardJavadocDocletOptions
        addBooleanOption("-allow-script-in-comments", true)
        header(
            """
            |<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/prismjs@1/themes/prism-okaidia.min.css">
            """
                .trimMargin()
        )
        footer(
            """
            |<script src="https://cdn.jsdelivr.net/npm/prismjs@1/components/prism-core.min.js"></script>
            |<script src="https://cdn.jsdelivr.net/npm/prismjs@1/plugins/autoloader/prism-autoloader.min.js"></script>
            """
                .trimMargin()
        )
    }
}

tasks.named<Test>("test") { useJUnitPlatform { includeEngines("junit-jupiter", "jqwik") } }
