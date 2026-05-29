# FastAnimation v0.1.0 [ALPHA] — Ultra-Fast Native Animation Engine for Java

[![Status](https://img.shields.io/badge/status-v0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastAnimation/releases/tag/v0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

---

**⚡ Ultra-fast animation and timeline orchestration for the FastJava ecosystem.**

**FastAnimation** is a high-performance timeline engine built for zero-latency UI transitions and complex motion graphics. It is deeply integrated and bundled with **FastTween**—our zero-overhead interpolation engine—to provide a complete, unified toolkit for orchestrating fluid, native-speed animations in Java.

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
import fastanimation.FastAnimation;
import fastanimation.AnimationEngine.HeartbeatMode;
import fasttween.FastTween;

public class Example {
    public static void main(String[] args) {
        // Optional: Switch to High-Precision Native VSync mode
        FastAnimation.setHeartbeatMode(HeartbeatMode.NATIVE_VSYNC);

        // Orchestrate a sequence of FastTweens seamlessly
        FastAnimation.sequence(
            FastTween.to(0, 100, 1000).onUpdate(val -> System.out.println("X: " + val)),
            FastTween.to(1.0f, 0.0f, 500).onUpdate(val -> System.out.println("Fade: " + val))
        ).onComplete(() -> System.out.println("Animation Complete!"))
         .start();
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

Add the JitPack repository and the dependency to your `pom.xml`:

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
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.andrestubbe:fastanimation:v0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JAR directly to add it to your classpath:

1. 📦 **[fastanimation-v0.1.0.jar](https://github.com/andrestubbe/FastAnimation/releases/download/v0.1.0/fastanimation-v0.1.0.jar)** (The Core Library)

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

