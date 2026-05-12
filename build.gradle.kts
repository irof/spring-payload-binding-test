allprojects {
	group = "com.github.irof"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "java")

	the<JavaPluginExtension>().toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}

	tasks.withType<Test> {
		useJUnitPlatform()
		System.getProperty("json.binding.mode")?.let {
			systemProperty("json.binding.mode", it)
		}
	}
}
