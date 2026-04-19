# Building from Source

## Prerequisites

- JDK 17+
- Maven 3.9+

## Build

```bash
mvn clean package
```

## Run Examples

```bash
cd examples/00-basic-usage
mvn compile exec:java
```

## Installation

### JitPack (Recommended)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastanimation</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastanimation:v1.0.0'
}
```

## Download Pre-built JAR

See [Releases Page](https://github.com/andrestubbe/FastAnimation/releases)
