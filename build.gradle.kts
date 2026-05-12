allprojects {
	group = "com.example"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "java")

	the<JavaPluginExtension>().toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}
