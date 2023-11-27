import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springSecurity = "6.2.0"
val snakeYaml = "2.2"
val mockk = "1.13.8"
val kotest = "5.8.0"
val apacheHttp = "4.5.12"
val gradleVersion = "8.4"
val json = "20231013"
val kotlinLoggin = "2.1.21"
val logbackStash = "7.4"
val mockOAuth2Server = "2.0.1"
val nimbus = "9.41"
val openapi = "1.7.0"
val unboundid = "6.0.10"
val wiremock = "3.0.1"
val wiremockCloud = "4.0.4"
val h2 = "2.2.224"
val jacksonDatatype = "2.16.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

plugins {
    application
    java
    val kotlinVersion = "1.9.20"
    kotlin("plugin.allopen") version kotlinVersion
    id("org.jmailen.kotlinter") version "3.10.0"
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
    implementation("ch.qos.logback:logback-classic")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonDatatype}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonDatatype}")
    implementation("com.google.code.gson:gson")
    implementation("com.nimbusds:oauth2-oidc-sdk:${nimbus}")
    implementation("com.unboundid:unboundid-ldapsdk:${unboundid}")
    implementation("io.github.microutils:kotlin-logging:${kotlinLoggin}")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("javax.inject:javax.inject:1")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("net.logstash.logback:logstash-logback-encoder:${logbackStash}")
    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.json:json:${json}")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.conscrypt:conscrypt-openjdk:2.5.2")
    implementation("org.springdoc:springdoc-openapi-ui:${openapi}")
    implementation("org.yaml:snakeyaml:${snakeYaml}")
    runtimeOnly("com.oracle.database.jdbc:ojdbc8")

    // test
    testImplementation("com.h2database:h2:${h2}")
    testImplementation("no.nav.security:mock-oauth2-server:${mockOAuth2Server}")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
        exclude(module = "junit")
    }
    testImplementation("org.springframework.security:spring-security-test:${springSecurity}")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:${wiremockCloud}")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.kotest:kotest-assertions-core:${kotest}")
    testImplementation("io.mockk:mockk:${mockk}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    create("printVersion") {
        println(project.version)
    }
    withType<org.jmailen.gradle.kotlinter.tasks.LintTask> {
        dependsOn("formatKotlin")
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
