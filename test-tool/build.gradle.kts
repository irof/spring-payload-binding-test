plugins {
	`java-library`
	`maven-publish`
}

dependencies {
	api(platform("org.springframework.boot:spring-boot-dependencies:3.5.14"))
	api("org.springframework:spring-webmvc")
	api("com.fasterxml.jackson.core:jackson-databind")
	api("org.springframework.boot:spring-boot-starter-test")
}

java {
	withSourcesJar()
	withJavadocJar()
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			artifactId = "spring-payload-binding-test"

			pom {
				name = "Spring Payload Binding Test"
				description = "Spring MVC endpoint request/response payload binding contract test tool"
				// TODO: set to real project URL before publishing to Maven Central
				url = "https://example.com/spring-payload-binding-test"

				licenses {
					license {
						name = "The Apache License, Version 2.0"
						url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
					}
				}
				developers {
					developer {
						// TODO: replace with real developer info before publishing to Maven Central
						id = "example"
						name = "Example"
					}
				}
				scm {
					// TODO: replace with real SCM URLs before publishing to Maven Central
					url = "https://example.com/spring-payload-binding-test"
					connection = "scm:git:https://example.com/spring-payload-binding-test.git"
					developerConnection = "scm:git:ssh://example.com/spring-payload-binding-test.git"
				}
			}
		}
	}
}
