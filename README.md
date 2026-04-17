# FastAnimation

> **Lightweight Java timeline engine** — Orchestrate multiple tweens and keyframes

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Maven](https://img.shields.io/badge/Maven-3.9+-orange.svg)](https://maven.apache.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io)

---

## Quick Start

```java
// Sequence of tweens
FastAnimation anim = FastAnimation.sequence(
    FastTween.to(0f, 100f, 200).ease(Ease.QUAD_OUT),
    FastTween.to(100f, 20f, 400).ease(Ease.IN_OUT_QUAD)
).loop(3).start();

// Parallel execution
FastAnimation parallel = FastAnimation.parallel(
    FastTween.to(x0, x1, 300).onUpdate(x -> setX(x)),
    FastTween.to(y0, y1, 300).onUpdate(y -> setY(y))
).start();
```

---

## Installation

### Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastanimation</artifactId>
    <version>v1.0.0</version>
</dependency>
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

---

## Features

- **Sequences** — Chain tweens one after another
- **Parallel** — Run tweens simultaneously
- **Loops** — Repeat with count or infinite
- **Keyframes** — Define progress points (0% → 30% → 100%)
- **Events** — onStart, onUpdate, onComplete hooks
- **Built on FastTween** — Zero overhead, pure Java

---

## Project Structure

```
fastanimation/
├── src/main/java/fastanimation/
│   ├── FastAnimation.java    # Static factory
│   ├── Animation.java        # Timeline control
│   ├── Keyframe.java         # Progress definition
│   └── Track.java            # Property binding
├── examples/00-basic-usage/
└── pom.xml
```

---

## License

MIT License — See [LICENSE](LICENSE) for details.

**Part of the FastJava Ecosystem** — *Making the JVM faster.*
