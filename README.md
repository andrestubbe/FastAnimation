# FastAnimation v0.1.0 [ALPHA] — Ultra-Fast Native Animation Engine for Java

[![Status](https://img.shields.io/badge/status-v0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastAnimation/releases/tag/v0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

---

**⚡ Ultra-fast animation and timeline orchestration for the FastJava ecosystem.**

**FastAnimation** is a high-performance timeline engine built for zero-latency UI transitions and complex motion
graphics. It is deeply integrated and bundled with **[FastTween](https://github.com/andrestubbe/FastTween)**—our
zero-overhead interpolation engine—to provide a complete, unified toolkit for orchestrating fluid, native-speed
animations in Java.

[![FastKeyboard Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=BZsqQl7WqWk)

---

## Table of Contents

- [Quick Start](#quick-start)
- [Features](#features)
- [Installation](#installation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

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

## Performance Benchmarks

See the FastAnimation engine in action under extreme load:
🎥 [**Watch the Ultimate Benchmark on YouTube**](https://youtube.com/your-video-link-here)

| Benchmark Metric | Java Mode (`Thread.sleep`) | Native Mode (`FastDWM`) | Improvement |
|------------------|----------------------------|-------------------------|-------------|
| **Tick Rate (1M Tweens)** | ~180 FPS | **~560 FPS** | **3.1× Faster** |
| **Tick Rate (10k Tweens)**| ~177 FPS | **~551 FPS** | **3.1× Faster** |
| **Max Jitter (GC Stress)**| ~4359 μs | **~3258 μs** | **25% Smoother** |

*Measured on Windows 11, Intel Core i7, Java 17*

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
    <!-- Recommended for interpolation -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fasttween</artifactId>
        <version>v0.1.0</version>
    </dependency>
    <!-- Required for NATIVE_MM and NATIVE_VSYNC -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastdwm</artifactId>
        <version>v0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>v1.0.0</version>
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

## Platform Support

| Platform      | Status            |
|---------------|-------------------|
| Windows 10/11 | ✅ Fully Supported |
| Linux         | ✅ Fully Supported |
| macOS         | ✅ Fully Supported |

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

## Related Projects

- [FastTween](https://github.com/andrestubbe/FastTween) — Zero overhead pool-based tweening
- [FastAnimation](https://github.com/andrestubbe/FastAnimation) — Zero overhead timeline orchestration
- [FastDWM](https://github.com/andrestubbe/FastDWM) — Native Desktop Window Manager API
- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI Loader and Utilities
- [FastTheme](https://github.com/andrestubbe/FastTheme) — High-performance native window styling

---
**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*

