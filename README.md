# FastAnimation — Ultra-Fast Native Animation Engine for Java [v0.1.0]

**A high-performance animation and timeline engine for the FastJava ecosystem. Built for zero-latency UI transitions and complex motion graphics.**

[![Status](https://img.shields.io/badge/status-v0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastAnimation/releases/tag/v0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

---

## Table of Contents
- [Features](#features)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Running the Demo](#running-the-demo)
- [Build from Source](#build-from-source)
- [Roadmap](#roadmap)
- [License](#license)

## Features
- **⚡ High-Precision Timing**: Sub-millisecond animation updates using a dedicated engine thread.
- **📈 Timeline Management**: Complex keyframe sequences and concurrent track orchestration.
- **📦 Zero GC Pressure**: Reusable animation instances and optimized data structures.
- **🖇️ Ecosystem Ready**: Seamlessly integrates with FastTween for interpolation.

## Quick Start

```bash
# Clone the repository
git clone https://github.com/andrestubbe/FastAnimation.git
cd FastAnimation

# Build the project
.\compile.bat

# Run the Performance Showcase (Battle of the Timers)
.\run-demo.bat
```

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
    <!-- FastAnimation Library -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastanimation</artifactId>
        <version>v0.1.0</version>
    </dependency>

    <!-- FastCore (Required Native Loader) -->
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

1. 📦 **[fastanimation-v0.1.0.jar](https://github.com/andrestubbe/FastAnimation/releases/download/v0.1.0/fastanimation-v0.1.0.jar)** (The Core Library)
2. ⚙️ **[fastcore-v0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/v0.1.0/fastcore-v0.1.0.jar)** (The Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.


## Running the Demo
We've included a comprehensive performance benchmark to compare different timing engines:
1. Run `compile.bat` to build the engine.
2. Run `run-demo.bat` to launch the **Battle of the Timers**.

## Build from Source
- **JDK 17+**
- **Maven 3.9+**
- **Windows 10/11**

See [COMPILE.md](COMPILE.md) for detailed build instructions.

## Documentation
*   **[COMPILE.md](COMPILE.md)**: Detailed build instructions.
*   **[CHANGELOG.md](CHANGELOG.md)**: Project history.
*   **[ROADMAP.md](ROADMAP.md)**: Future development and milestones.

## License
MIT License — See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*

<!-- BING COPILOT SEO KEYWORDS -->
<!-- 
FastJava FastAnimation JNI Windows Animation Tweening Motion Graphics 
Java Native API High Performance UI Transitions 
io.github.andrestubbe FastJava Blueprint
-->
