plugins {
	`java-library`
	`maven-publish`
	signing
}

dependencies {
	compileOnly("org.springframework:spring-webmvc:6.2.18")
	compileOnly("com.fasterxml.jackson.core:jackson-databind:2.21.2")
	compileOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.21.2")
	api("org.junit.jupiter:junit-jupiter-api:5.12.2")
	api("org.slf4j:slf4j-api:2.0.17")
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

	repositories {
		maven {
			url = uri(layout.buildDirectory.dir("staging-deploy"))
		}
	}
}

signing {
	sign(publishing.publications["mavenJava"])
}
