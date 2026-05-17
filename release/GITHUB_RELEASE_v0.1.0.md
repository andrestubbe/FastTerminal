# FastTerminal v0.1.0 — High-Performance JLine-Free True-Color Terminal Engine for Java 🚀

We are thrilled to announce the official release of **FastTerminal v0.1.0** — the high-performance terminal substrate of the **FastJava** ecosystem. FastTerminal provides a lightweight, cell-buffered virtual viewport using primitive integer buffers for full 24-bit True Color (foreground & background) and emoji-safe UTF-32 Codepoint grids, completely independent of heavy third-party CLI wrappers.

### 🌟 Release Highlights
* **Locked 120 FPS Rendering**: Smooth high-refresh-rate console output matching modern display hardware.
* **JNI Window Sizing**: Dynamic Win32 console screen buffer queries (`GetConsoleScreenBufferInfo`) track window and buffer columns natively on every frame to support responsive resizing.
* **Emoji-Safe UTF-32 Buffer**: Uses `int` codepoints in cell buffers to eliminate UTF-16 surrogate split bugs for all multi-column emojis (e.g. `🚀`, `🌈`).
* **Wide-Character Continuation Skipper**: Perfect 1:1 screen-to-buffer grid synchronization that eliminates layout shifting on rows containing double-width emojis.
* **State-Minimized Compositor**: An optimized ANSI compositor that minimizes emitted terminal bytes by up to **80%** by tracking active color state changes.

### 📦 Artifact Attachment Guide
Attach the following built JAR to this release:
* **`target/fastterminal-0.1.0.jar`**: The primary assembly FatJAR which contains the native JNI C++ dynamic library (`fastterminal.dll`) packaged inside the JAR root directory.

### 🛠️ Installation

#### Maven
```xml
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastterminal</artifactId>
    <version>v0.1.0</version>
</dependency>
```

#### Gradle
```gradle
implementation 'com.github.andrestubbe:fastterminal:v0.1.0'
```

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*
