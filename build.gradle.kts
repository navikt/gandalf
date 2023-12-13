import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springSecurity = "6.2.0"
val snakeYaml = "2.2"
val mockk = "1.13.8"
val kotest = "5.8.0"
val apacheHttp = "5.3"
val gradleVersion = "8.4"
val json = "20231013"
val kotlinLoggin = "3.0.5"
val logbackStash = "7.4"
val logbackClassic = "1.4.14"
val mockOAuth2Server = "2.1.0"
val nimbus = "11.7.1"
val openapi = "2.3.0"
val unboundid = "6.0.11"
val wiremock = "3.0.1"
val wiremockCloud = "4.1.0"
val h2 = "2.2.224"
val jacksonDatatype = "2.16.0"
val conscrypt = "2.5.2"
val prometheus = "1.12.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

plugins {
    application
    java
    val kotlinVersion = "1.9.21"
    kotlin("plugin.allopen") version kotlinVersion
    id("org.jlleitschuh.gradle.ktlint") version "12.0.3"
    id("com.github.ben-manes.versions") version "0.50.0"
    id("org.springframework.boot") version "3.2.0"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("io.spring.dependency-management") version "1.1.4"
}

application {
    mainClass.set("no.nav.gandalf.GandalfApplication")
}

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven")
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation("ch.qos.logback:logback-classic:$logbackClassic")
    implementation("ch.qos.logback:logback-core:$logbackClassic")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonDatatype")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonDatatype")
    implementation("com.google.code.gson:gson")
    implementation("com.nimbusds:oauth2-oidc-sdk:$nimbus")
    implementation("com.unboundid:unboundid-ldapsdk:$unboundid")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggin")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheus")
    implementation("javax.inject:javax.inject:1")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackStash")
    implementation("org.apache.httpcomponents.client5:httpclient5:$apacheHttp")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.json:json:$json")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.conscrypt:conscrypt-openjdk:$conscrypt")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$openapi")
    implementation("org.yaml:snakeyaml:$snakeYaml")
    runtimeOnly("com.oracle.database.jdbc:ojdbc8")

    // test
    testImplementation("com.h2database:h2:$h2")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2Server")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
        exclude(module = "junit")
    }
    testImplementation("org.springframework.security:spring-security-test:$springSecurity")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:$wiremockCloud")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.kotest:kotest-assertions-core:$kotest")
    testImplementation("io.mockk:mockk:$mockk")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    create("printVersion") {
        println(project.version)
    }
    bootJar {
        mainClass.set("no.nav.gandalf.GandalfApplication")
    }
    withType<Wrapper> {
        gradleVersion = gradleVersion
        distributionType = Wrapper.DistributionType.BIN
    }
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }
    withType<Test> {
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
}
