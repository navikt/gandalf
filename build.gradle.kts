import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav"
version = file("version.txt").readText().trim()

object Version {
    const val gradleVersion = "6.2"
    const val kotlinLoggin = "1.7.8"
    const val nimbus = "7.3"
    const val snake = "1.26"
    const val hibernate = "5.4.14.Final"
    const val json = "20190722"
}

val mainClass = "$group.no.nav.gandalf.GandalfApplication"

plugins {
    application
    java
    val kotlinVersion = "1.3.21"
    id("org.springframework.boot") version "2.1.6.RELEASE"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
}

application {
    mainClassName = mainClass
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JSON
    implementation("org.json:json:${Version.json}")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("io.github.microutils:kotlin-logging:${Version.kotlinLoggin}")
    implementation("com.nimbusds:oauth2-oidc-sdk:${Version.nimbus}")
    implementation("org.yaml:snakeyaml:${Version.snake}")

    // Hibernate
    implementation("org.hibernate:hibernate-core:${Version.hibernate}")

    // test
    testImplementation("org.hibernate:hibernate-testing:${Version.hibernate}")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("com.vaadin.external.google", module = "android-json")
        exclude(module = "junit")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    create("printVersion") {
        println(project.version)
    }
    bootJar {
        mainClassName = mainClass
    }
    withType<Wrapper> {
        gradleVersion = Version.gradleVersion
        distributionType = Wrapper.DistributionType.BIN
    }
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
}