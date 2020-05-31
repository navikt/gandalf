import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

group = "no.nav"
version = file("version.txt").readText().trim()

object Version {
    const val gradleVersion = "6.3"
    const val kotlinLoggin = "1.7.9"
    const val nimbus = "8.4.2"
    const val snake = "1.26"
    const val hibernate = "5.4.14.Final"
    const val json = "20200518"
    const val apacheHttp = "4.5.12"
    const val wiremock = "2.26.3"
    const val oracle = "19.3.0.0"
    const val wiremockCloud = "2.2.3.RELEASE"
    const val jackson = "2.11.0"
    const val unboundid = "5.0.1"
    const val micrometer = "1.1.5"
}

val mainClass = "no.nav.gandalf.GandalfApplication"

plugins {
    application
    java
    val kotlinVersion = "1.3.72"
    kotlin("plugin.allopen") version kotlinVersion
    id("org.jmailen.kotlinter") version "2.3.2"
    id("com.github.ben-manes.versions") version "0.28.0"
    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

application {
    mainClassName = "no.nav.gandalf.GandalfApplication"
}

repositories {
    maven(url = "http://packages.confluent.io/maven")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("javax.inject:javax.inject:1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Version.jackson}")
    implementation("org.json:json:${Version.json}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.apache.httpcomponents:httpclient:${Version.apacheHttp}")
    implementation("io.github.microutils:kotlin-logging:${Version.kotlinLoggin}")
    implementation("com.nimbusds:oauth2-oidc-sdk:${Version.nimbus}")
    implementation("org.yaml:snakeyaml:${Version.snake}")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.hibernate:hibernate-core:${Version.hibernate}")
    implementation("com.oracle.ojdbc:ojdbc8:${Version.oracle}")
    implementation("org.springframework.ldap:spring-ldap-core")
    implementation("org.springframework.security:spring-security-ldap")
    implementation("com.unboundid:unboundid-ldapsdk:${Version.unboundid}")
    implementation("io.micrometer:micrometer-registry-prometheus:${Version.micrometer}")

    // test
    testImplementation("org.hibernate:hibernate-testing:${Version.hibernate}")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("com.vaadin.external.google", module = "android-json")
        exclude(module = "junit")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8:${Version.wiremock}")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:${Version.wiremockCloud}")
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
    withType<Test> {
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
    withType<DependencyUpdatesTask> {

        // optional parameters
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
    }
}
