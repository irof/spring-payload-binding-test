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
				url = "https://github.com/irof/spring-payload-binding-test"

				licenses {
					license {
						name = "The Apache License, Version 2.0"
						url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
					}
				}
				developers {
					developer {
						id = "irof"
						name = "irof"
						email = "irof.ocean@gmail.com"
					}
				}
				scm {
					url = "https://github.com/irof/spring-payload-binding-test"
					connection = "scm:git:https://github.com/irof/spring-payload-binding-test.git"
					developerConnection = "scm:git:ssh://git@github.com/irof/spring-payload-binding-test.git"
				}
			}
		}
	}
}
