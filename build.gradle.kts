plugins {
    kotlin("jvm") version "1.5.31"
    application
}

group = "me.sttimort"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.github.ajalt.clikt:clikt:3.3.0")
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("com.londogard:londogard-nlp-toolkit:1.0.0")
    implementation("com.github.haifengl:smile-core:2.6.0")
    implementation("com.github.haifengl:smile-kotlin:2.6.0")

    implementation("io.github.microutils", "kotlin-logging", "1.7.9")
    implementation("org.slf4j:slf4j-simple:1.7.29")
}

application {
    mainClass.set("me.sttimort.aot3plsa.Aot3PlsaApplicationKt")
    applicationDefaultJvmArgs = listOf("-Xmx8g")
}