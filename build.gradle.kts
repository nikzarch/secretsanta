plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
    id("org.liquibase.gradle") version "3.1.0"
}

extra["jooq.version"] = "3.21.9"

group = "ru.nikzarch"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

sourceSets {
    main {
        java.srcDir("build/generated-sources/jooq")
    }
}

buildscript {
    dependencies {
        classpath("org.liquibase:liquibase-core:5.0.1")
    }
}


repositories {
    mavenCentral()
}


fun readDotEnv(): Map<String, String> {
    val envFile = file(".env")
    if (!envFile.exists()) return emptyMap()

    return envFile.readLines()
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { raw ->
            val line = raw.removePrefix("export ").trim()
            val eq = line.indexOf('=')
            if (eq <= 0) return@mapNotNull null

            val key = line.substring(0, eq).trim()
            val value = line.substring(eq + 1)
                .trim()
                .trim('"', '\'')

            if (key.isEmpty()) null else key to value
        }
        .toMap()
}

val dotenv: Map<String, String> by lazy { readDotEnv() }

fun env(name: String): String? = System.getenv(name) ?: dotenv[name]

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")

//    implementation("org.springframework.boot:spring-boot-starter-security")
//    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")


    developmentOnly("me.paulschwarz:springboot4-dotenv:5.1.0")


    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.postgresql:postgresql")
    jooqCodegen("org.postgresql:postgresql")
    liquibaseRuntime("org.postgresql:postgresql")

    liquibaseRuntime("org.liquibase:liquibase-core:4.33.0")
    liquibaseRuntime("info.picocli:picocli:4.7.7")
    liquibaseRuntime("org.yaml:snakeyaml:2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jooq-test")
    testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("compileKotlin") {
    dependsOn(tasks.named("jooqCodegen"))
}

jooq {
    configuration {

        jdbc {
            driver = "org.postgresql.Driver"
            url = env("DATASOURCE_URL_POSTGRES")
            user = env("DATASOURCE_USER_POSTGRES")
            password = env("DATASOURCE_PASSWORD_POSTGRES")
        }
        generator {
            database {
                inputSchema = "public"
                outputSchemaToDefault = true

                excludes = "databasechangelog|databasechangeloglock"
            }
            target {
                packageName = "ru.nikzarch.secretsanta.jooq.generated"
                directory = "build/generated-sources/jooq"
            }
        }

    }
}


liquibase {
    activities.register("main") {
        arguments = mapOf(
            "changelogFile" to "src/main/resources/db/changelog/db.changelog-master.yaml",
            "url" to (env("DATASOURCE_URL_POSTGRES") ?: error("DATASOURCE_URL_POSTGRES is missing")),
            "username" to (env("DATASOURCE_USER_POSTGRES") ?: error("DATASOURCE_USER_POSTGRES is missing")),
            "password" to (env("DATASOURCE_PASSWORD_POSTGRES") ?: error("DATASOURCE_PASSWORD_POSTGRES is missing")),
            "driver" to "org.postgresql.Driver"
        )
    }
    runList = "main"
}