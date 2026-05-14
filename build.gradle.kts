allprojects {
	group = "com.github.irof"
	version = "0.0.2"

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
		System.getProperty("json.binding.write")?.let {
			systemProperty("json.binding.write", it)
		}
	}
}

tasks.register("testBootCompatibility") {
	group = "verification"
	description = "Runs both Spring Boot 3 and 4 sample app tests."
	dependsOn(":todo-app:boot3:test", ":todo-app:boot4:test")
}
