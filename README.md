# FastTerminal — High-Performance True-Color Terminal Engine for Java [v0.1.0]

**A zero-dependency terminal graphics and TUI engine for Java, engineered for high-refresh rates, 24-bit True Color, and native JNI console optimizations with seamless keyboard and mouse integrations.**

[![FastTerminal Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=e0vSTnUgKEc)

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://jitpack.io/v/andrestubbe/FastTerminal.svg)](https://jitpack.io/#andrestubbe/FastTerminal)

FastTerminal is the high-performance terminal substrate of the **FastJava** ecosystem. It introduces a lightweight, cell-buffered virtual viewport using primitive integer buffers for full 24-bit True Color (foreground & background) and emoji-safe UTF-32 Codepoint grids, operating completely independently of standard bloated frameworks.

To achieve a completely responsive, zero-latency desktop terminal experience, FastTerminal is designed to pair natively with its twin telemetry and input modules:
*   🚀 **[FastKeyboard](https://github.com/andrestubbe/FastKeyboard)** — Direct, low-latency, asynchronous raw global and local keyboard event handling.
*   🖱️ **[FastMouse](https://github.com/andrestubbe/FastMouse)** — Precise hardware-level and virtual console-mode mouse tracking.

```java
// Quick Start — Example
import fastterminal.TerminalRenderer;
import fastterminal.TerminalScene;

public class Demo {
    public static void main(String[] args) {
        // Init a 80x24 viewport
        TerminalRenderer renderer = new TerminalRenderer(80, 24);
        TerminalScene scene = new TerminalScene(0, 0, 80, 24);
        
        // Write standard text & high-unicode emojis in True Color
        scene.writeString(5, 2, "⚡ FastTerminal Engine ⚡", 0xFFCC00, 0x111111);
        scene.writeString(5, 3, "🚀 True Color, Mouse & Keyboard support!", 0xFFFFFF, -1);
        
        renderer.addScene(scene);
        renderer.render(); // Blits directly to stdout
    }
}
```

## Table of Contents
- [Our Mission](#our-mission)
- [Key Features](#key-features)
- [Architecture & Grid Layout](#architecture--grid-layout)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Technical Examples & Demos](#technical-examples--demos)
- [Performance Optimization](#performance-optimization)
- [Platform JNI Capabilities](#platform-jni-capabilities)
- [License](#license)

---

## 🎯 Our Mission
Our mission is to build the fastest, most robust native execution kernel on the JVM for console rendering and TUI interactivity. By combining cell-buffered graphics, double-buffered layouts, and immediate OS hardware input telemetry via **[FastKeyboard](https://github.com/andrestubbe/FastKeyboard)** and **[FastMouse](https://github.com/andrestubbe/FastMouse)**, we empower developers to create premium, interactive command-line terminals that rival native C++ applications in visual fidelity and input response—without external dependencies.

---

## Key Features
*   **🚫 Zero Dependencies** — Bypasses all high-latency third-party blocking input loops.
*   **🎨 24-bit True Color** — Complete support for direct RGB escape codes (`\033[38;2;R;G;Bm` for foreground and `\033[48;2;R;G;Bm` for background).
*   **🌐 Emoji & Unicode-Safe** — Eliminates UTF-16 surrogate split bugs by using `int` (UTF-32) codepoint cell buffers rather than `char[]` arrays, guaranteeing that Emojis (e.g. `🚀`, `🌈`) fit exactly in 1 cell without warping rows.
*   **⚡ State-Minimized Renderer** — Optimizes stdout rendering by only emitting ANSI escape codes when color states change, reducing console stream bandwidth by up to **80%**.
*   **📺 Alternate Screen Buffer** — Seamlessly enters full-screen TUI buffer mode (`\033[?1049h`) and hides the cursor (`\033[?25l`) for clean dashboard applications.
*   **🎹 Native Input Substrates** — Built-in telemetry anchors designed for instant integration with `FastKeyboard` and `FastMouse` to process mouse tracking, window resizing, and raw key captures natively.
*   **📥 apt/npm Style Indicators** — Built-in dynamic bottom-anchored multi-line progress overlays using cursor save/restore positions.

---

## 📊 Architecture & Grid Layout

FastTerminal operates on a double-buffered layer compositor. Each cell in a `TerminalScene` is backed by primitive integer arrays, eliminating object allocation overhead:

```
[ TerminalScene Viewport Layer ]
  ├── codepointBuffer (int[] - UTF-32 Unicode codepoints)
  ├── fgColorBuffer   (int[] - packed 24-bit RGB values)
  └── bgColorBuffer   (int[] - packed 24-bit RGB values)
```

Composites are blitted to standard output using the highly-optimized **[TerminalRenderer](REFERENCE.md)**.

---

## API Quick Reference

| Method | Description | Path |
|--------|-------------|------|
| `writeCell(col, row, cp, fg, bg)` | Writes a single codepoint and packed colors to the grid. | [Reference →](REFERENCE.md#writecell) |
| `writeString(col, row, text, fg, bg)` | Renders a string including standard emojis safely. | [Reference →](REFERENCE.md#writestring) |
| `render()` | Blits all active scenes to the console screen buffer. | [Reference →](REFERENCE.md#render) |
| `clear()` | Resets all layers to default spaces and standard styles. | [Reference →](REFERENCE.md#clear) |

> [!TIP]
> See **[REFERENCE.md](REFERENCE.md)** for full class definitions, packed color utilities, and JNI specs.

---

## Installation

FastTerminal requires **two** dependencies: the module itself, and `FastCore` (which handles the native library extraction).

### Option 1: Maven (JitPack)
Add the JitPack repository and dependencies to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- 1. The FastTerminal Module -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastterminal</artifactId>
        <version>v0.1.0</version>
    </dependency>
    
    <!-- 2. FastCore (Required Native Loader) -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (JitPack)
Add this to your `build.gradle` file:
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastterminal:v0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest pre-compiled JARs directly to add them to your project's classpath:

1. 📦 [**fastterminal-v0.1.0.jar**](https://github.com/andrestubbe/FastTerminal/releases/download/v0.1.0/fastterminal-0.1.0.jar) (The Core Library & native JNI resources)
2. ⚙️ [**fastcore-v0.1.0.jar**](https://github.com/andrestubbe/FastCore/releases/download/v0.1.0/fastcore-0.1.0.jar) (The Mandatory Native Loader)

> [!IMPORTANT]
> Both JARs must be present in your classpath for FastTerminal's native functions to operate correctly.

---

## Technical Examples & Demos
See the active Java programs under the `examples/Demo` package:

| Case | Java Example | Performance / Demo | Details |
|------|--------------|--------------------|---------|
| Demoscene Megademo | [Demo.java](examples/Demo/src/main/java/fastterminal/Demo.java) | High-fidelity visual FX catalog | Auto-cycles through active fluid, fire, grids and 3D wireframes |
| Render Performance Race | [BenchmarkDemo.java](examples/Demo/src/main/java/fastterminal/BenchmarkDemo.java) | Pure throughput speed benchmark | Renders full-screen alternating patterns at raw throughput limits |
| Native Mouse Visualizer | [UI.java](examples/Demo/src/main/java/fastterminal/UI.java) | Real-time interactive coordinates & clicks | Renders a glowing neon crosshair and white mouse cursor on a dark blue background |

---

## Platform JNI Capabilities
While fully functional via optimized ANSI escapes, FastTerminal is architected for native Windows console hardware blitting:
*   **`GetConsoleScreenBufferInfo`** — Retrieves precise buffer columns and rows on-the-fly.
*   **`WriteConsoleOutputW`** — Direct console frame buffer hardware writes bypassing standard streams (up to **200 FPS**).
*   **`SetConsoleMode`** — Hooks raw mouse and window buffer change inputs natively.

---

## License
MIT License — See [LICENSE](LICENSE) file for details.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.*

Made with ⚡ by Andre Stubbe
