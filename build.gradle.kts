plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.3.21"
    id("com.google.devtools.ksp") version "2.3.7"
    id("groovy") 
    id("io.micronaut.application") version "4.6.2"
    id("com.gradleup.shadow") version "9.4.1"
    id("io.micronaut.test-resources") version "4.6.2"
    id("io.micronaut.aot") version "4.6.2"
}

version = "1.0.0"
group = "com.andriosol"


val kotlinVersion=project.properties.get("kotlinVersion")

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.micronaut.data:micronaut-data-processor")
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.jsonschema:micronaut-json-schema-processor")
    ksp("io.micronaut.openapi:micronaut-openapi")
    ksp("io.micronaut.security:micronaut-security-annotations")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    ksp("io.micronaut.validation:micronaut-validation-processor")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("io.micronaut.jsonschema:micronaut-json-schema-annotations")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.projectreactor:reactor-core")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("io.micronaut.liquibase:micronaut-liquibase")
    // Text extraction from PDF / DOC / DOCX (Tika wraps PDFBox + POI)
    implementation("org.apache.tika:tika-core:2.9.2")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.2")
    compileOnly("io.micronaut:micronaut-http-client")
    compileOnly("io.micronaut.openapi:micronaut-openapi-annotations")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.liquibase:liquibase-core")
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("io.micronaut.jsonschema:micronaut-json-schema-validation")
    aotPlugins(platform("io.micronaut.platform:micronaut-platform:4.10.13"))
    aotPlugins("io.micronaut.security:micronaut-security-aot")
}



application {
    mainClass = "com.andriosol.andriodocpro.ApplicationKt"
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
}




graalvmNative.toolchainDetection = false
graalvmNative {
    binaries {
        all {
            buildArgs.add("-H:+SharedArenaSupport")
        }
    }
}




micronaut {
    runtime("netty")
    testRuntime("spock2")
    processing {
        incremental(true)
        annotations("com.andriosol.andriodocpro.*")
    }
    testResources {
        // Disabled: the app and tests connect to the docker-compose Postgres directly
        // (application-local.yml / application-test.yml). Also, the bundled Testcontainers
        // cannot talk to Docker 29.x on this machine and logs a noisy (harmless) error.
        enabled = false
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
        configurationProperties.put("micronaut.security.jwks.enabled","false")
    }

}
tasks.withType<io.micronaut.gradle.testresources.StartTestResourcesService>().configureEach {
      useClassDataSharing.set(false)
}

tasks.named<io.micronaut.gradle.docker.MicronautDockerfile>("dockerfile") {

    baseImage = "eclipse-temurin:17-jre"
}







// https://docs.gradle.org/current/userguide/upgrading_major_version_9.html#test_task_fails_when_no_tests_are_discovered
tasks.withType<AbstractTestTask>().configureEach {
    failOnNoDiscoveredTests = false
}




