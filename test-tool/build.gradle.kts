plugins {
	`java-library`
	id("io.spring.dependency-management") version "1.1.7"
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.14")
	}
}

dependencies {
	api("org.springframework:spring-webmvc")
	api("com.fasterxml.jackson.core:jackson-databind")
	api("org.springframework.boot:spring-boot-starter-test")
}
