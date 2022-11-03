import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Version {
    const val mockk = "1.12.3"
    const val kotest = "5.4.2"
    const val apacheHttp = "4.5.12"
    const val gradleVersion = "7.4.1"
    const val json = "20220320"
    const val kotlinLoggin = "2.1.21"
    const val logbackStash = "7.0.1"
    const val mockOAuth2Server = "0.5.1"
    const val nimbus = "9.41"
    const val openapi = "1.6.6"
    const val unboundid = "6.0.3"
    const val wiremock = "2.32.0"
    const val wiremockCloud = "3.1.1"
    const val h2 = "2.1.214"
}

plugins {
    application
    java
    val kotlinVersion = "1.6.20"
    kotlin("plugin.allopen") version kotlinVersion
    id("org.jmailen.kotlinter") version "3.10.0"
    id("com.github.ben-manes.versions") version "0.43.0"
    id("org.springframework.boot") version "2.7.5"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("io.spring.dependency-management") version "1.1.0"
}

application {
    mainClass.set("no.nav.gandalf.GandalfApplication")
}

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven")
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

//override Spring dependency management which uses a older version
ext["okhttp3.version"] = "4.9.2"

dependencies {
    implementation("ch.qos.logback:logback-classic")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.google.code.gson:gson")
    implementation("com.nimbusds:oauth2-oidc-sdk:${Version.nimbus}")
    implementation("com.unboundid:unboundid-ldapsdk:${Version.unboundid}")
    implementation("io.github.microutils:kotlin-logging:${Version.kotlinLoggin}")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("javax.inject:javax.inject:1")
    implementation("javax.validation:validation-api")
    implementation("net.logstash.logback:logstash-logback-encoder:${Version.logbackStash}")
    implementation("org.apache.httpcomponents:httpclient")
    implementation("org.hibernate:hibernate-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.json:json:${Version.json}")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.conscrypt:conscrypt-openjdk:2.5.2")
    implementation("org.springdoc:springdoc-openapi-ui:${Version.openapi}")
    implementation("org.yaml:snakeyaml")
    runtimeOnly("com.oracle.database.jdbc:ojdbc8")

    // test
    testImplementation("com.h2database:h2:${Version.h2}")
    testImplementation("no.nav.security:mock-oauth2-server:${Version.mockOAuth2Server}")
    testImplementation("org.hibernate:hibernate-testing")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
        exclude(module = "junit")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8:${Version.wiremock}")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:${Version.wiremockCloud}")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.kotest:kotest-assertions-core:${Version.kotest}")
    testImplementation("io.mockk:mockk:${Version.mockk}")
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
        gradleVersion = Version.gradleVersion
        distributionType = Wrapper.DistributionType.BIN
    }
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "15"
        }
    }
    withType<Test> {
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
}
