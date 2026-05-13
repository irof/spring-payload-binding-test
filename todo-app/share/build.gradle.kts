plugins {
	`java-library`
}

dependencies {
	compileOnly(platform("org.springframework.boot:spring-boot-dependencies:3.5.14"))
	compileOnly("org.springframework:spring-webmvc")
	compileOnly("com.fasterxml.jackson.core:jackson-annotations")
}

sourceSets {
	test {
		java.setSrcDirs(emptyList<String>())
		resources.setSrcDirs(emptyList<String>())
	}
}
