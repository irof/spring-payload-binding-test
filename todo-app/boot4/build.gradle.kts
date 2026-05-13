plugins {
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
	implementation(project(":todo-app:share"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-json")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation(project(":spring-payload-binding-test"))
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets {
	test {
		java.srcDir("../share/src/test/java")
		resources.srcDir("../share/src/test/resources")
	}
}
