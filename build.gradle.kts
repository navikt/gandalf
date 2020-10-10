import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Version {
    const val apacheHttp = "4.5.12"
    const val gradleVersion = "6.5"
    const val json = "20200518"
    const val kotlinLoggin = "2.0.3"
    const val logbackStash = "6.4"
    const val mockOAuth2Server = "0.1.35"
    const val nimbus = "8.22"
    const val openapi = "1.4.8"
    const val unboundid = "5.1.1"
    const val wiremock = "2.27.2"
    const val wiremockCloud = "2.2.4.RELEASE"
}

val mainClass = "no.nav.gandalf.GandalfApplication"

plugins {
    application
    java
    val kotlinVersion = "1.4.10"
    kotlin("plugin.allopen") version kotlinVersion
    id("org.jmailen.kotlinter") version "3.2.0"
    id("com.github.ben-manes.versions") version "0.33.0"
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.json:json:${Version.json}")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-ui:${Version.openapi}")
    // implementation("org.springdoc:springdoc-openapi-kotlin:${Version.openapi}")
    implementation("org.yaml:snakeyaml")
    runtimeOnly("com.oracle.ojdbc:ojdbc8")

    // test
    testImplementation("com.h2database:h2")
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
        mainClassName = mainClass
    }
    withType<Wrapper> {
        gradleVersion = Version.gradleVersion
        distributionType = Wrapper.DistributionType.BIN
    }
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "13"
        }
    }
    withType<Test> {
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
}
