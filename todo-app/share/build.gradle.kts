plugins {
	`java-library`
}

dependencies {
	implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.14"))
	api("org.springframework:spring-webmvc")
	api("com.fasterxml.jackson.core:jackson-annotations")
}

sourceSets {
	test {
		java.setSrcDirs(emptyList<String>())
		resources.setSrcDirs(emptyList<String>())
	}
}
