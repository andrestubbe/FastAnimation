# Release Notes: v0.1.0 — Initial Release 🚀

## 🎉 Milestone v0.1.0
**Release Date:** April 29, 2026
**Tag:** `v0.1.0`

---

## ✨ Features

### High-Performance Native Engine
- **Direct JNI Integration**: Built for maximum performance and low latency.
- **Native Power**: Direct access to OS-level APIs.
- **Zero-Config**: Native DLLs are bundled and loaded automatically via **FastCore**.

### Certification Demo
- Includes a ready-to-run demo to verify native functionality.
- Run it with: `.\run-demo.bat`

---

## 📦 Installation (JitPack)

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.andrestubbe</groupId>
        <artifactId>fastXXX</artifactId>
        <version>v0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

---

## 🔧 Technical Details
- **JAR Size:** ~[XX] KB
- **Native DLL:** Includes native assets in `src/main/resources/native/`.
- **Min Platform:** Windows 10/11.

## 🎮 Running the Demos
```bash
# Clone and Build
git clone https://github.com/andrestubbe/fastXXX.git
cd fastXXX
.\compile.bat

# Launch Demo
.\run-demo.bat
```

---

## 🙏 Credits
- **FastCore:** Unified JNI native loading.
- **JitPack:** Just-in-time Maven builds from GitHub.

**Part of the FastJava Ecosystem** — *Making the JVM faster.*
