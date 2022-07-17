# curupira
curupira

[![Java CI with Gradle](https://github.com/Softawii/curupira/actions/workflows/gradle.yml/badge.svg)](https://github.com/Softawii/curupira/actions/workflows/gradle.yml)


### Install with Gradle

The Package is in github packages

```
repositories {
  maven {
        url 'https://maven.pkg.github.com/Softawii/curupira'
        credentials {
            username = System.getenv("GITHUB_USER")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}
    
dependencies {   
  implementation("com.softawii:curupira:0.2.0:all")
}
```
