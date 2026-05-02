import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaToolchainService

plugins {
    java
    application
}

group = "com.selflearn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
}

val javaToolchains = extensions.getByType<JavaToolchainService>()

tasks.withType<JavaExec>().configureEach {
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
}

tasks.withType<Test>().configureEach {
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "com.selflearn.Main"
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
}

tasks.named<JavaExec>("run") {
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
}

tasks.register<JavaExec>("runHashMapLab") {
    group = "application"
    description = "Day 1: HashMap treeify / resize observation via reflection"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.selflearn.labs.hashmap.HashMapInternalsLab"
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
}

tasks.register<JavaExec>("runPrerequisitesExamples") {
    group = "application"
    description = "HashMap prerequisites: hashing, lookup cost, reflection, JVM constants"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.selflearn.labs.hashmap.PrerequisitesRunner"
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
}

tasks.register<JavaExec>("runNetworkFoundationsServer") {
    group = "application"
    description = "Start local server for networking/HTTP exercises"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.selflearn.labs.networkfoundations.NetworkFoundationsServer"
}

tasks.register<JavaExec>("runNetworkFoundationsClient") {
    group = "application"
    description = "Run networking client drills against local server"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.selflearn.labs.networkfoundations.NetworkFoundationsClientLab"
}

tasks.register<JavaExec>("runNetworkFoundationsDemo") {
    group = "application"
    description = "Run embedded server + client networking drills end-to-end"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.selflearn.labs.networkfoundations.NetworkFoundationsEmbeddedDemo"
}

tasks.register<JavaExec>("runRetryBackoffJitterLab") {
    group = "application"
    description = "Compare fixed retries vs exponential backoff+jitter"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.selflearn.labs.networkfoundations.RetryBackoffJitterLab"
}

tasks.register<JavaExec>("runTokenBucketRateLimiterLab") {
    group = "application"
    description = "Run token bucket burst simulation"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.selflearn.labs.networkfoundations.TokenBucketRateLimiterLab"
}

tasks.register<JavaExec>("runHedgedRequestsLab") {
    group = "application"
    description = "Simulate hedged requests and p99/load trade-off"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.selflearn.labs.networkfoundations.HedgedRequestsLab"
}

tasks.register<JavaExec>("runTracePropagationDemo") {
    group = "application"
    description = "Run simple traceparent propagation demo (service A -> B)"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.selflearn.labs.networkfoundations.TracePropagationDemo"
}

tasks.register<JavaExec>("runOopPrinciplesDemo") {
    group = "application"
    description = "OOP principles lab: encapsulation, polymorphism, abstraction, composition"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.selflearn.labs.oopprinciples.OopPrinciplesDemo"
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
