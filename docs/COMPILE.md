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
cd examples/Benchmark
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
        <artifactId>FastAnimation</artifactId>
        <version>0.1.1</version>
    </dependency>
</dependencies>
```

### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastAnimation:0.1.1'
}
```

## Download Pre-built JAR

See [Releases Page](https://github.com/andrestubbe/FastAnimation/releases)
