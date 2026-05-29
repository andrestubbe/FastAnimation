# FastAnimation v0.1.0 [ALPHA] — Ultra-Fast Native Animation Engine for Java

[![Status](https://img.shields.io/badge/status-v0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastAnimation/releases/tag/v0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

---

**⚡ A high-performance animation and timeline engine for the FastJava ecosystem. Built for zero-latency UI transitions and
complex motion graphics.**

---

[![FastKeyboard Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=BZsqQl7WqWk)

---

## Table of Contents

- [Quick Start](#quick-start)
- [Features](#features)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Running the Demo](#running-the-demo)
- [Roadmap](#roadmap)
- [License](#license)

---

## Quick Start

```java
import fastanimation.AnimationEngine;

public class Example {
    public static void main(String[] args) {
        // Initialize the Native Animation Engine
        AnimationEngine engine = new AnimationEngine();
        
        // Start the main render loop
        engine.start();
    }
}
```

---

## Features

- **⚡ High-Precision Timing**: Sub-millisecond animation updates using a dedicated engine thread.
- **📈 Timeline Management**: Complex keyframe sequences and concurrent track orchestration.
- **📦 Zero GC Pressure**: Reusable animation instances and optimized data structures.
- **🖇️ Ecosystem Ready**: Seamlessly integrates with FastTween for interpolation.

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependencies to your `pom.xml`:

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
       <version>v0.1.0</version>
   </dependency>
   <dependency>
       <groupId>com.github.andrestubbe</groupId>
       <artifactId>fastcore</artifactId>
       <version>v0.1.0</version>
   </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.andrestubbe:fastanimation:v0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 *
   *[fastanimation-v0.1.0.jar](https://github.com/andrestubbe/FastAnimation/releases/download/v0.1.0/fastanimation-v0.1.0.jar)
   ** (The Core Library)
2. ⚙️ **[fastcore-v0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/v0.1.0/fastcore-v0.1.0.jar)** (
   The Mandatory Native Loader)

---

## Related Projects

- [FastTween](https://github.com/andrestubbe/FastTween) — Zero overhead pool-based tweening
- [FastAnimation](https://github.com/andrestubbe/FastAnimation) — Zero overhead timeline orchestration
- [FastDWM](https://github.com/andrestubbe/FastDWM) — Native Desktop Window Manager API
- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI Loader and Utilities
- [FastTheme](https://github.com/andrestubbe/FastTheme) — High-performance native window styling

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*

