# FastTerminal — High-Performance JLine-Free True-Color Terminal Engine for Java

**A zero-dependency, JLine-free terminal graphics and TUI engine for Java, engineered for high-refresh rates, 24-bit True Color, and native JNI console optimizations with seamless keyboard and mouse integrations.**

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://jitpack.io/v/andrestubbe/FastTerminal.svg)](https://jitpack.io/#andrestubbe/FastTerminal)

FastTerminal is the high-performance terminal substrate of the **FastJava** ecosystem. It introduces a lightweight, cell-buffered virtual viewport using primitive integer buffers for full 24-bit True Color (foreground & background) and emoji-safe UTF-32 Codepoint grids, operating completely independently of heavy standard frameworks.

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
- [Technical Examples & Hero Demos](#technical-examples--hero-demos)
- [Performance Optimization](#performance-optimization)
- [Platform JNI Capabilities](#platform-jni-capabilities)
- [License](#license)

---

## 🎯 Our Mission
Our mission is to build the fastest, most robust native execution kernel on the JVM for console rendering and TUI interactivity. By combining cell-buffered graphics, double-buffered layouts, and immediate OS hardware input telemetry via **[FastKeyboard](https://github.com/andrestubbe/FastKeyboard)** and **[FastMouse](https://github.com/andrestubbe/FastMouse)**, we empower developers to create premium, interactive command-line terminals that rival native C++ applications in visual fidelity and input response—without a single external dependency.

---

## Key Features
*   **🚫 JLine-Free & Zero Dependencies** — Bypasses all high-latency third-party blocking input loops.
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

FastTerminal works natively with `FastCore` to handle cross-platform JNI loading:

### Maven (JitPack)
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
        <artifactId>fastterminal</artifactId>
        <version>v0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

### Gradle (JitPack)
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastterminal:v0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v0.1.0'
}
```

---

## Technical Examples & Hero Demos
See the **[Demo.md](Demo.md)** catalog for technical implementations:

| Case | Java Example | Performance Race / Demo | Details |
|------|--------------|-------------------------|---------|
| Fullscreen OLED Sine Wave | [Demo.java](examples/Demo/src/main/java/fastterminal/Demo.java) | Dynamic Rainbow wave, Emojis, titles | Scales dynamically to terminal size |
| Dynamic Progress Indicators | See [Demo.md](Demo.md#demo-2-multi-line-download-progress-apt-style) | apt/npm style multi-line indicators | Bottom-anchored overlays |
| Keyboard Draw / Paint | See [Demo.md](Demo.md#demo-5-fastterminal-paint) | Arrow keys drawing pixels | Direct keyboard interaction |
| Mouse-Move Rectangle | See [Demo.md](Demo.md#demo-6-mouse-move-rectangle) | Rect tracking pointer coords | Full-screen ANSI mouse input |

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
