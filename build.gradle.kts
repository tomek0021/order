plugins {
    `java`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-core:1.4.5")
    implementation("ch.qos.logback:logback-classic:1.4.5")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.awaitility:awaitility:4.2.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
